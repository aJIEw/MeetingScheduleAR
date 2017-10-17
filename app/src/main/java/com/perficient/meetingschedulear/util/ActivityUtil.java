package com.perficient.meetingschedulear.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.perficient.meetingschedulear.R;

import java.util.ArrayList;
import java.util.List;

import static cn.easyar.engine.EasyAR.getApplicationContext;
import static com.perficient.meetingschedulear.BaseApplication.getResourcesObject;

public class ActivityUtil {

    private static final String TAG = ActivityUtil.class.getSimpleName();

    private static List<Activity> activities = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    private static Toolbar sToolbar;

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public static void setUpToolBar(final Activity activity, String title) {
        ViewGroup root = activity.findViewById(android.R.id.content);
        View content = root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(activity, R.layout.custom_title_toolbar, null);
        sToolbar = toolbarContainer.findViewById(R.id.toolbar);
        sToolbar.setTitle("");
        sToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        TextView titleText = toolbarContainer.findViewById(R.id.toolbar_title_textView);
        titleText.setText(title);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);
    }

    public static void setUpToolBar(Activity activity, int rsId) {
        setUpToolBar(activity, getResourcesObject().getString(rsId));
    }

    @SuppressWarnings("deprecation")
    public static void toggleFlashLight(Activity activity) {
        boolean available = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (available) {
            Log.d(TAG, "toggleFlashLight: toggle on");
            Camera cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.startPreview();
        } else {
            Log.w(TAG, "toggleFlashLight: No flash light found!");
        }
    }

    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.M)
    public static void toggleFlashLightForM(boolean toggle) {
        try {
            CameraManager cameraManager = (CameraManager) getApplicationContext()
                    .getSystemService(Context.CAMERA_SERVICE);

            for (String id : cameraManager.getCameraIdList()) {

                // Turn on the flash if camera has one
                if (cameraManager.getCameraCharacteristics(id)
                        .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    cameraManager.setTorchMode(id, toggle);
                    Log.w(TAG, "toggleFlashLightForM: toggled" );
                }
            }

        } catch (Exception e) {
            ToastUtil.showToast("Torch Failed: " + e.getMessage());
        }
    }

    public static Toolbar getToolbar() {
        return sToolbar;
    }

    public static void longLog(String content) {
        if (content.length() > 4000) {
            for (int i = 0; i < content.length(); i += 4000) {
                if (i + 4000 < content.length()) {
                    Log.i(TAG + i, content.substring(i, i + 4000));
                } else {
                    Log.i(TAG + i, content.substring(i, content.length()));
                }
            }
        } else {
            Log.i(TAG, content);
        }
    }
}