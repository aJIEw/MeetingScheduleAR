package com.perficient.meetingschedulear.util;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.renderer.BoxRenderer;

import java.util.ArrayList;

import cn.easyar.*;

/**
 * A manager for Renderer and AR Camera
 */
public class ARManager {

    private static final String TAG = ARManager.class.getSimpleName();

    private CameraDevice camera;
    private CameraFrameStreamer streamer;
    private ArrayList<ImageTracker> trackers;
    private Renderer renderer;
    private BoxRenderer box_renderer;
    private boolean viewport_changed = false;
    private Vec2I view_size = new Vec2I(0, 0);
    private int rotation = 0;
    private Vec4I viewport = new Vec4I(0, 0, 1280, 720);

    private ViewRefresher mViewRefresher;
    private Context mContext;

    public interface ViewRefresher{
        void refresh(String text);
    }

    public ARManager(Context context) {
        mContext = context;
        trackers = new ArrayList<>();
    }

    /**
     * initialize CameraDevice, CameraFrameStreamer and ImageTracker
     */
    public boolean initialize(ViewRefresher viewRefresher) {

        Log.d(TAG, "initialize: ");

        mViewRefresher = viewRefresher;
        camera = new CameraDevice();
        streamer = new CameraFrameStreamer();
        streamer.attachCamera(camera); // Connect CameraDevice to this streamer

        boolean status = camera.open(CameraDeviceType.Default); // Open camera
        camera.setSize(new Vec2I(1280, 720));

        if (!status) {
            return false;
        }
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer); // connect streamer to the ImageTracker

        // load trackers
        loadAllFromJsonFile(tracker, "Data/targets.json");

        trackers.add(tracker);

        return true;
    }

    /**
     * Start track target
     */
    public boolean start() {

        Log.d(TAG, "start: ");

        boolean status = (camera != null) && camera.start();
        status &= (streamer != null) && streamer.start();

        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);// continuously auto focus

        for (ImageTracker tracker : trackers) {
            status &= tracker.start();// for each ImageTracker, start track ImageTarget
        }

        return status;
    }

    /**
     * Initiate renderer
     */
    public void initGL() {

        Log.d(TAG, "initGL: ");

        if (renderer != null) {
            renderer.dispose();
        }
        renderer = new Renderer();
        box_renderer = new BoxRenderer();
        box_renderer.init();
    }

    /**
     * Resize GLSurfaceView
     */
    public void resizeGL(int width, int height) {

        Log.d(TAG, "resizeGL: ");

        view_size = new Vec2I(width, height);
        viewport_changed = true;
    }

    /**
     * Here we render the graphics through Renderer
     */
    public void render() {
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (renderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, view_size.data[0], view_size.data[1]);
            GLES20.glViewport(
                    default_viewport.data[0],
                    default_viewport.data[1],
                    default_viewport.data[2],
                    default_viewport.data[3]);

            if (renderer.renderErrorMessage(default_viewport)) {
                return;
            }
        }

        if (streamer == null) {
            return;
        }

        // get the newest frame from streamer
        Frame frame = streamer.peek();
        Log.d(TAG, "render: timestamp = " + frame.timestamp() + ", index = " + frame.index());
        try {
            // update viewport
            updateViewport();

            // set viewport
            GLES20.glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3]);

            // render the frame into fragment buffer object
            // will be called in every frame
            if (renderer != null) {
                renderer.render(frame, viewport);
            }

            for (TargetInstance targetInstance : frame.targetInstances()) {
                int status = targetInstance.status();
                if (status == TargetStatus.Tracked) {
                    Target target = targetInstance.target();
                    ImageTarget imagetarget = target instanceof ImageTarget ?
                            (ImageTarget) (target)
                            :
                            null;
                    if (imagetarget == null) {
                        continue;
                    }
                    if (box_renderer != null) {
                        /*
                        * Render the box, here we pass the Camera Coordinates
                        * along with the Projection Matrix
                        * and the 2 x 1 float vector
                        * */
                        box_renderer.render(
                                // get Projection Matrix, pass near plane and far plane
                                camera.projectionGL(0.2f, 500.f),
                                // get OpenGL coordinate matrix
                                targetInstance.poseGL(),
                                // target size, width and height in 2x1 float vector
                                imagetarget.size());

                        if (box_renderer.boxRendered()) {
                            Log.d(TAG, "render: do some work to fetch text");

                            // here we get the name and and decide what text to fetch
                            String text = fetchText(imagetarget.name());

                            mViewRefresher.refresh(text);
                        }
                    }
                }
            }
        } finally {
            frame.dispose();
        }
    }

    /**
     * make api call
     * */
    private String fetchText(String filter) {
        switch (filter) {
            case "sp0":
                return mContext.getString(R.string.tools_meeting_info_text1);
            case "sp1":
                return mContext.getString(R.string.tools_meeting_info_text2);
            case "arwhale":
                return mContext.getString(R.string.tools_meeting_info_text3);
            default:
                return "";
        }
    }

    /**
     * Stop tracking
     */
    public boolean stop() {

        Log.d(TAG, "stop: ");

        boolean status = true;
        for (ImageTracker tracker : trackers) {
            status &= tracker.stop();
        }
        status &= (streamer != null) && streamer.stop();
        status &= (camera != null) && camera.stop();
        return status;
    }

    /**
     * Dispose all resources
     */
    public void dispose() {

        Log.d(TAG, "dispose: ");

        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        box_renderer = null;
        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
        if (streamer != null) {
            streamer.dispose();
            streamer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
    }

    /**
     * Update Viewport
     */
    private void updateViewport() {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.rotation) {
            this.rotation = rotation;
            viewport_changed = true;
        }

        if (viewport_changed) {
            Vec2I size = new Vec2I(1, 1);
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();
            }
            // set camera size if the camera is vertical
            if (rotation == 90 || rotation == 270) {
                size = new Vec2I(size.data[1], size.data[0]);
            }

            // determine scale ratio according the large one
            float scaleRatio = Math.max(
                    (float) view_size.data[0] / (float) size.data[0],
                    (float) view_size.data[1] / (float) size.data[1]);

            // calculate viewport size
            Vec2I viewport_size = new Vec2I(
                    Math.round(size.data[0] * scaleRatio),
                    Math.round(size.data[1] * scaleRatio));

            // set viewport
            viewport = new Vec4I(
                    (view_size.data[0] - viewport_size.data[0]) / 2,
                    (view_size.data[1] - viewport_size.data[1]) / 2,
                    viewport_size.data[0],
                    viewport_size.data[1]);

            if ((camera != null) && camera.isOpened())
                viewport_changed = false;
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
}
