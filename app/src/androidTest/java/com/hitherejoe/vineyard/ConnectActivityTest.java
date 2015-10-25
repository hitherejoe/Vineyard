package com.hitherejoe.vineyard;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.test.common.ClearDataRule;
import com.hitherejoe.vineyard.test.common.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.ConnectActivity;
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
public class ConnectActivityTest {

    public final TestComponentRule component = new TestComponentRule(
            VineyardApplication.get(InstrumentationRegistry.getTargetContext()),
            true);

    @Rule
    public final ClearDataRule clearDataRule = new ClearDataRule(component);

    @Rule
    public final ActivityTestRule<ConnectActivity> main =
            new ActivityTestRule<>(ConnectActivity.class, false, false);

    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(clearDataRule).around(main);

    @Test
    public void testInitialViewState() {
        main.launchActivity(null);
        onView(withId(R.id.image_vine))
                .check(matches(isDisplayed()));
        onView(withId(R.id.edit_text_username))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(withHint(R.string.text_hint_username)));
        onView(withId(R.id.edit_text_password))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .check(matches(withHint(R.string.text_hint_password)));
        onView(withId(R.id.button_sign_in))
                .check(matches(isDisplayed()));
        onView(withId(R.id.progress))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void testSuccessfulConnect() {
        // Stub sign in method in the DataManager
        Authentication authentication = MockModelsUtil.createMockSuccessAuthentication();
        doReturn(Observable.just(authentication))
                .when(component.getDataManager())
                .getAccessToken(anyString(), anyString());
        main.launchActivity(null);

        onView(withId(R.id.edit_text_username))
                .perform(typeText("test@test.com"));
        onView(withId(R.id.edit_text_password))
                .perform(typeText("password"));
        onView(withId(R.id.button_sign_in))
                .perform(click());

        //TODO: Check navigation to MainActivity
    }

    @Test
    public void testFormErrorMessagesDisplay() throws InterruptedException {
        main.launchActivity(null);

        closeSoftKeyboard();
        onView(withId(R.id.button_sign_in))
                .perform(click());
        onView(withText(R.string.error_message_sign_in_blank))
                .inRoot(CustomMatchers.isToast())
                .check(matches(isDisplayed()));

        Thread.sleep(2000);

        onView(withId(R.id.edit_text_username))
                .perform(typeText("test@test.com"));
        closeSoftKeyboard();
        onView(withId(R.id.button_sign_in))
                .perform(click());
        onView(withText(R.string.error_message_sign_in_blank_password))
                .inRoot(CustomMatchers.isToast())
                .check(matches(isDisplayed()));

        Thread.sleep(2000);

        onView(withId(R.id.edit_text_username))
                .perform(clearText());
        onView(withId(R.id.edit_text_password))
                .perform(typeText("password"));
        closeSoftKeyboard();
        onView(withId(R.id.button_sign_in))
                .perform(click());
        onView(withText(R.string.error_message_sign_in_blank_username))
                .inRoot(CustomMatchers.isToast())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testUnsuccessfulConnect() {
        Authentication authentication = MockModelsUtil.createMockErrorAuthentication();
        doReturn(Observable.just(authentication))
                .when(component.getDataManager())
                .getAccessToken(anyString(), anyString());
        main.launchActivity(null);

        onView(withId(R.id.edit_text_username))
                .perform(typeText("test@test.com"));
        onView(withId(R.id.edit_text_password))
                .perform(typeText("password"));
        onView(withId(R.id.button_sign_in))
                .perform(click());

        onView(withId(R.id.image_vine))
                .check(matches(isDisplayed()));

        onView(withText(R.string.error_message_sign_in))
                .inRoot(CustomMatchers.isToast())
                .check(matches(isDisplayed()));
        //assertThat(component.getPreferencesHelper().getAccessToken(), null);
    }

}