/*
 *  FPSCounter.java
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

package org.artoolkit.ar6.base;

/**
 * Utility class to measure performance in frames per second. This can be useful for camera
 * capture or graphics rendering.
 */
public class FPSCounter {

    /**
     * The number of frames that have occurred in the current time period.
     */
    private int frameCount;

    /**
     * When the current time period started.
     */
    private long periodStart;

    /**
     * The last calculated FPS value.
     */
    private float currentFPS;

    public FPSCounter() {
        reset();
    }

    @SuppressWarnings("WeakerAccess")
    public void reset() {
        frameCount = 0;
        periodStart = 0;
        currentFPS = 0.0f;
    }

    /**
     * Call this function during each frame. The count of elapsed frames will be
     * increased and if a second has elapsed since the last FPS calculation, a new
     * FPS value will be calculated. If this happens, then the function will return
     * true.
     *
     * @return true if a new FPS value has been calculated.
     */
    public boolean frame() {

        frameCount++;

        long time = System.currentTimeMillis();
        if (periodStart <= 0) periodStart = time;

        long elapsed = time - periodStart;

        if (elapsed >= 1000) {

            currentFPS = (1000 * frameCount) / (float) elapsed;
            frameCount = 0;

            periodStart = time;

            return true;

        }

        return false;

    }

    /**
     * Returns the last calculated FPS value.
     *
     * @return The last calculated FPS value.
     */
    public float getFPS() {
        return currentFPS;
    }

    /**
     * Provides a string representation of the current FPS value.
     *
     * @return A string representation of the current FPS value.
     */
    @Override
    public String toString() {
        return "FPS: " + currentFPS;
    }

}
