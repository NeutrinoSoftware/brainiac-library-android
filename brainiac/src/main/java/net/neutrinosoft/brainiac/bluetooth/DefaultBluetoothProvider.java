package net.neutrinosoft.brainiac.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.widget.Toast;

import net.neutrinosoft.brainiac.callback.OnDeviceCallback;
import net.neutrinosoft.brainiac.utils.PermissionsUtils;

public class DefaultBluetoothProvider implements BluetoothProvider {
    private static final String TAG = DefaultBluetoothProvider.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter;
    private ScanCallback scanCallback;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private Activity context;
    private OnDeviceCallback onDeviceCallback;

    public void setOnDeviceCallback(OnDeviceCallback onDeviceCallback) {
        this.onDeviceCallback = onDeviceCallback;
    }

    public DefaultBluetoothProvider(Activity context, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
    }

    @Override
    public void startScan() {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
            if (PermissionsUtils.isCoarseLocationAllowed(context)) {
                initScanCallback();
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
                }
            } else {
                Log.d(TAG, "Location permission does not allowed");
                Toast toast = Toast.makeText(context, "Location permission does not allowed", Toast.LENGTH_SHORT);
                if (toast != null) {
                    toast.show();
                }
                stopScan();
            }
        } else

        {
            initLeScanCallback();
            if (bluetoothAdapter != null) {
                bluetoothAdapter.startLeScan(leScanCallback);
            }
        }

    }

    @Override
    public void stopScan() {
        if (bluetoothAdapter != null) {
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            } else {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        }
    }

    private void initScanCallback() {
        if (scanCallback == null) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    onDeviceFound(device);
                }
            };
        }
    }

    private void initLeScanCallback() {
        if (leScanCallback == null) {
            leScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    onDeviceFound(device);
                }
            };
        }
    }

    private void onDeviceFound(BluetoothDevice bluetoothDevice) {
        onDeviceCallback.onDeviceFound(bluetoothDevice);
    }

}
