/*
 *  ARRendererGLES20.java
 *  ARToolKit6
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Thorsten Bux, Philip Lamb, Julian Looser, John Wolf
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

package org.artoolkit.ar6.base.rendering;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.artoolkit.ar6.base.ARActivity;
import org.artoolkit.ar6.base.ARToolKit;
import org.artoolkit.ar6.base.NativeInterface;
import org.artoolkit.ar6.base.rendering.shader_impl.SimpleFragmentShader;
import org.artoolkit.ar6.base.rendering.shader_impl.SimpleShaderProgram;
import org.artoolkit.ar6.base.rendering.shader_impl.SimpleVertexShader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Base renderer which should be subclassed in the main application and provided
 * to the ARActivity using its {@link ARActivity#supplyRenderer() supplyRenderer} method.
 * <p/>
 * Subclasses should override {@link #configureARScene() configureARScene}, which will be called by
 * the Activity when AR initialisation is complete. The Renderer can use this method
 * to add markers to the scene, and perform other scene initialisation.
 * <p/>
 * The {@link #draw() render} method should also be overridden to perform actual rendering. This is
 * in preference to directly overriding {@link #onDrawFrame(GL10) onDrawFrame}, because {@link ARRenderer} will check
 * that ARToolKit is running before calling {@link #draw()}.
 */
public abstract class ARRenderer implements GLSurfaceView.Renderer {

    private final static String TAG = ARRenderer.class.getName();

    protected ShaderProgram shaderProgram;
    private boolean firstRun = true;

    private int width;
    private int height;
    private int[] viewport = new int[4];

    /**
     * Allows subclasses to load markers and prepare the scene. This is called after
     * initialisation is complete.
     */
    abstract public boolean configureARScene();

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Transparent background
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.f);
        if(this.shaderProgram == null) { //ShaderProgram should have been set by the derived class of ARRenderer. If not we create a simple programm to be able to proceed
            Log.e(TAG,"Proceed with simple shader program as we didn't get one from derived class");
            this.shaderProgram = new SimpleShaderProgram(new SimpleVertexShader(), new SimpleFragmentShader());
        }
        GLES20.glUseProgram(shaderProgram.getShaderProgramHandle());
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int w, int h) {
        this.width = w;
        this.height = h;
        if(ARToolKit.getInstance().isRunning()) {
            //Update the frame settings for native rendering
            ARToolKit.getInstance().displayFrameSettings(w, h, false, false, false, NativeInterface.ARW_H_ALIGN_CENTRE, NativeInterface.ARW_V_ALIGN_CENTRE, NativeInterface.ARW_SCALE_MODE_FIT, viewport);
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if (ARToolKit.getInstance().isRunning()) {
            // Initialize ARToolKit6 video background rendering.
            if (firstRun) {
                boolean isDisplayFrameInited = ARToolKit.getInstance().displayFrameInit();
                if (!isDisplayFrameInited) {
                    Log.e(TAG, "Display Frame not inited");
                }

                if (!ARToolKit.getInstance().displayFrameSettings(this.width, this.height, false, false,
                        false, NativeInterface.ARW_H_ALIGN_CENTRE, NativeInterface.ARW_V_ALIGN_CENTRE,
                        NativeInterface.ARW_SCALE_MODE_FIT, viewport)) {
                    Log.e(TAG, "Error during call of displayFrameSettings.");
                } else {
                    Log.i(TAG, "Viewport {" + viewport[0] + ", " + viewport[1] + ", " + viewport[2] + ", " + viewport[3] + "}.");
                }

                firstRun = false;
            }
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            if (!ARToolKit.getInstance().displayFrame()) {
                Log.e(TAG, "Error during call of displayFrame.");
            }
            draw();
        }
    }

    /**
     * Should be overridden in subclasses and used to perform rendering.
     */
    public void draw() {
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        shaderProgram.setProjectionMatrix(ARToolKit.getInstance().getProjectionMatrix());
        float[] camPosition = {1f, 1f, 1f};
        shaderProgram.render(camPosition);
    }
}
