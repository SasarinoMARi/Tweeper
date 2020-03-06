package com.sasarinomari.tweeper.permissionhelper;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

class PermissionRequester {
    protected static final String TAG = "PermissionRequester";
    static final int NOT_SUPPORTED_VERSION = 2;
    static final int ALREADY_GRANTED = -1;
    static final int REQUEST_PERMISSION = 0;

    private final Activity context;
    private Builder builder;

    private void setBuilder(Builder builder) {
        this.builder = builder;
    }

    private PermissionRequester(Activity context) {
        this.context = context;
    }

    public int request(final String permission, final int requestCode, final OnClickDenyButtonListener denyAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = context.checkCallingOrSelfPermission(permission);

            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                if (context.shouldShowRequestPermissionRationale(permission)) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle(builder.getTitle())
                            .setPositiveButton(builder.getPositiveButtonName(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    context.requestPermissions(new String[]{permission}, requestCode);
                                }
                            })
                            .setNegativeButton(builder.getNegativeButtonName(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    denyAction.onClick(context);
                                }
                            }).create().show();
                    return REQUEST_PERMISSION;
                } else {
                    context.requestPermissions(new String[]{permission}, requestCode);
                    return REQUEST_PERMISSION;
                }
            } else {
                return ALREADY_GRANTED;
            }
        }
        return NOT_SUPPORTED_VERSION;
    }

    int request(final String[] permissions, final int requestCode, final OnClickDenyButtonListener denyAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final List<String> permissionList = new ArrayList<>();
            boolean shouldShowAlertDialog = false;

            for (String permission : permissions) {
                if (context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    permissionList.add(permission);
                    if (context.shouldShowRequestPermissionRationale(permission)) {
                        shouldShowAlertDialog = true;
                    }
                }
            }
            if (permissionList.size() >= 1) {
                if (shouldShowAlertDialog) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle(builder.getTitle())
                            .setPositiveButton(builder.getPositiveButtonName(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    context.requestPermissions(permissionList.toArray(new String[permissionList.size()]), requestCode);
                                }
                            })
                            .setNegativeButton(builder.getNegativeButtonName(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    denyAction.onClick(context);
                                }
                            }).create().show();
                    return REQUEST_PERMISSION;
                } else {
                    context.requestPermissions(permissionList.toArray(new String[permissionList.size()]), requestCode);
                    return REQUEST_PERMISSION;
                }
            } else {
                return ALREADY_GRANTED;
            }
        }
        return NOT_SUPPORTED_VERSION;
    }

    static class Builder {

        private final PermissionRequester requester;

        Builder(Activity context) {
            requester = new PermissionRequester(context);
        }

        private String title;
        private String positiveButtonName;
        private String negativeButtonName;

        String getTitle() {
            return title;
        }

        Builder setTitle(String title) {
            this.title = title;
            return this;
        }


        String getPositiveButtonName() {
            return positiveButtonName;
        }

        Builder setPositiveButtonName(String positiveButtonName) {
            this.positiveButtonName = positiveButtonName;
            return this;
        }

        String getNegativeButtonName() {
            return negativeButtonName;
        }

        Builder setNegativeButtonName(String negativeButtonName) {
            this.negativeButtonName = negativeButtonName;
            return this;
        }

        PermissionRequester create() {
            this.requester.setBuilder(this);
            return this.requester;
        }
    }

    public interface OnClickDenyButtonListener {
        void onClick(Activity activity);
    }
}