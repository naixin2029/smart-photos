package smart.photos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends Activity {

    private TextInputLayout emailField, passwordField;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.login_email_layout);
        passwordField = findViewById(R.id.login_password_layout);
        progressBar = findViewById(R.id.login_progressBar);

        mAuth = FirebaseAuth.getInstance();
    }

    public void login(View view) {
        final String email = emailField.getEditText().getText().toString().trim();
        final String password = passwordField.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            emailField.getEditText().setError("Email required.");
            emailField.getEditText().requestFocus();
        } else if (password.isEmpty()) {
            passwordField.getEditText().setError("Password required.");
            passwordField.getEditText().requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);

                            // Check if the login was successful or not
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(LoginActivity.this, HomePageActivity.class));
                            } else {
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void onBackButtonClick(View view) {
        super.onBackPressed();
    }
}
