package smart.photos;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class TermsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }

}
