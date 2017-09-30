package com.perficient.meetingschedulear.renderer;

import android.opengl.GLES20;

import org.artoolkit.ar6.base.ARToolKit;
import org.artoolkit.ar6.base.NativeInterface;
import org.artoolkit.ar6.base.rendering.ARRenderer;
import org.artoolkit.ar6.base.rendering.shader_impl.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A very simple Renderer that adds a marker and draws a cube on it.
 */
public class CubeRenderer extends ARRenderer {

    private static final class Trackable {
        String name;
        float height;

        Trackable(String name, float height) {
            this.name = name;
            this.height = height;
        }
    }

    private static final Trackable trackables[] = new Trackable[]{
            new Trackable("Alterra_Ticket_1.jpg", 95.3f),
            new Trackable("Alterra_Postcard_2.jpg", 95.3f),
            new Trackable("Alterra_Postcard_3.jpg", 127.0f),
            new Trackable("Alterra_Postcard_4.jpg", 95.3f)
    };
    private int trackableUIDs[] = new int[trackables.length];

    private Cube mCube;

    private Line mLine;

    /**
     * Markers can be configured here.
     */
    @Override
    public boolean configureARScene() {
        int i = 0;
        for (Trackable trackable : trackables) {
            // Configure image as a marker: 2d;target_image_pathname;image_height
            trackableUIDs[i] = ARToolKit.getInstance().addMarker("2d;Data/2d/" + trackable.name + ";" + trackable.height);
            if (trackableUIDs[i] < 0) return false;
            i++;
        }
        NativeInterface.arwSetTrackerOptionInt(NativeInterface.ARW_TRACKER_OPTION_2D_MAX_IMAGES, trackables.length);
        return true;
    }

    /**
     * Shader calls should be within a GL thread.
     * GL threads are onSurfaceChanged(), onSurfaceCreated() or onDrawFrame()
     * <p>
     * As the mCube instantiates the shader during setShaderProgram call
     * we need to create the mCube here.
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        this.shaderProgram = new SimpleShaderProgram(new SimpleVertexShader(), new SimpleFragmentShader());
        mCube = new Cube(40.0f, 0.0f, 0.0f, 0.0f);
        mCube.setShaderProgram(shaderProgram);

        //mLine = new Line(3.0f);
        //float[] start = new float[]{30, 0, 0};
        //float[] end = new float[]{40, 80, 80};
        //mLine.setStart(start);
        //mLine.setEnd(end);
        //mLine.setShaderProgram(shaderProgram);
        super.onSurfaceCreated(unused, config);
    }

    /**
     * Override the draw function from ARRenderer.
     */
    @Override
    public void draw() {
        super.draw();

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glFrontFace(GLES20.GL_CCW);

        // Look for trackables, and draw on each found one.
        for (int trackableUID : trackableUIDs) {
            // If the marker is visible, apply its transformation, and render a mCube
            if (ARToolKit.getInstance().queryMarkerVisible(trackableUID)) {
                float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();
                float[] modelViewMatrix = ARToolKit.getInstance().queryMarkerTransformation(trackableUID);
                mCube.draw(projectionMatrix, modelViewMatrix);

                //mLine.draw(projectionMatrix, modelViewMatrix);
            }
        }
    }
}