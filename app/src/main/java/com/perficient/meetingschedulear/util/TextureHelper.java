package com.perficient.meetingschedulear.util;

import android.content.Context;
import android.graphics.*;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.perficient.meetingschedulear.BaseApplication;

import java.util.ArrayList;
import java.util.Scanner;

public class TextureHelper {

    public static int loadTexture(final Context context, final int resourceId, String text) {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;    // No pre-scaling

            // Read in the resource
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            ArrayList<String> textList = new ArrayList<>();
            Scanner scanner = new Scanner(text).useDelimiter("\\n");
            while (scanner.hasNext()) {
                textList.add(scanner.next());
            }
            bitmap = drawText(bitmap, textList);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);// GL_NEAREST make the low resolution texture more clear on a large object
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);// stretched edge pattern
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    private static Bitmap drawText(Bitmap bitmap, ArrayList<String> text) {
        float scale = BaseApplication.getResourcesObject().getDisplayMetrics().density;

        Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 255, 255));
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        int x = 0;
        int y = 0;
        for (int i = 0; i < text.size(); i++) {
            String oneLine = text.get(i);
            Rect bounds = new Rect();
            paint.getTextBounds(oneLine, 0, oneLine.length(), bounds);
            x = (bitmap.getWidth() - bounds.width()) / 2;
            y = (bitmap.getHeight() + bounds.height()) / 2 + i * 50;
            canvas.drawText(oneLine, x, y, paint);
        }

        return bitmap;
    }
}