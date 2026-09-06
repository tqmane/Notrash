"""Export all Android icon variants from the approved dot-N VectorDrawable.
Run from any directory: python tools/generate-icons.py
"""
from pathlib import Path
import re
import xml.etree.ElementTree as ET
from PIL import Image, ImageDraw

root = Path(__file__).resolve().parents[1]
res = root / 'app/src/main/res'
ns = '{http://schemas.android.com/apk/res/android}'
source = ET.parse(res / 'drawable/ic_notrash_foreground.xml').getroot()
dots = []
for node in source:
    color = node.get(ns + 'fillColor')
    data = node.get(ns + 'pathData')
    for part in data.split('M')[1:]:
        match = re.fullmatch(r'([\d.]+),([\d.]+)a([\d.]+),([\d.]+) 0,1 0,([\d.]+),0a([\d.]+),([\d.]+) 0,1 0,-([\d.]+),0\s*', part)
        assert match, 'Update the exporter if the canonical vector stops using circular dots'
        x, y, radius, ry, diameter, r2, ry2, d2 = map(float, match.groups())
        assert radius == ry == r2 == ry2 and diameter == d2 == radius*2
        dots.append((x+radius, y, radius, color))
assert len(dots) == 13, f'Unexpected canonical dot count: {len(dots)}'

for density, size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    for kind in ('icon','icon_round','icon_qs'):
        factor=4
        image=Image.new('RGBA',(size*factor,size*factor))
        draw=ImageDraw.Draw(image)
        edge=size*factor-1
        if kind=='icon': draw.rounded_rectangle((0,0,edge,edge), radius=size*factor*.22, fill='#0A0A0A')
        if kind=='icon_round': draw.ellipse((0,0,edge,edge), fill='#0A0A0A')
        viewport, origin = (56,26) if kind=='icon_qs' else (72,18)
        scale=size*factor/viewport
        for x,y,radius,color in dots:
            box=((x-radius-origin)*scale,(y-radius-origin)*scale,(x+radius-origin)*scale,(y+radius-origin)*scale)
            draw.ellipse(box, fill='#FFFFFF' if kind=='icon_qs' else color)
        image=image.resize((size,size),Image.Resampling.LANCZOS)
        output=res/f'mipmap-{density}'/f'{kind}.png'
        image.save(output)
        assert image.getpixel((0,0))[3] == 0
        assert image.getbbox() is not None

# Keep old resource names as aliases, with no stale geometry or double insets.
alias='''<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/ic_notrash_foreground" android:inset="0dp" />
'''
for name in ['app_icon_foreground.xml','app_icon_adaptive_foreground.xml']:
    (res/'drawable'/name).write_text(alias,encoding='utf-8')
paths='\n'.join(f'        <path android:fillColor="#FFFFFF" android:pathData="{node.get(ns+"pathData")}" />' for node in source)
(res/'drawable/ic_qs_tile.xml').write_text('''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="56" android:viewportHeight="56">
    <group android:translateX="-26" android:translateY="-26">
'''+paths+'\n    </group>\n</vector>\n',encoding='utf-8')
print('PASS: 15 PNGs exported, round/transparent variants checked, legacy vector names updated')
