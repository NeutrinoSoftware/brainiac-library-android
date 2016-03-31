package net.neutrinosoft.brainiac;

import android.app.Activity;

import junit.framework.Assert;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BrainiacManagerTest {

    @Test
    public void getBrainiacManager() throws Exception {
        BrainiacManager brainiacManager1 = BrainiacManager.getBrainiacManager(new Activity());
        BrainiacManager brainiacManager2 = BrainiacManager.getBrainiacManager(new Activity());
        //verify singleton
        Assert.assertSame(brainiacManager1, brainiacManager2);
    }

    @Test
    public void startScan() throws Exception {
        BrainiacManager brainiacManager = mock(BrainiacManager.class);
        brainiacManager.startScan();
        verify(brainiacManager, times(1)).startScan();
    }

    @Test
    public void stopScan() throws Exception {
        BrainiacManager brainiacManager = mock(BrainiacManager.class);
        brainiacManager.stopScan();
        verify(brainiacManager, times(1)).stopScan();
    }
}