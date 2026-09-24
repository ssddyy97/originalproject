import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[2]/'.presentation_tools'))
from playwright.sync_api import sync_playwright
root=Path(__file__).resolve().parents[2]
with sync_playwright() as p:
    browser=p.chromium.launch(executable_path='C:/Program Files/Google/Chrome/Application/chrome.exe',headless=True)
    page=browser.new_page(viewport={'width':1440,'height':1120},device_scale_factor=1)
    page.route('http://reserve-hub.local/',lambda r:r.fulfill(content_type='text/html',body=(root/'src/main/resources/static/index.html').read_text(encoding='utf-8')))
    page.route('**/api/reservations/availability?*',lambda r:r.fulfill(json={'available':True}))
    page.goto('http://reserve-hub.local/')
    page.wait_for_timeout(500)
    page.locator('#booking-date').fill('2030-10-01')
    page.locator('#booking-date').dispatch_event('change')
    page.wait_for_timeout(300)
    page.locator('.slot.available').nth(1).click()
    page.screenshot(path=str(root/'original/presentation_assets/reserve_hub_ui.png'),full_page=True)
    browser.close()
