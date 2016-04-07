package net.neutrinosoft.brainiac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import net.neutrinosoft.brainiac.callback.OnDeviceCallback;
import net.neutrinosoft.brainiac.callback.OnIndicatorsStateChangedCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveDataCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveFftDataCallback;
import net.neutrinosoft.brainiac.callback.OnScanCallback;
import net.neutrinosoft.brainiac.utils.PermissionsUtils;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * The BrainiacManager class handles connections and data transfer between Braniac (alpha title) accessory and Android device.
 */
public class BrainiacManager extends BluetoothGattCallback {

    public static final String TAG = BrainiacManager.class.getSimpleName();

    private static final String DEVICE_NAME = "NeuroBLE";
    private static BrainiacManager singleton;
    double VRef = 2.4 / 6.0 / 32.0;
    double K = 1000000000 * VRef / 0x7FFF;


    private static ArrayList<Value> values = new ArrayList<>();
    private static ArrayList<FftValue[]> fftValues = new ArrayList<>();
    private static int batteryLevel = 0;
    private Activity context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice neuroBLE;
    private BluetoothGatt bluetoothGatt;
    private OnReceiveFftDataCallback onReceiveFftDataCallback;
    private OnDeviceCallback onDeviceCallback;
    private OnScanCallback onScanCallback;
    private OnIndicatorsStateChangedCallback onIndicatorsStateChangedCallback;
    private boolean isTestMode;

    private OnReceiveDataCallback onReceiveDataCallback;
    private Handler handler;
    private Runnable testCallback, indicatorsCallBack;

    private final static String TRANSFER_CHARACTERISTIC_UUID = "6E400002-B534-f393-67a9-e50e24dcca9e";
    private final static String BATTERY_LEVEL_CHARACTERISTIC_UUID = "00000000-0000-0000-0000-000000000000";

    private final int INDICATOR_PERIOD = 5;
    private final int BASIC_VALUES_PERIOD = 10;
    private double averageBasicAlpha;
    private double averageBasicBeta;
    private BasicValues basicValues;
    private ScanCallback scanCallback;
    private LeScanCallback leScanCallback;

    /**
     * Register a callback to be invoked when fft data received.
     *
     * @param onReceiveFftDataCallback - An implementation of OnReceiveFftDataCallback
     */
    public void setOnReceiveFftDataCallback(OnReceiveFftDataCallback onReceiveFftDataCallback) {
        this.onReceiveFftDataCallback = onReceiveFftDataCallback;
    }

    /**
     * Register a callback for device scanning events
     *
     * @param onScanCallback
     */
    public void setOnScanCallback(OnScanCallback onScanCallback) {
        this.onScanCallback = onScanCallback;
    }

    /**
     * Register a callback for device connecting events
     *
     * @param onDeviceCallback
     */
    public void setOnDeviceCallback(OnDeviceCallback onDeviceCallback) {
        this.onDeviceCallback = onDeviceCallback;
    }

    /**
     * Get BrainiacManager singleton.
     *
     * @param context - application context
     * @return instance of BrainiacManager
     */
    public static BrainiacManager getBrainiacManager(Activity context) {
        if (singleton == null) {
            singleton = new BrainiacManager(context);
        }
        return singleton;
    }

    private BrainiacManager(Activity context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Register a callback to be invoked when raw data received.
     *
     * @param onReceiveDataCallback - An implementation of OnReceiveDataCallback
     */
    public void setOnReceiveDataCallback(OnReceiveDataCallback onReceiveDataCallback) {
        this.onReceiveDataCallback = onReceiveDataCallback;
    }

    public void setOnIndicatorsStateChangedCallback(OnIndicatorsStateChangedCallback onIndicatorsStateChangedCallback) {
        this.onIndicatorsStateChangedCallback = onIndicatorsStateChangedCallback;
    }

    private FftValue[] getFftData() {
        List<Value> rawData = values.subList(values.size() - 250, values.size());

        double[] fftArray1 = new double[256];
        double[] fftArray2 = new double[256];
        double[] fftArray3 = new double[256];
        double[] fftArray4 = new double[256];

        for (int i = 0; i < 256; i++) {
            if (i < 250) {
                Value value = rawData.get(i);
                fftArray1[i] = value.getChannel1();
                fftArray2[i] = value.getChannel2();
                fftArray3[i] = value.getChannel3();
                fftArray4[i] = value.getChannel4();
            } else {
                fftArray1[i] = 0;
                fftArray2[i] = 0;
                fftArray3[i] = 0;
                fftArray4[i] = 0;
            }

        }

        long timeframe = new Date().getTime();

        FftValue[] fftValues = new FftValue[4];
        fftValues[0] = fft(fftArray1);
        fftValues[1] = fft(fftArray2);
        fftValues[2] = fft(fftArray3);
        fftValues[3] = fft(fftArray4);

        for (FftValue fftValue : fftValues) {
            fftValue.setTimeframe(timeframe);
            fftValue.setCounter(BrainiacManager.fftValues.size());
        }

        BrainiacManager.fftValues.add(fftValues);

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
     */
    public void startScan() {
        Log.d(TAG, "startScan()");
        if (onScanCallback != null) {
            onScanCallback.onScanStart();
        }
        values.clear();
        fftValues.clear();
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
            if (PermissionsUtils.isCoarseLoactionAllowed(context)) {
                initScanCallback();
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
                }
            } else {
                Log.d(TAG, "Location permission does not allowed");
                Toast toast = Toast.makeText(context, "Location permission does not allowed", Toast.LENGTH_SHORT);
                stopScan();
            }
        } else {
            initLeScanCallback();
            if (bluetoothAdapter != null) {
                bluetoothAdapter.startLeScan(leScanCallback);
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
            leScanCallback = new LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    onDeviceFound(device);
                }
            };
        }
    }

    /**
     * Indicates whether BrainiacManager connected to device.
     *
     * @return true if instance connected to device, false otherwise
     */
    public boolean isConnected() {
        return neuroBLE != null;
    }

    /**
     * Indicates whether BrainiacManager launched in test mode.
     *
     * @return true if instance is in test mode, false otherwise
     */
    public boolean isInTestMode() {
        return isTestMode;
    }

    public void onDeviceFound(BluetoothDevice bluetoothDevice) {
        String deviceName = bluetoothDevice.getName();
        Log.d(TAG, "onLeScan()");
        Log.d(TAG, deviceName);
        Log.d(TAG, bluetoothDevice.getAddress());
        Log.d(TAG, Arrays.toString(bluetoothDevice.getUuids()));
        if (DEVICE_NAME.equals(deviceName)) {
            this.neuroBLE = bluetoothDevice;
            if (onDeviceCallback != null) {
                onDeviceCallback.onDeviceFound(bluetoothDevice);
            }
            stopScan();
            connectToDevice();
        }
    }

    /**
     * Stops an ongoing Bluetooth LE device scan.
     */
    public void stopScan() {
        if (onScanCallback != null) {
            onScanCallback.onScanStop();
        }
        if (bluetoothAdapter != null) {
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            } else {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        }

    }

    private void connectToDevice() {
        Log.d(TAG, "connectToDevice()");
        if (onDeviceCallback != null) {
            onDeviceCallback.onDeviceConnecting(neuroBLE);
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        neuroBLE.connectGatt(context, false, BrainiacManager.this);
            }
        });
    }


    @Override
    public final void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "onConnectionStateChange()");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothGatt.STATE_CONNECTING) {
                if (onDeviceCallback != null) {
                    onDeviceCallback.onDeviceConnecting(neuroBLE);
                }
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (onDeviceCallback != null) {
                    onDeviceCallback.onDeviceConnected(neuroBLE);
                }
                bluetoothGatt = gatt;
                bluetoothGatt.discoverServices();
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTING) {
                if (onDeviceCallback != null) {
                    onDeviceCallback.onDeviceConnecting(neuroBLE);
                }
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                if (onDeviceCallback != null) {
                    onDeviceCallback.onDeviceDisconnected(neuroBLE);
                }
            }
        } else {
            if (onDeviceCallback != null) {
                onDeviceCallback.onDeviceConnectionError(neuroBLE);
                if (onIndicatorsStateChangedCallback != null) {
                    disableIndicators();
                }
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
        } else {
            if (onDeviceCallback != null) {
                onDeviceCallback.onDeviceConnectionError(neuroBLE);
            }
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public final void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(TRANSFER_CHARACTERISTIC_UUID))) {
            byte[] data = characteristic.getValue();

            Log.d("Data received", Arrays.toString(data));
            short orderNumber = BitUtils.getShortFromBigBytes(data[0], data[1]);

            if (values.size() > 2048) {
                BrainiacManager.values.clear();
            }

            Value firstValue = new Value();
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
        } else if (characteristic.getUuid().equals(UUID.fromString(BATTERY_LEVEL_CHARACTERISTIC_UUID))) {
            batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        }

    }

    /**
     * Returns battery level
     *
     * @return battery level
     */
    public int getBatteryLevel() {
        return batteryLevel;
    }

    private static int average(List<Integer> list) {
        // 'average' is undefined if there are no elements in the list.
        if (list == null || list.isEmpty())
            return 0;
        // Calculate the summation of the elements in the list
        long sum = 0;
        int n = list.size();
        // Iterating manually is faster than using an enhanced for loop.
        for (int i = 0; i < n; i++)
            sum += list.get(i);
        // We don't want to perform an integer division, so the cast is mandatory.
        return (int) (sum / n);
    }

    /**
     * Release all using resources
     */
    public void release() {
        stopScan();
        if (bluetoothGatt != null) {
            if (onDeviceCallback != null) {
                onDeviceCallback.onDeviceDisconnecting(neuroBLE);
            }
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
            bluetoothGatt.close();
            bluetoothGatt.disconnect();
            neuroBLE = null;
                }
            });
        }
        values.clear();
    }

    /**
     * Starts sending test data values via delegate methods (without using hardware accessory).
     * Starts dispatching data via delegate methods immediately.
     * Use this method to test correct data receiving sequences and draw sample data plots.
     *
     * @param frequency value of testing frequency
     */

    public void startTest(final int frequency) {
        isTestMode = true;
        handler = new Handler();
        testCallback = new Runnable() {
            @Override
            public void run() {
                short orderNumber = (short) values.size();

                double d = 100000 * Math.sin(2 * Math.PI * frequency * orderNumber / 250);
                Value value = new Value();
                value.setCounter(orderNumber);
                value.setTimeframe(new Date().getTime());
                value.setChannel1(d);
                value.setChannel2(d);
                value.setChannel3(d);
                value.setChannel4(d);

                values.add(value);
                if (onReceiveDataCallback != null) {
                    onReceiveDataCallback.onReceiveData(value);
                }
                if (onReceiveFftDataCallback != null && (BrainiacManager.values.size() % 256) == 0) {
                    onReceiveFftDataCallback.onReceiveData(getFftData());
                }
                handler.postDelayed(this, 4);
            }
        };
        handler.postDelayed(testCallback, 4);

    }

    /**
     * Stop sending test data
     */
    public void stopTest() {
        values.clear();
        fftValues.clear();
        if (isTestMode) {
            isTestMode = false;
            handler.removeCallbacks(testCallback);
        }
    }

    public boolean enableIndicators() {
        if (fftValues.size() > INDICATOR_PERIOD) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startIndicators();
                }
            }, BASIC_VALUES_PERIOD * 1000);
            return true;
        } else {
            return false;
        }
    }

    public void disableIndicators() {
        if (onIndicatorsStateChangedCallback != null) {
            handler.removeCallbacks(indicatorsCallBack);
        }
    }

    private void startIndicators() {
        initIndicators();
        handler = new Handler();
        indicatorsCallBack = new Runnable() {
            @Override
            public void run() {
                if (onIndicatorsStateChangedCallback != null && fftValues.size() % INDICATOR_PERIOD == 0) {
                    onIndicatorsStateChangedCallback.onIndicatorsStateChanged(getIndicatorsState());
                }
                handler.post(this);
            }
        };
        handler.post(indicatorsCallBack);
    }

    private void initIndicators() {
        double[] averages = defineBasicAverageValuesForRange(BASIC_VALUES_PERIOD);

        averageBasicAlpha = averages[0];
        averageBasicBeta = averages[1];

        fillStartXYValues();
    }

    private double[] defineBasicAverageValuesForRange(int range) {
        double[] averages = new double[2];
        if (fftValues.size() > range) {
            List<Integer> alpha2 = new ArrayList<>();
            for (int i = (fftValues.size() - range); i < fftValues.size(); i++) {
                if (fftValues.get(i)[1].getCounter() == 0) {
                    return null;
                }
                alpha2.add(fftValues.get(i)[1].getData2());
                Log.d(TAG, "alpha " + fftValues.get(i)[1].getData2());
            }

            List<Integer> alpha4 = new ArrayList<>();
            for (int i = (fftValues.size() - range); i < fftValues.size(); i++) {
                if (fftValues.get(i)[3].getCounter() == 0) {
                    return null;
                }
                alpha4.add(fftValues.get(i)[3].getData2());
                Log.d(TAG, "alpha " + fftValues.get(i)[3].getData2());
            }

            List<Integer> beta1 = new ArrayList<>();
            for (int i = (fftValues.size() - range); i < fftValues.size(); i++) {
                if (fftValues.get(i)[0].getCounter() == 0) {
                    return null;
                }
                beta1.add(fftValues.get(i)[0].getData3());
                Log.d(TAG, "beta " + fftValues.get(i)[0].getData3());
            }

            List<Integer> beta3 = new ArrayList<>();
            for (int i = (fftValues.size() - range); i < fftValues.size(); i++) {
                if (fftValues.get(i)[2].getCounter() == 0) {
                    return null;
                }
                beta3.add(fftValues.get(i)[2].getData3());
                Log.d(TAG, "beta " + fftValues.get(i)[2].getData3());
            }

            double averageAlpha2 = doubleAverage(alpha2);
            double averageAlpha4 = doubleAverage(alpha4);
            double averageBeta1 = doubleAverage(beta1);
            double averageBeta3 = doubleAverage(beta3);
            averages[0] = (averageAlpha2 + averageAlpha4) / 2;
            averages[1] = (averageBeta1 + averageBeta3) / 2;
            return averages;
        }
        return null;
    }

    private double doubleAverage(List<Integer> array) {
        if (array == null || array.isEmpty()) {
            return 0;
        }

        double sum = 0;

        for (int elem : array) {
            sum += elem;
        }
        return sum / array.size();
    }

    public IndicatorsState getIndicatorsState() {
        double[] averages = defineBasicAverageValuesForRange(INDICATOR_PERIOD);

        if (averages == null) {
            return null;
        }
        return new IndicatorsState(processXYValues(averages), processColorIndicators(averages));
    }

    private List<String> processColorIndicators(double[] averages) {
        double X = averages[0];
        double Y = averages[1];

        double X0 = basicValues.getX0();
        double Y0 = basicValues.getY0();
        Log.d(TAG, "X = " + X + "  Y = " + Y + ", X0 = " + X0 + "  Y0 = " + Y0);
        List<String> colors = new ArrayList<>();

        if ((0.7 * X0 <= X && X <= 1.3 * X0 && 0 <= Y && Y <= 1.25 * Y0) || (1.3 * X0 < X && X <= 1.6 * X0 && Y0 <= Y && Y < 1.25 * Y0) || (X > 1.6 * X0 && Y > Y0) || (0 < X && X <= 0.7 * X0 && Y0 > Y && Y > 0.75 * Y0)) {
            colors.add("green");
        }
        if (((1.3 * X0) < X && X <= (1.6 * X0) && 0 < Y && Y <= Y0) || (0 < X && X <= (0.7 * X0) && 0 < Y && Y <= (0.75 * Y0))) {
            colors.add("yellow");
        }
        if ((0 < X && X <= 0.7 * X0 && Y0 < Y) || (1.3 * X0 < X && X <= 1.6 * X0 && Y >= 1.25 * Y0)) {
            colors.add("red1");
        }
        if (X > 1.6 * X0 && 0.75 * 0 < Y && Y <= Y0) {
            colors.add("red2");
        }

        return colors;
    }

    private UserActivity processXYValues(double[] averages) {
        double X = averages[0];
        double Y = averages[1];

        double X0 = basicValues.getX0();
        double Y0 = basicValues.getY0();
        double X1p = basicValues.getX1p();
        double X1m = basicValues.getX1m();
        double Y1p = basicValues.getX1p();
        double Y1m = basicValues.getY1m();
        double X2p = basicValues.getX2p();
        double X2m = basicValues.getX2m();
        double Y2p = basicValues.getY2p();
        double Y2m = basicValues.getY2m();
        double X3p = basicValues.getX3p();
        double X3m = basicValues.getX3m();
        double Y3p = basicValues.getY3p();
        double Y3m = basicValues.getY3m();
        double X4p = basicValues.getX4p();
        double X4m = basicValues.getX4m();
        double Y4p = basicValues.getY4p();
        double Y4m = basicValues.getY4m();

        ManagerActivityZone activityZone = ManagerActivityZone.NormalActivity;
        float percent = 1.0F;
        if (X > X0) {
            //positive values
            if (X1p >= X && X > X0 && Y >= Y0) {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 0.25F;
            }
            if (X1p >= X && X > X0 && Y >= Y1p && Y0 >= Y) {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 0.5F;
            }
            if (X1p >= X && X > X0 && Y1p >= Y) {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 0.75F;
            }
            if (X2p >= X && X > X1p && Y0 >= Y && Y >= Y2p) {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 1;
            }
            if (X3p >= X && X > X2p && Y >= Y0) {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 0.25F;
            }
            if (X3p >= X && X > X2p && Y0 >= Y && Y >= Y3p) {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 0.5F;
            }
            if (X3p >= X && X > X2p && Y3p >= Y) {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 0.75F;
            }
            if (X4p >= X && X > X3p && Y0 >= Y && Y >= Y4p) {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 1;
            }
            if (X >= X4p) {
                activityZone = ManagerActivityZone.Dream;
                percent = 0.5F;
            }

        } else {
            //negative values
            if (X1m < X && X <= X0 && Y < Y0) {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 0.25F;
            }
            if (X1m < X && X <= X0 && Y0 <= Y && Y <= Y1m) {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 0.5F;
            }
            if (X1m < X && X <= X0 && Y1m < Y) {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 0.75F;
            }
            if (X2m < X && X <= X1m && Y0 < Y && Y <= Y2m) {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 1;
            }
            if (X3m < X && X <= X2m && Y < Y0) {
                activityZone = ManagerActivityZone.Agitation;
                percent = 0.25F;
            }
            if (X3m < X && X <= X2m && Y0 < Y && Y <= Y3m) {
                activityZone = ManagerActivityZone.Agitation;
                percent = 0.5F;
            }
            if (X3m < X && X <= X2m && Y3m < Y) {
                activityZone = ManagerActivityZone.Agitation;
                percent = 0.75F;
            }
            if (X4m < X && X <= X3m && Y0 < Y && Y <= Y4m) {
                activityZone = ManagerActivityZone.Agitation;
                percent = 1;
            }
            if (0 < X && X < 0.1 * X0) {
                activityZone = ManagerActivityZone.HighAgitation;
                percent = 0.5F;
            }
        }
        return new UserActivity(activityZone, percent);
    }

    private void fillStartXYValues() {
        double X0 = averageBasicAlpha;
        double Y0 = averageBasicBeta;
        double X1p = 1.3 * X0;
        double X1m = 0.65 * (X0 - 0.1 * X0);
        double Y1p = 0.9 * Y0;
        double Y1m = 1.1 * Y0;
        double X2p = 1.45 * X0;
        double X2m = 0.45 * (X0 - 0.1 * X0);
        double Y2p = 0.85 * Y0;
        double Y2m = 1.15 * Y0;
        double X3p = 1.55 * X0;
        double X3m = 0.2 * (X0 - 0.1 * X0);
        double Y3p = 0.8 * Y0;
        double Y3m = 1.25 * Y0;
        double X4p = 1.7 * X0;
        double X4m = (X0 - 0.1 * X0);
        double Y4p = 0.7 * Y0;
        double Y4m = 1.3 * Y0;

        basicValues = new BasicValues(X0, Y0, X1p, X1m, Y1p, Y1m, X2p, X2m, Y2p, Y2m, X3p, X3m, Y3p, Y3m, X4p, X4m, Y4p, Y4m);

    }

}
