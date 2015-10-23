package net.neutrinosoft.brainiac.callback;

import android.bluetooth.BluetoothDevice;

/**
 * OnDeviceCallback is used for handling searching device
 */
public interface OnDeviceCallback {

    /**
     * Callback calls when device found
     * @param device Bluetooth device
     */
    void onDeviceFound(BluetoothDevice device);

    /**
     * Callback calls when device is connected
     * @param device Bluetooth device
     */
    void onDeviceConnected(BluetoothDevice device);

    /**
     * Callback calls when device is connect<b>ing</b>
     * @param device Bluetooth device
     */
    void onDeviceConnecting(BluetoothDevice device);

    /**
     * Callback calls when device is disconnected
     * @param device Bluetooth device
     */
    void onDeviceDisconnected(BluetoothDevice device);

    /**
     * Callback calls when there is an error while device connecting
     * @param device Bluetooth device
     */
    void onDeviceConnectionError(BluetoothDevice device);

    /**
     * Callback calls when device is disconnect<b>ing</b>
     * @param device Bluetooth device
     */
    void onDeviceDisconnecting(BluetoothDevice device);
}
