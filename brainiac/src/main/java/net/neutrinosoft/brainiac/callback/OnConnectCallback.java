package net.neutrinosoft.brainiac.callback;

/**
 * OnConnectCallback is used for handling connection state.
 */
public interface OnConnectCallback {
    /**
     * Callback is called after successful connection to Brainiac accessory.
     */
    void onConnectSuccess();

    /**
     * Callback is called after failed connection to Brainiac accessory.
     */
    void onConnectFailed();
}
