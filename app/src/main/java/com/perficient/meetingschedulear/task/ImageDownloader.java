package com.perficient.meetingschedulear.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.perficient.meetingschedulear.model.ImageTargetInfo;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.perficient.meetingschedulear.common.Constants.FILE_DIR_TARGET_IMAGE;


public class ImageDownloader extends AsyncTask<List<ImageTargetInfo>, Void, Void> {

    private static final String TAG = ImageDownloader.class.getSimpleName();

    private Context mContext;

    public ImageDownloader(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(List<ImageTargetInfo>... params) {
        List<ImageTargetInfo> targetInfos = params[0];
        for (ImageTargetInfo target: targetInfos) {
            try {
                Bitmap bitmap = Picasso.with(mContext)
                        .load(target.getUrl()).get();
                File imageDir = new File(
                        mContext.getExternalFilesDir(null),
                        FILE_DIR_TARGET_IMAGE);

                if (!imageDir.exists()) {
                    imageDir.mkdir();
                }
                // save image to external storage card
                saveImage(bitmap, imageDir, target.getImageName());

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    private void saveImage(Bitmap bitmap, File imageDir, String imageName) {
        Log.d(TAG, "onBitmapLoaded: saving image " + imageName);
        if (!imageDir.mkdir()) {
            Log.w(TAG, "SaveImageTarget: cannot create folder " + imageDir.getName());
            return;
        }
        File image = new File(imageDir, imageName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SaveImageTarget implements Target {

        private String mImageName;

        private File mImageDir;

        public SaveImageTarget(String imageName) {
            this.mImageName = imageName;
            this.mImageDir = new File(mContext.getExternalFilesDir(null), FILE_DIR_TARGET_IMAGE);
        }

        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.d(TAG, "onBitmapLoaded: saving image " + mImageName);
            if (!mImageDir.mkdir()) {
                Log.w(TAG, "SaveImageTarget: cannot create folder " + mImageDir.getName());
                return;
            }
            File image = new File(mImageDir, mImageName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i("image", "image saved to >>>" + image.getAbsolutePath());
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.e(TAG, "onBitmapFailed: " + errorDrawable.toString());
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d(TAG, "onPrepareLoad: ");
        }
    }
}
