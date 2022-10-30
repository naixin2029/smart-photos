package smart.photos.share;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.time.Instant;
import java.util.Date;

import smart.photos.R;

public class GenerateLinkActivity extends Activity {

    private Uri shareLink;

    private EditText hoursEditText;
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_link);

        hoursEditText = findViewById(R.id.generate_hours);
        progressBar = findViewById(R.id.generate_progressBar);
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    private void generateShareLink(String path, long expiry) {
        Task<ShortDynamicLink> dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://smartphoto.com" + "/?" + path + "?" + String.valueOf(expiry)))
                .setDomainUriPrefix("https://smartphoto.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle("Smart Photos")
                        .setDescription("Take and share images along with all their metadata safely and securely.")
                        .build()
                )
                .buildShortDynamicLink()
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            shareLink = shortLink;
                            Log.d("Share Link", "Generated share link.");
                            if (null != shareLink) {
                                try {
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Smart Photos");
                                    String shareMessage = shareLink.toString();
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                                } catch (Exception e) {
                                    // Nothing
                                }
                            }
                        } else {
                            // Error
                            Log.e("Share Link", "Error generating share link.");
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        finish();
                    }
                });
    }

    public void onClickGenerate(View view) {
        long expiry = -1;

        // Convert input text to integer
        String hoursString = hoursEditText.getText().toString();
        if(hoursString.length() > 0) {
            int tempHours = -1;
            try {
                tempHours = Integer.parseInt(hoursString);
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Time given is too large.", Toast.LENGTH_SHORT).show();
                hoursEditText.setText("");
                return;
            }

            if(0 < tempHours) {
                Date now = new Date();
                long currTime = now.getTime();
                expiry = currTime + (3600000 * tempHours);
                if(expiry < currTime) {
                    Toast.makeText(getApplicationContext(), "Time given is too large.", Toast.LENGTH_SHORT).show();
                    hoursEditText.setText("");
                    return;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Time must greater than 0.", Toast.LENGTH_SHORT).show();
                hoursEditText.setText("");
                return;
            }
        } else {
            Toast.makeText(getApplicationContext(), "Must enter a time limit.", Toast.LENGTH_SHORT).show();
            hoursEditText.setText("");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        generateShareLink(getIntent().getStringExtra("ImagePath"), expiry);
    }

}
