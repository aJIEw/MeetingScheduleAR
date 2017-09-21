package org.artoolkit.ar6.base;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.artoolkit.ar6.base.rendering.ARRenderer;

public final class ARToolKitCallbackImpl implements ARToolKitCallback {

    private static String TAG = ARToolKitCallbackImpl.class.getSimpleName();
    private ARRenderer renderer;
    private Activity activity;
    private GLSurfaceView openGlSurfaceView;

    public ARToolKitCallbackImpl(ARRenderer renderer, Activity activity, GLSurfaceView openGlSurfaceView){
        this.renderer = renderer;
        this.activity = activity;
        this.openGlSurfaceView = openGlSurfaceView;
    }

    @Override
    public void firstFrame() {
        // ARToolKit has been initialised. The renderer can now add markers, etc...
        //TODO: Isn't there another place to see if ARTK is initialized so that we can do the configure scene there?
        if(renderer != null) {
            if (renderer.configureARScene()) {
                Log.i(TAG, "cameraPreviewFrame(): Scene configured successfully");
            } else {
                // Error
                Log.e(TAG, "cameraPreviewFrame(): Error configuring scene. Cannot continue.");
                activity.finish();
            }
        }
    }

    @Override
    public void onFrameProcessed() {
        // Update the renderer as the frame has changed
        if (openGlSurfaceView != null) {
            openGlSurfaceView.requestRender();
        }
    }

}
