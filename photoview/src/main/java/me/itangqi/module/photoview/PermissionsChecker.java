package me.itangqi.module.photoview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

class PermissionsChecker {
    private final Context mContext;

    PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }

    // 判断权限集合
    boolean isLackedPermissions(String... permissions) {
        for (String permission : permissions) {
            if (isLackedPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private boolean isLackedPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
