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
 * */
public class BoxRenderer {

    private int program_box;

    private int pos_coord_box;
    private int pos_color_box;
    private int pos_trans_box;
    private int pos_proj_box;

    private int vbo_coord_box;
    private int vbo_color_box;
    private int vbo_faces_box;

    private boolean isRendered;

    private String box_vert = "uniform mat4 trans;\n"
            + "uniform mat4 proj;\n"
            + "attribute vec4 coord;\n"
            + "attribute vec4 color;\n"
            + "varying vec4 vcolor;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    vcolor = color;\n"
            + "    gl_Position = proj*trans*coord;\n"
            + "}\n"
            + "\n";

    private String box_frag = "#ifdef GL_ES\n"
            + "precision highp float;\n"
            + "#endif\n"
            + "varying vec4 vcolor;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    gl_FragColor = vcolor;\n"
            + "}\n"
            + "\n";

    /**
     * Initiate vertex shader, fragment shader and link program
     */
    public void init() {
        program_box = GLES20.glCreateProgram();

        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, box_vert);
        GLES20.glCompileShader(vertShader);
        GLES20.glAttachShader(program_box, vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, box_frag);
        GLES20.glCompileShader(fragShader);
        GLES20.glAttachShader(program_box, fragShader);

        GLES20.glLinkProgram(program_box);
        GLES20.glUseProgram(program_box);

        // init all the attributes' locations
        pos_coord_box = GLES20.glGetAttribLocation(program_box, "coord");
        pos_color_box = GLES20.glGetAttribLocation(program_box, "color");
        pos_trans_box = GLES20.glGetUniformLocation(program_box, "trans");
        pos_proj_box = GLES20.glGetUniformLocation(program_box, "proj");

        // create buffer for coordinates
        vbo_coord_box = generateOneBuffer();

        // create buffer for cube vertices' color
        vbo_color_box = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box);
        int cube_vertex_colors[][] = {
                {255, 0, 0, 255},       // red
                {255, 255, 0, 255},     // yellow
                {0, 255, 0, 255},       // green
                {255, 0, 255, 255},     // magenta
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
        vbo_faces_box = generateOneBuffer();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box);
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
        GLES20.glUseProgram(program_box);

        // bind buffer data for cube vertices
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box);
        float cube_vertices[][] = {
                // +z
                {imageWidth / 4, imageHeight / 4, imageWidth / 4},
                {imageWidth / 4, -imageHeight / 4, imageWidth / 4},
                {-imageWidth / 4, -imageHeight / 4, imageWidth / 4},
                {-imageWidth / 4, imageHeight / 4, imageWidth / 4},
                // -z
                {imageWidth / 4, imageHeight / 4, -imageWidth / 4},
                {imageWidth / 4, -imageHeight / 4, -imageWidth / 4},
                {-imageWidth / 4, -imageHeight / 4, -imageWidth / 4},
                {-imageWidth / 4, imageHeight / 4, -imageWidth / 4}};
        FloatBuffer cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices));
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                cube_vertices_buffer.limit() * 4,
                cube_vertices_buffer,
                GLES20.GL_DYNAMIC_DRAW);

        // determine final coordinates position
        GLES20.glEnableVertexAttribArray(pos_coord_box);
        GLES20.glVertexAttribPointer(pos_coord_box, 3, GLES20.GL_FLOAT, false, 0, 0);

        // determine final colors
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box);
        GLES20.glEnableVertexAttribArray(pos_color_box);
        GLES20.glVertexAttribPointer(pos_color_box, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, 0);

        // view transformation
        GLES20.glUniformMatrix4fv(pos_trans_box, 1, false, cameraView.data, 0);
        // projection
        GLES20.glUniformMatrix4fv(pos_proj_box, 1, false, projectionMatrix.data, 0);

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
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        float[] l = new float[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private int[] flatten(int[][] a) {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        int[] l = new int[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private short[] flatten(short[][] a) {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        short[] l = new short[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
        }
        return l;
    }

    private byte[] flatten(byte[][] a) {
        int size = 0;
        for (int k = 0; k < a.length; k += 1) {
            size += a[k].length;
        }
        byte[] l = new byte[size];
        int offset = 0;
        for (int k = 0; k < a.length; k += 1) {
            System.arraycopy(a[k], 0, l, offset, a[k].length);
            offset += a[k].length;
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
