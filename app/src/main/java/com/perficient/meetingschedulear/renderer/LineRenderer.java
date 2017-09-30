package com.perficient.meetingschedulear.renderer;

import android.opengl.GLES20;
import android.util.Log;

import org.artoolkit.ar6.base.ARToolKit;
import org.artoolkit.ar6.base.rendering.ARRenderer;
import org.artoolkit.ar6.base.rendering.shader_impl.Line;

import java.util.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LineRenderer extends ARRenderer {

    private final static String TAG = "ARRenderer";
    private int mMarkerId1;
    private int mMarkerId2;
    private List<Integer> mMarkerList = new ArrayList<>();
    private ARToolKit mARToolKit;
    private Line mLine;

    @Override
    public boolean configureARScene() {

        mARToolKit = ARToolKit.getInstance();
        mMarkerId2 = mARToolKit.addMarker("single;Data/cat.patt;80");
        if (mMarkerId2 < 0) {
            Log.e(TAG, "Unable to load marker 2");
            return false;
        }
        mMarkerId1 = mARToolKit.addMarker("single;Data/minion.patt;80");
        if (mMarkerId1 < 0) {
            Log.e(TAG, "Unable to load marker 1");
            return false;
        }

        mMarkerList.add(mMarkerId1);
        mMarkerList.add(mMarkerId2);
        mARToolKit.setBorderSize(0.1f);
        Log.i(TAG, "Border size: " + mARToolKit.getBorderSize());

        return true;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        super.onSurfaceCreated(unused, config);
    }

    @Override
    public void draw() {
        super.draw();

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glFrontFace(GLES20.GL_CCW);

        Map<Integer, float[]> transformationMatrixPerVisibleMarker = storeTransformationMatrixPerVisibleMarker();

        float[] positionMarker2 = mARToolKit.retrievePosition(mMarkerId1, mMarkerId2);

        if (transformationMatrixPerVisibleMarker.size() > 1) {

            Log.d(TAG, "draw: per visible ");

            if (positionMarker2 != null) {

                Log.d(TAG, "draw: draw line");

                //Draw mLine from referenceMarker to another marker
                //In relation to the second marker the referenceMarker is on position 0/0/0
                float[] basePosition = {0f, 0f, 0f};
                mLine = new Line(basePosition, positionMarker2, 3);

                float[] projectionMatrix = ARToolKit.getInstance().getProjectionMatrix();
                float[] modelViewMatrix = ARToolKit.getInstance().queryMarkerTransformation(mMarkerId1);

                mLine.draw(projectionMatrix, modelViewMatrix);
            }
        }
    }

    public void draw(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(mARToolKit.getProjectionMatrix(), 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glFrontFace(GL10.GL_CW);

        gl.glMatrixMode(GL10.GL_MODELVIEW);

        Map<Integer, float[]> transformationMatrixPerVisibleMarker = storeTransformationMatrixPerVisibleMarker();

        workWithVisibleMarkers(gl, transformationMatrixPerVisibleMarker);

    }

    private void workWithVisibleMarkers(GL10 gl, Map<Integer, float[]> transformationArray) {
        //if more than one marker visible
        if (transformationArray.size() > 1) {
            Log.i(TAG, "transformationArray size = " + transformationArray.size());
            for (Map.Entry<Integer, float[]> entry : transformationArray.entrySet()) {

                gl.glLoadMatrixf(entry.getValue(), 0);
                gl.glPushMatrix();

                gl.glPopMatrix();
            }

            float distance = mARToolKit.distance(mMarkerId1, mMarkerId2);

            Log.i(TAG, "Distance: " + distance);

            float[] positionMarker2 = mARToolKit.retrievePosition(mMarkerId1, mMarkerId2);

            if (positionMarker2 != null) {
                //Draw mLine from referenceMarker to another marker
                //In relation to the second marker the referenceMarker is on position 0/0/0
                float[] basePosition = {0f, 0f, 0f};
                mLine = new Line(basePosition, positionMarker2, 3);
                mLine.draw(gl);
            }
        }


    }

    private Map<Integer, float[]> storeTransformationMatrixPerVisibleMarker() {
        Map<Integer, float[]> transformationArray = new HashMap<>();

        for (int markerId : mMarkerList) {
            if (mARToolKit.queryMarkerVisible(markerId)) {

                float[] transformation = mARToolKit.queryMarkerTransformation(markerId);

                if (transformation != null) {
                    transformationArray.put(markerId, transformation);
                    Log.d(TAG, "Found Marker " + markerId + " with transformation " + Arrays.toString(transformation));
                }
            } else {
                transformationArray.remove(markerId);
            }

        }
        return transformationArray;
    }
}