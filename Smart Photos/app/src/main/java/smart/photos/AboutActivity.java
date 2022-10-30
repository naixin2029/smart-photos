package smart.photos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }

    public void onTermsPressed(View view) {
        startActivity(new Intent(this, TermsActivity.class));
    }
}
