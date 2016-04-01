package net.neutrinosoft.brainiac;

import android.app.Activity;

import junit.framework.Assert;

import net.neutrinosoft.brainiac.callback.OnScanCallback;

import org.junit.Test;

import static org.mockito.Mockito.mock;

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
    public void startScanOnScanStartAndroid4() throws Exception {
        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(new Activity());

        brainiacManager.startScan();
    }

}