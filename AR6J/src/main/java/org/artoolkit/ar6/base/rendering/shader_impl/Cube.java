/*
 *  Cube.java
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

import org.artoolkit.ar6.base.rendering.ARDrawable;
import org.artoolkit.ar6.base.rendering.ShaderProgram;
import org.artoolkit.ar6.base.rendering.util.RenderUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Simple class to render a coloured cube.
 */
public class Cube implements ARDrawable {

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
    private ShaderProgram mShaderProgram;

    @SuppressWarnings("unused")
    public Cube() {
        this(1.0f);
    }

    @SuppressWarnings("WeakerAccess")
    public Cube(float size) {
        this(size, 0.0f, 0.0f, 0.0f);
    }

    public Cube(float size, float x, float y, float z) {
        setArrays(size, x, y, z);
    }

    @SuppressWarnings("unused")
    public Cube(ShaderProgram shaderProgram) {
        super();
        this.mShaderProgram = shaderProgram;
    }

    @SuppressWarnings("WeakerAccess")
    public FloatBuffer getVertexBuffer() {
        return mVertexBuffer;
    }
    @SuppressWarnings("WeakerAccess")
    public FloatBuffer getColorBuffer() {
        return mColorBuffer;
    }
    @SuppressWarnings("WeakerAccess")
    public ByteBuffer getIndexBuffer() {
        return mIndexBuffer;
    }

    private void setArrays(float size, float x, float y, float z) {

        float hs = size / 2.0f;

        // scale factor
        float sf = 0.4f;

        /*
        In the marker coordinate system z points from the marker up. x goes to the right and y to the top
         */
        float vertices[] = {
                x - hs, y - hs, z - hs, // 0 --> If you look at the cube from the front, this is the corner
                // in the front on the left of the ground plane.
                x + hs, y - hs, z - hs, // 1 --> That is the one to the right of corner 0
                x + hs, y + hs, z - hs, // 2 --> That is the one to the back right of corner 0
                x - hs, y + hs, z - hs, // 3 --> That is the one to the left of corner 2, Or if you imaging (or paint) a 3D cube on paper this is the only corner that is hidden
                (x - hs) * sf, (y - hs) * sf, (z + hs) * sf, // 4 --> That is the top left corner. Directly on top of 0
                (x + hs) * sf, (y - hs) * sf, (z + hs) * sf, // 5 --> That is directly on top of 1
                (x + hs) * sf, (y + hs) * sf, (z + hs) * sf, // 6 --> That is directly on top of 2
                (x - hs) * sf, (y + hs) * sf, (z + hs) * sf, // 7 --> That is directly on top of 3
        };
        float c = 1.0f;

        // the color of the 8 vertices
        float colors[] = {
                0, 0, 0, c, // 0 black
                c, 0, 0, c, // 1 red
                c, c, 0, c, // 2 yellow
                0, c, 0, c, // 3 green
                0, 0, c, c, // 4 blue
                c, 0, c, c, // 5 magenta
                c, c, c, c, // 6 white
                0, c, c, c, // 7 cyan
        };

        // draw vertices order
        byte drawOrderList[] = {
                // bottom
                1, 0, 2,
                2, 0, 3,
                // right
                1, 2, 5,
                5, 2, 6,
                // top
                4, 5, 7,
                7, 5, 6,
                // left
                0, 4, 3,
                3, 4, 7,
                // back
                7, 6, 3, // clockwise order if you see it from the near face
                6, 2, 3,
                // front
                0, 1, 4,
                4, 1, 5
        };

        mVertexBuffer = RenderUtils.buildFloatBuffer(vertices);
        mColorBuffer = RenderUtils.buildFloatBuffer(colors);
        mIndexBuffer = RenderUtils.buildByteBuffer(drawOrderList); // use ByteBuffer instead of ShortBuffer

    }

    @Override
    /**
     * Used to render objects when working with OpenGL ES 2.x
     *
     * @param projectionMatrix The projection matrix obtained from the ARToolkit
     * @param modelViewMatrix  The marker transformation matrix obtained from ARToolkit
     */
    public void draw(float[] projectionMatrix, float[] modelViewMatrix) {

        mShaderProgram.setProjectionMatrix(projectionMatrix);
        mShaderProgram.setModelViewMatrix(modelViewMatrix);

        mShaderProgram.render(this.getVertexBuffer(), this.getColorBuffer(), this.getIndexBuffer());
    }

    @Override
    /**
     * Sets the shader program used by this geometry.
     */
    public void setShaderProgram(ShaderProgram shaderProgram) {
        this.mShaderProgram = shaderProgram;
    }
}
