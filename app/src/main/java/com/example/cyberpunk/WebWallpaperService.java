package com.example.cyberpunk;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() { return new WebEngine(); }

    private class WebEngine extends Engine {
        private WebView webView;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean visible = false, pageReady = false;
        private int surfaceW = 0, surfaceH = 0;

        private final Runnable drawLoop = new Runnable() {
            @Override public void run() { drawFrame(); handler.postDelayed(this, 33); }
        };

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            surfaceHolder.setFormat(PixelFormat.RGBA_8888);
            webView = new WebView(WebWallpaperService.this);
            WebSettings s = webView.getSettings();
            s.setJavaScriptEnabled(true);
            s.setDomStorageEnabled(true);
            s.setMediaPlaybackRequiresUserGesture(false);
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            webView.setBackgroundColor(0xFF000000);
            webView.setWebViewClient(new WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) {
                    pageReady = true; forceVisible();
                }
            });
            webView.loadUrl("file:///android_asset/index.html");
        }

        private void forceVisible() {
            try {
                java.lang.reflect.Method m = View.class.getDeclaredMethod("onWindowVisibilityChanged", int.class);
                m.setAccessible(true);
                m.invoke(webView, View.VISIBLE);
            } catch (Exception ignored) {}
            webView.onResume();
            webView.resumeTimers();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            surfaceW = width; surfaceH = height; layoutWeb();
        }

        private void layoutWeb() {
            if (webView == null || surfaceW == 0) return;
            webView.measure(View.MeasureSpec.makeMeasureSpec(surfaceW, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(surfaceH, View.MeasureSpec.EXACTLY));
            webView.layout(0, 0, surfaceW, surfaceH);
        }

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            super.onVisibilityChanged(isVisible);
            visible = isVisible;
            if (isVisible) {
                forceVisible();
                handler.removeCallbacks(drawLoop); handler.post(drawLoop);
                if (pageReady) webView.evaluateJavascript("window.WP&&WP.resume()", null);
            } else {
                handler.removeCallbacks(drawLoop);
                if (pageReady) webView.evaluateJavascript("window.WP&&WP.pause()", null);
            }
        }

        @Override
        public void onOffsetsChanged(float x, float y, float xs, float ys, int xp, int yp) {
            super.onOffsetsChanged(x, y, xs, ys, xp, yp);
            if (pageReady) webView.evaluateJavascript("window.WP&&WP.offset(" + x + ")", null);
        }

        private void drawFrame() {
            if (!visible || surfaceW == 0) return;
            SurfaceHolder holder = getSurfaceHolder();
            android.graphics.Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) webView.draw(canvas);
            } catch (Exception ignored) {
            } finally {
                if (canvas != null) {
                    try { holder.unlockCanvasAndPost(canvas); } catch (Exception ignored) {}
                }
            }
        }

        @Override
        public void onDestroy() {
            handler.removeCallbacks(drawLoop);
            if (webView != null) { webView.stopLoading(); webView.destroy(); webView = null; }
            super.onDestroy();
        }
    }
}