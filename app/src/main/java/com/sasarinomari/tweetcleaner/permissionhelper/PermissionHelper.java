package com.sasarinomari.tweetcleaner.permissionhelper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import com.sasarinomari.tweetcleaner.R;

public class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    private static Runnable callback;

    private static final int PERMISSION_REQUEST_CODE = 1000;

    public static void activatePermission(final Activity context, String[] permissions, Runnable callback) {
        PermissionHelper.callback = callback;

        int permissionResult = new PermissionRequester.Builder(context)
                .setTitle(context.getString(R.string.PermissionRequestTitle))
                .setPositiveButtonName(context.getString(R.string.Yes))
                .setNegativeButtonName(context.getString(R.string.No))
                .create()
                .request(permissions, PERMISSION_REQUEST_CODE,
                        activity -> Log.d(TAG, "Permission denied by user."));

        if (permissionResult == PermissionRequester.ALREADY_GRANTED) {
            Log.d(TAG, "Permission already granted.");
            if (checkSelfAllPermissions(context, permissions)) {
                callback.run();
            }
        } else if (permissionResult == PermissionRequester.NOT_SUPPORTED_VERSION) {
            Log.d(TAG, "No supported version");
            callback.run();
        } else if (permissionResult == PermissionRequester.REQUEST_PERMISSION) {
            Log.d(TAG, "Requested permission");
        }

    }

    private static boolean checkSelfAllPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            // Activity Compat 기반 권한 체크
            //if ( ActivityCompat.checkSelfPermission( context, permission ) == PackageManager.PERMISSION_DENIED )

            // Context 기반 권한 체크
            if (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public static void onRequestPermissionsResult(Context context, String[] permissions, int requestCode, int[] grantResults, Runnable deniedCallback) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean haveAllPermissionsGranted = true;

                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        haveAllPermissionsGranted = false;
                    }
                }
                if (haveAllPermissionsGranted) {
                    Log.d(TAG, "Permission Granted by user.");

                    if (checkSelfAllPermissions(context, permissions)) {
                        new Handler().post(PermissionHelper.callback);
                    }
                } else {
                    Log.d(TAG, "Permission denied by user.");
                    deniedCallback.run();
                }
            } else {
                Log.d(TAG, "No result");
            }
        }
    }
}

