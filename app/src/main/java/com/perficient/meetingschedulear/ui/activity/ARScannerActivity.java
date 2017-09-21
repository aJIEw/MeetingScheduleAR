package com.perficient.meetingschedulear.ui.activity;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.renderer.ARCubeRenderer;

import org.artoolkit.ar6.base.ARActivity;
import org.artoolkit.ar6.base.rendering.ARRenderer;

public class ARScannerActivity extends ARActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_scanner);
    }

    @Override
    protected ARRenderer supplyRenderer() {
        return new ARCubeRenderer();
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.activity_ar_scanner_main_frameLayout);
    }
}
