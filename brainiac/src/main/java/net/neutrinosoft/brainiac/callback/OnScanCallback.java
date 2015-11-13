package net.neutrinosoft.brainiac.callback;

/**
 * OnScanCallback is used to handle scan failed error
 */
public interface OnScanCallback {

    /**
     * Callback calls when scanning is started
     */
    void onScanStart();

    /**
     * Callback calls when scanning is stopped
     */
    void onScanStop();

    /**
     * Callback calls when scanning is failed
     */
    void onScanFailed(int errorCode);

}
