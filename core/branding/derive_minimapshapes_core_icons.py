from pathlib import Path

from PIL import Image


def main():
    # 確定済み512px画像は書き換えず、同じ比率で縮小版だけを出力する。
    directory = Path(__file__).resolve().parent
    source = directory / "minimapshapes_core_icon_512.png"
    with Image.open(source) as image:
        if image.size != (512, 512):
            raise ValueError("Expected a 512 x 512 PNG")
        for size in (256, 128, 64):
            image.resize((size, size), Image.Resampling.LANCZOS).save(
                directory / f"minimapshapes_core_icon_{size}.png", format="PNG"
            )


if __name__ == "__main__":
    main()
