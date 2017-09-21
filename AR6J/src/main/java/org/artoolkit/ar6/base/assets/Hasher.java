/*
 *  Hasher.java
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

package org.artoolkit.ar6.base.assets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

//import android.util.Log;

public class Hasher {

    //private final static String TAG = "Hasher";

    private final static String HEX = "0123456789ABCDEF";

    public static String toHex(byte[] buf) {

        if (buf == null) return "";

        StringBuffer result = new StringBuffer(2 * buf.length);

        for (int i = 0; i < buf.length; i++) {
            result.append(HEX.charAt((buf[i] >> 4) & 0x0f)).append(HEX.charAt(buf[i] & 0x0f));
        }

        return result.toString();
    }

    public static long computeCRC(String filename) throws HashComputationException {

        InputStream in = null;
        byte[] buffer = new byte[16384];
        int bytesRead = -1;
        CRC32 crc = new CRC32();

        try {
            in = new FileInputStream(filename);
        } catch (FileNotFoundException fnfe) {
            throw new HashComputationException("File not found: " + filename, fnfe);
        }

        //long crcStartTime = System.nanoTime();

        try {
            while ((bytesRead = in.read(buffer)) != -1) crc.update(buffer, 0, bytesRead);
            in.close();
            in = null;
        } catch (IOException ioe) {
            throw new HashComputationException("IOException while reading from file", ioe);
        }

        //long elapsedTime = System.nanoTime() - crcStartTime;
        //Log.i(TAG, "CRC time: " + (elapsedTime / 1000000.0f) + " ms");

        //Log.i(TAG, "CRC result of " + filename + ": " + value);

        return crc.getValue();

    }


    public static String computeHash(String filename) throws HashComputationException, IOException {

        InputStream in = null;
        MessageDigest digest = null;
        String algorithm = "SHA-1";
        byte[] buffer = new byte[16384];
        int bytesRead = -1;


        try {
            in = new FileInputStream(filename);
        } catch (FileNotFoundException fnfe) {
            throw new HashComputationException("File not found: " + filename, fnfe);
        }


        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException nsae) {
            try {
                in.close();
            } catch (IOException e) {
                throw e;
            }
            in = null;
            throw new HashComputationException("No such algorithm: " + algorithm, nsae);
        }

        //long hashStartTime = System.nanoTime();

        try {
            while ((bytesRead = in.read(buffer)) != -1) digest.update(buffer, 0, bytesRead);
            in.close();
            in = null;
        } catch (IOException ioe) {
            throw new HashComputationException("IOException while reading from file", ioe);
        }

        byte[] digestResult = digest.digest();

        //long elapsedTime = System.nanoTime() - hashStartTime;
        //Log.i(TAG, "Hash time: " + (elapsedTime / 1000000.0f) + " ms");

        //Log.i(TAG, "Hash result of " + filename + ": " + hash);

        return toHex(digestResult);

    }

}
