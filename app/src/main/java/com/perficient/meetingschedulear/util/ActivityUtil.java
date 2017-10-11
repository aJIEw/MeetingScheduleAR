package com.perficient.meetingschedulear.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import com.perficient.meetingschedulear.R;

import java.util.ArrayList;
import java.util.List;

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