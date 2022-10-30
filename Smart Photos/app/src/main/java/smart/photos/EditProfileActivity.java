package smart.photos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.FileNotFoundException;
import java.io.InputStream;


public class EditProfileActivity extends Activity {

    private EditText editNameField, editEmailField, editPasswordField;
    private ImageView profilePictureImageView;
    private ProgressBar progressBar;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_page);

        editNameField = findViewById(R.id.name_field_edit_profile);
        editEmailField = findViewById(R.id.email_field_edit_profile);
        editPasswordField = findViewById(R.id.password_field_edit_profile);
        profilePictureImageView = findViewById(R.id.profile_image_edit_profile);
        progressBar = findViewById(R.id.progress_bar);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (null != user) {
            editNameField.setText(user.getDisplayName());
            editEmailField.setText(user.getEmail());
            Bitmap profilePicture = ImageHandler.getProfileImage();
            if (null != profilePicture){
                profilePictureImageView.setImageBitmap(profilePicture);
            }
        } else {
            onLogoutPressed(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (saving()) {
            return;
        }

        if (anyChanges()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Discard Changes")
                    .setMessage("Are you sure you want to discard changes?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditProfileActivity.super.onBackPressed();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /* Do nothing */
                        }
                    })
                    .create()
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean anyChanges() {
        return !editEmailField.getText().toString().equals(user.getEmail()) ||
               !editNameField.getText().toString().equals(user.getDisplayName()) ||
               !editPasswordField.getText().toString().trim().isEmpty();
    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }

    public boolean saving() {
        return progressBar.getVisibility() == View.VISIBLE;
    }


    public void editProfile(View view) {
        if (null != FirebaseAuth.getInstance().getCurrentUser() && !saving() && anyChanges()) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            final String new_user_name = editNameField.getText().toString().trim();
            final String new_email_address = editEmailField.getText().toString().trim();
            final String new_password = editPasswordField.getText().toString().trim();

            progressBar.setVisibility(View.VISIBLE);

            //update user name
            final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(new_user_name)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            user.updateEmail(new_email_address).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (new_password.equals("")) {
                                        finish();
                                        return;
                                    }
                                    user.updatePassword(new_password).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            /* Successfully uploaded all information, so we can finish the activity */
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        } else {
            onBackPressed();
        }
    }

    public void changePicture(View view) {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        int RESULT_LOAD_IMAGE = 1;
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = this;
        if(data==null){
            return;
        }

        Bitmap image = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
            image = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(image != null) {
            profilePictureImageView.setImageBitmap(image);
            ImageHandler.saveProfileImage(image);
        }
    }

    public void onLogoutPressed(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onAboutPressed(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

}