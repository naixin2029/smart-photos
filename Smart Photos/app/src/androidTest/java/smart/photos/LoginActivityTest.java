package smart.photos;

import android.content.Intent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
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
public class LoginActivityTest {

    @BeforeClass
    public static void setup() {
        /* Log user out to avoid login persistence issues */
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void activityInViewTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.activity_login)).check(matches(isDisplayed()));
    }

    @Test
    public void visibilityWelcomeBackTextTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.welcome_back)).check(matches(withText(R.string.welcome_back)));
    }

    @Test
    public void visibilityEmailTextFieldTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.login_email_text)).check(matches(withHint(R.string.email)));
    }

    @Test
    public void visibilityPasswordTextFieldTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.login_password_text)).check(matches(withHint(R.string.password)));
    }

    @Test
    public void loginWithoutEmailTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.login_email_text)).check(matches(hasErrorText("Email required.")));
    }

    @Test
    public void loginWithoutPasswordTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.login_email_text)).perform(typeText("benji@live.com.au"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        onView(withId(R.id.login_password_text)).check(matches(hasErrorText("Password required.")));
    }

    @Test
    public void successfulLoginTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.login_email_text)).perform(typeText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.login_password_text)).perform(typeText("test123"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        /* Sleep current thread to allow login process to occur */
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.activity_login)).check(doesNotExist());
        onView(withId(R.id.activity_home_page)).check(matches(isDisplayed()));
    }

    @Test
    public void unsuccessfulLoginTest() {
        ActivityTestRule<LoginActivity> activity = new ActivityTestRule<>(LoginActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.login_email_text)).perform(typeText("hacker@hacks.com"), closeSoftKeyboard());
        onView(withId(R.id.login_password_text)).perform(typeText("hacker"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        /* Sleep current thread to allow login process to occur */
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.activity_login)).check(matches(isDisplayed()));
        onView(withId(R.id.activity_home_page)).check(doesNotExist());
    }
}