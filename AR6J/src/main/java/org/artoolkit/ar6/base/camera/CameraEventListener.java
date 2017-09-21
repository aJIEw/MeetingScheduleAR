/*
 *  CameraEventListener.java
 *  ARToolKit6
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Julian Looser, Philip Lamb
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.artoolkit.ar6.base.camera;

import java.nio.ByteBuffer;

/**
 * The CameraEventListener interface allows an observer to respond to events
 * from a {@link CaptureCameraPreview} or {@link Cam2CaptureSurface}.
 */
public interface CameraEventListener {

    /**
     * Called when the camera preview is started. The video dimensions and frame rate
     * are passed through, along with information about the camera.
     *
     * @param width               The width of the video image in pixels.
     * @param height              The height of the video image in pixels.
     * @param pixelFormat         A string with format in which buffers will be pushed. Supported values include "NV21", "NV12", "YUV_420_888", "RGBA", "RGB_565", and "MONO".
     * @param cameraIndex         Integer 0-based index of the camera in use. E.g. 0 represents the first (usually rear) camera on the device. The
	 *            camera represented by a given index must not change over the lifetime of the device.
     * @param cameraIsFrontFacing false if camera is rear-facing (the default) or true if camera is facing toward the user.
     */
    void cameraStarted(int width, int height, String pixelFormat, int cameraIndex, boolean cameraIsFrontFacing);

    /**
     * Called when the camera preview has a new frame ready (single-planar).
     *
     * @param frame A byte array from the camera's frame, in the camera's capture format.
     */
    void cameraFrame1(byte[] frame, int frameSize);

    /**
     * Called when the camera preview has a new frame ready.
     *
     * @param framePlanes An array of ByteBuffers from the camera's frame planes, in the camera's capture format.
     */
    void cameraFrame2(ByteBuffer[] framePlanes, int[] framePlanePixelStrides, int[] framePlaneRowStrides);

    /**
     * Called when the capture preview is stopped. No new frames will be sent.
     */
    void cameraStopped();

}
