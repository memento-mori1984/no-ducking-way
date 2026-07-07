package com.example.noduckingway

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRouting
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

class NoDuckingService : Service() {

    private var audioTrack: AudioTrack? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isPlayingSilence = false
    private var playbackThread: Thread? = null
    private var routeWatcher: AudioRouteWatcher? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var hasAudioFocus = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val focusWatchdog = object : Runnable {
        override fun run() {
            if (!running) return
            val mixerMode = NoDuckingPrefs.isMixerMode(this@NoDuckingService)
            if (!mixerMode && !hasAudioFocus) {
                Log.w(TAG, "Focus watchdog re-requesting focus (owner mode)")
                requestAudioFocus()
            }
            if (!isPlayingSilence) {
                Log.w(TAG, "Focus watchdog restarting silent playback (mixer=$mixerMode)")
                startPlayingSilence(mixerMode)
            }
            mainHandler.postDelayed(this, FOCUS_WATCHDOG_INTERVAL_MS)
        }
    }

    private val sampleRate = 44100
    private val channelMask = AudioFormat.CHANNEL_OUT_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val routingListener = AudioRouting.OnRoutingChangedListener {
        mainHandler.post { restartSilentTrack() }
    }

    override fun onCreate() {
        super.onCreate()
        running = true
        createNotificationChannel()
        routeWatcher = AudioRouteWatcher(this) { restartSilentTrack() }
        routeWatcher?.register()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        running = true
        startForegroundWithType(createNotification())
        acquireWakeLock()
        applyProtectionStrategy()
        mainHandler.removeCallbacks(focusWatchdog)
        mainHandler.postDelayed(focusWatchdog, FOCUS_WATCHDOG_INTERVAL_MS)
        return START_STICKY
    }

    private fun applyProtectionStrategy() {
        val mixerMode = NoDuckingPrefs.isMixerMode(this)
        if (mixerMode) {
            // Do not take audio focus — YouTube/Spotify keep playing.
            abandonAudioFocus()
            startPlayingSilence(mixerMode = true)
        } else {
            requestAudioFocus()
            startPlayingSilence(mixerMode = false)
        }
    }

    private fun startForegroundWithType(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun requestAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    hasAudioFocus = true
                    Log.d(TAG, "Audio focus gained (owner mode)")
                }
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    hasAudioFocus = false
                    Log.d(TAG, "Audio focus lost ($focusChange); reclaiming")
                    mainHandler.postDelayed({
                        if (running && !NoDuckingPrefs.isMixerMode(this@NoDuckingService)) {
                            requestAudioFocus()
                        }
                    }, 150)
                }
            }
        }

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attrs)
            .setAcceptsDelayedFocusGain(true)
            .setWillPauseWhenDucked(false)
            .setOnAudioFocusChangeListener(focusListener, mainHandler)
            .build()

        val result = audioManager.requestAudioFocus(audioFocusRequest!!)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED ||
            result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED
        Log.i(TAG, "requestAudioFocus owner result=$result hasAudioFocus=$hasAudioFocus")
        return hasAudioFocus
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoDuckingWay::Silence").apply {
            setReferenceCounted(false)
            acquire(WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) lock.release()
        }
        wakeLock = null
    }

    private fun buildSilentTrack(mixerMode: Boolean): AudioTrack {
        val attrs = if (mixerMode) {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        } else {
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                .build()
        }

        val format = AudioFormat.Builder()
            .setEncoding(audioFormat)
            .setSampleRate(sampleRate)
            .setChannelMask(channelMask)
            .build()

        val bufSize = AudioTrack.getMinBufferSize(sampleRate, channelMask, audioFormat)

        val builder = AudioTrack.Builder()
            .setAudioAttributes(attrs)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
        }

        return builder.build().apply {
            setVolume(0.0001f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addOnRoutingChangedListener(routingListener, mainHandler)
            }
        }
    }

    private fun startPlayingSilence(mixerMode: Boolean) {
        if (isPlayingSilence) return

        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelMask, audioFormat)
        audioTrack = buildSilentTrack(mixerMode)
        audioTrack?.play()
        isPlayingSilence = true

        playbackThread = Thread {
            val buffer = ByteArray(minBufferSize)
            while (isPlayingSilence) {
                val track = audioTrack ?: break
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) break
                track.write(buffer, 0, buffer.size)
            }
        }.apply {
            name = "NoDuckingSilence"
            start()
        }
    }

    private fun restartSilentTrack() {
        if (!running) return
        stopPlayingSilence()
        applyProtectionStrategy()
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, createNotification())
    }

    private fun stopPlayingSilence() {
        isPlayingSilence = false
        playbackThread?.interrupt()
        playbackThread = null

        audioTrack?.let { track ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                track.removeOnRoutingChangedListener(routingListener)
            }
            try {
                track.stop()
            } catch (_: IllegalStateException) {
                /* already stopped */
            }
            track.release()
        }
        audioTrack = null
    }

    private fun abandonAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
        hasAudioFocus = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NoDuckingWay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps music at full volume"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val tapIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, NoDuckingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val modeLabel = if (NoDuckingPrefs.isMixerMode(this)) "Mixer" else "Owner"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NoDuckingWay is active")
            .setContentText("Music protection on ($modeLabel mode)")
            .setSmallIcon(R.drawable.ic_noduck)
            .setOngoing(true)
            .setContentIntent(tapIntent)
            .addAction(0, "Stop", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        running = false
        mainHandler.removeCallbacks(focusWatchdog)
        routeWatcher?.unregister()
        stopPlayingSilence()
        releaseWakeLock()
        abandonAudioFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "NoDuckingService"
        private const val FOCUS_WATCHDOG_INTERVAL_MS = 3000L
        private const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 60 * 1000L

        const val ACTION_STOP = "com.example.noduckingway.ACTION_STOP"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "noducking_channel"

        @Volatile
        var running = false
            private set

        fun isRunning(@Suppress("UNUSED_PARAMETER") context: Context): Boolean = running
    }
}