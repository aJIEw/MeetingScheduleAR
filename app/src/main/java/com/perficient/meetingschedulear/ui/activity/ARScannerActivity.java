package com.perficient.meetingschedulear.ui.activity;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.renderer.CubeRenderer;
import com.perficient.meetingschedulear.renderer.NativeCarRenderer;

import org.artoolkit.ar6.base.ARActivity;
import org.artoolkit.ar6.base.rendering.ARRenderer;

public class ARScannerActivity extends ARActivity {

    private NativeCarRenderer mNativeCarRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_scanner);

        //mNativeCarRenderer = new NativeCarRenderer();
    }

    @Override
    protected ARRenderer supplyRenderer() {
        //return mNativeCarRenderer;
        return new CubeRenderer();
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.activity_ar_scanner_main_frameLayout);
    }

    @Override
    public void onStop() {
        //NativeCarRenderer.demoShutdown();

        super.onStop();
    }
}
