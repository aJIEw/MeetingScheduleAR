/*
 *  CaptureCameraPreview.java
 *  ARToolKit5
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2013-2015 ARToolworks, Inc.
 *
 *  Author(s): Philip Lamb, John Wolf
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import org.artoolkit.ar6.base.ARActivity;
import org.artoolkit.ar6.base.NativeInterface;
import org.artoolkit.ar6.base.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
/*
SurfaceView provides a dedicated drawing surface embedded inside of a view hierarchy. You can control the format
of this surface and, if you like, its size. The SurfaceView takes care of placing the surface at the correct
location on the screen
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP) // Camera2 available in OS API Level 21
public class Cam2CaptureSurface implements CamCaptureHandler
{
    /**
     * Android logging tag for this class.
     */
    private static final String TAG = Cam2CaptureSurface.class.getSimpleName();
    private final Context mAppContext;
    /**
     * Listener to inform of camera related events: start, frame, and stop.
     */
    private CameraEventListener cameraEventListener;

    private CameraDevice mCamera2Device;

    private CameraDevice.StateCallback mCamera2DeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera2DeviceInstance) {
            mCamera2Device = camera2DeviceInstance;
            startCaptureAndForwardFramesSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera2DeviceInstance) {
            camera2DeviceInstance.close();
            mCamera2Device = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera2DeviceInstance, int error) {
            camera2DeviceInstance.close();
            mCamera2Device = null;
        }
    };

    /**
     * Tracks if SurfaceView instance was created.
     */
    public boolean mImageReaderCreated = false;
    private boolean mustAskPermissionFirst = false;
    public boolean gettingCameraAccessPermissionsFromUser()
    {
        return mustAskPermissionFirst;
    }

    public void resetGettingCameraAccessPermissionsFromUserState() {
        mustAskPermissionFirst = false;
    }

    /**
     * Indicates whether or not camera2 device instance is available, opened, enabled.
     */
    public boolean isCamera2DeviceOpen() {
        return (null != mCamera2Device);
    }

    /**
     * Constructor takes a {@link CameraEventListener} which will be called on
     * to handle camera related events.
     *
     * @param cel CameraEventListener to use.
     */
    public Cam2CaptureSurface(Activity activity, CameraEventListener cel) {
        Log.i(TAG, "Cam2CaptureSurface(): ctor called");

        mAppContext = activity.getApplicationContext();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)) {
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
                    activity.requestPermissions(new String[] { Manifest.permission.CAMERA }, ARActivity.REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "CaptureCameraPreview(): exception caught, " + ex.getMessage() + ", abnormal exit");
        }
        this.cameraEventListener = cel;
    }

    private ImageReader mImageReader;
    private Size mImageReaderVideoSize;


    private ImageReader.OnImageAvailableListener mImageAvailableAndProcessHandler = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader)
        {

            Image imageInstance = reader.acquireLatestImage();
            if (imageInstance == null) {
                //Note: This seems to happen quite often.
                Log.v(TAG, "onImageAvailable(): unable to acquire new image");
                return;
            }

            // Get a ByteBuffer for each plane.
            final Image.Plane[] imagePlanes = imageInstance.getPlanes();
            final int imagePlaneCount = Math.min(4, imagePlanes.length); // We can handle up to 4 planes max.
            final ByteBuffer[] imageBuffers = new ByteBuffer[imagePlaneCount];
            final int[] imageBufferPixelStrides = new int[imagePlaneCount];
            final int[] imageBufferRowStrides = new int[imagePlaneCount];
            for (int i = 0; i < imagePlaneCount; i++) {
                imageBuffers[i] = imagePlanes[i].getBuffer();
                // For ImageFormat.YUV_420_888 the order of planes in the array returned by Image.getPlanes()
                // is guaranteed such that plane #0 is always Y, plane #1 is always U (Cb), and plane #2 is always V (Cr).
                // The Y-plane is guaranteed not to be interleaved with the U/V planes (in particular, pixel stride is
                // always 1 in yPlane.getPixelStride()). The U/V planes are guaranteed to have the same row stride and
                // pixel stride (in particular, uPlane.getRowStride() == vPlane.getRowStride() and uPlane.getPixelStride() == vPlane.getPixelStride(); ).
                imageBufferPixelStrides[i] = imagePlanes[i].getPixelStride();
                imageBufferRowStrides[i] = imagePlanes[i].getRowStride();
            }

            if (cameraEventListener != null) {
                cameraEventListener.cameraFrame2(imageBuffers, imageBufferPixelStrides, imageBufferRowStrides);
            }

            imageInstance.close();
        }
    };

    @SuppressLint("NewApi")
    public void surfaceCreated() {
        Log.i(TAG, "surfaceCreated(): called");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        int defaultCameraIndexId = mAppContext.getResources().getIdentifier("pref_defaultValue_cameraIndex","string",mAppContext.getPackageName());
        mCamera2DeviceID = Integer.parseInt(prefs.getString("pref_cameraIndex", mAppContext.getResources().getString(defaultCameraIndexId)));
        Log.i(TAG, "surfaceCreated(): will attempt to open camera \"" + mCamera2DeviceID +
                       "\", set orientation, set preview surface");

        /*
        Set the resolution from the settings as size for the glView. Because the video stream capture
        is requested based on this size.

        WARNING: While coding the preferences are taken from the AR6J res/xml/preferences.xml!!!
        When building for Unity the actual used preferences are taken from the UnityARPlayer project!!!
        */
        int defaultCameraValueId = mAppContext.getResources().getIdentifier("pref_defaultValue_cameraResolution","string",mAppContext.getPackageName());
        String camResolution = prefs.getString("pref_cameraResolution", mAppContext.getResources(). getString(defaultCameraValueId));
        String[] dims = camResolution.split("x", 2);
        mImageReaderVideoSize =  new Size(Integer.parseInt(dims[0]),Integer.parseInt(dims[1]));

        // Note that maxImages should be at least 2 for acquireLatestImage() to be any different than acquireNextImage() -
        // discarding all-but-the-newest Image requires temporarily acquiring two Images at once. Or more generally,
        // calling acquireLatestImage() with less than two images of margin, that is (maxImages - currentAcquiredImages < 2)
        // will not discard as expected.
        mImageReader = ImageReader.newInstance(mImageReaderVideoSize.getWidth(),mImageReaderVideoSize.getHeight(), ImageFormat.YUV_420_888, /* The maximum number of images the user will want to access simultaneously:*/ 2 );
        mImageReader.setOnImageAvailableListener(mImageAvailableAndProcessHandler, null);

        mImageReaderCreated = true;

    } // end: public void surfaceCreated(SurfaceHolder holder)

    /* Interface implemented by this SurfaceView subclass
       holder: SurfaceHolder instance associated with SurfaceView instance that changed
       format: pixel format of the surface
       width: of the SurfaceView instance
       height: of the SurfaceView instance
    */
    public void surfaceChanged() {
        Log.i(TAG, "surfaceChanged(): called");

        // This is where to calculate the optimal size of the display and set the aspect ratio
        // of the surface view (probably the service holder). Also where to Create transformation
        // matrix to scale and then rotate surface view, if the app is going to handle orientation
        // changes.
        if (!mImageReaderCreated) {
            surfaceCreated();
        }
        if (!isCamera2DeviceOpen()) {
            openCamera2(mCamera2DeviceID);
        }
        if (isCamera2DeviceOpen() && (null == mYUV_CaptureAndSendSession)) {
            startCaptureAndForwardFramesSession();
        }
    }

    private void openCamera2(int camera2DeviceID) {
        Log.i(TAG, "openCamera2(): called");
        CameraManager camera2DeviceMgr = (CameraManager)mAppContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(mAppContext, Manifest.permission.CAMERA)) {
                camera2DeviceMgr.openCamera(Integer.toString(camera2DeviceID), mCamera2DeviceStateCallback, null);
                return;
            }
        } catch (CameraAccessException ex) {
            Log.e(TAG, "openCamera2(): CameraAccessException caught, " + ex.getMessage());
        } catch (Exception ex) {
            Log.e(TAG, "openCamera2(): exception caught, " + ex.getMessage());
        }
        if (null == camera2DeviceMgr) {
            Log.e(TAG, "openCamera2(): Camera2 DeviceMgr not set");
        }
        Log.e(TAG, "openCamera2(): abnormal exit");
    }

    private int mCamera2DeviceID = -1;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CameraCaptureSession mYUV_CaptureAndSendSession;

    private void startCaptureAndForwardFramesSession() {

        if ((null == mCamera2Device) || (!mImageReaderCreated) /*|| (null == mPreviewSize)*/) {
            return;
        }

        closeYUV_CaptureAndForwardSession();

        try {
            mCaptureRequestBuilder = mCamera2Device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List<Surface> surfaces = new ArrayList<>();

            Surface surfaceInstance;
            surfaceInstance = mImageReader.getSurface();
            surfaces.add(surfaceInstance);
            mCaptureRequestBuilder.addTarget(surfaceInstance);

            mCamera2Device.createCaptureSession(
                surfaces, // Output surfaces
                new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        try {
                            if (cameraEventListener != null) {
                                cameraEventListener.cameraStarted(mImageReaderVideoSize.getWidth(), mImageReaderVideoSize.getHeight(), "YUV_420_888", mCamera2DeviceID, false);
                            }
                            mYUV_CaptureAndSendSession = session;
                            // Session to repeat request to update passed in camSensorSurface
                            mYUV_CaptureAndSendSession.setRepeatingRequest(mCaptureRequestBuilder.build(), /* CameraCaptureSession.CaptureCallback cameraEventListener: */null, /* Background thread: */ null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Toast.makeText(mAppContext, "Unable to setup camera sensor capture session", Toast.LENGTH_SHORT).show();
                   }
                }, // Callback for capture session state updates
                null); // Secondary thread message queue
        } catch (CameraAccessException ex) {
            ex.printStackTrace();
        }
    }

    public void closeCameraDevice() {
        closeYUV_CaptureAndForwardSession();
        if (null != mCamera2Device) {
            mCamera2Device.close();
            mCamera2Device = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
        if (cameraEventListener != null) {
            cameraEventListener.cameraStopped();
        }
        mImageReaderCreated = false;
    }

    @Override
    public void registerCameraEventListener(CameraEventListener cel) {
        this.cameraEventListener = cel;
    }

    private void closeYUV_CaptureAndForwardSession() {
        if (mYUV_CaptureAndSendSession != null) {
            mYUV_CaptureAndSendSession.close();
            mYUV_CaptureAndSendSession = null;
        }
    }
} // end: public class Cam2CaptureSurface extends SurfaceView implements SurfaceHolder.Callback
