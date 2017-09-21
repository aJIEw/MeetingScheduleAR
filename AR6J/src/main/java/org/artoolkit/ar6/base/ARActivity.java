/*
 *  ARActivity.java
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

package org.artoolkit.ar6.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import org.artoolkit.ar6.base.camera.CamCaptureHandler;
import org.artoolkit.ar6.base.camera.CameraEventListener;
import org.artoolkit.ar6.base.camera.CameraEventListenerImpl;
import org.artoolkit.ar6.base.camera.CameraPreferencesActivity;
import org.artoolkit.ar6.base.camera.LayoutChangeListenerImpl;
import org.artoolkit.ar6.base.rendering.ARRenderer;

/**
 * An activity which can be subclassed to create an AR application. ARActivity handles almost all of
 * the required operations to create a simple augmented reality application.
 * <p/>
 * ARActivity automatically creates a camera preview surface and an OpenGL surface view, and
 * arranges these correctly in the user interface.The subclass simply needs to provide a FrameLayout
 * object which will be populated with these UI components, using {@link #supplyFrameLayout() supplyFrameLayout}.
 * <p/>
 * To create a custom AR experience, the subclass should also provide a custom renderer using
 * {@link #supplyRenderer() Renderer}. This allows the subclass to handle OpenGL drawing calls on its own.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public abstract class ARActivity extends Activity implements View.OnClickListener {

    /**
     * Used to match-up permission user request to user response
     */
    public static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;

    /**
     * Android logging tag for this class.
     */
    private final static String TAG = "AR6J::ARActivity";

    /**
     * Renderer to use. This is provided by the subclass using {@link #supplyRenderer() Renderer()}.
     */
    private ARRenderer renderer;

    /**
     * Layout that will be filled with the camera preview and GL views. This is provided by the subclass using {@link #supplyFrameLayout() supplyFrameLayout()}.
     */
    private FrameLayout mainFrameLayout;

    /**
     * Camera preview which will provide video frames.
     */
    private CamCaptureHandler mCamCaptureSurfaceView = null;

    /**
     * GL surface to render the virtual objects
     */
    private GLSurfaceView mOpenGlSurfaceViewInstance;

	/**
     * For any square template (pattern) markers, the number of rows
     * and columns in the template. May not be less than 16 or more than AR_PATT_SIZE1_MAX.
     */
    private int mPattSize = 16;

	/**
     * For any square template (pattern) markers, the maximum number
     * of markers that may be loaded for a single matching pass. Must be > 0.
     */
    private int mPattCountMax = 25;

    private boolean mUsingCamera2APIs;


    private Context mContext;
    private ImageButton mSettingButton;

    @SuppressWarnings("unused")
    public Context getAppContext() {
        return mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        // This needs to be done just only the very first time the application is run,
        // or whenever a new preference is added (e.g. after an application upgrade).
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // This locks the orientation. Hereafter, any API returning display orientation data will
        // return the data representing this orientation no matter the current position of the
        // device.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        AndroidUtils.reportDisplayInformation(this);
    }

    /**
     * Allows subclasses to supply a custom {@link Renderer}.
     *
     * @return The {@link Renderer} to use.
     */
    protected abstract ARRenderer supplyRenderer();

    /**
     * Allows subclasses to supply a {@link FrameLayout} which will be populated
     * with a camera preview and GL surface view.
     *
     * @return The {@link FrameLayout} to use.
     */
    protected abstract FrameLayout supplyFrameLayout();

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart(): called");
        // Use cache directory as root for native path references.
        // The AssetFileTransfer class can help with unpacking from the built .apk to the cache.
        if (!ARToolKit.getInstance().initialiseNativeWithOptions(this.getCacheDir().getAbsolutePath(), mPattSize, mPattCountMax)) {
            notifyFinish("The native library is not loaded. The application cannot continue.\n\nNavigate to ARTOOLKIT6_HOME/Source and run './build.sh android'. Copy the created ABI-directories from build-android/ to AR6J/src/main/jniLibs/");
            return;
        }

        mainFrameLayout = supplyFrameLayout();
        if (mainFrameLayout == null) {
            Log.e(TAG, "onStart(): Error: supplyFrameLayout did not return a layout.");
            return;
        }

        renderer = supplyRenderer();
        if (renderer == null) {
            Log.e(TAG, "onStart(): Error: supplyRenderer did not return a renderer.");
            notifyFinish("You need to supply a renderer. Create your own renderer class (MyArRenderer) and derive it from ARRenderer.");
        }
    }



    @Override
    public void onResume() {
        Log.i(TAG, "onResume(): called");
        super.onResume();

        if (!ARToolKit.getInstance().isNativeInited()){
            return;
        }

        //API Level: 22 use camera2API
        mUsingCamera2APIs = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

        // Create the GL view
        mOpenGlSurfaceViewInstance = new GLSurfaceView(this);

        ARToolKitCallback artkCallback = new ARToolKitCallbackImpl(renderer,this,mOpenGlSurfaceViewInstance);
        CameraEventListener cameraEventListener = new CameraEventListenerImpl(this,artkCallback);
        mCamCaptureSurfaceView = AndroidUtils.createCamCaptureView(mUsingCamera2APIs, this, cameraEventListener);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            Log.i(TAG, "onResume(): OpenGL ES 2.x is supported");
            // Request an OpenGL ES 2.0 compatible mContext.
            mOpenGlSurfaceViewInstance.setEGLContextClientVersion(2);

        } else {
            Log.e(TAG, "onResume(): Only OpenGL ES 1.x is supported. ARToolKit6 requires devices with at least OpenGL ES 2.0 support.");
            throw new RuntimeException("\"ARToolKit6 requires devices with at least OpenGL ES 2.0 support.\"");
        }

        if(renderer != null) { //In case of using this method from UNITY we do not provide a renderer
            mOpenGlSurfaceViewInstance.setRenderer(renderer);
        }
        mOpenGlSurfaceViewInstance.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // Only render when we have a frame (must call requestRender()).
        mOpenGlSurfaceViewInstance.addOnLayoutChangeListener(new LayoutChangeListenerImpl(this, mUsingCamera2APIs, mCamCaptureSurfaceView));

        Log.i(TAG, "onResume(): GLSurfaceView created");

        // Add the OpenGL view which will be used to render the video background and the virtual environment.
        if (!mUsingCamera2APIs) {
            // In order for Camera to correctly generate preview frames, the preview has to be somewhere in the
            // view hierarchy. It will be covered completely by the OpenGL surface, but will still function OK.
            mainFrameLayout.addView((SurfaceView)mCamCaptureSurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mOpenGlSurfaceViewInstance.setZOrderMediaOverlay(true); // Request that GL view's SurfaceView be on top of other SurfaceViews (including CameraPreview's SurfaceView).
        }
        mainFrameLayout.addView(mOpenGlSurfaceViewInstance, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Log.i(TAG, "onResume(): Views added to main layout.");
        mOpenGlSurfaceViewInstance.onResume();

        if (mCamCaptureSurfaceView.gettingCameraAccessPermissionsFromUser()) {
            //No need to go further, must ask user to allow access to the camera first.
            return;
        }

        //Load settings button
        View settingsButtonLayout = this.getLayoutInflater().inflate(R.layout.settings_button_layout,mainFrameLayout,false);
        mSettingButton = (ImageButton) settingsButtonLayout.findViewById(R.id.button_settings);
        mainFrameLayout.addView(settingsButtonLayout);
        mSettingButton.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause(): called");
        if(ARToolKit.getInstance().isNativeInited()) {
            mCamCaptureSurfaceView.closeCameraDevice();

            if (mOpenGlSurfaceViewInstance != null) {
                mOpenGlSurfaceViewInstance.onPause();
                mainFrameLayout.removeView(mOpenGlSurfaceViewInstance);

            }

            // System hardware must be released in onPause(), so it's available to
            // any incoming activity. Removing the CameraPreview will do this for the
            // camera. Also do it for the GLSurfaceView, since it serves no purpose
            // with the camera preview gone.

            if (!mUsingCamera2APIs) {
                mainFrameLayout.removeView((SurfaceView) mCamCaptureSurfaceView);
            }
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop(): Activity stopping.");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, CameraPreferencesActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Returns the GL surface view.
     *
     * @return The GL surface view.
     */
    @SuppressWarnings("unused")
    public GLSurfaceView getGLView() {
        return mOpenGlSurfaceViewInstance;
    }

    @SuppressWarnings("unused")
    protected void showInfo() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setMessage("ARToolKit Version: " + NativeInterface.arwGetARToolKitVersion());

        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = dialogBuilder.create();
        alert.setTitle("ARToolKit");
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult(): called");

        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                notifyFinish("Application will not run with camera access denied");
                return;
            } else if (permissions.length > 0) {
                Toast.makeText(getApplicationContext(),
                               String.format("Camera access permission \"%s\" allowed", permissions[0]),
                               Toast.LENGTH_SHORT).show();
            }
            Log.i(TAG, "onRequestPermissionsResult(): reset ask for cam access perm");
            mCamCaptureSurfaceView.resetGettingCameraAccessPermissionsFromUserState();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        onStart();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus)
        {
            // Now can configure view to run  full screen
            decorView.setSystemUiVisibility(AndroidUtils.VIEW_VISIBILITY);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.equals(mSettingButton)) {
            v.getContext().startActivity(new Intent(v.getContext(), CameraPreferencesActivity.class));
        }

    }

    public ARRenderer getRenderer() {
        return renderer;
    }


    public void notifyFinish(String errorMessage) {
        new AlertDialog.Builder(this)
                .setMessage(errorMessage)
                .setTitle("Error")
                .setCancelable(true)
                .setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        })
                .show();
    }


} // end: public abstract class ARActivity extends Activity implements CameraEventListener