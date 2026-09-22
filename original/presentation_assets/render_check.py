from pathlib import Path
import sys,json
ROOT=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/'.presentation_tools'))
from playwright.sync_api import sync_playwright
from PIL import Image,ImageDraw
ASSET=Path(__file__).resolve().parent
with sync_playwright() as p:
    b=p.chromium.launch(executable_path='C:/Program Files/Google/Chrome/Application/chrome.exe',headless=True)
    page=b.new_page(viewport={'width':1360,'height':820},device_scale_factor=1)
    page.goto((ROOT/'original/オリジナル作品発表会_プレビュー.html').as_uri())
    page.evaluate('document.fonts.ready')
    issues=page.evaluate('''() => [...document.querySelectorAll('.text')].flatMap(e=>{
      let range=document.createRange();range.selectNodeContents(e);let r=range.getBoundingClientRect(),b=e.getBoundingClientRect();
      if(r.bottom>b.bottom+3||r.right>b.right+3)return [{slide:e.closest('.slide').id,text:e.innerText,actualHeight:r.height,boxHeight:b.height,actualWidth:r.width,boxWidth:b.width}];return [];
    })''')
    print(json.dumps(issues,ensure_ascii=False,indent=2))
    for i,s in enumerate(page.locator('.slide').all()):s.screenshot(path=str(ASSET/f'slide_{i+1:02}.png'))
    page.pdf(path=str(ROOT/'original/オリジナル作品発表会_確認用.pdf'),prefer_css_page_size=True,print_background=True)
    b.close()
files=sorted(ASSET.glob('slide_*.png'))
sheet=Image.new('RGB',(1280,math_rows:=((len(files)+3)//4)*204),'#DCE4EC'); d=ImageDraw.Draw(sheet)
for i,f in enumerate(files):
    im=Image.open(f);im.thumbnail((312,176));x=(i%4)*320+4;y=(i//4)*204+22;sheet.paste(im,(x,y));d.text((x,y-17),f'{i+1:02}',fill='#182A40')
sheet.save(ASSET/'overview.jpg',quality=92)
print('Rendered',len(files),'slides and PDF')
