"""
Convert backpack JPEG references to 16x16 transparent PNGs for the mod.
Removes the white/checkered background via corner-flood-fill before resizing.
"""
from PIL import Image
import os
from collections import deque

DESKTOP = "C:/Users/Odin/Desktop"
DEST = os.path.join(os.path.dirname(os.path.abspath(__file__)),
    "src/main/resources/assets/retro_sophisticated_backpacks/textures/items")

TIERS = [
    ("leather.jpg",  "backpack_leather.png"),
    ("iron.jpg",     "backpack_iron.png"),
    ("gold.jpg",     "backpack_gold.png"),
    ("diamond.jpg",  "backpack_diamond.png"),
    ("Obsidian.jpg", "backpack_obsidian.png"),
]

def remove_background(img: Image.Image, threshold: int = 30) -> Image.Image:
    """Flood-fill from all four corners to mark background pixels as transparent."""
    rgba = img.convert("RGBA")
    pixels = rgba.load()
    w, h = rgba.size

    visited = [[False] * h for _ in range(w)]
    queue = deque()

    # Seed from all four corners
    for sx, sy in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]:
        if not visited[sx][sy]:
            visited[sx][sy] = True
            queue.append((sx, sy))

    # Sample background color from top-left corner
    br, bg, bb, _ = pixels[0, 0]

    while queue:
        x, y = queue.popleft()
        r, g, b, a = pixels[x, y]
        dist = ((r - br) ** 2 + (g - bg) ** 2 + (b - bb) ** 2) ** 0.5
        if dist > threshold:
            continue
        pixels[x, y] = (r, g, b, 0)  # make transparent
        for nx, ny in [(x+1, y), (x-1, y), (x, y+1), (x, y-1)]:
            if 0 <= nx < w and 0 <= ny < h and not visited[nx][ny]:
                visited[nx][ny] = True
                queue.append((nx, ny))

    return rgba

for src_name, dst_name in TIERS:
    src_path = os.path.join(DESKTOP, src_name)
    dst_path = os.path.join(DEST, dst_name)
    if not os.path.exists(src_path):
        print(f"MISSING: {src_path}")
        continue
    img = Image.open(src_path)
    img = remove_background(img)
    img = img.resize((16, 16), Image.LANCZOS)
    img.save(dst_path)
    print(f"Saved {dst_name}  ({img.size})")

print("Done.")
