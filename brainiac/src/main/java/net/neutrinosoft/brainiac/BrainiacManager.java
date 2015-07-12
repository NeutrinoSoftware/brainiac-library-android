package net.neutrinosoft.brainiac;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import net.neutrinosoft.brainiac.callback.OnConnectCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveDataCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveFftDataCallback;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The BrainiacManager class handles connections and data transfer between Braniac (alpha title) accessory and Android device.
 */


public class BrainiacManager extends BluetoothGattCallback implements BluetoothAdapter.LeScanCallback {

    private static final String DEVICE_NAME = "NeuroBLE";
    private static BrainiacManager singleton;
    double VRef = 2.4 / 6.0 / 32.0;
    double K = 1000000000 * VRef / 0x7FFF;


    private static ArrayList<Value> values = new ArrayList<>();
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private OnReceiveFftDataCallback onReceiveFftDataCallback;
    private OnConnectCallback onConnectCallback;


    private OnReceiveDataCallback onReceiveDataCallback;

    /**
     * Register a callback to be invoked when fft data received.
     * @param onReceiveFftDataCallback - An implementation of OnReceiveFftDataCallback
     */
    public void setOnReceiveFftDataCallback(OnReceiveFftDataCallback onReceiveFftDataCallback) {
        this.onReceiveFftDataCallback = onReceiveFftDataCallback;
    }

    /**
     * Get BrainiacManager singleton.
     *
     * @param context - application context
     * @return instance of BrainiacManager
     */

    public static BrainiacManager getBrainiacManager(Context context) {
        if (singleton == null) {
            singleton = new BrainiacManager(context);
        }
        return singleton;
    }

    private BrainiacManager(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Register a callback to be invoked when raw data received.
     * @param onReceiveDataCallback - An implementation of OnReceiveDataCallback
     */
    public void setOnReceiveDataCallback(OnReceiveDataCallback onReceiveDataCallback) {
        this.onReceiveDataCallback = onReceiveDataCallback;
    }

    private FftValue[] getFftData() {
        List<Value> rawData = values.subList(values.size() - 256, values.size());

        double[] fftArray1 = new double[256];
        double[] fftArray2 = new double[256];
        double[] fftArray3 = new double[256];
        double[] fftArray4 = new double[256];

        for (int i = 0; i < 256; i++) {
            Value value = rawData.get(i);
            fftArray1[i] = value.getChannel1();
            fftArray2[i] = value.getChannel2();
            fftArray3[i] = value.getChannel3();
            fftArray4[i] = value.getChannel4();

        }

        FftValue[] fftValues = new FftValue[4];
        fftValues[0] = fft(fftArray1);
        fftValues[1] = fft(fftArray2);
        fftValues[2] = fft(fftArray3);
        fftValues[3] = fft(fftArray4);


        return fftValues;
    }


    private int maxIndex(double[] array, int start, int count) {
        double max = array[start];
        int index = start;
        for (int j = start; j < start + count; j++) {
            if (array[j] > max) {
                max = array[j];
                index = j;
            }
        }

        return index;
    }

    private synchronized FftValue fft(double[] data) {
        DoubleFFT_1D fftDo = new DoubleFFT_1D(data.length);
        double[] fft = new double[data.length * 2];
        System.arraycopy(data, 0, fft, 0, data.length);
        fftDo.complexForward(fft);


        double[] output = new double[128];
        for (int index = 0; index < 128; index++) {
            output[index] = Math.sqrt(fft[2 * index] * fft[2 * index] + fft[2 * index + 1] * fft[2 * index + 1]);
        }

        FftValue fftValue = new FftValue();

        fftValue.setData1(maxIndex(output, 3, 4));
        fftValue.setData2(maxIndex(output, 7, 7));
        fftValue.setData3(maxIndex(output, 14, 11));

        return fftValue;

    }

    /**
     * Starts a scan for Brainiac devices.
     *
     * @param onConnectCallback - These callbacks may get called at any time, when connected to a brainiac device
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startScan(final OnConnectCallback onConnectCallback) {
        this.onConnectCallback = onConnectCallback;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdapter.startLeScan(this);
        } else {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(new ScanFilter.Builder().setDeviceName(DEVICE_NAME).build());
            ScanCallback scanCallback = new ScanCallback() {

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    onConnectCallback.onConnectFailed();
                }

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (result.getDevice() != null && DEVICE_NAME.equals(result.getDevice().getName())) {
                        bluetoothDevice = result.getDevice();
                        stopScan();
                        connectToDevice();
                    }
                }
            };
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        }


    }


    /**
         * Indicates whether BrainiacManager connected to device.
     *
     * @return true if instance connected to device, false otherwise
     */
    public boolean isConnected() {
        return bluetoothDevice != null;
    }


    @Override
    public final void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        if (DEVICE_NAME.equals(bluetoothDevice.getName())) {
            this.bluetoothDevice = bluetoothDevice;
//            stopScan();

            connectToDevice();
        }
    }


    /**
     * Stops an ongoing Bluetooth LE device scan.
     */
    public void stopScan() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdapter.stopLeScan(this);
        } else {

        }
    }

    private void connectToDevice() {
        try {
//            onConnectCallback.onConnectSuccess();
            bluetoothDevice.connectGatt(context, false, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public final void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt = gatt;
                bluetoothGatt.discoverServices();
            }
        }
    }


    @Override
    public final void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    gatt.setCharacteristicNotification(characteristic, true);
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bluetoothGatt.writeDescriptor(descriptor);
                    }
                }
            }
        }
    }

    @Override
    public final void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        Log.d("onCharacteristicChanged", Arrays.toString(data));
        Value firstValue = new Value();

        short orderNumber = BitUtils.getShortFromLittleBytes(data[0], data[1]);
        firstValue.setHardwareOrderNumber(orderNumber);
        firstValue.setTimeframe(new Date().getTime());
        firstValue.setChannel1(Math.floor(K * BitUtils.getShortFromBigBytes(data[3], data[4])));
        firstValue.setChannel2(Math.floor(K * BitUtils.getShortFromBigBytes(data[5], data[6])));
        firstValue.setChannel3(Math.floor(K * BitUtils.getShortFromBigBytes(data[7], data[8])));
        firstValue.setChannel4(Math.floor(K * BitUtils.getShortFromBigBytes(data[9], data[10])));
        BrainiacManager.values.add(firstValue);


        Value secondValue = new Value();
        secondValue.setHardwareOrderNumber(orderNumber + 1);
        secondValue.setTimeframe(new Date().getTime());
        secondValue.setChannel1(Math.floor(K * BitUtils.getShortFromBigBytes(data[12], data[13])));
        secondValue.setChannel2(Math.floor(K * BitUtils.getShortFromBigBytes(data[14], data[15])));
        secondValue.setChannel3(Math.floor(K * BitUtils.getShortFromBigBytes(data[16], data[17])));
        secondValue.setChannel4(Math.floor(K * BitUtils.getShortFromBigBytes(data[18], data[19])));
        BrainiacManager.values.add(secondValue);
        if (onReceiveDataCallback != null) {
            onReceiveDataCallback.onReceiveData(firstValue);
            onReceiveDataCallback.onReceiveData(secondValue);
        }
        if (onReceiveFftDataCallback != null && (BrainiacManager.values.size() % 256) == 0) {
            onReceiveFftDataCallback.onReceiveData(getFftData());
        }

    }

    /**
     *  Release all using resources
     */
    public void release() {
        stopScan();
        bluetoothAdapter.disable();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
        values.clear();
    }

}
