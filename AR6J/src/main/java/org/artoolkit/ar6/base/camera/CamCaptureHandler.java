/*
 *  ARActivity.java
 *  ARToolKit6
 *
 *  This file is part of CamCaptureView.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Philip Lamb, John Wolf
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

public interface CamCaptureHandler {

        void resetGettingCameraAccessPermissionsFromUserState();
        boolean gettingCameraAccessPermissionsFromUser();
        void closeCameraDevice();
        void registerCameraEventListener(CameraEventListener cel);
}
