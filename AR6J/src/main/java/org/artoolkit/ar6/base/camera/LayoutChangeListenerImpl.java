package org.artoolkit.ar6.base.camera;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import org.artoolkit.ar6.base.AndroidUtils;

/*
 *  UnityCameraEventListener.java
 *  ARToolKit6
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2010-2015 ARToolworks, Inc.
 *
 *  Author(s): Thorsten Bux, Philip Lamb
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
public class LayoutChangeListenerImpl implements View.OnLayoutChangeListener {
    private final static String TAG = LayoutChangeListenerImpl.class.getSimpleName();
    private final Activity activity;
    private final boolean usingCamera2APIs;
    private final CamCaptureHandler cameraCaptureSurfaceView;

    public LayoutChangeListenerImpl(Activity activity, boolean usingCamera2APIs, CamCaptureHandler cameraCaptureSurfaceView) {
        this.activity = activity;
        this.usingCamera2APIs = usingCamera2APIs;
        this.cameraCaptureSurfaceView = cameraCaptureSurfaceView;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

        View decorView = activity.getWindow().getDecorView();
        if (AndroidUtils.VIEW_VISIBILITY == decorView.getSystemUiVisibility()) {
            if (usingCamera2APIs && !cameraCaptureSurfaceView.gettingCameraAccessPermissionsFromUser()) {

                if (!((Cam2CaptureSurface) (cameraCaptureSurfaceView)).mImageReaderCreated) {
                    ((Cam2CaptureSurface) (cameraCaptureSurfaceView)).surfaceCreated();
                }
                if (!((Cam2CaptureSurface) (cameraCaptureSurfaceView)).isCamera2DeviceOpen())
                    ((Cam2CaptureSurface) (cameraCaptureSurfaceView)).surfaceChanged();
            }
        } else{
            Log.v(TAG,"Not in fullscreen.");
        }
    }
}
