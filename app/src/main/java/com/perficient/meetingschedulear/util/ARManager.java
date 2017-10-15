package com.perficient.meetingschedulear.util;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.renderer.BlackboardRenderer;

import java.util.*;

import cn.easyar.*;

/**
 * A manager for Renderer and AR Camera
 */
public class ARManager {

    private static final String TAG = ARManager.class.getSimpleName();

    private static final String TARGET_RENDERED = "rendered";

    private CameraDevice mCamera;
    private CameraFrameStreamer mStreamer;
    private ArrayList<ImageTracker> mImageTrackers;
    private Renderer mRenderer;
    private BlackboardRenderer mBlackboardRenderer;

    private boolean mViewportChanged = false;
    private Vec2I mViewSize = new Vec2I(0, 0);
    private int mRotation = 0;
    private Vec4I mViewport = new Vec4I(0, 0, 1280, 720);
    private ImageTarget mPreviousTarget;

    private Context mContext;

    public ARManager(Context context) {
        mContext = context;
        mImageTrackers = new ArrayList<>();
    }

    /**
     * initialize CameraDevice, CameraFrameStreamer and ImageTracker
     */
    public boolean initialize() {

        Log.d(TAG, "initialize: ");

        mCamera = new CameraDevice();
        mStreamer = new CameraFrameStreamer();
        mStreamer.attachCamera(mCamera); // Connect CameraDevice to this mStreamer

        boolean status = mCamera.open(CameraDeviceType.Default); // Open mCamera
        mCamera.setSize(new Vec2I(1280, 720));

        if (!status) {
            return false;
        }
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(mStreamer); // connect mStreamer to the ImageTracker

        // load into tracker
        // or we should download images from the server and load them into tracker
        loadAllFromJsonFile(tracker, "Data/targets.json");

        mImageTrackers.add(tracker);

        return true;
    }

    /**
     * Start track target
     */
    public boolean start() {

        Log.d(TAG, "start: ");

        boolean status = (mCamera != null) && mCamera.start();
        status &= (mStreamer != null) && mStreamer.start();

        mCamera.setFocusMode(CameraDeviceFocusMode.Continousauto);// continuously auto focus

        for (ImageTracker tracker : mImageTrackers) {
            status &= tracker.start();// for each ImageTracker, start track ImageTarget
        }

        return status;
    }

    /**
     * Initiate mRenderer
     */
    public void initGL() {

        Log.d(TAG, "initGL: ");

        if (mRenderer != null) {
            mRenderer.dispose();
        }
        mRenderer = new Renderer();
        mBlackboardRenderer = new BlackboardRenderer(mContext);
        mPreviousTarget = null;
    }

    /**
     * Resize GLSurfaceView
     */
    public void resizeGL(int width, int height) {

        Log.d(TAG, "resizeGL: ");

        mViewSize = new Vec2I(width, height);
        mViewportChanged = true;
    }

    /**
     * Here we render the graphics through Renderer
     */
    public void render() {
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mRenderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, mViewSize.data[0], mViewSize.data[1]);
            GLES20.glViewport(
                    default_viewport.data[0],
                    default_viewport.data[1],
                    default_viewport.data[2],
                    default_viewport.data[3]);

            if (mRenderer.renderErrorMessage(default_viewport)) {
                return;
            }
        }

        if (mStreamer == null) {
            return;
        }

        // get the newest frame from mStreamer
        Frame frame = mStreamer.peek();
        try {
            // update mViewport
            updateViewport();

            // set mViewport
            GLES20.glViewport(mViewport.data[0], mViewport.data[1], mViewport.data[2], mViewport.data[3]);

            // render the frame into fragment buffer object
            // will be called in every frame
            if (mRenderer != null) {
                mRenderer.render(frame, mViewport);
            }

            for (TargetInstance targetInstance : frame.targetInstances()) {
                int status = targetInstance.status();
                if (status == TargetStatus.Tracked) {
                    Target target = targetInstance.target();
                    ImageTarget imageTarget = target instanceof ImageTarget ?
                            (ImageTarget) (target)
                            :
                            null;
                    if (imageTarget == null) {
                        continue;
                    }
                    if (mBlackboardRenderer != null) {

                        TextTextureContainer resContainer = fetchResources(imageTarget.name());
                        // load target texture once only to reduce memory usage
                        if (mPreviousTarget == null ||
                                !imageTarget.name().equals(mPreviousTarget.name())) {
                            mBlackboardRenderer.loadTexture(resContainer.getText(), resContainer.getTexture());
                        }

                        /*
                        * Render the box, here we pass the Camera Coordinates
                        * along with the Projection Matrix
                        * and the 2 x 1 float vector
                        * */
                        mBlackboardRenderer.render(
                                // get Projection Matrix, pass near plane and far plane
                                mCamera.projectionGL(0.2f, 500.f),
                                // get OpenGL coordinate matrix
                                targetInstance.poseGL(),
                                // target size, width and height in 2x1 float vector
                                imageTarget.size());

                        mPreviousTarget = imageTarget;
                        break;
                    }
                }
            }
        } finally {
            frame.dispose();
        }
    }

    // TODO: 2017/10/15 replace this with API call
    private TextTextureContainer fetchResources(String targetName) {
        TextTextureContainer container = new TextTextureContainer();
        switch (targetName) {
            case "sp0":
                container.setText(mContext.getString(R.string.tools_meeting_info_text1));
                container.setTexture(R.drawable.texture_chalkboard);
                break;
            case "sp1":
                container.setText(mContext.getString(R.string.tools_meeting_info_text2));
                container.setTexture(R.drawable.texture_chalkboard);
                break;
            case "whale":
                container.setText(mContext.getString(R.string.tools_meeting_info_text3));
                container.setTexture(R.drawable.texture_blackboard);
                break;
            case "desktop":
                container.setText(mContext.getString(R.string.tools_meeting_info_text4));
                container.setTexture(R.drawable.texture_blackboard);
                break;
            default:
                container.setText("");
                container.setTexture(R.drawable.texture_blackboard);
                break;
        }
        return container;
    }

    /**
     * Stop tracking
     */
    public boolean stop() {

        Log.d(TAG, "stop: ");

        boolean status = true;
        for (ImageTracker tracker : mImageTrackers) {
            status &= tracker.stop();
        }
        status &= (mStreamer != null) && mStreamer.stop();
        status &= (mCamera != null) && mCamera.stop();
        return status;
    }

    /**
     * Dispose all resources
     */
    public void dispose() {

        Log.d(TAG, "dispose: ");

        for (ImageTracker tracker : mImageTrackers) {
            tracker.dispose();
        }
        mImageTrackers.clear();
        mBlackboardRenderer = null;
        if (mRenderer != null) {
            mRenderer.dispose();
            mRenderer = null;
        }
        if (mStreamer != null) {
            mStreamer.dispose();
            mStreamer = null;
        }
        if (mCamera != null) {
            mCamera.dispose();
            mCamera = null;
        }
    }

    /**
     * Update Viewport
     */
    private void updateViewport() {
        CameraCalibration calib = mCamera != null ? mCamera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.mRotation) {
            this.mRotation = rotation;
            mViewportChanged = true;
        }

        if (mViewportChanged) {
            Vec2I size = new Vec2I(1, 1);
            if ((mCamera != null) && mCamera.isOpened()) {
                size = mCamera.size();
            }
            // set mCamera size if the mCamera is vertical
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);
            }

            // determine scale ratio according the large one
            float scaleRatio = Math.max(
                    (float) mViewSize.data[0] / (float) size.data[0],
                    (float) mViewSize.data[1] / (float) size.data[1]);

            // calculate mViewport size
            Vec2I viewport_size = new Vec2I(
                    Math.round(size.data[0] * scaleRatio),
                    Math.round(size.data[1] * scaleRatio));

            // set mViewport
            mViewport = new Vec4I(
                    (mViewSize.data[0] - viewport_size.data[0]) / 2,
                    (mViewSize.data[1] - viewport_size.data[1]) / 2,
                    viewport_size.data[0],
                    viewport_size.data[1]);

            if ((mCamera != null) && mCamera.isOpened())
                mViewportChanged = false;
        }
    }

    /**
     * Use asynchronous method {@link ImageTracker#loadTarget(Target, FunctorOfVoidFromPointerOfTargetAndBool)}
     * to load ImageTracker from specified path
     */
    private void loadFromImage(ImageTracker tracker, String path) {
        ImageTarget target = new ImageTarget();
        String jstr = "{\n"
                + "  \"images\" :\n"
                + "  [\n"
                + "    {\n"
                + "      \"image\" : \"" + path + "\",\n"
                + "      \"name\" : \"" + path.substring(0, path.indexOf(".")) + "\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        target.setup(jstr, StorageType.Assets | StorageType.Json, "");// set up first
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadFromJsonFile(ImageTracker tracker, String path, String targetname) {
        ImageTarget target = new ImageTarget();
        target.setup(path, StorageType.Assets, targetname);
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadAllFromJsonFile(ImageTracker tracker, String path) {
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
                @Override
                public void invoke(Target target, boolean status) {
                    Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                }
            });
        }
    }

    class TextTextureContainer{
        String text;

        @DrawableRes
        int texture;

        public TextTextureContainer() {
        }

        public TextTextureContainer(String text, int texture) {
            this.text = text;
            this.texture = texture;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getTexture() {
            return texture;
        }

        public void setTexture(int texture) {
            this.texture = texture;
        }
    }
}
