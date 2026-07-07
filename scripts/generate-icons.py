"""Generate Android launcher and in-app icons from the source logo PNG."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
SOURCE = Path(
    r"C:\Users\Ranzh\Downloads\ChatGPT Image Jul 7, 2026, 01_32_42 PM.png"
)

MIPMAP_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

FOREGROUND_SIZES = {
    "drawable-mdpi": 108,
    "drawable-hdpi": 162,
    "drawable-xhdpi": 216,
    "drawable-xxhdpi": 324,
    "drawable-xxxhdpi": 432,
}

LOGO_SIZES = {
    "drawable-mdpi": 160,
    "drawable-hdpi": 240,
    "drawable-xhdpi": 320,
    "drawable-xxhdpi": 480,
    "drawable-xxxhdpi": 640,
}

NOTIFICATION_SIZES = {
    "drawable-mdpi": 24,
    "drawable-hdpi": 36,
    "drawable-xhdpi": 48,
    "drawable-xxhdpi": 72,
    "drawable-xxxhdpi": 96,
}


def fit_square(image: Image.Image, size: int, scale: float = 1.0) -> Image.Image:
    target = max(1, int(size * scale))
    resized = image.resize((target, target), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    offset = (size - target) // 2
    canvas.paste(resized, (offset, offset), resized if resized.mode == "RGBA" else None)
    return canvas


def save_webp(path: Path, image: Image.Image) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.convert("RGBA").save(path, format="WEBP", quality=95, method=6)


def save_png(path: Path, image: Image.Image) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.convert("RGBA").save(path, format="PNG", optimize=True)


def main() -> None:
    source = Image.open(SOURCE).convert("RGBA")

    for folder, size in MIPMAP_SIZES.items():
        icon = fit_square(source, size, scale=0.92)
        save_webp(RES / folder / "ic_launcher.webp", icon)
        save_webp(RES / folder / "ic_launcher_round.webp", icon)

    for folder, size in FOREGROUND_SIZES.items():
        foreground = fit_square(source, size, scale=0.82)
        save_png(RES / folder / "ic_launcher_foreground.png", foreground)

    for folder, size in LOGO_SIZES.items():
        logo = fit_square(source, size, scale=0.9)
        save_png(RES / folder / "ic_app_logo.png", logo)

    for folder, size in NOTIFICATION_SIZES.items():
        note = fit_square(source, size, scale=0.88)
        save_png(RES / folder / "ic_noduck.png", note)

    save_png(RES / "drawable-nodpi" / "ic_app_logo.png", fit_square(source, 512, scale=0.9))
    print("Icons generated successfully.")


if __name__ == "__main__":
    main()