/*
 *  CameraEventListenerImpl.java
 *  ARToolKit6
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Thorsten Bux, Julian Looser, Philip Lamb
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

package org.artoolkit.ar6.base.camera;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.artoolkit.ar6.base.ARToolKit;
import org.artoolkit.ar6.base.ARToolKitCallback;

import java.nio.ByteBuffer;

public class CameraEventListenerImpl implements CameraEventListener {
    private final Activity arActivity;
    private static String TAG = CameraEventListenerImpl.class.getName();
    private final ARToolKitCallback arCallbackListener;
    private boolean firstUpdate;

    public CameraEventListenerImpl(Activity arActivity, ARToolKitCallback arCallbackListener) {
        this.arActivity = arActivity;
        this.arCallbackListener = arCallbackListener;
    }

    @Override
    public void cameraStarted(int width, int height, String pixelFormat, int cameraIndex, boolean cameraIsFrontFacing) {

        if (ARToolKit.getInstance().startWithPushedVideo(width, height, pixelFormat, null, cameraIndex, cameraIsFrontFacing)) {
            // Expects Data to be already in the cache dir. This can be done with the AssetUnpacker.
            Log.i(TAG, "Initialised AR.");
        } else {
            // Error
            Log.e(TAG, "Error initialising AR. Cannot continue.");
            arActivity.finish();
        }

        Toast.makeText(arActivity, "Camera settings: " + width + "x" + height, Toast.LENGTH_SHORT).show();
        firstUpdate = true;
    }

    @Override
    public void cameraFrame1(byte[] frame, int frameSize) {
        if (firstUpdate) {
            arCallbackListener.firstFrame();
            firstUpdate = false;
        }

        if (ARToolKit.getInstance().convertAndDetect1(frame, frameSize)) {
            arCallbackListener.onFrameProcessed();
        }
    }

    @Override
    public void cameraFrame2(ByteBuffer[] framePlanes, int[] framePlanePixelStrides, int[] framePlaneRowStrides) {
        if (firstUpdate) {
            arCallbackListener.firstFrame();
            firstUpdate = false;
        }

        if (ARToolKit.getInstance().convertAndDetect2(framePlanes, framePlanePixelStrides, framePlaneRowStrides)) {
            arCallbackListener.onFrameProcessed();
        }
    }

    @Override
    public void cameraStopped() {
        ARToolKit.getInstance().stopAndFinal();
    }
}
