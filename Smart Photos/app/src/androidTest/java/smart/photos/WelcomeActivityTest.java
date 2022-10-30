package smart.photos;

import android.content.Intent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WelcomeActivityTest {

    @BeforeClass
    public static void setup() {
        /* Log user out to avoid login persistence issues */
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void activityInViewTest() {
        ActivityTestRule<WelcomeActivity> activity = new ActivityTestRule<>(WelcomeActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.activity_welcome)).check(matches(isDisplayed()));
    }

    @Test
    public void welcomeToSmartPhotosTextInViewTest() {
        ActivityTestRule<WelcomeActivity> activity = new ActivityTestRule<>(WelcomeActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.welcome_text)).check(matches(isDisplayed()));
        onView(withId(R.id.welcome_text)).check(matches(withText(R.string.welcome)));
    }

    @Test
    public void visibilitySignUpButtonTest() {
        ActivityTestRule<WelcomeActivity> activity = new ActivityTestRule<>(WelcomeActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.signup_button)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_button)).check(matches(withText(R.string.sign_up)));
    }

    @Test
    public void visibilityExistingUserButtonTest() {
        ActivityTestRule<WelcomeActivity> activity = new ActivityTestRule<>(WelcomeActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.existing_user_button)).check(matches(isDisplayed()));
        onView(withId(R.id.existing_user_button)).check(matches(withText(R.string.existing_user)));
    }

    @Test
    public void transitionToSignUpPageTest() {
        ActivityTestRule<WelcomeActivity> activity = new ActivityTestRule<>(WelcomeActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.signup_button)).perform(click());
        onView(withId(R.id.activity_signup)).check(matches(isDisplayed()));

    }

    @Test
    public void transitionToLoginPageTest() {
        ActivityTestRule<WelcomeActivity> activity = new ActivityTestRule<>(WelcomeActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.existing_user_button)).perform(click());
        onView(withId(R.id.activity_login)).check(matches(isDisplayed()));
    }
}
