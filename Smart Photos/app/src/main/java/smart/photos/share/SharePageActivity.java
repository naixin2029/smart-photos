package smart.photos.share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import smart.photos.R;
import smart.photos.SensorsHandler;
import smart.photos.map.MapActivity;
import smart.photos.map.MapViewFragment;

public class SharePageActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ImageView newImage;
    private Map<String, String> metadataMap;
    private String[] latLon;

    /* Constants */
    private final int CAPTURE_NEW_IMAGE = 601;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_page);

        Log.d("REACHED", "ACTIVITYYYYYYASDASDAS");

        newImage = findViewById(R.id.new_image_view);
        metadataMap = new HashMap<>();

        // Set the image
        byte[] byteArray = getIntent().getByteArrayExtra("ImageBytes");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        newImage.setImageBitmap(bitmap);

        final String path = getIntent().getStringExtra("ImagePath");

        // Hide the share button if we don't own the image
        if (!path.contains(mAuth.getCurrentUser().getUid())) {
            findViewById(R.id.share_button).setVisibility(View.INVISIBLE);
        }

        // Get metadata
        FirebaseStorage.getInstance().getReference().child(path)
                .getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'
                String[] data = storageMetadata.getCustomMetadata("sensorData")
                        .replace("{", "")
                        .replace("}","")
                        .split(",");
                // Convert the string data back into a map
                for(String pair : data){
                    String[] contents = pair.split("=");
                    if(contents.length == 2) {
                        metadataMap.put(contents[0].trim(), contents[1].replace("~", ",").trim());
                    }
                }
                latLon = new String[2];
                latLon[0] = metadataMap.get("Lat");
                latLon[1] = metadataMap.get("Long");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                finish();
            }
        });

    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }

    public void onClickShare(View view) {

        Intent intent = new Intent(getApplicationContext(), GenerateLinkActivity.class);
        intent.putExtra("ImagePath", getIntent().getStringExtra("ImagePath"));
        startActivity(intent);

    }

    public void onMetadataPress(View view) {
        Intent intent = new Intent(this, MetadataActivity.class);
        intent.putExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_ACCELEROMETER), metadataMap.get(SensorsHandler.sensorTypeToString(Sensor.TYPE_ACCELEROMETER)));
        intent.putExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_MAGNETIC_FIELD), metadataMap.get(SensorsHandler.sensorTypeToString(Sensor.TYPE_MAGNETIC_FIELD)));
        intent.putExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_GYROSCOPE), metadataMap.get(SensorsHandler.sensorTypeToString(Sensor.TYPE_GYROSCOPE)));
        intent.putExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_PRESSURE), metadataMap.get(SensorsHandler.sensorTypeToString(Sensor.TYPE_PRESSURE)));
        intent.putExtra("Timestamp", metadataMap.get("Timestamp"));
        intent.putExtra("Owner", metadataMap.get("Owner"));
        startActivity(intent);
    }

    public void onMapButtonPress(View view) {
        if (null == latLon[0] || null == latLon[1] ||
                latLon[0].trim().isEmpty() || latLon[1].trim().isEmpty()) {
            /* No location */
            Toast.makeText(this, "Image has no recorded location", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("Lat", latLon[0]);
        intent.putExtra("Lon", latLon[1]);
        startActivity(intent);
    }
}
