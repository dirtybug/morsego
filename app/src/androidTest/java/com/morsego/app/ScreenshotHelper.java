package com.morsego.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Screenshot capturing utility for instrumented behavior and UI tests.
 * Captures the full screen using UiAutomation and writes PNGs to device storage.
 */
public class ScreenshotHelper {

    private static final String TAG = "MorseGOScreenshot";

    public static File capture(String name) {
        Bitmap bitmap = null;

        // Try capturing via UiAutomation (captures full system-rendered screen)
        try {
            bitmap = InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        } catch (Exception e) {
            Log.w(TAG, "UiAutomation.takeScreenshot failed: " + e.getMessage());
        }

        if (bitmap == null) {
            Log.e(TAG, "Failed to capture bitmap for screenshot: " + name);
            return null;
        }

        File outputFile = null;
        try {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "behavior_screenshots");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String fileName = String.format("%s_%s.png", name, timestamp);
            outputFile = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Log.i(TAG, "SCREENSHOT CAPTURED: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error saving screenshot: " + e.getMessage(), e);
        }

        return outputFile;
    }

    /**
     * Fallback capture directly from an Activity's root DecorView.
     */
    public static File captureActivityView(Activity activity, String name) {
        if (activity == null) return capture(name);

        final View rootView = activity.getWindow().getDecorView().getRootView();
        final Bitmap[] bitmapHolder = new Bitmap[1];

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Bitmap b = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            rootView.draw(c);
            bitmapHolder[0] = b;
        });

        if (bitmapHolder[0] == null) {
            return capture(name);
        }

        try {
            Context context = activity.getApplicationContext();
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "behavior_screenshots");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File outputFile = new File(dir, String.format("%s_view_%s.png", name, timestamp));

            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmapHolder[0].compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Log.i(TAG, "SCREENSHOT (VIEW) CAPTURED: " + outputFile.getAbsolutePath());
            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, "Error saving view screenshot: " + e.getMessage(), e);
            return null;
        }
    }
}
