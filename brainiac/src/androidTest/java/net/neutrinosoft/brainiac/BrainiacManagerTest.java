package net.neutrinosoft.brainiac;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static java.util.regex.Pattern.matches;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BrainiacManagerTest {

    @Test
    public void startScanAndroid6() throws Exception {
        /*InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(InstrumentationRegistry.getTargetContext());
                brainiacManager.startScan();
            }
        });*/

        onView(withText("Location permission does not allowed")).inRoot(withDecorView(not(is(new Activity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }

}
