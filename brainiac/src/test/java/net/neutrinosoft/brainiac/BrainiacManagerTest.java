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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}