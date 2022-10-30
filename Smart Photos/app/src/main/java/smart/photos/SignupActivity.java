package smart.photos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.*;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;


public class SignupActivity extends Activity {

    private FirebaseAuth mAuth;
    private TextInputLayout usernameField, emailField, passwordField;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameField = findViewById(R.id.signup_username_layout);
        emailField = findViewById(R.id.signup_email_layout);
        passwordField = findViewById(R.id.signup_password_layout);

        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.signup_progressBar);
    }

    public void registerUser(View view) {
        final String username = usernameField.getEditText().getText().toString().trim();
        final String email = emailField.getEditText().getText().toString().trim();
        final String password = passwordField.getEditText().getText().toString().trim();

        if (username.isEmpty()) {
            usernameField.getEditText().setError("Username required.");
            usernameField.getEditText().requestFocus();
        } else if (email.isEmpty()) {
            emailField.getEditText().setError("Email required.");
            emailField.getEditText().requestFocus();
        } else if (password.isEmpty()) {
            passwordField.getEditText().setError("Password required.");
            passwordField.getEditText().requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);

                        // Check if the registration was successful or not
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            mAuth.getCurrentUser().updateProfile(profileUpdates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("User Information", "Successfully updated user's profile information");
                                            startActivity(new Intent(SignupActivity.this, HomePageActivity.class));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("User Information", e.getMessage());
                                            startActivity(new Intent(SignupActivity.this, HomePageActivity.class));
                                        }
                                    });

                        } else {
                            Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        }
    }

    public void onBackButtonClick(View view) {
        super.onBackPressed();
    }

    public void onTermsPressed(View view) {
        startActivity(new Intent(this, TermsActivity.class));
    }
}
