package com.example.cyberpunk;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(64, 96, 64, 64);
        TextView tv = new TextView(this);
        tv.setText("Replaced Rain\n\nЖивые обои: дождь, неон, мокрое стекло.\nНажми кнопку и выбери \"Cyberpunk Rain\" в списке.");
        tv.setTextSize(16f);
        Button btn = new Button(this);
        btn.setText("Установить обои");
        btn.setOnClickListener(v -> {
            Intent i = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(this, WebWallpaperService.class));
            try { startActivity(i); }
            catch (Exception e) {
                startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            }
        });
        root.addView(tv);
        root.addView(btn);
        setContentView(root);
    }
}