package com.hitherejoe.vineyard;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.test.common.rules.ClearDataRule;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.ConnectActivity;
import com.hitherejoe.vineyard.ui.activity.LauncherActivity;
import com.hitherejoe.vineyard.util.CustomMatchers;
import com.hitherejoe.vineyard.util.MockModelsUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import rx.Observable;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(AndroidJUnit4.class)
public class LauncherActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext(), true);

    public final ClearDataRule clearDataRule = new ClearDataRule(component);

    public final ActivityTestRule<LauncherActivity> main =
            new ActivityTestRule<>(LauncherActivity.class, false, false);

    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(clearDataRule).around(main);

    @Test
    public void testConnectActivityShown() {
        main.launchActivity(null);
        onView(withId(R.id.main_browse_fragment))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testMainActivityShown() {
        main.launchActivity(null);
        onView(withId(R.id.main_browse_fragment))
                .check(matches(isDisplayed()));
    }

}