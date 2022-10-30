//package smart.photos;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.matcher.ViewMatchers.*;
//import static androidx.test.espresso.assertion.ViewAssertions.*;
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.google.firebase.auth.FirebaseAuth;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//public class EditProfileTest {
//
//    @BeforeClass
//    public static void setup() {
//        /* Sign user in */
//        FirebaseAuth.getInstance().signInWithEmailAndPassword("test@test.com", "test123");
//    }
//
//    /* Testing that Edit Profile Activity is displayed */
//    @Test
//    public void editProfileActivityInViewTest() {
//        ActivityScenario<EditProfileActivity> activity = ActivityScenario.launch(EditProfileActivity.class);
//        onView(withId(R.id.activity_edit_profile_page)).check(matches(isDisplayed()));
//    }
//
//    /* EMAIL TESTS */
//
//    /* Testing that Email heading is displayed */
//    @Test
//    public void visibilityUserEmailTest() {
//        ActivityScenario<EditProfileActivity> activity = ActivityScenario.launch(EditProfileActivity.class);
//        onView(withId(R.id.email_label_edit_profile)).check(matches(isDisplayed()));
//        onView(withId(R.id.email_label_edit_profile)).check(matches(withText(R.string.e_mail)));
//    }
//
//    /* PASSWORD TESTS */
//
//    /* Testing that Password heading is displayed */
//    @Test
//    public void visibilityPasswordTest() {
//        ActivityScenario<EditProfileActivity> activity = ActivityScenario.launch(EditProfileActivity.class);
//        onView(withId(R.id.password_label_edit_profile)).check(matches(isDisplayed()));
//        onView(withId(R.id.password_label_edit_profile)).check(matches(withText(R.string.user_password)));
//    }
//
//    /* SAVE BUTTON TESTS */
//
//    /* Tests that save button is displayed */
//    @Test
//    public void visibilitySaveButtonTest() {
//        ActivityScenario<EditProfileActivity> activity = ActivityScenario.launch(EditProfileActivity.class);
//        onView(withId(R.id.button_save_edit_profile)).check(matches(isDisplayed()));
//        onView(withId(R.id.button_save_edit_profile)).check(matches(withText(R.string.save)));
//    }
//
//    /* BACK BUTTON TESTS */
//
//    /* Tests that back button is displayed */
//    @Test
//    public void visibilityBackButtonTest() {
//        ActivityScenario<EditProfileActivity> activity = ActivityScenario.launch(EditProfileActivity.class);
//        onView(withId(R.id.button_back_edit_profile)).check(matches(isDisplayed()));
//    }
//
//    /* Tests that back button takes user back to homepage */
//    @Test
//    public void backButtonFunctionTest() {
//        ActivityScenario<EditProfileActivity> activity = ActivityScenario.launch(EditProfileActivity.class);
//        onView(withId(R.id.button_back_edit_profile)).perform(click());
//        onView(withId(R.id.activity_home_page)).check(matches(isDisplayed()));
//    }
//
//    /* PLAIN TEXT DISPLAY TESTS */
//
//    /* Tests that edit profile heading is displayed */
//    @Test
//    public void visibilityEditProfileHeadingTest() {
//        ActivityScenario<EditProfileActivity> activity = ActivityScenario.launch(EditProfileActivity.class);
//        onView(withId(R.id.edit_profile_heading)).check(matches(isDisplayed()));
//        onView(withId(R.id.edit_profile_heading)).check(matches(withText(R.string.edit_profile)));
//    }
//}
