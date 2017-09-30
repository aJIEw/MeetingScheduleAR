package com.perficient.meetingschedulear.renderer;


import android.util.Log;

import org.artoolkit.ar6.base.FPSCounter;
import org.artoolkit.ar6.base.rendering.ARRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressWarnings("JniMissingFunction")
public class NativeCarRenderer extends ARRenderer {

    // Load the native libraries.
    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("ARWrapper");
        System.loadLibrary("ARWrapperNativeCarsExample");
    }

    private FPSCounter counter = new FPSCounter();

    public static native void demoInitialise();

    public static native void demoShutdown();

    public static native void demoSurfaceCreated();

    public static native void demoSurfaceChanged(int w, int h);

    public static native void demoDrawFrame();

    /**
     * By overriding {@link #configureARScene}, the markers and other settings can be configured
     * after the native library is initialised, but prior to the rendering actually starting.
     * Note that this does not run on the OpenGL thread. Use onSurfaceCreated/demoSurfaceCreated
     * to do OpenGL initialisation.
     */
    @Override
    public boolean configureARScene() {
        NativeCarRenderer.demoInitialise();
        return true;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        super.onSurfaceChanged(gl, w, h);
        NativeCarRenderer.demoSurfaceChanged(w, h);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        NativeCarRenderer.demoSurfaceCreated();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        NativeCarRenderer.demoDrawFrame();

        if (counter.frame()) Log.i("demo", counter.toString());
    }

}
