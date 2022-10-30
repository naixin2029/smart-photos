package smart.photos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
//import androidx.test.espresso.contrib.RecyclerViewActions;

import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.ByteArrayOutputStream;

import smart.photos.share.SharePageActivity;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class SharePageActivityTest {

    @BeforeClass
    public static void setup() {
        /* Sign user in */
        FirebaseAuth.getInstance().signInWithEmailAndPassword("test@test.com", "test123");
    }

    @Test
    public void activityInViewTest() {
        Context c = getApplicationContext();
        Intent intent = new Intent(c, SharePageActivity.class);
        String filePath = "random";
        byte[] byteArray = {0};

        intent.putExtra("ImageBytes", byteArray);
        intent.putExtra("ImagePath", filePath);
        ActivityTestRule<SharePageActivity> activity =
                new ActivityTestRule<>(SharePageActivity.class);
        activity.launchActivity(intent);
        onView(withId(R.id.activity_share_page)).check(matches(isDisplayed()));
    }

}
