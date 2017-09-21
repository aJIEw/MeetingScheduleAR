/*
 *  CaptureCameraPreview.java
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

package org.artoolkit.ar6.base.camera;

import android.annotation.SuppressLint;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.artoolkit.ar6.base.ARActivity;
import org.artoolkit.ar6.base.FPSCounter;
import org.artoolkit.ar6.base.NativeInterface;
import org.artoolkit.ar6.base.R;

//Deprecation is ignored because we use Camera1 API on purpose in this class
@SuppressWarnings("deprecation")
@SuppressLint("ViewConstructor")
public class CaptureCameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback, CamCaptureHandler
{

    /**
     * Android logging tag for this class.
     */
    private static final String TAG = "CaptureCameraPreview";

    /**
     * The Camera doing the capturing.
     */
    private Camera camera = null;

    /**
     * The camera capture width in pixels.
     */
    private int m_captureWidth;

    /**
     * The camera capture height in pixels.
     */
    private int m_captureHeight;

    /**
     * Counter to monitor the actual rate at which frames are captured from the camera.
     */
    private FPSCounter fpsCounter = new FPSCounter();

    private boolean m_isPreviewing = false;
    /**
     * Listener to inform of camera related events: start, frame, and stop.
     */
    private CameraEventListener cameraEventListener;

    private boolean mustAskPermissionFirst = false;
    public boolean gettingCameraAccessPermissionsFromUser()
    {
        return mustAskPermissionFirst;
    }

    public void resetGettingCameraAccessPermissionsFromUserState()
    {
        mustAskPermissionFirst = false;
    }

    /**
     * Constructor takes a {@link CameraEventListener} which will be called on
     * to handle camera related events.
     *
     * @param cel CameraEventListener to use. Can be null.
     */
    @SuppressWarnings("deprecation")
    public CaptureCameraPreview(Activity activity, CameraEventListener cel) {
        super(activity);

        Log.i(TAG, "CaptureCameraPreview(): ctor called");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                                                                           activity,
                                                                           Manifest.permission.CAMERA)) {
                    mustAskPermissionFirst = true;
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        // Will drop in here if user denied permissions access camera before.
                        // Or no uses-permission CAMERA element is in the
                        // manifest file. Must explain to the end user why the app wants
                        // permissions to the camera devices.
                        Toast.makeText(activity.getApplicationContext(),
                                       "App requires access to camera to be granted",
                                       Toast.LENGTH_SHORT).show();
                    }
                    // Request permission from the user to access the camera.
                    Log.i(TAG, "CaptureCameraPreview(): must ask user for camera access permission");
                    activity.requestPermissions(new String[]
                                                    {
                                                        Manifest.permission.CAMERA
                                                    },
                                                    ARActivity.REQUEST_CAMERA_PERMISSION_RESULT);
                    return;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "CaptureCameraPreview(): exception caught, " + ex.getMessage());
            return;
        }

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

         this.cameraEventListener = cel;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolderInstance) {
        int cameraIndex = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_cameraIndex", "0"));
        Log.i(TAG, "surfaceCreated(): called, opening camera " + cameraIndex + ", setting preview surface and orientation.");
        try {
            camera = Camera.open(cameraIndex);
        } catch (RuntimeException ex) {
            Log.e(TAG, "surfaceCreated(): RuntimeException " + ex.getMessage() + ".");
            return;
        //} catch (CameraAccessException ex) {
        //    Log.e(TAG, "openCamera(): CameraAccessException caught, " + ex.getMessage() + ", abnormal exit");
        //    return;
        } catch (Exception ex) {
            Log.e(TAG, "surfaceCreated()): Exception " + ex.getMessage() + ".");
            return;
        }

        if (!setPreviewOrientationAndSurface(surfaceHolderInstance, cameraIndex)) {
            Log.e(TAG, "surfaceCreated(): call to setPreviewOrientationAndSurface() failed.");
        } else {
            Log.i(TAG, "surfaceCreated(): succeeded");
        }
    }

    private boolean setPreviewOrientationAndSurface(SurfaceHolder surfaceHolderInstance, int cameraIndex)
    {
        Log.i(TAG, "setPreviewOrientationAndSurface(): called");
        boolean success = true;
        try {
            setCameraPreviewDisplayOrientation(cameraIndex, camera);
            camera.setPreviewDisplay(surfaceHolderInstance);
        } catch (IOException ex) {
            Log.e(TAG, "setPreviewOrientationAndSurface(): IOException " + ex.toString());
            success = false;
        } catch (Exception ex) {
            Log.e(TAG, "setPreviewOrientationAndSurface(): Exception " + ex.toString());
            success = false;
        }
        if (!success) {
            if (null != camera)  {
                camera.release();
                camera = null;
            }
            Log.e(TAG, "setPreviewOrientationAndSurface(): released camera due to caught exception");
        }
        return success;
    }

    private void setCameraPreviewDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        WindowManager wMgr = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = wMgr.getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;     // Landscape with camera on left side
            case Surface.ROTATION_90: degrees = 90; break;   // Portrait with camera on top side
            case Surface.ROTATION_180: degrees = 180; break; // Landscape with camera on right side
            case Surface.ROTATION_270: degrees = 270; break; // Portrait with camera on bottom side
        }

        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        // Set the clockwise rotation of preview display in degrees. This affects the preview frames and
        // the picture displayed after snapshot.
        camera.setDisplayOrientation(result);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolderInstance) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        Log.i(TAG, "surfaceDestroyed(): called");
        this.closeCameraDevice();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.i(TAG, "surfaceChanged(): called");
        if (camera == null) {
            // Camera wasn't opened successfully?
            Log.e(TAG, "surfaceChanged(): No camera in surfaceChanged");
            return;
        }

        if (!m_isPreviewing) {
            Log.i(TAG, "surfaceChanged(): Surfaced changed, setting up camera and starting preview");
            //String camResolution = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_cameraResolution", getResources().getString(R.string.pref_defaultValue_cameraResolution));
            int defaultCameraValueId = getResources().getIdentifier("pref_defaultValue_cameraResolution","string",getContext().getPackageName());
            String camResolution = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_cameraResolution", getResources(). getString(defaultCameraValueId));
            String[] dims = camResolution.split("x", 2);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
            camera.setParameters(parameters);

            parameters = camera.getParameters();
            m_captureWidth = parameters.getPreviewSize().width;
            m_captureHeight = parameters.getPreviewSize().height;
            int imageFormatEnum = parameters.getPreviewFormat(); // android.graphics.imageformat
            PixelFormat pixelinfo = new PixelFormat();
            PixelFormat.getPixelFormatInfo(imageFormatEnum, pixelinfo);
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int defaultCameraIndexId = getResources().getIdentifier("pref_defaultValue_cameraIndex","string",getContext().getPackageName());
            int cameraIndex = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("pref_cameraIndex", getResources().getString(defaultCameraIndexId)));
            Camera.getCameraInfo(cameraIndex, cameraInfo);
            boolean cameraIsFrontFacing = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);

            int bufSize = m_captureWidth * m_captureHeight * pixelinfo.bitsPerPixel / 8; // For the default NV21 format, bitsPerPixel = 12.
            Log.i(TAG, "surfaceChanged(): Camera buffers will be " + m_captureWidth + "x" + m_captureHeight + "@" + pixelinfo.bitsPerPixel + "bpp, " + bufSize + "bytes.");

            camera.setPreviewCallbackWithBuffer(this);
            for (int i = 0; i < 10; i++) {
                camera.addCallbackBuffer(new byte[bufSize]);
            }

            camera.startPreview();

            if (cameraEventListener != null) {
                cameraEventListener.cameraStarted(m_captureWidth, m_captureHeight, "NV21", cameraIndex, cameraIsFrontFacing);
            }

            m_isPreviewing = true;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if (cameraEventListener != null) {
            cameraEventListener.cameraFrame1(data, m_captureWidth*m_captureHeight + 2 * m_captureWidth/2*m_captureHeight/2);
        }

        camera.addCallbackBuffer(data);

        if (fpsCounter.frame()) {
            Log.i(TAG, "onPreviewFrame(): Camera capture FPS: " + fpsCounter.getFPS());
        }
    }

    @Override
    public void closeCameraDevice() {
        if (camera != null) {

            camera.setPreviewCallback(null);
            camera.stopPreview();
            m_isPreviewing = false;

            camera.release();
            camera = null;
        }

        if (cameraEventListener != null) cameraEventListener.cameraStopped();
    }

    /**
     * Sets the {@link CameraEventListener} which will be called on to handle camera
     * related events.
     *
     * @param cel CameraEventListener to use. Can be null.
     */
    @Override
    public void registerCameraEventListener(CameraEventListener cel) {
        this.cameraEventListener = cel;
    }
}
