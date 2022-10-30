package smart.photos;

import android.content.Intent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SignupActivityTest {

    @BeforeClass
    public static void setup() {
        /* Log user out to avoid login persistence issues */
        FirebaseAuth.getInstance().signOut();
    }

    @Test
    public void activityInViewTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.activity_signup)).check(matches(isDisplayed()));
    }

    @Test
    public void visibilityUsernameTextFieldTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.signup_username_text)).check(matches(withHint(R.string.username)));
    }

    @Test
    public void visibilityEmailTextFieldTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.signup_email_text)).check(matches(withHint(R.string.email)));
    }

    @Test
    public void visibilityPasswordTextFieldTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.signup_password_text)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_password_text)).check(matches(withHint(R.string.password)));
    }

    @Test
    public void visibilitySignUpButtonTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.signup_activity_signup_button)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_activity_signup_button)).check(matches(withText(R.string.sign_up)));
    }

    @Test
    public void visibilityTermsAndConditionsTextTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.signup_agreement_text)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_agreement_text)).check(matches(withText(R.string.terms_of_service)));

        onView(withId(R.id.signup_terms_and_conditions_text)).check(matches(isDisplayed()));
        onView(withId(R.id.signup_terms_and_conditions_text)).check(matches(withText(R.string.terms_of_use_privacy_policy)));
    }

    @Test
    public void signupWithoutUsernameTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());
        onView(withId(R.id.signup_activity_signup_button)).perform(click());
        onView(withId(R.id.signup_username_text)).check(matches(hasErrorText("Username required.")));
    }

    @Test
    public void signupWithoutEmailTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.signup_username_text)).perform(typeText("Benji"), closeSoftKeyboard());

        onView(withId(R.id.signup_activity_signup_button)).perform(click());

        onView(withId(R.id.signup_email_text)).check(matches(not(hasErrorText("Username required."))));
        onView(withId(R.id.signup_email_text)).check(matches(hasErrorText("Email required.")));
    }

    @Test
    public void signupWithoutPasswordTest() {
        ActivityTestRule<SignupActivity> activity = new ActivityTestRule<>(SignupActivity.class);
        activity.launchActivity(new Intent());

        onView(withId(R.id.signup_username_text)).perform(typeText("Benji"), closeSoftKeyboard());
        onView(withId(R.id.signup_email_text)).perform(typeText("benji@live.com.au"), closeSoftKeyboard());

        onView(withId(R.id.signup_activity_signup_button)).perform(click());

        onView(withId(R.id.signup_email_text)).check(matches(not(hasErrorText("Username required."))));
        onView(withId(R.id.signup_email_text)).check(matches(not(hasErrorText("Email required."))));
        onView(withId(R.id.signup_password_text)).check(matches(hasErrorText("Password required.")));
    }
}