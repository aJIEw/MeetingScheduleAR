package com.perficient.meetingschedulear.renderer;


import android.content.Context;
import android.opengl.GLES20;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import cn.easyar.Matrix44F;
import cn.easyar.Vec2F;

/**
 * VBO: Vertex Buffer Object
 * FBO: Fragment Buffer Object
 */
public class BlackboardRenderer {

    /**
     * This is a handle to our blackboard shading program.
     */
    private int mProgramHandle;

    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;
    /**
     * This will be used to pass in model color information.
     */
    private int mColorHandle;
    /**
     * This will be used to pass in the modelview matrix.
     */
    private int mMVMatrixHandle;
    /**
     * This will be used to pass in the transformation matrix.
     */
    private int mMVPMatrixHandle;
    /**
     * This will be used to pass in the texture.
     */
    private int mTextureUniformHandle;
    /**
     * This will be used to pass in model texture coordinate information.
     */
    private int mTextureCoordinateHandle;
    /**
     * This is a handle to our texture data.
     */
    private int mTextureDataHandle;

    /**
     * VBO for cube coordinate
     */
    private int mCoordVBO;
    /**
     * VBO for cube color
     */
    private int mColorVBO;
    /**
     * VBO for cube faces
     */
    private int mFacesVBO;

    private final FloatBuffer mCubeTextureCoordinates;

    private final int mTextureCoordinateDataSize = 2;

    /**
     * Notice that the y axis of the texture coordinate is on the opposite direction
     * compared to the android coordinate system, so we have to adjust it to
     * make the texture looks normal on the object.
     */
    final float[] mCubeTextureCoordinateData =
            {
                    1.0f, -1.0f,
                    1.0f, 1.0f,
                    -1.0f, 1.0f,
                    -1.0f, -1.0f
            };

    private boolean mHasRendered;

    private Context mContext;

    private static final String VERTEX_SHADER =
            "uniform mat4 u_MVMatrix;\n"                // model/view matrix
                    + "uniform mat4 u_MVPMatrix;\n"     // model/view/projection matrix
                    + "attribute vec4 a_Position;\n"
                    + "attribute vec4 a_Color;\n"
                    + "attribute vec2 a_TexCoordinate;\n"
                    + "varying vec4 v_Color;\n"
                    + "varying vec2 v_TexCoordinate;\n"
                    + "\n"
                    + "void main()\n"
                    + "{\n"
                    + "    v_Color = a_Color;\n"
                    + "    v_TexCoordinate = a_TexCoordinate;\n"
                    + "    gl_Position = u_MVPMatrix * u_MVMatrix * a_Position;\n"
                    + "}\n"
                    + "\n";

    private static String FRAGMENT_SHADER =
            "#ifdef GL_ES\n"
                    + "precision highp float;\n"
                    + "#endif\n"
                    + "uniform sampler2D u_Texture;\n"
                    + "varying vec4 v_Color;\n"
                    + "varying vec2 v_TexCoordinate;\n"
                    + "\n"
                    + "void main()\n"
                    + "{\n"
                    + "    gl_FragColor = v_Color * texture2D(u_Texture, v_TexCoordinate);\n"
                    + "}\n"
                    + "\n";

    /**
     * Initiate vertex shader, fragment shader and link program
     */
    public BlackboardRenderer(Context context) {
        mContext = context;

        // init buffer for cube texture coordinate data
        ByteBuffer ctb = ByteBuffer.allocateDirect(
                mCubeTextureCoordinateData.length * 4);
        ctb.order(ByteOrder.nativeOrder());
        mCubeTextureCoordinates = ctb.asFloatBuffer();
        mCubeTextureCoordinates.put(mCubeTextureCoordinateData);
        mCubeTextureCoordinates.position(0);

        mProgramHandle = GLES20.glCreateProgram();

        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, VERTEX_SHADER);
        GLES20.glCompileShader(vertShader);
        GLES20.glAttachShader(mProgramHandle, vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, FRAGMENT_SHADER);
        GLES20.glCompileShader(fragShader);
        GLES20.glAttachShader(mProgramHandle, fragShader);

        GLES20.glLinkProgram(mProgramHandle);
        GLES20.glUseProgram(mProgramHandle);

        // init all the attributes' locations
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");

        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
        mTextureDataHandle = TextureHelper.loadTexture(mContext, R.drawable.texture_blackboard);

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
                //{0, 0, 255, 255},       // blue
                //{255, 255, 255, 255},   // white
                //{0, 255, 255, 255},     // cyan
                //{0, 0, 0, 255},         // black
                {255, 255, 255, 249},
                {255, 255, 255, 249},
                {255, 255, 255, 249},
                {255, 255, 255, 249},
                {0, 0, 0, 255},
                {0, 0, 0, 255},
                {0, 0, 0, 255},
                {0, 0, 0, 255}};
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
                {imageWidth / 2, imageHeight / 2, imageWidth / 6},
                {imageWidth / 2, -imageHeight / 2, imageWidth / 6},
                {-imageWidth / 2, -imageHeight / 2, imageWidth / 6},
                {-imageWidth / 2, imageHeight / 2, imageWidth / 6},
                // -z
                {imageWidth / 2, imageHeight / 2, -imageWidth / 6},
                {imageWidth / 2, -imageHeight / 2, -imageWidth / 6},
                {-imageWidth / 2, -imageHeight / 2, -imageWidth / 6},
                {-imageWidth / 2, imageHeight / 2, -imageWidth / 6}};
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

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

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

        mHasRendered = true;
    }

    private void drawTexture() {
        mCubeTextureCoordinates.position(0);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize,
                GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);
    }

    public boolean boxRendered() {
        return mHasRendered;
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
