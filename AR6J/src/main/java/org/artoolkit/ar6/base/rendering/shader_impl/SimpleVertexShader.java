/*
 *  ARActivity.java
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

import android.opengl.GLES20;

import org.artoolkit.ar6.base.rendering.OpenGLShader;

/**
 * Created by Thorsten Bux on 21.01.2016.
 * Here you define your vertex shader and what it does with the geometry position.
 * This vertex shader class calculates the MVP matrix and applies it to the passed
 * in geometry position vectors.
 * <p/>
 * This class also provides the implementation of the {@link #configureShader()} method. For your own
 * shader you can derive from this class and simply call setShaderSource and configureShader from it.
 */
public class SimpleVertexShader implements OpenGLShader {

    static String colorVectorString = "a_Color";

    private String vertexShader =
            "uniform mat4 u_MVPMatrix;        \n"     // A constant representing the combined model/view/projection matrix.

                    + "uniform mat4 " + OpenGLShader.projectionMatrixString + "; \n"        // projection matrix
                    + "uniform mat4 " + OpenGLShader.modelViewMatrixString + "; \n"        // modelView matrix

                    + "attribute vec4 " + OpenGLShader.positionVectorString + "; \n"     // Per-vertex position information we will pass in.
                    + "attribute vec4 " + colorVectorString + "; \n"     // Per-vertex color information we will pass in.

                    + "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.

                    + "void main()                    \n"     // The entry point for our vertex shader.
                    + "{                              \n"
                    + "   v_Color = " + colorVectorString + "; \n"     // Pass the color through to the fragment shader.
                    // It will be interpolated across the triangle.
                    + "   vec4 p = " + OpenGLShader.modelViewMatrixString + " * " + OpenGLShader.positionVectorString + "; \n "     // transform vertex position with modelview matrix
                    + "   gl_Position = " + OpenGLShader.projectionMatrixString + " \n"     // gl_Position is a special variable used to store the final position.
                    + "                     * p;              \n"     // Multiply the vertex by the matrix to get the final point in
                    + "}                              \n";    // normalized screen coordinates.

    @Override
    public int configureShader() {
        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        String vertexShaderErrorLog = "";

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                vertexShaderErrorLog = GLES20.glGetShaderInfoLog(vertexShaderHandle);
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.\n" + vertexShaderErrorLog);
        }

        return vertexShaderHandle;
    }

    @Override
    public void setShaderSource(String source) {
        this.vertexShader = source;
    }
}
