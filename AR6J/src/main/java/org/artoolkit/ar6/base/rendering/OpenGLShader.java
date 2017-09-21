/*
 *  ARActivity.java
 *  ARToolKit6
 *
 *  This file is part of ARToolKit.
 *
 *  Copyright 2015-2016 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Thorsten Bux, Philip Lamb
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

package org.artoolkit.ar6.base.rendering;

/**
 * Provides an interface that ensures the basic shader methods and information in a shader implementation
 * are provided.
 */
public interface OpenGLShader {

    //These properties are used to make the connection between the code and the shader. We use them
    //to link the projection and model matrix to the shader and to pass these matrices to the shader
    //from the AR application.
    String projectionMatrixString = "u_projection";
    String modelViewMatrixString = "u_modelView";
    //Also used to provide a link to the shader program. In this case we pass in the position vectors from the
    //AR application to the shader.
    String positionVectorString = "a_Position";

    int configureShader();

    @SuppressWarnings("unused")
    void setShaderSource(String source);

}
