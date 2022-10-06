/**
 *
 */
package com.multicamerapleno;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MultiCameraPleno extends CordovaPlugin {
    private static final String TAG = "MultiCameraPleno";

    public static final int TAKEONCAMERA = 1002;

    private static final String[] PROJECTION = {MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA};

    private static long startTemp = Long.MAX_VALUE;
    private static int state = 0;

    //Actions names
    private static final String TAKE_PICTURE_ACTION = "takePicture";
    private static final String GET_PICTURE_ACTION = "getTakePhoto";


    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Activity activity = this.cordova.getActivity();
        if (action.equals("saludar")) {
            // An example of returning data back to the web layer
            String phrase = args.getString(0);
            // Echo back the first argument
            final PluginResult result = new PluginResult(PluginResult.Status.OK, "Hola todo el... " + phrase);
            callbackContext.sendPluginResult(result);
        }

        if (action.equals(TAKE_PICTURE_ACTION)) {
            this.takeOnCamera(activity);
        }
        if (action.equals(GET_PICTURE_ACTION)) {
            List photos = this.getTakePhoto(activity);
            if (!(photos.size() == 0)) {
                //Loop index size()
                JSONObject eachData = new JSONObject();

                for (int index = 0; index < photos.size(); index++) {
                    try {
                        eachData.put(Integer.toString(index), photos.get(index));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                final PluginResult result = new PluginResult(PluginResult.Status.OK, eachData);
                callbackContext.sendPluginResult(result);
            } else {
                final PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT, "something goes wrong");
                callbackContext.sendPluginResult(result);
                //Do something when sList is empty
                return false;
            }

        }
        return true;
    }

    public static void takeOnCamera(Activity activity) {
        startTemp = System.currentTimeMillis();
        Intent intent = new Intent();
        try {
            intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            activity.startActivityForResult(intent, TAKEONCAMERA);
        } catch (Exception e) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                activity.startActivityForResult(intent, TAKEONCAMERA);
            } catch (Exception e1) {
                try {
                    intent.setAction(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
                    activity.startActivityForResult(intent, TAKEONCAMERA);
                } catch (Exception ell) {
//                    ToastUtils.showToast(activity, "Failed to open camera, please select photo from album");
                }
            }
        }
    }

    public static List<String> getTakePhoto(Activity activity) {
        ContentResolver resolver = activity.getBaseContext().getContentResolver();

        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION,
                    null, null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT 0,30");
            if (cursor != null) {
                List<String> photoList = new ArrayList<>(cursor.getCount());
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    long createTemp = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                    if (isAfterStart(startTemp, createTemp)) {
                        photoList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                    }
                    cursor.moveToNext();
                }
                return photoList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    private static boolean isAfterStart(long startTemp, long createTemp) {
        if (state == 0) {
            int startTempLength = String.valueOf(startTemp).length();
            int createTempLength = String.valueOf(createTemp).length();
            if (startTempLength == createTempLength) {
                state = 1;
            } else if (startTempLength == 10 && createTempLength == 13) {
                state = 2;
            } else if (startTempLength == 13 && createTempLength == 10) {
                state = 3;
            }
        }
        if (state == 2) {
            startTemp = startTemp * 1000;
        } else if (state == 3) {
            startTemp = startTemp / 1000;
        }
        return createTemp - startTemp >= 0;
    }


}
