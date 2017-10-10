package com.perficient.meetingschedulear.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.widget.TextView;

import com.perficient.meetingschedulear.util.ARManager;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import cn.easyar.Engine;

public class GLView extends GLSurfaceView {

    private static final String TAG = GLView.class.getSimpleName();

    private Context mContext;

    private final ARManager mARManager;

    private ARManager.ViewRefresher mViewRefresher;

    private TextView mMeetingInfoTv;

    public GLView(Context context, TextView textView) {
        super(context);

        mContext = context;
        mMeetingInfoTv = textView;

        setEGLContextFactory(new ContextFactory());
        setEGLConfigChooser(new ConfigChooser());

        mARManager = new ARManager(context);
        mViewRefresher = new ARManager.ViewRefresher() {
            @Override
            public void refresh(final String text) {
                mMeetingInfoTv.post(new Runnable() {
                    @Override
                    public void run() {
                        mMeetingInfoTv.setText(text);
                    }
                });
            }
        };

        this.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                synchronized (mARManager) {
                    mARManager.initGL();
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                synchronized (mARManager) {
                    mARManager.resizeGL(w, h);
                }
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                synchronized (mARManager) {
                    mARManager.render();
                }
            }
        });

        this.setZOrderMediaOverlay(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        synchronized (mARManager) {
            if (mARManager.initialize(mViewRefresher)) {
                mARManager.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        synchronized (mARManager) {
            mARManager.stop();
            mARManager.dispose();
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onResume() {
        super.onResume();
        Engine.onResume();
    }

    @Override
    public void onPause() {
        Engine.onPause();
        super.onPause();
    }

    /**
     * Custom context for OpenGL ES
     */
    private static class ContextFactory implements GLSurfaceView.EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            EGLContext context;
            int[] attrib = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    /**
     * Custom ConfigChooser
     */
    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int[] attrib = {EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

            int[] num_config = new int[1];
            egl.eglChooseConfig(display, attrib, null, 0, num_config);

            int numConfigs = num_config[0];
            if (numConfigs <= 0)
                throw new IllegalArgumentException("fail to choose EGL configs");

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, attrib, configs, numConfigs,
                    num_config);

            for (EGLConfig config : configs) {
                int[] val = new int[1];
                int r = 0, g = 0, b = 0, a = 0, d = 0;
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, val))
                    d = val[0];
                if (d < 16)
                    continue;

                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, val))
                    r = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, val))
                    g = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, val))
                    b = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, val))
                    a = val[0];
                if (r == 8 && g == 8 && b == 8 && a == 0)
                    return config;
            }

            return configs[0];
        }
    }
}
