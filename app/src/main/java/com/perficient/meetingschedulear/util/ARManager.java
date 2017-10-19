package com.perficient.meetingschedulear.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.util.Log;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.model.ImageTargetInfo;
import com.perficient.meetingschedulear.model.MeetingRoomInfo;
import com.perficient.meetingschedulear.renderer.BlackboardRenderer;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import cn.easyar.*;

import static com.perficient.meetingschedulear.common.Constants.FILE_DIR_TARGET_IMAGE;
import static com.perficient.meetingschedulear.common.Constants.PREF_MEETING_INFO;
import static com.perficient.meetingschedulear.util.TimeUtil.FORMAT_DATE_TIME_SECOND;

/**
 * A manager for Renderer and AR Camera
 */
public class ARManager {

    private static final String TAG = ARManager.class.getSimpleName();

    private static final String PICASSO_WEEPING_WOMAN = "http://www.pablopicasso.org/images/paintings/the-weeping-woman.jpg";
    private static final String PICASSO_SMILING_GIRL = "https://s-media-cache-ak0.pinimg.com/736x/88/df/66/88df66a4cb0fdffc7188a5b417f72398.jpg";

    private static final String TARGET_JSON_PATH = "Data/targets.json";

    private CameraDevice mCamera;
    private CameraFrameStreamer mStreamer;
    private ArrayList<ImageTracker> mImageTrackers;
    private ImageTracker mTracker;
    private Renderer mRenderer;
    private BlackboardRenderer mBlackboardRenderer;

    private boolean mViewportChanged = false;
    private Vec2I mViewSize = new Vec2I(0, 0);
    private int mRotation = 0;
    private Vec4I mViewport = new Vec4I(0, 0, 1280, 720);
    private ImageTarget mPreviousTarget;
    private double mPreviousTimeStamp;

    private Context mContext;

    private SharedPreferences mPreferences;

    public ARManager(Context context) {
        mContext = context;
        mImageTrackers = new ArrayList<>();
        mPreferences = context.getSharedPreferences(PREF_MEETING_INFO, Context.MODE_PRIVATE);
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
        mTracker = new ImageTracker();
        mTracker.attachStreamer(mStreamer); // connect mStreamer to the ImageTracker

        //loadAllFromJsonFile(mTracker, TARGET_JSON_PATH);
        loadFromJsonFile(mTracker, TARGET_JSON_PATH, "whale");
        loadFromJsonFile(mTracker, TARGET_JSON_PATH, "sp0");
        loadFromJsonFile(mTracker, TARGET_JSON_PATH, "sp1");
        loadFromJsonFile(mTracker, TARGET_JSON_PATH, "at1");
        mImageTrackers.add(mTracker);

        loadExternalTargets(mStreamer);

        return true;
    }

    @SuppressWarnings("unchecked")
    private void loadExternalTargets(CameraFrameStreamer streamer) {
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer);

        List<ImageTargetInfo> targetInfoList = fetchDummyUrls();
        new ImageDownloader(mContext).execute(targetInfoList);

        File dir = new File(mContext.getExternalFilesDir(null).getAbsoluteFile(), FILE_DIR_TARGET_IMAGE);
        loadFromDir(tracker, dir);

        mImageTrackers.add(tracker);
    }

    // TODO: 2017/10/17 this should be replaced with API call to fetch image urls
    private List<ImageTargetInfo> fetchDummyUrls() {
        List<ImageTargetInfo> urls = new ArrayList<>();
        urls.add(new ImageTargetInfo(PICASSO_WEEPING_WOMAN, "weeping_woman.png"));
        urls.add(new ImageTargetInfo(PICASSO_SMILING_GIRL, "smiling_girl.png"));
        return urls;
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
            Vec4I defaultViewport = new Vec4I(0, 0, mViewSize.data[0], mViewSize.data[1]);
            GLES20.glViewport(
                    defaultViewport.data[0],
                    defaultViewport.data[1],
                    defaultViewport.data[2],
                    defaultViewport.data[3]);

            if (mRenderer.renderErrorMessage(defaultViewport)) {
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

                        // get current time stamp
                        double timeStamp = frame.timestamp();
                        // load target texture only once to reduce memory usage
                        if (mPreviousTarget == null ||
                                !imageTarget.name().equals(mPreviousTarget.name())) {

                            // fetch dummy data from resources
                            TextureContainer resContainer = fetchResources(imageTarget.name());

                            /*
                            * Since the frame will be cached, so when the user return to the scanning
                            * from other activities, the image target will still be there. We can use
                            * timestamp to check the current frame and to save the info only once.
                            * */
                            if (timeStamp != mPreviousTimeStamp) {
                                // save scanned info into shared preference
                                saveScannedInfo(resContainer.getMeetingRoomInfo());
                            }

                            mBlackboardRenderer.loadTexture(
                                    resContainer.getMeetingRoomInfo(),
                                    resContainer.getTexture());
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
                        mPreviousTimeStamp = frame.timestamp();
                        break;
                    }
                }
            }
        } finally {
            frame.dispose();
        }
    }

    private void saveScannedInfo(MeetingRoomInfo meetingRoomInfo) {
        if (meetingRoomInfo != null) {
            Log.d(TAG, "saveScannedInfo: " + meetingRoomInfo.getRoomName());
            Set<String> stringSet = new TreeSet<>();
            stringSet.add(TimeUtil.getFormatNow(FORMAT_DATE_TIME_SECOND));
            stringSet.addAll(meetingRoomInfo.getMeetings());

            mPreferences.edit()
                    // use room name as key
                    .putStringSet(meetingRoomInfo.getRoomName(), stringSet)
                    .apply();
        }
    }

    private TextureContainer fetchResources(String targetName) {
        TextureContainer container = new TextureContainer();
        switch (targetName) {
            case "sp0":
                container.setMeetingRoomInfo(fetchDummyData(R.string.tools_meeting_info_text1));
                container.setTexture(R.drawable.texture_chalkboard);
                break;
            case "sp1":
                container.setMeetingRoomInfo(fetchDummyData(R.string.tools_meeting_info_text2));
                container.setTexture(R.drawable.texture_chalkboard);
                break;
            case "whale":
                container.setMeetingRoomInfo(fetchDummyData(R.string.tools_meeting_info_text3));
                container.setTexture(R.drawable.texture_blackboard);
                break;
            case "desktop":
                container.setMeetingRoomInfo(fetchDummyData(R.string.tools_meeting_info_text4));
                container.setTexture(R.drawable.texture_blackboard);
                break;
            default:
                container.setMeetingRoomInfo(null);
                container.setTexture(R.drawable.texture_blackboard);
                break;
        }
        return container;
    }

    // TODO: 2017/10/15 replace this with API call
    private MeetingRoomInfo fetchDummyData(int stringRes) {
        String text = mContext.getString(stringRes);
        List<String> textList = new ArrayList<>();
        Scanner scanner = new Scanner(text).useDelimiter("\\n");
        while (scanner.hasNext()) {
            textList.add(scanner.next());
        }

        String roomName = textList.get(0);
        List<String> meetings = textList.subList(1, textList.size());
        return new MeetingRoomInfo(roomName, meetings);
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

    private void loadFromDir(ImageTracker tracker, File dir) {
        if (!dir.isDirectory()) {
            Log.w(TAG, "loadFromDir: not a directory!");
            return;
        }

        for (File file : dir.listFiles()) {
            loadFromImage(tracker, file.getAbsolutePath());
        }
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

    private class TextureContainer {

        private MeetingRoomInfo mMeetingRoomInfo;

        @DrawableRes
        private int texture;

        public TextureContainer() {
        }

        public TextureContainer(MeetingRoomInfo meetingRoomInfo, int texture) {
            this.mMeetingRoomInfo = meetingRoomInfo;
            this.texture = texture;
        }

        public MeetingRoomInfo getMeetingRoomInfo() {
            return mMeetingRoomInfo;
        }

        public void setMeetingRoomInfo(MeetingRoomInfo meetingRoomInfo) {
            this.mMeetingRoomInfo = meetingRoomInfo;
        }

        public int getTexture() {
            return texture;
        }

        public void setTexture(int texture) {
            this.texture = texture;
        }
    }

    /**
     * Download image from the server, and load them
     */
    private class ImageDownloader extends AsyncTask<List<ImageTargetInfo>, Void, List<String>> {

        private Context mContext;

        public ImageDownloader(Context context) {
            mContext = context;
        }

        @Override
        protected List<String> doInBackground(List<ImageTargetInfo>... params) {
            List<ImageTargetInfo> targetInfos = params[0];
            List<String> downloadedImages = new ArrayList<>();
            for (ImageTargetInfo target : targetInfos) {
                try {
                    File imageDir = new File(
                            mContext.getExternalFilesDir(null),
                            FILE_DIR_TARGET_IMAGE);

                    if (!imageDir.exists()) {
                        imageDir.mkdir();
                    }

                    File imageFile = new File(imageDir, target.getImageName());
                    if (imageFile.exists()) {
                        break;
                    }

                    Log.d(TAG, "doInBackground: ---- Downloading image " + target.getImageName() + " -----");
                    Bitmap bitmap = Picasso.with(mContext)
                            .load(target.getUrl()).get();
                    // save image to external storage card
                    saveImage(bitmap, imageFile);

                    downloadedImages.add(imageFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return downloadedImages;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            for (String path : result) {
                loadFromImage(mTracker, path);
            }
        }

        private void saveImage(Bitmap bitmap, File imageFile) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "image saved to >>>" + imageFile.getAbsolutePath());
        }
    }
}
