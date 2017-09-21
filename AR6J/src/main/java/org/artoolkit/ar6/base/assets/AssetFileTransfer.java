/*
 *  AssetFileTransfer.java
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

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetFileTransfer {

    private static final String TAG = "AssetFileTransfer";

    public File assetFile;
    public boolean assetAvailable;

    public File targetFile;
    public File targetDirectory;

    public boolean targetFileAlreadyExists;
    public String targetFileHash;
    public long targetFileCRC;
    public File tempFile;
    public String tempFileHash;
    public long tempFileCRC;

    public boolean filesMatch;

    public boolean assetCopied;

    private void copyContents(InputStream in, OutputStream out) throws IOException {

        final int bufferSize = 16384;
        byte[] buffer = new byte[bufferSize];

        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }

        out.flush();
    }


    public void copyAssetToTargetDir(AssetManager manager, String assetFilePath, String targetDirPath) throws AssetFileTransferException {

        assetFile = new File(assetFilePath);

        InputStream in;
        OutputStream out;

        try {
            in = manager.open(assetFilePath);
            assetAvailable = true;
        } catch (IOException e) {
            assetAvailable = false;
            throw new AssetFileTransferException("Unable to open the asset file: " + assetFilePath, e);
        }

        targetFile = new File(targetDirPath, assetFilePath);
        targetFileAlreadyExists = targetFile.exists();

        Log.i(TAG, "copyAssetToTargetDir(): [" + assetFilePath + "] -> [" + targetFile.getPath() + "]");

        if (targetFileAlreadyExists) {

            //Log.i(TAG, "Target file exists. Unpacking to temporary file first.");

            // Create temporary file to unpack to
            try {
                tempFile = File.createTempFile("unpacker", null, Environment.getExternalStorageDirectory());
                //Log.i(TAG, "Created temp file for unpacking: " + tempFile.getPath());
            } catch (IOException ioe) {
                throw new AssetFileTransferException("Error creating temp file: " + tempFile.getPath(), ioe);
            }

            // Copy asset to temporary file
            try {
                out = new FileOutputStream(tempFile);
            } catch (FileNotFoundException fnfe) {
                throw new AssetFileTransferException("Error creating temp file: " + tempFile.getPath(), fnfe);
            }
            try {
                copyContents(in, out);
                in.close();
                in = null;
                out.close();
                out = null;
            } catch (IOException ioe) {
                throw new AssetFileTransferException("Error copying asset to temp file: " + tempFile.getPath(), ioe);
            }

            // Get hashes for new temporary file and existing file
            try {

                //tempFileHash = Hasher.computeHash(tempFile.getPath());
                tempFileCRC = Hasher.computeCRC(tempFile.getPath());

                //targetFileHash = Hasher.computeHash(targetFile.getPath());
                targetFileCRC = Hasher.computeCRC(targetFile.getPath());

            } catch (HashComputationException hce) {
                throw new AssetFileTransferException("Error hashing files", hce);
            }

            if (tempFileCRC == targetFileCRC) {

                // The hashes match. The files are the same, so don't need to do anything.
                //Log.i(TAG, "The hashes match. Keeping existing file, removing temp file.");
                // Clean up temporary file
                tempFile.delete();

            } else {

                // The hashes do not match. Overwrite the existing file with the new one.
                targetFile.delete();
                //Log.i(TAG, "Deleted existing file");
                tempFile.renameTo(targetFile);
                //Log.i(TAG, "Moved temp file: " + tempFile.getPath() + " to " + targetFile.getPath());
                assetCopied = true;
            }

        } else {

            Log.i(TAG, "copyAssetToTargetDir(): Target file does not exist. Creating directory structure.");

            // Ensure parent directories exist so we can create the file
            targetDirectory = targetFile.getParentFile();
            targetDirectory.mkdirs();

            // Copy asset to target file
            try {
                out = new FileOutputStream(targetFile);
            } catch (FileNotFoundException fnfe) {
                throw new AssetFileTransferException("Error creating target file: " + targetFile.getPath(), fnfe);
            }
            try {
                copyContents(in, new FileOutputStream(targetFile));
                //Log.i(TAG, "Copied asset to target file");

                in.close();
                in = null;
                out.close();
                out = null;
            } catch (IOException ioe) {
                throw new AssetFileTransferException("Error copying asset to target file: " + targetFile.getPath(), ioe);
            }
            assetCopied = true;
        }
    }
}