package net.neutrinosoft.brainiac;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BrainiacManagerTest {

    @Test
    public void startScanOnScanStart() throws Exception {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(InstrumentationRegistry.getTargetContext());
                brainiacManager.startScan();
            }
        });

    }

}
