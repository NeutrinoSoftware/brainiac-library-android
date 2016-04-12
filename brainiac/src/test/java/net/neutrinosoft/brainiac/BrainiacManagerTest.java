package net.neutrinosoft.brainiac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import net.neutrinosoft.brainiac.callback.OnDeviceCallback;
import net.neutrinosoft.brainiac.callback.OnIndicatorsStateChangedCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveDataCallback;
import net.neutrinosoft.brainiac.callback.OnReceiveFftDataCallback;
import net.neutrinosoft.brainiac.callback.OnScanCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BrainiacManager.class)
public class BrainiacManagerTest {

    @Mock
    private BluetoothAdapter bluetoothAdapter;

    private final static String TRANSFER_CHARACTERISTIC_UUID = "6E400002-B534-f393-67a9-e50e24dcca9e";
    private final static String BATTERY_LEVEL_CHARACTERISTIC_UUID = "00000000-0000-0000-0000-000000000000";

    private Activity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = new Activity();
    }


    @Test
    public void getBrainiacManager() throws Exception {
        BrainiacManager brainiacManager1 = BrainiacManager.getBrainiacManager(activity);
        BrainiacManager brainiacManager2 = BrainiacManager.getBrainiacManager(activity);
        //verify singleton
        assertSame(brainiacManager1, brainiacManager2);
    }

    @Test
    public void startScan() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        OnScanCallback onScanCallback = mock(OnScanCallback.class);
        brainiacManager.setOnScanCallback(onScanCallback);
        brainiacManager.startScan();
        BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        when(bluetoothDevice.getName()).thenReturn("NeuroBLE");
        OnDeviceCallback onDeviceCallback = mock(OnDeviceCallback.class);
        brainiacManager.setOnDeviceCallback(onDeviceCallback);
        ((OnDeviceCallback) getBrainiacManagerPrivateField(brainiacManager, "onDeviceFoundCallback")).onDeviceFound(bluetoothDevice);
    }

    @Test
    public void stopScan() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        OnScanCallback onScanCallback = mock(OnScanCallback.class);
        brainiacManager.setOnScanCallback(onScanCallback);
        brainiacManager.stopScan();
    }

    @Test
    public void onConnectionStateChangeConnecting() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        brainiacManager.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_CONNECTING);
    }

    @Test
    public void onConnectionStateChangeConnected() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        brainiacManager.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_CONNECTED);
    }

    @Test
    public void onConnectionStateChangeDisconnecting() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        brainiacManager.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_DISCONNECTING);
    }

    @Test
    public void onConnectionStateChangeDisconnected() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        brainiacManager.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothGatt.STATE_DISCONNECTED);
    }

    @Test
    public void onConnectionStateChangeFailure() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        OnIndicatorsStateChangedCallback onIndicatorsStateChangedCallback = mock(OnIndicatorsStateChangedCallback.class);
        brainiacManager.setOnIndicatorsStateChangedCallback(onIndicatorsStateChangedCallback);
        brainiacManager.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_FAILURE, BluetoothGatt.STATE_DISCONNECTED);
    }

    @Test
    public void onServicesDiscoveredSuccess() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        List<BluetoothGattService> services = new ArrayList<>();
        BluetoothGattService bluetoothGattService = mock(BluetoothGattService.class);
        services.add(bluetoothGattService);
        when(bluetoothGatt.getServices()).thenReturn(services);
        List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = new ArrayList<>();
        BluetoothGattCharacteristic bluetoothGattCharacteristic = mock(BluetoothGattCharacteristic.class);
        bluetoothGattCharacteristics.add(bluetoothGattCharacteristic);
        when(bluetoothGattService.getCharacteristics()).thenReturn(bluetoothGattCharacteristics);
        List<BluetoothGattDescriptor> bluetoothGattDescriptors = new ArrayList<>();
        bluetoothGattDescriptors.add(mock(BluetoothGattDescriptor.class));
        when(bluetoothGattCharacteristic.getDescriptors()).thenReturn(bluetoothGattDescriptors);
        setBrainiacManagerPrivateField(brainiacManager, "bluetoothGatt", bluetoothGatt);
        brainiacManager.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS);
    }

    @Test
    public void onServicesDiscoveredFailure() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        brainiacManager.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_FAILURE);
    }

    @Test
    public void onCharacteristicRead() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        brainiacManager.onCharacteristicRead(mock(BluetoothGatt.class), mock(BluetoothGattCharacteristic.class), BluetoothGatt.GATT_SUCCESS);
    }

    @Test
    public void onCharacteristicChanged() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = mock(BluetoothGattCharacteristic.class);
        when(bluetoothGattCharacteristic.getUuid()).thenReturn(UUID.fromString(TRANSFER_CHARACTERISTIC_UUID));
        when(bluetoothGattCharacteristic.getValue()).thenReturn(new byte[20]);
        ArrayList<Value> values = new ArrayList<Value>();
        for (int i = 0; i < 2049; i++) {
            values.add(new Value());
        }
        setBrainiacManagerPrivateField(brainiacManager, "values", values);
        OnReceiveDataCallback onReceiveDataCallback = mock(OnReceiveDataCallback.class);
        OnReceiveFftDataCallback onReceiveFftDataCallback = mock(OnReceiveFftDataCallback.class);
        brainiacManager.setOnReceiveDataCallback(onReceiveDataCallback);
        brainiacManager.setOnReceiveFftDataCallback(onReceiveFftDataCallback);
        brainiacManager.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
    }

    @Test
    public void onCharacteristicChangedCharacteristicFft() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = mock(BluetoothGattCharacteristic.class);
        when(bluetoothGattCharacteristic.getUuid()).thenReturn(UUID.fromString(TRANSFER_CHARACTERISTIC_UUID));
        when(bluetoothGattCharacteristic.getValue()).thenReturn(new byte[20]);
        ArrayList<Value> values = new ArrayList<Value>();
        for (int i = 0; i < 254; i++) {
            values.add(new Value());
        }
        setBrainiacManagerPrivateField(brainiacManager, "values", values);
        OnReceiveFftDataCallback onReceiveFftDataCallback = mock(OnReceiveFftDataCallback.class);
        brainiacManager.setOnReceiveFftDataCallback(onReceiveFftDataCallback);
        brainiacManager.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
    }

    @Test
    public void onCharacteristicChangedBatteryFft() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        BluetoothGattCharacteristic bluetoothGattCharacteristic = mock(BluetoothGattCharacteristic.class);
        when(bluetoothGattCharacteristic.getUuid()).thenReturn(UUID.fromString(BATTERY_LEVEL_CHARACTERISTIC_UUID));
        brainiacManager.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
    }

    @Test
    public void isConnected() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        brainiacManager.isConnected();
    }

    @Test
    public void isInTestMode() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        brainiacManager.isInTestMode();
    }

    @Test
    public void getBatteryLevel() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        brainiacManager.getBatteryLevel();
    }

    @Test
    public void release() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        OnDeviceCallback onDeviceCallback = mock(OnDeviceCallback.class);
        BluetoothGatt bluetoothGatt = mock(BluetoothGatt.class);
        brainiacManager.setOnDeviceCallback(onDeviceCallback);
        setBrainiacManagerPrivateField(brainiacManager, "bluetoothGatt", bluetoothGatt);
        brainiacManager.release();
    }

    @Test
    public void startTest() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        OnReceiveDataCallback onReceiveDataCallback = mock(OnReceiveDataCallback.class);
        OnReceiveFftDataCallback onReceiveFftDataCallback = mock(OnReceiveFftDataCallback.class);
        brainiacManager.setOnReceiveDataCallback(onReceiveDataCallback);
        brainiacManager.setOnReceiveFftDataCallback(onReceiveFftDataCallback);
        ArrayList<Value> values = new ArrayList<Value>();
        for (int i = 0; i < 255; i++) {
            values.add(new Value());
        }
        setBrainiacManagerPrivateField(brainiacManager, "values", values);
        brainiacManager.startTest(3);
        Runnable testCallback = (Runnable) getBrainiacManagerPrivateField(brainiacManager, "testCallback");
        testCallback.run();
    }

    @Test
    public void stopTest() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        brainiacManager.stopTest();
    }

    @Test
    public void enableIndicatorsTrue() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        List<FftValue> fftValues = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            fftValues.add(new FftValue());
        }
        setBrainiacManagerPrivateField(brainiacManager, "fftValues", fftValues);
        brainiacManager.enableIndicators();
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "enableIndicatorsCallback")).run();
        OnIndicatorsStateChangedCallback onIndicatorsStateChangedCallback = mock(OnIndicatorsStateChangedCallback.class);
        brainiacManager.setOnIndicatorsStateChangedCallback(onIndicatorsStateChangedCallback);
        fftValues.clear();
        for (int i = 0; i < 5; i++) {
            fftValues.add(new FftValue());
        }
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "indicatorsCallBack")).run();
    }

    @Test
    public void enableIndicatorsFalse() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        brainiacManager.enableIndicators();
    }

    @Test
    public void disableIndicators() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        OnIndicatorsStateChangedCallback onIndicatorsStateChangedCallback = mock(OnIndicatorsStateChangedCallback.class);
        brainiacManager.setOnIndicatorsStateChangedCallback(onIndicatorsStateChangedCallback);
        android.os.Handler handler = new android.os.Handler();
        setBrainiacManagerPrivateField(brainiacManager, "handler", handler);
        brainiacManager.disableIndicators();
    }

    @Test
    public void defineBasicAverageValuesForRangeCounterNullChannel1() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        ArrayList<FftValue[]> fftValues = new ArrayList<>();
        FftValue[] fft = new FftValue[4];
        FftValue fftValue = new FftValue();
        fftValue.setData1(1);
        fftValue.setData2(1);
        fftValue.setData3(1);
        fftValue.setCounter(1);
        fft[0] = new FftValue();
        fft[1] = fftValue;
        fft[2] = fftValue;
        fft[3] = fftValue;
        for (int i = 0; i < 15; i++) {
            fftValues.add(fft);
        }
        setBrainiacManagerPrivateField(brainiacManager, "fftValues", fftValues);
        brainiacManager.enableIndicators();
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "enableIndicatorsCallback")).run();
    }

    @Test
    public void defineBasicAverageValuesForRangeCounterNullChannel2() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        ArrayList<FftValue[]> fftValues = new ArrayList<>();
        FftValue[] fft = new FftValue[4];
        FftValue fftValue = new FftValue();
        fftValue.setData1(1);
        fftValue.setData2(1);
        fftValue.setData3(1);
        fftValue.setCounter(1);
        fft[0] = fftValue;
        fft[1] = new FftValue();
        fft[2] = fftValue;
        fft[3] = fftValue;
        for (int i = 0; i < 15; i++) {
            fftValues.add(fft);
        }
        setBrainiacManagerPrivateField(brainiacManager, "fftValues", fftValues);
        brainiacManager.enableIndicators();
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "enableIndicatorsCallback")).run();
    }

    @Test
    public void defineBasicAverageValuesForRangeCounterNullChannel3() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        ArrayList<FftValue[]> fftValues = new ArrayList<>();
        FftValue[] fft = new FftValue[4];
        FftValue fftValue = new FftValue();
        fftValue.setData1(1);
        fftValue.setData2(1);
        fftValue.setData3(1);
        fftValue.setCounter(1);
        fft[0] = fftValue;
        fft[1] = fftValue;
        fft[2] = new FftValue();
        fft[3] = fftValue;
        for (int i = 0; i < 15; i++) {
            fftValues.add(fft);
        }
        setBrainiacManagerPrivateField(brainiacManager, "fftValues", fftValues);
        brainiacManager.enableIndicators();
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "enableIndicatorsCallback")).run();
    }

    @Test
    public void defineBasicAverageValuesForRangeCounterNullChannel4() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        ArrayList<FftValue[]> fftValues = new ArrayList<>();
        FftValue[] fft = new FftValue[4];
        FftValue fftValue = new FftValue();
        fftValue.setData1(1);
        fftValue.setData2(1);
        fftValue.setData3(1);
        fftValue.setCounter(1);
        fft[0] = fftValue;
        fft[1] = fftValue;
        fft[2] = fftValue;
        fft[3] = new FftValue();
        for (int i = 0; i < 15; i++) {
            fftValues.add(fft);
        }
        setBrainiacManagerPrivateField(brainiacManager, "fftValues", fftValues);
        brainiacManager.enableIndicators();
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "enableIndicatorsCallback")).run();
    }

    @Test
    public void defineBasicAverageValuesForRangeCounterNotNull() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        ArrayList<FftValue[]> fftValues = new ArrayList<>();
        FftValue[] fft = new FftValue[4];
        FftValue fftValue = new FftValue();
        fftValue.setData1(1);
        fftValue.setData2(1);
        fftValue.setData3(1);
        fftValue.setCounter(1);
        fft[0] = fftValue;
        fft[1] = fftValue;
        fft[2] = fftValue;
        fft[3] = fftValue;
        for (int i = 0; i < 15; i++) {
            fftValues.add(fft);
        }
        setBrainiacManagerPrivateField(brainiacManager, "fftValues", fftValues);
        brainiacManager.enableIndicators();
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "enableIndicatorsCallback")).run();
    }

    @Test
    public void getIndicatorsState() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);
        ArrayList<FftValue[]> fftValues = new ArrayList<>();
        FftValue[] fft = new FftValue[4];
        FftValue fftValue = new FftValue();
        fftValue.setData1(1);
        fftValue.setData2(1);
        fftValue.setData3(1);
        fftValue.setCounter(1);
        fft[0] = fftValue;
        fft[1] = fftValue;
        fft[2] = fftValue;
        fft[3] = fftValue;
        for (int i = 0; i < 15; i++) {
            fftValues.add(fft);
        }
        setBrainiacManagerPrivateField(brainiacManager, "fftValues", fftValues);
        brainiacManager.enableIndicators();
        ((Runnable) getBrainiacManagerPrivateField(brainiacManager, "enableIndicatorsCallback")).run();
        brainiacManager.getIndicatorsState();
    }

    @Test
    public void processColorIndicatorsGreen() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 5};
        BasicValues basicValues = new BasicValues(5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processColorIndicators", averages);
    }

    @Test
    public void processColorIndicatorsYellow() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 1};
        BasicValues basicValues = new BasicValues(7D, 7D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processColorIndicators", averages);
    }

    @Test
    public void processColorIndicatorsRed1() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 10};
        BasicValues basicValues = new BasicValues(10D, 7D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processColorIndicators", averages);
    }

    @Test
    public void processColorIndicatorsRed2() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 10};
        BasicValues basicValues = new BasicValues(5D, 11D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processColorIndicators", averages);
    }

    @Test
    public void processXYValuesPositiveRelaxation25() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 10};
        BasicValues basicValues = new BasicValues(5D, 5D, 15D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveRelaxation50() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 5};
        BasicValues basicValues = new BasicValues(5D, 10D, 15D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveRelaxation75() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 5};
        BasicValues basicValues = new BasicValues(5D, 10D, 15D, 5D, 10D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveRelaxation100() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 5};
        BasicValues basicValues = new BasicValues(5D, 10D, 5D, 5D, 10D, 5D, 15D, 5D, 3D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveHighRelaxation25() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 10};
        BasicValues basicValues = new BasicValues(5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 15D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveHighRelaxation50() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 5};
        BasicValues basicValues = new BasicValues(5D, 10D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 15D, 5D, 3D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveHighRelaxation75() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 5};
        BasicValues basicValues = new BasicValues(5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 15D, 5D, 15D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveHighRelaxation100() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 5};
        BasicValues basicValues = new BasicValues(5D, 10D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 15D, 5D, 3D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesPositiveDream50() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {10, 5};
        BasicValues basicValues = new BasicValues(5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeNormalActivity25() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 5};
        BasicValues basicValues = new BasicValues(10D, 10D, 5D, 3D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeNormalActivity50() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 10};
        BasicValues basicValues = new BasicValues(10D, 5D, 5D, 3D, 5D, 15D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeNormalActivity75() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 10};
        BasicValues basicValues = new BasicValues(10D, 5D, 5D, 3D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeNormalActivity100() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 10};
        BasicValues basicValues = new BasicValues(10D, 5D, 5D, 10D, 5D, 5D, 5D, 3D, 5D, 15D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeAgitation25() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 5};
        BasicValues basicValues = new BasicValues(10D, 10D, 5D, 5D, 5D, 5D, 5D, 10D, 5D, 5D, 5D, 3D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeAgitation50() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 5};
        BasicValues basicValues = new BasicValues(10D, 3D, 5D, 5D, 5D, 5D, 5D, 10D, 5D, 5D, 5D, 3D, 5D, 10D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeAgitation75() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 5};
        BasicValues basicValues = new BasicValues(10D, 3D, 5D, 5D, 5D, 5D, 5D, 10D, 5D, 5D, 5D, 3D, 5D, 3D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeAgitation100() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 5};
        BasicValues basicValues = new BasicValues(10D, 3D, 5D, 5D, 5D, 5D, 5D, 10D, 5D, 5D, 5D, 10D, 5D, 5D, 5D, 3D, 5D, 10D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    @Test
    public void processXYValuesNegativeHighAgitation50() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        double[] averages = {5, 5};
        BasicValues basicValues = new BasicValues(60D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D, 5D);
        setBrainiacManagerPrivateField(brainiacManager, "basicValues", basicValues);
        invokeBrainiacManagerPrivateMethod(brainiacManager, double[].class, "processXYValues", averages);
    }

    private static void setBrainiacManagerPrivateField(BrainiacManager brainiacManager, String name, Object value) {
        try {
            TestUtils.setPrivateField(brainiacManager, BrainiacManager.class.getDeclaredField(name), value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static Object getBrainiacManagerPrivateField(BrainiacManager brainiacManager, String name) {
        try {
            return TestUtils.getPrivateField(brainiacManager, BrainiacManager.class.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object invokeBrainiacManagerPrivateMethod(BrainiacManager brainiacManager, Class typeParameters, String name, Object... args) {
        try {
            return TestUtils.invokePrivateMethod(brainiacManager, BrainiacManager.class.getDeclaredMethod(name, typeParameters), args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}