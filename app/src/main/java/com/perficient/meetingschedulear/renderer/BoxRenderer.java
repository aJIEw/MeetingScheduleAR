package com.perficient.meetingschedulear.renderer;


import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import cn.easyar.Matrix44F;
import cn.easyar.Vec2F;

/**
 * VBO: Vertex Buffer Object
 * FBO: Fragment Buffer Object
 */
public class BoxRenderer {

    private int mProgramHandle;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVMatrixHandle;
    private int mMVPMatrixHandle;

    private int mCoordVBO;
    private int mColorVBO;
    private int mFacesVBO;

    private boolean isRendered;

    private String box_vert =
            "uniform mat4 u_MVMatrix;\n"               // model/view matrix
                    + "uniform mat4 u_MVPMatrix;\n"     // model/view/projection matrix
                    + "attribute vec4 a_Position;\n"
                    + "attribute vec4 a_Color;\n"
                    + "varying vec4 v_Color;\n"
                    + "\n"
                    + "void main()\n"
                    + "{\n"
                    + "    v_Color = a_Color;\n"
                    + "    gl_Position = u_MVPMatrix * u_MVMatrix * a_Position;\n"
                    + "}\n"
                    + "\n";

    private String box_frag =
            "#ifdef GL_ES\n"
                    + "precision highp float;\n"
                    + "#endif\n"
                    + "varying vec4 v_Color;\n"
                    + "\n"
                    + "void main()\n"
                    + "{\n"
                    + "    gl_FragColor = v_Color;\n"
                    + "}\n"
                    + "\n";

    /**
     * Initiate vertex shader, fragment shader and link program
     */
    public BoxRenderer() {
        mProgramHandle = GLES20.glCreateProgram();

        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, box_vert);
        GLES20.glCompileShader(vertShader);
        GLES20.glAttachShader(mProgramHandle, vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, box_frag);
        GLES20.glCompileShader(fragShader);
        GLES20.glAttachShader(mProgramHandle, fragShader);

        GLES20.glLinkProgram(mProgramHandle);
        GLES20.glUseProgram(mProgramHandle);

        // init all the attributes' locations
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");

        // create buffer for coordinates
        mCoordVBO = generateOneBuffer();

        // create buffer for cube vertices' color
        mColorVBO = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mColorVBO);
        int cube_vertex_colors[][] = {
                //{255, 0, 0, 255},       // red
                //{255, 255, 0, 255},     // yellow
                //{0, 255, 0, 255},       // green
                //{255, 0, 255, 255},     // magenta
                {0, 255, 0, 255},
                {0, 255, 0, 255},
                {0, 255, 0, 255},
                {0, 255, 0, 255},
                {0, 0, 255, 255},       // blue
                {255, 255, 255, 255},   // white
                {0, 255, 255, 255},     // cyan
                {0, 0, 0, 255}};        // black
        ByteBuffer cube_vertex_colors_buffer =
                ByteBuffer.wrap(byteArrayFromIntArray(flatten(cube_vertex_colors)));
        // set buffer data for colors
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                cube_vertex_colors_buffer.limit(),
                cube_vertex_colors_buffer,
                GLES20.GL_STATIC_DRAW);

        // create buffer for drawing cube faces
        mFacesVBO = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mFacesVBO);
        short cube_faces[][] = {
                /* +z */{3, 2, 1, 0},
                /* -y */{2, 3, 7, 6},
                /* +y */{0, 1, 5, 4},
                /* -x */{3, 0, 4, 7},
                /* +x */{1, 2, 6, 5},
                /* -z */{4, 5, 6, 7}};
        ShortBuffer cube_faces_buffer =
                ShortBuffer.wrap(flatten(cube_faces));
        // allocate memory in this buffer for drawing faces
        GLES20.glBufferData(
                GLES20.GL_ELEMENT_ARRAY_BUFFER,
                cube_faces_buffer.limit() * 2,
                cube_faces_buffer,
                GLES20.GL_STATIC_DRAW);
    }

    /**
     * Render graphics
     */
    public void render(Matrix44F projectionMatrix, Matrix44F cameraView, Vec2F size) {
        float imageWidth = size.data[0]; // image target width
        float imageHeight = size.data[1]; // image target height

        // load shader program
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(mProgramHandle);

        // bind buffer data for cube vertices
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCoordVBO);
        float cube_vertices[][] = {
                // +z
                {imageWidth / 2, imageHeight / 2, imageWidth / 8},
                {imageWidth / 2, -imageHeight / 2, imageWidth / 8},
                {-imageWidth / 2, -imageHeight / 2, imageWidth / 8},
                {-imageWidth / 2, imageHeight / 2, imageWidth / 8},
                // -z
                {imageWidth / 2, imageHeight / 2, -imageWidth / 8},
                {imageWidth / 2, -imageHeight / 2, -imageWidth / 8},
                {-imageWidth / 2, -imageHeight / 2, -imageWidth / 8},
                {-imageWidth / 2, imageHeight / 2, -imageWidth / 8}};
        FloatBuffer cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices));
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                cube_vertices_buffer.limit() * 4,
                cube_vertices_buffer,
                GLES20.GL_DYNAMIC_DRAW);

        // determine final coordinates position
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

        // determine final colors
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mColorVBO);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, 0);

        // view transformation
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, cameraView.data, 0);
        // projection
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, projectionMatrix.data, 0);

        /*
        * Here we draw the faces with GL_TRIANGLE_FAN, it means we draw each triangle based on
        * the first point, for example, as for {3, 2, 1, 0}, we draw the triangle starts with 3,
        * first {3, 2, 1} and then {3, 1, 0}, so that compose one face.
        * */
        for (int i = 0; i < 6; i++) {
            //GLES20.glDrawArrays();
            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLE_FAN,     // mode
                    4,                          // count
                    GLES20.GL_UNSIGNED_SHORT,   // type
                    i * 4 * 2                   // offset for VBO indices
            );
        }

        isRendered = true;
    }

    public boolean boxRendered() {
        return isRendered;
    }

    private float[] flatten(float[][] a) {
        int size = 0;
        for (float[] anA : a) {
            size += anA.length;
        }
        float[] l = new float[size];
        int offset = 0;
        for (float[] anA : a) {
            System.arraycopy(anA, 0, l, offset, anA.length);
            offset += anA.length;
        }
        return l;
    }

    private int[] flatten(int[][] a) {
        int size = 0;
        for (int[] anA : a) {
            size += anA.length;
        }
        int[] l = new int[size];
        int offset = 0;
        for (int[] anA : a) {
            System.arraycopy(anA, 0, l, offset, anA.length);
            offset += anA.length;
        }
        return l;
    }

    private short[] flatten(short[][] a) {
        int size = 0;
        for (short[] anA : a) {
            size += anA.length;
        }
        short[] l = new short[size];
        int offset = 0;
        for (short[] anA : a) {
            System.arraycopy(anA, 0, l, offset, anA.length);
            offset += anA.length;
        }
        return l;
    }

    private byte[] flatten(byte[][] a) {
        int size = 0;
        for (byte[] anA : a) {
            size += anA.length;
        }
        byte[] l = new byte[size];
        int offset = 0;
        for (byte[] anA : a) {
            System.arraycopy(anA, 0, l, offset, anA.length);
            offset += anA.length;
        }
        return l;
    }

    private byte[] byteArrayFromIntArray(int[] a) {
        byte[] l = new byte[a.length];
        for (int k = 0; k < a.length; k += 1) {
            l[k] = (byte) (a[k] & 0xFF);
        }
        return l;
    }

    private int generateOneBuffer() {
        int[] buffer = {0};
        GLES20.glGenBuffers(1, buffer, 0);
        return buffer[0];
    }
}
