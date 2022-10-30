package smart.photos.map;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import smart.photos.R;

public class  MapActivity extends AppCompatActivity {

    private MapViewFragment mapFragment;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        String lat = getIntent().getStringExtra("Lat");
        String lon = getIntent().getStringExtra("Lon");
        LatLng coords = new LatLng(Float.parseFloat(lat), Float.parseFloat(lon));

        mapFragment = new MapViewFragment(coords);
        getSupportFragmentManager().beginTransaction().replace(R.id.share_map_fragment,
                mapFragment).commit();

        TextView latTextView = findViewById(R.id.map_lat_text);
        TextView lonTextView = findViewById(R.id.map_lon_text);

        latTextView.setText(roundCoordinate(lat));
        lonTextView.setText(roundCoordinate(lon));
    }
    
    public void onBackButtonClick(View view) {
        onBackPressed();
    }

    private String roundCoordinate(String coord) {
        return String.format(
                Locale.ENGLISH,
                "%.4f",
                Float.parseFloat(coord)
        );
    }
}
