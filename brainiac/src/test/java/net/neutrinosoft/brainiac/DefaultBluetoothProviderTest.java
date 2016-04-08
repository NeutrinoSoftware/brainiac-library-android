package net.neutrinosoft.brainiac;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.widget.Toast;

import net.neutrinosoft.brainiac.bluetooth.DefaultBluetoothProvider;
import net.neutrinosoft.brainiac.callback.OnDeviceCallback;
import net.neutrinosoft.brainiac.utils.PermissionsUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PermissionsUtils.class, Toast.class})
public class DefaultBluetoothProviderTest {
    @Mock
    private BluetoothAdapter bluetoothAdapter;

    //@Captor
    //private ArgumentCaptor<ScanCallback>

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void startScanAndroid4() throws Exception {
        DefaultBluetoothProvider bluetoothProvider = new DefaultBluetoothProvider(new Activity(), bluetoothAdapter);

        setSdkVersion(Build.VERSION_CODES.KITKAT);
        assertEquals(Build.VERSION.SDK_INT, 19);

        bluetoothProvider.startScan();

    }

    @Test
    public void stopScanAndroid4() throws Exception {
        DefaultBluetoothProvider bluetoothProvider = new DefaultBluetoothProvider(new Activity(), bluetoothAdapter);

        setSdkVersion(Build.VERSION_CODES.KITKAT);
        assertEquals(Build.VERSION.SDK_INT, 19);

        bluetoothProvider.stopScan();

    }

    @Test
    public void stopScanAndroid5() throws Exception {
        DefaultBluetoothProvider bluetoothProvider = new DefaultBluetoothProvider(new Activity(), bluetoothAdapter);
        when(bluetoothAdapter.getBluetoothLeScanner()).thenReturn(mock(BluetoothLeScanner.class));

        setSdkVersion(Build.VERSION_CODES.LOLLIPOP);
        assertEquals(Build.VERSION.SDK_INT, 21);

        bluetoothProvider.stopScan();

    }

    @Test
    public void startScanAndroid5() throws Exception {
        DefaultBluetoothProvider bluetoothProvider = new DefaultBluetoothProvider(new Activity(), bluetoothAdapter);
        when(bluetoothAdapter.getBluetoothLeScanner()).thenReturn(mock(BluetoothLeScanner.class));

        setSdkVersion(Build.VERSION_CODES.LOLLIPOP);
        assertEquals(Build.VERSION.SDK_INT, 21);

        bluetoothProvider.startScan();
    }

    @Test
    public void startScanAndroid6() throws Exception {
        Activity activity = new Activity();
        DefaultBluetoothProvider bluetoothProvider = new DefaultBluetoothProvider(activity, bluetoothAdapter);
        when(bluetoothAdapter.getBluetoothLeScanner()).thenReturn(mock(BluetoothLeScanner.class));

        setSdkVersion(Build.VERSION_CODES.M);
        assertEquals(Build.VERSION.SDK_INT, 23);

        PowerMockito.mockStatic(PermissionsUtils.class);
        PowerMockito.mockStatic(Toast.class);
        when(PermissionsUtils.isCoarseLocationAllowed(activity)).thenReturn(false);
        when(Toast.makeText(activity, "Location permission does not allowed", Toast.LENGTH_SHORT)).thenReturn(mock(Toast.class));

        bluetoothProvider.startScan();
    }

    static void setSdkVersion(int version) {
        try {
            setFinalStatic(Build.VERSION.class.getDeclaredField("SDK_INT"), version);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    @Test
    public void leScanCallbackAndroid4() throws Exception {
        DefaultBluetoothProvider bluetoothProvider = new DefaultBluetoothProvider(new Activity(), bluetoothAdapter);
        when(bluetoothAdapter.getBluetoothLeScanner()).thenReturn(mock(BluetoothLeScanner.class));
        OnDeviceCallback onDeviceCallback = mock(OnDeviceCallback.class);

        setSdkVersion(Build.VERSION_CODES.KITKAT);
        assertEquals(Build.VERSION.SDK_INT, 19);

        bluetoothProvider.setOnDeviceCallback(onDeviceCallback);
        bluetoothProvider.startScan();
        BluetoothDevice bluetoothDevice = mock(BluetoothDevice.class);
        bluetoothProvider.getLeScanCallback().onLeScan(bluetoothDevice, 1, new byte[5]);
    }

    @Test
    public void scanCallbackAndroid5() {
        DefaultBluetoothProvider bluetoothProvider = new DefaultBluetoothProvider(new Activity(), bluetoothAdapter);
        when(bluetoothAdapter.getBluetoothLeScanner()).thenReturn(mock(BluetoothLeScanner.class));
        OnDeviceCallback onDeviceCallback = mock(OnDeviceCallback.class);

        setSdkVersion(Build.VERSION_CODES.LOLLIPOP);
        assertEquals(Build.VERSION.SDK_INT, 21);

        bluetoothProvider.setOnDeviceCallback(onDeviceCallback);
        bluetoothProvider.startScan();
        ScanResult scanResult = mock(ScanResult.class);
        bluetoothProvider.getScanCallback().onScanResult(1, scanResult);
    }
}
