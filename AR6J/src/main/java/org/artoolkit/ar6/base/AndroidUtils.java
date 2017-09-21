/*
 *  AndroidUtils.java
 *  ARToolKit6
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Julian Looser, Philip Lamb
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.artoolkit.ar6.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.artoolkit.ar6.base.camera.Cam2CaptureSurface;
import org.artoolkit.ar6.base.camera.CamCaptureHandler;
import org.artoolkit.ar6.base.camera.CameraEventListener;
import org.artoolkit.ar6.base.camera.CaptureCameraPreview;
import org.artoolkit.ar6.base.rendering.ARRenderer;

import java.io.File;
import java.text.DecimalFormat;

/**
 * A collection of utility functions for performing common and useful operations
 * specific to Android.
 */
public class AndroidUtils {

    /**
     * Android logging tag for this class.
     */
    private static final String TAG = AndroidUtils.class.getSimpleName();

    public static final int VIEW_VISIBILITY = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    /**
     * Returns a String summarising the Android build information.
     *
     * @return The Android build information.
     */
    public static String androidBuildVersion() {

        StringBuffer buf = new StringBuffer();
        buf.append("Version\n *Release: " + Build.VERSION.RELEASE);    // The user-visible version string. E.g., "1.0" or "3.4b5".
        buf.append("\n Incremental: " + Build.VERSION.INCREMENTAL);    // The internal value used by the underlying source control to represent this build.
        buf.append("\n Codename: " + Build.VERSION.CODENAME);        // The current development codename, or the string "REL" if this is a release build.
        buf.append("\n SDK: " + Build.VERSION.SDK_INT);                // The user-visible SDK version of the framework.
        buf.append("\n\n*Model: " + Build.MODEL);                    // The end-user-visible name for the end product..
        buf.append("\nManufacturer: " + Build.MANUFACTURER);        // The manufacturer of the product/hardware.
        buf.append("\nBoard: " + Build.BOARD);                        // The name of the underlying board, like "goldfish".
        buf.append("\nBrand: " + Build.BRAND);                        // The brand (e.g., carrier) the software is customized for, if any.
        buf.append("\nDevice: " + Build.DEVICE);                    // The name of the industrial design.
        buf.append("\nProduct: " + Build.PRODUCT);                    // The name of the overall product.
        buf.append("\nHardware: " + Build.HARDWARE);                // The name of the hardware (from the kernel command line or /proc).
        buf.append("\nCPU ABI: " + Build.CPU_ABI);                    // The name of the instruction set (CPU type + ABI convention) of native code.
        buf.append("\nCPU second ABI: " + Build.CPU_ABI2);            // The name of the second instruction set (CPU type + ABI convention) of native code.
        buf.append("\n\n*Displayed ID: " + Build.DISPLAY);            // A build ID string meant for displaying to the user.
        buf.append("\nHost: " + Build.HOST);
        buf.append("\nUser: " + Build.USER);
        buf.append("\nID: " + Build.ID);                            // Either a changelist number, or a label like "M4-rc20".
        buf.append("\nType: " + Build.TYPE);                        // The type of build, like "user" or "eng".
        buf.append("\nTags: " + Build.TAGS);                        // Comma-separated tags describing the build, like "unsigned,debug".
        buf.append("\n\nFingerprint: " + Build.FINGERPRINT);        // A string that uniquely identifies this build. 'BRAND/PRODUCT/DEVICE:RELEASE/ID/VERSION.INCREMENTAL:TYPE/TAGS'.
        buf.append("\n\nItems with * are intended for display to the end user.");

        return buf.toString();
    }

    /**
     * Returns whether or not there is an SD card mounted.
     *
     * @return true if an SD card is mounted, otherwise false.
     */
    public static boolean isSDCardMounted() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }


    /**
     * Returns the number of bytes of external storage available.
     *
     * @return Bytes of external storage available.
     */
    static public long getAvailableExternalMemorySize() {
        if (isSDCardMounted()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    /**
     * Returns the total number of bytes of external storage.
     *
     * @return Bytes of external storage in total, or -1 if no SD card mounted.
     */
    static public long getTotalExternalMemorySize() {
        if (isSDCardMounted()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }


    /**
     * Returns a formatted string representation of the number of bytes specified. The largest
     * suitable suffix up until GB will be used, with the returned value expressed to two
     * decimal places.
     *
     * @param bytes The number of bytes to be reported.
     * @return The specified number of bytes, formatted as a string, in bytes, KB, MB or GB.
     */
    static public String formatBytes(long bytes) {

        double val = 0;
        String units = "";

        if (bytes < 1024) {
            val = bytes;
            units = "bytes";
        } else if (bytes < 1048576) {
            val = (bytes / 1024.0f);
            units = "KB";
        } else if (bytes < 1073741824) {
            val = (bytes / 1048576.0f);
            units = "MB";
        } else {
            val = (bytes / 1073741824.0f);
            units = "GB";
        }

        DecimalFormat df = new DecimalFormat("###.##");
        return df.format(val) + " " + units;
    }

    /**
     * Reports to the log information about the device's display. This information includes
     * the width and height, and density (low, medium, high).
     *
     * @param activity The Activity to report on.
     */
    public static void reportDisplayInformation(Activity activity) {

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;

        String density = "unknown";
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                density = "Low";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                density = "Medium";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                density = "High";
                break;
        }

        Log.i(TAG, "reportDisplayInformation(): Display is " + displayWidth + "x" + displayHeight
                + ", Density: " + density);
    }

    public static void prepareRenderSurfaceAndAddToLayout(FrameLayout frameLayout, final boolean usingCamera2APIs, CamCaptureHandler cameraCaptureSurfaceView, GLSurfaceView openGlSurfaceView, final Activity activity, ARRenderer renderer) {

    }

    @NonNull
    public static CamCaptureHandler createCamCaptureView(boolean usingCamera2APIs, Activity activity, CameraEventListener cameraEventListener) {
        CamCaptureHandler cameraCaptureSurfaceView;
        if (usingCamera2APIs) {
            // Create the camera preview
            cameraCaptureSurfaceView = new Cam2CaptureSurface(activity, cameraEventListener);
            Log.i(TAG, "onResume(): Cam2CaptureSurface constructed");
        } else {
            cameraCaptureSurfaceView = new CaptureCameraPreview(activity, cameraEventListener);
            Log.i(TAG, "onResume(): CaptureCameraPreview constructed");
        }
        return cameraCaptureSurfaceView;
    }
}
