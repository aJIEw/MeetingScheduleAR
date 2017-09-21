/*
 *  AssetHelper.java
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

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;

//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;

public class AssetHelper {

    private static final String TAG = "AssetHelper";

    private AssetManager manager;

    public AssetHelper(AssetManager am) {

        manager = am;

    }


    public List<AssetFileTransfer> copyAssetFolder(String assetBasePath, String targetDirPath) {

        Set<String> filenames = getAssetFilenames(assetBasePath);
        List<AssetFileTransfer> transfers = new ArrayList<>();

        for (String f : filenames) {
            AssetFileTransfer aft = new AssetFileTransfer();
            transfers.add(aft);
            try {
                aft.copyAssetToTargetDir(manager, f, targetDirPath);
            } catch (AssetFileTransferException afte) {
                afte.printStackTrace();
            }
        }

        return transfers;
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public void cacheAssetFolder(Context ctx, String assetBasePath) {

        // If the folder has already been cached, we will inspect the cache.
        // If it's all OK, we'll return nice and quickly. Otherwise, any previous
        // cached version will be removed and then a new copy of the folder written
        // to the cache.
        boolean reCache = false;

        // First, look for the folder's cache index which would have been previously
        // written. We name it cacheIndex-X.txt where X is the "VersionCode" field
        // from the manifest.
        int versionCode;
        try {
            versionCode = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException nnfe) {
            nnfe.printStackTrace();
            return;
        }
        File cacheFolder = new File(ctx.getCacheDir().getAbsolutePath() + "/" + assetBasePath);
        File cacheIndexFile = new File(cacheFolder, "cacheIndex-" + versionCode + ".txt");

        BufferedReader inBuf = null;
        try {
            inBuf = new BufferedReader(new FileReader(cacheIndexFile));
            if (inBuf != null) {
                // Cache index exists and is readable. Read it and make sure all files it lists
                // are still in cache.
                String line;
                while ((line = inBuf.readLine()) != null) {
                    File cachedFile = new File(line);
                    if (!cachedFile.exists()) {
                        Log.i(TAG, "cacheAssetFolder(): Cache for folder '" + assetBasePath + "' incomplete. Re-caching.");
                        reCache = true;
                        break;
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            // If cache index does not exist, recreate whole cache.
            Log.i(TAG, "cacheAssetFolder(): Cache index not found for folder '" + assetBasePath + "'. Re-caching.");
            reCache = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } finally {
            if (inBuf != null) {
                try {
                    inBuf.close();
                    inBuf = null;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        // If needed, write a fresh copy of the folder to the cache.
        if (reCache) {
            deleteRecursive(cacheFolder); // Delete remnant, if any, of cached folder.
            List<AssetFileTransfer> transfers = copyAssetFolder(assetBasePath, ctx.getCacheDir().getAbsolutePath()); // Recreate it.

            // Now write a new cache index inside the folder.
            BufferedWriter outBuf = null;
            try {
                outBuf = new BufferedWriter(new FileWriter(cacheIndexFile));
                for (AssetFileTransfer aft : transfers) {
                    outBuf.write(aft.targetFile.getAbsolutePath());
                    outBuf.newLine();
                }
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                if (outBuf != null) {
                    try {
                        outBuf.flush();
                        outBuf.close();
                        outBuf = null;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        } else {
            Log.i(TAG, "cacheAssetFolder(): Using cached folder '" + assetBasePath + "'.");
        }

    }

    public Set<String> getAssetFilenames(String path) {
        Set<String> files = new HashSet<>();
        getAssetFilenames(path, files);
        return files;
    }

    // Recursive subroutine which appends to 'files'.
    private void getAssetFilenames(String path, Set<String> files) {
        try {
            String[] filenames = manager.list(path); // Equivalent to "ls path"
            // Recursion logic.
            // A little logic to decide if a path is a file. Empty folders are never saved
            // in the archive, so if there are no files below this path, it's a file.
            // If there are files beneath, then this is a directory and we can add it to the folder
            // structure.
            if (filenames.length == 0) { // A file.
                files.add(path);
                Log.i(TAG, "getAssetFilenames(): Found asset '" + path + "'");
            } else { // A directory.
                for (String f : filenames) {
                    // Create a full path by concatenating path and the filename
                    File file = new File(path, f);
                    String fileName = file.getPath();
                    getAssetFilenames(fileName, files); // Recurse.
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
