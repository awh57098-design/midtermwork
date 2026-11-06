from pathlib import Path
from PIL import Image,ImageFont,ImageDraw
import argparse
parser=argparse.ArgumentParser(description='Rebuild the Korean bitmap font after editing game text.')
parser.add_argument('font',help='Path to NotoSansKR variable TTF (SIL OFL).')
args=parser.parse_args()
p=Path(__file__).resolve().parents[1]
font=ImageFont.truetype(args.font,18)
font.set_variation_by_axes([500])
chars=set(chr(n) for n in range(32,127));chars.update('↓×')
for f in (p/'core/src/main/java').rglob('*.java'):
 chars.update(c for c in f.read_text(encoding='utf-8') if '\uac00'<=c<='\ud7a3')
ascent,descent=font.getmetrics();atlas=Image.new('RGBA',(1024,1024));draw=ImageDraw.Draw(atlas)
x=y=1;row=0;records=[]
for c in sorted(chars):
 l,t,r,b=font.getbbox(c,anchor='ls');w=max(1,r-l);h=max(1,b-t)
 if x+w+2>=1024:x=1;y+=row+2;row=0
 draw.text((x-l,y-t),c,font=font,fill='white',anchor='ls')
 records.append(f'char id={ord(c)} x={x} y={y} width={w} height={h} xoffset={l} yoffset={ascent+t} xadvance={round(font.getlength(c))} page=0 chnl=15')
 x+=w+2;row=max(row,h)
height=1
while height<y+row+2:height*=2
atlas=atlas.crop((0,0,1024,height));out=p/'assets/fonts';atlas.save(out/'korean.png')
header=f'info face="Noto Sans KR" size=18 bold=0 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1\ncommon lineHeight={ascent+descent} base={ascent} scaleW=1024 scaleH={height} pages=1 packed=0\npage id=0 file="korean.png"\nchars count={len(records)}\n'
(out/'korean.fnt').write_text(header+'\n'.join(records)+'\n',encoding='utf-8')
print('Korean font:',len(records),'glyphs; atlas',atlas.size)