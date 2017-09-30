/*
 *  Line.java
 *  ARToolKit6
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
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

package org.artoolkit.ar6.base.rendering.shader_impl;

import android.opengl.GLES10;

import org.artoolkit.ar6.base.rendering.ARDrawable;
import org.artoolkit.ar6.base.rendering.util.RenderUtils;
import org.artoolkit.ar6.base.rendering.ShaderProgram;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Line implements ARDrawable {

    private int vertexLength = 3; //We only work with position vectors with three elements
    private float[] start = new float[3];
    private float[] end = new float[3];
    private float width;
    private float[] color = {1, 0, 0, 1}; // red
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;

    private ShaderProgram shaderProgram;

    /**
     * @param width Width of the line
     */
    @SuppressWarnings("WeakerAccess")
    public Line(float width) {
        shaderProgram = null;
        this.setWidth(width);
    }

    @SuppressWarnings("unused")
    public Line(float width, ShaderProgram shaderProgram) {
        this(width);
        this.shaderProgram = shaderProgram;
    }

    /**
     * @param start Vector were the line starts
     * @param end   Vector were the line ends
     * @param width Width of the vector
     */
    public Line(float[] start, float[] end, float width) {
        setStart(start);
        setEnd(end);
        this.width = width;
        setArrays();
    }

    private void setArrays() {

        float[] vertices = new float[vertexLength * 2];

        for (int i = 0; i < vertexLength; i++) {
            vertices[i] = start[i];
            vertices[i + vertexLength] = end[i];
        }

        mVertexBuffer = RenderUtils.buildFloatBuffer(vertices);
        mColorBuffer = RenderUtils.buildFloatBuffer(color);
    }

    @SuppressWarnings("unused")
    public void draw(GL10 gl) {
        gl.glVertexPointer(vertexLength, GLES10.GL_FLOAT, 0, mVertexBuffer);

        gl.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
        gl.glColor4f(1, 0, 0, 1); // Red
        gl.glLineWidth(this.width);
        gl.glDrawArrays(GLES10.GL_LINES, 0, 2);
        gl.glDisableClientState(GLES10.GL_VERTEX_ARRAY);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    @SuppressWarnings("WeakerAccess")
    public FloatBuffer getMVertexBuffer() {
        return this.mVertexBuffer;
    }

    public float[] getStart() {
        return start;
    }

    public void setStart(float[] start) {
        if (start.length > vertexLength) {
            this.start[0] = start[0];
            this.start[1] = start[1];
            this.start[2] = start[2];
        } else {
            this.start = start;
        }
    }

    public float[] getEnd() {
        return end;
    }

    public void setEnd(float[] end) {
        if (end.length > vertexLength) {
            this.end[0] = end[0];
            this.end[1] = end[1];
            this.end[2] = end[2];
        } else {
            this.end = end;
        }
    }


    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    @SuppressWarnings("WeakerAccess")
    public FloatBuffer getColorBuffer() {
        return mColorBuffer;
    }

    @Override
    /**
     * Used to render objects when working with OpenGL ES 2.x
     *
     * @param projectionMatrix The projection matrix obtained from the ARToolkit
     * @param modelViewMatrix  The marker transformation matrix obtained from ARToolkit
     */
    public void draw(float[] projectionMatrix, float[] modelViewMatrix) {

        shaderProgram.setProjectionMatrix(projectionMatrix);
        shaderProgram.setModelViewMatrix(modelViewMatrix);

        this.setArrays();
        shaderProgram.render(this.getMVertexBuffer(), this.getColorBuffer(), null);

    }

    @Override
    /**
     * Sets the shader program used by this geometry.
     */
    public void setShaderProgram(ShaderProgram program) {
        this.shaderProgram = program;
    }
}
