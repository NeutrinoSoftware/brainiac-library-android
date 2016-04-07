package net.neutrinosoft.brainiac;

import android.app.Activity;
import android.os.Build;
import android.widget.Toast;

import junit.framework.Assert;

import net.neutrinosoft.brainiac.callback.OnScanCallback;
import net.neutrinosoft.brainiac.utils.PermissionsUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PermissionsUtils.class, Toast.class})
public class BrainiacManagerTest {

    @Test
    public void getBrainiacManager() throws Exception {
        BrainiacManager brainiacManager1 = BrainiacManager.getBrainiacManager(new Activity());
        BrainiacManager brainiacManager2 = BrainiacManager.getBrainiacManager(new Activity());
        //verify singleton
        Assert.assertSame(brainiacManager1, brainiacManager2);

    }

    @Test
    public void startScanOnScanStart() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(new Activity());
        OnScanCallback onScanCallback = mock(OnScanCallback.class);
        brainiacManager.setOnScanCallback(onScanCallback);
        brainiacManager.startScan();
    }

    @Test
    public void startScanAndroid4() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(new Activity());

        setSdkVersion(Build.VERSION_CODES.KITKAT);
        assertEquals(Build.VERSION.SDK_INT, 19);

        brainiacManager.startScan();

    }

    @Test
    public void startScanAndroid5() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(new Activity());

        setSdkVersion(Build.VERSION_CODES.LOLLIPOP);
        assertEquals(Build.VERSION.SDK_INT, 21);

        brainiacManager.startScan();
    }

    @Test
    public void startScanAndroid6() throws Exception {
        Activity activity = new Activity();
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        setSdkVersion(Build.VERSION_CODES.M);
        assertEquals(Build.VERSION.SDK_INT, 23);

        PowerMockito.mockStatic(PermissionsUtils.class);
        PowerMockito.mockStatic(Toast.class);
        when(PermissionsUtils.isCoarseLoactionAllowed(activity)).thenReturn(false);
        when(Toast.makeText(activity, "Location permission does not allowed", Toast.LENGTH_SHORT)).thenReturn(Mockito.mock(Toast.class));

        brainiacManager.startScan();
    }

    /*@Test
    public void startScanCallbackAndroid4() throws Exception {
        Activity activity = new Activity();
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(activity);

        setSdkVersion(Build.VERSION_CODES.M);
        assertEquals(Build.VERSION.SDK_INT, 23);

        PowerMockito.mockStatic(PermissionsUtils.class);
        PowerMockito.mockStatic(Toast.class);
        when(PermissionsUtils.isCoarseLoactionAllowed(activity)).thenReturn(false);

        brainiacManager.startScan();
    }*/

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
}