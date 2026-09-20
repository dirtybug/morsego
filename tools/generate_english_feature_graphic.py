import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def generate_english_feature_graphic():
    orig_path = "docs/playstore/playstore_feature_graphic_1024x500.png"
    im = Image.open(orig_path).convert("RGBA")
    
    # First, restore pure original image from git or memory
    import subprocess
    orig_bytes = subprocess.check_output(['git', 'show', 'HEAD:docs/playstore/playstore_feature_graphic_1024x500.png'])
    import io
    im = Image.open(io.BytesIO(orig_bytes)).convert("RGBA")
    
    w, h = 1024, 500
    bg_clean = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    
    # Sample the boundary column at x=640 from the original image
    boundary_colors = [im.getpixel((640, y)) for y in range(h)]
    
    # Generate smooth gradient matching top-left (10, 15, 29) transitioning to boundary at x=630
    for y in range(h):
        t_y = y / float(h)
        # Left edge color for row y
        left_r = 10.0 + 3.0 * t_y
        left_g = 15.0 + 5.0 * t_y
        left_b = 29.0 + 7.0 * t_y
        
        # Right boundary color for row y
        br, bg, bb, _ = boundary_colors[y]
        
        for x in range(650):
            t_x = x / 630.0
            r = left_r + (br - left_r) * t_x
            g = left_g + (bg - left_g) * t_x
            b = left_b + (bb - left_b) * t_x
            
            if x <= 620:
                alpha = 255
            elif x < 645:
                alpha = int(255 * (1.0 - (x - 620) / 25.0))
            else:
                alpha = 0
                
            bg_clean.putpixel((x, y), (int(r), int(g), int(b), alpha))
            
    # Draw the 24px dot grid on the cleaned area
    for y in range(11, h, 24):
        for x in range(11, 640, 24):
            t_x = x / 630.0
            t_y = y / float(h)
            dot_r = int(10 + 3.0 * t_x)
            dot_g = int(33 + 3.0 * t_x)
            dot_b = int(38 + 3.0 * t_x)
            
            if x <= 620:
                alpha = 255
            elif x < 640:
                alpha = int(255 * (1.0 - (x - 620) / 20.0))
            else:
                alpha = 0
                
            dot_color = (dot_r, dot_g, dot_b, alpha)
            for dx in [0, 1]:
                for dy in [0, 1]:
                    if x + dx < 645 and y + dy < h and alpha > 0:
                        bg_clean.putpixel((x + dx, y + dy), dot_color)
                        
    # Paste clean background with alpha mask onto original
    im.paste(bg_clean, (0, 0), bg_clean)
    
    # 2. Render typography and badges
    draw = ImageDraw.Draw(im)
    
    font_bold = "C:\\Windows\\Fonts\\segoeuib.ttf"
    font_reg = "C:\\Windows\\Fonts\\segoeui.ttf"
    font_semi = "C:\\Windows\\Fonts\\segoeuisb.ttf" if os.path.exists("C:\\Windows\\Fonts\\segoeuisb.ttf") else font_bold
    font_emoji = "C:\\Windows\\Fonts\\seguiemj.ttf"
    
    f_badge = ImageFont.truetype(font_bold, 14)
    f_badge_emoji = ImageFont.truetype(font_emoji, 13)
    
    f_title = ImageFont.truetype(font_bold, 44)
    f_sub = ImageFont.truetype(font_reg, 16)
    
    f_pill = ImageFont.truetype(font_semi, 13)
    f_pill_emoji = ImageFont.truetype(font_emoji, 13)
    
    f_amber = ImageFont.truetype(font_bold, 13)
    
    # --- Top Pill Badge: "⚡ INTERACTIVE CW TELEGRAPHY" ---
    bx, by = 60, 75
    badge_label = "INTERACTIVE CW TELEGRAPHY"
    bbox_l = draw.textbbox((0, 0), badge_label, font=f_badge)
    lw = bbox_l[2] - bbox_l[0]
    lh = bbox_l[3] - bbox_l[1]
    
    badge_w = 20 + 8 + lw + 28
    badge_h = 32
    draw.rounded_rectangle(
        [bx, by, bx + badge_w, by + badge_h],
        radius=14,
        fill=(8, 46, 54, 255),
        outline=(0, 230, 118, 255),
        width=2
    )
    # Draw lightning bolt emoji
    draw.text((bx + 12, by + 8), "⚡", font=f_badge_emoji, embedded_color=True)
    draw.text((bx + 32, by + 7), badge_label, font=f_badge, fill=(0, 230, 118, 255))
    
    # --- Title: "Master Morse Code \n with MorseGO" ---
    tx, ty1 = 60, 132
    draw.text((tx, ty1), "Master Morse Code", font=f_title, fill=(255, 255, 255, 255))
    
    ty2 = ty1 + 54
    draw.text((tx, ty2), "with ", font=f_title, fill=(255, 255, 255, 255))
    bbox_with = draw.textbbox((tx, ty2), "with ", font=f_title)
    mg_x = bbox_with[2]
    
    # Neon glow for MorseGO
    glow_img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_img)
    for off_x in range(-5, 6):
        for off_y in range(-5, 6):
            glow_draw.text((mg_x + off_x, ty2 + off_y), "MorseGO", font=f_title, fill=(0, 230, 118, 70))
    glow_blurred = glow_img.filter(ImageFilter.GaussianBlur(radius=8))
    im = Image.alpha_composite(im, glow_blurred)
    draw = ImageDraw.Draw(im)
    draw.text((mg_x, ty2), "MorseGO", font=f_title, fill=(0, 230, 118, 255))
    
    # --- Subtitle ---
    sub_lines = [
        "Visual learning with the Morse Binary Tree, dynamic audio practice, physical",
        "USB dual-paddle keyers with PARIS 10 WPM timing, and Ham Radio QSO training."
    ]
    sy = ty2 + 64
    for line in sub_lines:
        draw.text((tx, sy), line, font=f_sub, fill=(154, 169, 188, 255))
        sy += 24
        
    # --- Feature Pills ---
    pills_row1 = [
        ("🌳", "Morse Binary Tree"),
        ("🎧", "Audio Training"),
        ("📻", "USB Paddle Keyer")
    ]
    pills_row2 = [
        ("⚡", "PARIS Timing"),
        ("📱", "Responsive Layout")
    ]
    
    def draw_pills_row(pills, start_x, start_y):
        cur_x = start_x
        for emoji, label in pills:
            bbox = draw.textbbox((0, 0), label, font=f_pill)
            text_w = bbox[2] - bbox[0]
            text_h = bbox[3] - bbox[1]
            
            box_w = 14 + 18 + 6 + text_w + 14
            box_h = 32
            draw.rounded_rectangle(
                [cur_x, start_y, cur_x + box_w, start_y + box_h],
                radius=8,
                fill=(18, 28, 45, 255),
                outline=(35, 52, 78, 255),
                width=1
            )
            draw.text((cur_x + 12, start_y + 8), emoji, font=f_pill_emoji, embedded_color=True)
            draw.text((cur_x + 36, start_y + 7), label, font=f_pill, fill=(230, 237, 243, 255))
            cur_x += box_w + 12
            
    draw_pills_row(pills_row1, tx, 345)
    draw_pills_row(pills_row2, tx, 395)
    
    # --- Amber Pill under Bat Logo on the Right ---
    # Exact coordinates of original: x: 707 to 963 (width: 256), y: 364 to 394 (height: 30)
    ax, ay, aw, ah = 707, 364, 256, 30
    draw.rounded_rectangle(
        [ax, ay, ax + aw, ay + ah],
        radius=15,
        fill=(22, 26, 18, 255),
        outline=(255, 179, 0, 255),
        width=1
    )
    amber_text = "PARIS Standard 50 Dits • 10 WPM"
    bbox_a = draw.textbbox((0, 0), amber_text, font=f_amber)
    text_aw = bbox_a[2] - bbox_a[0]
    text_ax = ax + (aw - text_aw) // 2
    draw.text((text_ax, ay + 7), amber_text, font=f_amber, fill=(255, 179, 0, 255))
    
    # Convert to RGB and save
    out_img = im.convert("RGB")
    out_img.save("docs/playstore/playstore_feature_graphic_1024x500.png", "PNG", optimize=True)
    print("[OK] Successfully generated English feature graphic at docs/playstore/playstore_feature_graphic_1024x500.png")

if __name__ == "__main__":
    generate_english_feature_graphic()
