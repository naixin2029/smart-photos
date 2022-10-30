package smart.photos.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import smart.photos.R;

public class ViewImageInvalidActivity extends Activity {

    TextView description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_invalid);

        description = findViewById(R.id.invalid_view_description);

        InvalidType type = InvalidType.fromInt(getIntent().getIntExtra("Type", -1));
        if(InvalidType.IMAGE_DELETED == type) {
            description.setText("The image you are trying to view may have been deleted.");
        } else if(InvalidType.LINK_EXPIRED == type) {
            description.setText("The link you are trying to use has expired.");
        } else {
            description.setText("An error has occurred when trying to load this image.");
        }
    }

    public void onBackButtonClick(View view) {
        finish();
    }

}
