package com.hitherejoe.vineyard.util;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.internal.util.Checks;
import android.support.v17.leanback.widget.HeaderItem;
import android.view.View;
import android.view.ViewGroup;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.hamcrest.Matchers.*;

public class EspressoTestMatchers {

    // From Espresso.java
    private static final Matcher<View> OVERFLOW_BUTTON_MATCHER = anyOf(
            allOf(isDisplayed(), withContentDescription("More options")),
            allOf(isDisplayed(), withClassName(endsWith("OverflowMenuButton")))
    );

    public static Matcher<View> overflowMenu() {
        return OVERFLOW_BUTTON_MATCHER;
    }

    public static Matcher<String> isEmpty() {
        return new TypeSafeMatcher<String>() {
            @Override
            public boolean matchesSafely(String target) {
                return target.length() == 0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is empty");
            }
        };
    }

    public static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    public static Matcher<View> withChildCount(final Matcher<Integer> numberMatcher) {
        return new BoundedMatcher<View, ViewGroup>(ViewGroup.class) {
            @Override
            protected boolean matchesSafely(ViewGroup viewGroup) {
                return numberMatcher.matches(viewGroup.getChildCount());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("number of child views ");
                numberMatcher.describeTo(description);
            }
        };
    }

    public static Matcher<Object> withYourListItemText(final Matcher<String> yourListItemText) {
        Checks.checkNotNull(yourListItemText);
        return new BoundedMatcher<Object, HeaderItem>(HeaderItem.class) {
            @Override
            public boolean matchesSafely(HeaderItem item) {
                return yourListItemText.matches(item.getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with text: " + yourListItemText.toString());
                yourListItemText.describeTo(description);
            }
        };
    }
}