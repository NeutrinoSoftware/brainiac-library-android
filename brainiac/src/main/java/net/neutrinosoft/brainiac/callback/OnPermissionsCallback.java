package net.neutrinosoft.brainiac.callback;

import android.support.annotation.NonNull;

public interface OnPermissionsCallback {
    void onPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults);
}
