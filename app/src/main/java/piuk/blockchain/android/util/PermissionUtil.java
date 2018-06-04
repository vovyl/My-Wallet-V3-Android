package piuk.blockchain.android.util;

import android.Manifest;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import piuk.blockchain.android.R;

/**
 * This class is pretty nasty - use {@link com.karumi.dexter.Dexter} instead.
 */
@Deprecated
public class PermissionUtil {

    public static final int PERMISSION_REQUEST_CAMERA = 161;
    public static final int PERMISSION_REQUEST_WRITE_STORAGE = 162;

    public static void requestCameraPermissionFromActivity(View parentView, final Activity activity) {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {

            Snackbar.make(parentView, activity.getString(R.string.request_camera_permission),
                    Snackbar.LENGTH_INDEFINITE).setAction(activity.getString(R.string.ok_cap), view -> {
                // Request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CAMERA);
            }).setActionTextColor(ContextCompat.getColor(parentView.getContext(), R.color.primary_blue_accent))
                    .show();

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    public static void requestWriteStoragePermissionFromFragment(View parentView, final android.support.v4.app.Fragment fragment) {
        // Permission has not been granted and must be requested.
        if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            Snackbar.make(parentView, fragment.getString(R.string.request_write_storage_permission),
                    Snackbar.LENGTH_INDEFINITE).setAction(fragment.getString(R.string.ok_cap), view -> {
                // Request the permission
                if (!fragment.isDetached()) {
                    fragment.requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_WRITE_STORAGE);
                }
            }).setActionTextColor(ContextCompat.getColor(parentView.getContext(), R.color.primary_blue_accent))
                    .show();

        } else {
            fragment.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_STORAGE);
        }
    }
}
