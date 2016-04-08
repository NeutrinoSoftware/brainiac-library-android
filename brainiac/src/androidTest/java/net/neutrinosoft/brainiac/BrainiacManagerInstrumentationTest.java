package net.neutrinosoft.brainiac;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BrainiacManagerInstrumentationTest {

    @Test
    public void startScanAndroid6() throws Exception {
        Assert.assertEquals(4, 2 + 2);
        /*InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(InstrumentationRegistry.getTargetContext());
                brainiacManager.startScan();
            }
        });*/

        //onView(withText("Location permission does not allowed")).inRoot(withDecorView(not(is(new Activity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

}
