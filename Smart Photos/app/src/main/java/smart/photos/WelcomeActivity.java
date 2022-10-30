package smart.photos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends Activity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        /* Go directly to HomePage if user is already logged in. */
        if(null != currentUser) {
            ImageHandler.syncProfilePicture();
            Intent intent = new Intent(this, HomePageActivity.class);
            startActivity(intent);
        }
    }

    // Had to restore login/sign up bc need a user id to fetch images
    public void onExistingUserButtonClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    // Had to restore login/sign up bc need a user id to fetch images
    public void onSignupButtonClick(View view) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}