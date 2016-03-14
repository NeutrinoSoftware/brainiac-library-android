package net.neutrinosoft.brainiac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import net.neutrinosoft.brainiac.callback.OnDeviceCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveDataCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveFftDataCallback;
import net.neutrinosoft.brainiac.callback.OnScanCallback;
import net.neutrinosoft.brainiac.common.ManagerActivityZone;

import org.jtransforms.fft.DoubleFFT_1D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * The BrainiacManager class handles connections and data transfer between Braniac (alpha title) accessory and Android device.
 */
public class BrainiacManager extends BluetoothGattCallback implements BluetoothAdapter.LeScanCallback {

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
    private boolean isTestMode;

    private OnReceiveDataCallback onReceiveDataCallback;
    private int lastGreenValue = 0;
    private double lastGreenAverage = 0;
    private final static int STEP = 10;
    private final static int TIMESPAN = 180;
    private final static double YELLOW_FLAG_LOW = 0.2;
    private final static double YELLOW_FLAG_HIGH = 0.3;
    private final static double YELLOW_DIFF_LOW = YELLOW_FLAG_LOW / (TIMESPAN / STEP);
    private final static double YELLOW_DIFF_HIGH = YELLOW_FLAG_HIGH / (TIMESPAN / STEP);

    private final static double RED_1_FLAG = 0.2;
    private final static double RED_2_FLAG = 0.3;
    private final static double RED_1_DIFF_HIGH = RED_1_FLAG / (TIMESPAN / STEP);
    private final static double RED_2_DIFF_HIGH = RED_2_FLAG / (TIMESPAN / STEP);
    private Handler handler, indicatorsHandler;
    private Runnable testCallback, indicatorsCallBack;

    private final static String TRANSFER_CHARACTERISTIC_UUID = "6E400002-B534-f393-67a9-e50e24dcca9e";
    private final static String BATTERY_LEVEL_CHARACTERISTIC_UUID = "00000000-0000-0000-0000-000000000000";

    private boolean hasStartedIndicators = false;
    private boolean hasStartedProcessBasicValues = false;
    private final int INDICATOR_PERIOD = 5;
    private final int BASIC_VALUES_PERIOD = 10;
    double averageBasicTeta;
    double averageBasicAlpha;
    double averageBasicBeta;
    List<XYValue> basicValues;


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
        basicValues = new ArrayList<>();
    }

    /**
     * Register a callback to be invoked when raw data received.
     *
     * @param onReceiveDataCallback - An implementation of OnReceiveDataCallback
     */
    public void setOnReceiveDataCallback(OnReceiveDataCallback onReceiveDataCallback) {
        this.onReceiveDataCallback = onReceiveDataCallback;
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
        bluetoothAdapter.startLeScan(this);

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

    @Override
    public final void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        Log.d(TAG, "onLeScan()");
        Log.d(TAG, bluetoothDevice.getName());
        Log.d(TAG, bluetoothDevice.getAddress());
        Log.d(TAG, Arrays.toString(bluetoothDevice.getUuids()));
        if (DEVICE_NAME.equals(bluetoothDevice.getName())) {
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
        if(bluetoothAdapter!=null) {
            bluetoothAdapter.stopLeScan(this);
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

    /**
     * Returns flag which define the state of examined man detecting if the state of brain activity is full and active.
     * Defined as: When closing the eyes or with simple contemplation of neutral images dominant frequency in the range of alpha (7-13 Hz)
     * has no oscillations more than 20% for 3 minutes during the registration process.
     *
     * @param channel Number for processing channel (0-3)
     * @return flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)
     */
    /*public boolean processGreenChannel(int channel) {
        int length = BrainiacManager.fftValues.size();
        ArrayList<FftValue[]> fft = BrainiacManager.fftValues;

        ArrayList<Integer> greens = new ArrayList<>();

        for (int i = lastGreenValue; i < length; i++) {
            FftValue[] fftValues = fft.get(i);
            greens.add(fftValues[channel].getData2());
        }

        int average = average(greens);

        for (int i = 0; i < (length - lastGreenValue); i++) {
            int value = greens.get(i);
            if (Math.abs((lastGreenValue - value)) > (lastGreenValue * 0.2)) {
                lastGreenAverage = (lastGreenAverage + average) / 2.0;
                lastGreenValue = length;
                return false;

            }
        }

        lastGreenAverage = (lastGreenAverage + average) / 2.0;
        lastGreenValue = length;

        return true;
    }*/

    /**
     * Returns flag which define the state of examined man detecting if the state of brain activity is
     * Relaxation brain activity (EEG spectrum for simply “nice” relaxation, during which the person can not adequately drive or write software).
     * Defined as: the dominant frequency in the range of alpha (7-13 Hz) increases in amplitude (power spectrum) on greater than 20% but
     * less than 30% within 3 minutes.
     *
     * @param channel Number for processing channel (0-3)
     * @return flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)
     */
    /*public boolean processYellowForChannel(int channel) {
        int length = fftValues.size();
        ArrayList<FftValue[]> fft = BrainiacManager.fftValues;


        if (length >= STEP) {
            ArrayList<Integer> yellows = new ArrayList<>();
            ArrayList<Integer> yellowsUpper = new ArrayList<>();
            ArrayList<Integer> yellowsLower = new ArrayList<>();

            for (int i = length - STEP; i < length; i++) {
                FftValue[] fftValues = fft.get(i);
                yellowsLower.add(fftValues[channel].getData1());
                yellows.add(fftValues[channel].getData2());
                yellowsUpper.add(fftValues[channel].getData3());
            }

            for (int i = length - STEP; i < length; i++) {
                FftValue[] fftValues = fft.get(i);
                yellowsUpper.add(fftValues[channel].getData3());
            }

            int val1 = yellows.get(0);
            int val2 = yellows.get(STEP - 1);

            int val1lower = yellowsLower.get(0);
            int val2lower = yellowsLower.get(STEP - 1);

            int val1upper = yellowsUpper.get(0);
            int val2upper = yellowsUpper.get(STEP - 1);

            if (val2 > val1) {
                boolean mainCondition = (val2 - val1) > YELLOW_DIFF_LOW && (val2 - val1) < YELLOW_DIFF_HIGH;
                boolean lowCondition = val2lower > val1lower && (val2lower - val1lower) > (0.1 / (TIMESPAN / STEP));
                boolean highCondition = val2upper < val1upper && (val1upper - val2upper) > (0.05 / (TIMESPAN / STEP));


                return mainCondition && lowCondition && highCondition;
            } else {
                return false;
            }

        }

        return false;
    }*/

    /**
     * Returns flag which define the state of examined man detecting if the state of brain activity is Excessive stimulation of neurons and therefore
     * the beginning of inappropriate, excessive actions. Defined as: the dominant frequency (range) of alpha (7-13 Hz) is reduced in amplitude (power spectrum)
     * on greater than 20% for 3 minutes.
     *
     * @param channel Number for processing channel (0-3)
     * @return flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)
     */
    /*public boolean processRed1ForChannel(int channel) {
        int length = fftValues.size();
        ArrayList<FftValue[]> fft = BrainiacManager.fftValues;

        if (length >= STEP) {
            ArrayList<Integer> reds = new ArrayList<>();
            ArrayList<Integer> redsUpper = new ArrayList<>();
            ArrayList<Integer> redsLower = new ArrayList<>();

            for (int i = length - STEP; i < length; i++) {
                FftValue[] fftValues = fft.get(i);
                redsLower.add(fftValues[channel].getData1());
                reds.add(fftValues[channel].getData2());
                redsUpper.add(fftValues[channel].getData3());
            }


            int val1 = reds.get(0);
            int val2 = reds.get(STEP - 1);

            int val1lower = redsLower.get(0);
            int val2lower = redsLower.get(STEP - 1);

            int val1upper = redsUpper.get(0);
            int val2upper = redsUpper.get(STEP - 1);

            if (val2 < val1) {
                boolean mainCondition = (val1 - val2) > RED_1_DIFF_HIGH;
                boolean lowCondition = val2lower < val1lower && (val1lower - val2lower) > (0.1 / (TIMESPAN / STEP));
                boolean highCondition = val2upper > val1upper && (val2upper - val1upper) > (0.05 / (TIMESPAN / STEP));

                return mainCondition && lowCondition && highCondition;
            } else {
                return false;
            }

        }

        return false;


    }*/

    /**
     * Returns flag which define the state of examined man detecting if the state of brain activity is in super relaxation.
     * Defined as: the dominant frequency (range) of alpha (7-13 Hz) is increasing in amplitude (power spectrum) on greater than 30% for 3 minutes.
     *
     * @param channel Number for processing channel (0-3)
     * @return flag for such activity for last 5 sec (so app should call this method each 5 sec to get trend activity)
     */
    /*public boolean processRed2ForChannel(int channel) {
        int length = fftValues.size();
        ArrayList<FftValue[]> fft = BrainiacManager.fftValues;

        if (length >= STEP) {
            ArrayList<Integer> reds = new ArrayList<>();
            ArrayList<Integer> redsUpper = new ArrayList<>();
            ArrayList<Integer> redsLower = new ArrayList<>();

            for (int i = length - STEP; i < length; i++) {
                FftValue[] fftValues = fft.get(i);
                redsLower.add(fftValues[channel].getData1());
                reds.add(fftValues[channel].getData2());
                redsUpper.add(fftValues[channel].getData3());
            }


            int val1 = reds.get(0);
            int val2 = reds.get(STEP - 1);

            int val1lower = redsLower.get(0);
            int val2lower = redsLower.get(STEP - 1);

            int val1upper = redsUpper.get(0);
            int val2upper = redsUpper.get(STEP - 1);

            if (val2 < val1) {
                boolean mainCondition = (val1 - val2) > RED_2_DIFF_HIGH;
                boolean lowCondition = val2lower < val1lower && (val1lower - val2lower) > (0.15 / (TIMESPAN / STEP));
                boolean highCondition = val2upper > val1upper && (val2upper - val1upper) > (0.15 / (TIMESPAN / STEP));

                return mainCondition && lowCondition && highCondition;
            } else {
                return false;
            }

        }

        return false;
    }*/


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

    public void enableIndicators() {
        indicatorsHandler = new Handler();
        indicatorsCallBack = new Runnable() {
            @Override
            public void run() {
                startIndicatorsProcessing();
                indicatorsHandler.postDelayed(this, 10000);
            }
        };
        indicatorsHandler.postDelayed(indicatorsCallBack, 10000);
        //startIndicatorsProcessing();
        //hasStartedProcessBasicValues = true;
    }

    public void disableIndicators() {
        indicatorsHandler.removeCallbacks(indicatorsCallBack);
    }

    private void startIndicatorsProcessing() {
        double[] averages = defineBasicAverageValuesForRange(BASIC_VALUES_PERIOD);

        if (averages == null) {
            return;
        }

        averageBasicAlpha = averages[0];
        averageBasicBeta = averages[1];

        fillStartXYValues();

        hasStartedIndicators = true;
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

            double averageAlpha2 = getArrayAverage(alpha2);
            double averageAlpha4 = getArrayAverage(alpha4);
            double averageBeta1 = getArrayAverage(beta1);
            double averageBeta3 = getArrayAverage(beta3);
            averages[0] = (averageAlpha2 + averageAlpha4) / 2;
            averages[1] = (averageBeta1 + averageBeta3) / 2;
        }
        return null;
    }

    private double getArrayAverage(List<Integer> array) {
        double sum = 0;
        for (int elem: array){
            sum += elem;
        }
        return sum/array.size();
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

        XYValue basics = basicValues.get(0);
        double X0 = basics.getX0();
        double Y0 = basics.getY0();
        Log.d(TAG, "X = " + X + "  Y = " + Y + ", X0 = " + X0 + "  Y0 = " + Y0);
        List<String> colors = new ArrayList<>();

        if((0.7 * X0 <= X && X <= 1.3 * X0 && 0 <= Y && Y <= 1.25 * Y0) || (1.3 * X0 < X && X <= 1.6 * X0 && Y0 <= Y && Y < 1.25 * Y0) || ( X > 1.6 * X0 && Y > Y0) || (0 < X && X <= 0.7 * X0 && Y0 > Y && Y > 0.75 * Y0))
        {
            colors.add("green");
        }
        if(((1.3 * X0) < X && X <= (1.6 * X0) && 0 < Y && Y <= Y0) || ( 0 < X && X <= (0.7 * X0) && 0 < Y && Y <= (0.75 * Y0)))
        {
            colors.add("yellow");

        }
        if((0 < X && X <= 0.7 * X0 && Y0 < Y) || (1.3 * X0 < X && X <= 1.6 * X0 && Y >= 1.25 * Y0))
        {
            colors.add("red");

        }
        if(X > 1.6 * X0 && 0.75 * 0 < Y && Y <= Y0)
        {
            colors.add("orange");

        }

        return colors;
    }

    private UserActivity processXYValues(double[] averages) {
        double X = averages[0];
        double Y = averages[1];

        XYValue basics = basicValues.get(0);
        double X0 = basics.getX0();
        double Y0 = basics.getY0();
        double X1p = basics.getX1p();
        double X1m = basics.getX1m();
        double Y1p = basics.getX1p();
        double Y1m = basics.getY1m();
        double X2p = basics.getX2p();
        double X2m = basics.getX2m();
        double Y2p = basics.getY2p();
        double Y2m = basics.getY2m();
        double X3p = basics.getX3p();
        double X3m = basics.getX3m();
        double Y3p = basics.getY3p();
        double Y3m = basics.getY3m();
        double X4p = basics.getX4p();
        double X4m = basics.getX4m();
        double Y4p = basics.getY4p();
        double Y4m = basics.getY4m();

        ManagerActivityZone activityZone = ManagerActivityZone.NormalActivity;
        double percent = 1.0;
        if(X > X0)
        {
            //positive values
            if(X1p >= X && X > X0 && Y >= Y0)
            {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 0.25;
            }
            if(X1p >= X && X > X0 && Y >= Y1p && Y0 >= Y)
            {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 0.5;
            }
            if(X1p >= X && X > X0 && Y1p >= Y)
            {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 0.75;
            }
            if(X2p >= X && X > X1p && Y0 >= Y && Y >= Y2p)
            {
                activityZone = ManagerActivityZone.Relaxation;
                percent = 1;
            }
            if(X3p >= X && X > X2p && Y >= Y0)
            {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 0.25;
            }
            if(X3p >= X && X > X2p && Y0 >= Y && Y >= Y3p)
            {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 0.5;
            }
            if(X3p >= X && X > X2p && Y3p >= Y)
            {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 0.75;
            }
            if(X4p >= X && X > X3p && Y0 >= Y && Y >= Y4p)
            {
                activityZone = ManagerActivityZone.HighRelaxation;
                percent = 1;
            }
            if(X >= X4p)
            {
                activityZone = ManagerActivityZone.Dream;
                percent = 0.5;
            }

        }
        else
        {
            //negative values
            if(X1m < X && X <= X0 && Y < Y0)
            {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 0.25;
            }
            if(X1m < X && X <= X0 && Y0 <= Y && Y <= Y1m)
            {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 0.5;
            }
            if(X1m < X && X <= X0 && Y1m < Y)
            {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 0.75;
            }
            if(X2m < X && X <= X1m && Y0 < Y && Y <= Y2m)
            {
                activityZone = ManagerActivityZone.NormalActivity;
                percent = 1;
            }
            if(X3m < X && X <= X2m && Y < Y0)
            {
                activityZone = ManagerActivityZone.Agitation;
                percent = 0.25;
            }
            if(X3m < X && X <= X2m && Y0 < Y && Y <= Y3m)
            {
                activityZone = ManagerActivityZone.Agitation;
                percent = 0.5;
            }
            if(X3m < X && X <= X2m && Y3m < Y)
            {
                activityZone = ManagerActivityZone.Agitation;
                percent = 0.75;
            }
            if(X4m < X && X <= X3m && Y0 < Y && Y <= Y4m)
            {
                activityZone = ManagerActivityZone.Agitation;
                percent = 1;
            }
            if(0 < X && X < 0.1 * X0)
            {
                activityZone = ManagerActivityZone.HighAgitation;
                percent = 0.5;
            }
        }
        return new UserActivity(activityZone, percent);
    }

    void fillStartXYValues()
    {
        for(int i = 0; i < 1; i++)
        {
            double X0 = 0;
            double Y0 = 0;
            if (i == 0) {
                X0 = averageBasicAlpha;
                Y0 = averageBasicBeta;
            }
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

            basicValues.add(new XYValue((i+1), X0, Y0, X1p, X1m, Y1p, Y1m, X2p, X2m, Y2p, Y2m, X3p, X3m, Y3p, Y3m, X4p, X4m, Y4p, Y4m));
        }

    }

}
