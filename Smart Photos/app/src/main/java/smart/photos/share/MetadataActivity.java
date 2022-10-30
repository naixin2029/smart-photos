package smart.photos.share;

import android.app.Activity;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Locale;

import smart.photos.R;
import smart.photos.SensorsHandler;

public class MetadataActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        String acceleration = getIntent().getStringExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_ACCELEROMETER));
        String magneticField = getIntent().getStringExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_MAGNETIC_FIELD));
        String gyroscope = getIntent().getStringExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_GYROSCOPE));
        String airPressure = getIntent().getStringExtra(SensorsHandler.sensorTypeToString(Sensor.TYPE_PRESSURE));
        String timestamp = getIntent().getStringExtra("Timestamp");
        String owner = getIntent().getStringExtra("Owner");

        TextView accelerationTextView = findViewById(R.id.acceleration_text_view);
        TextView magneticFieldTextView = findViewById(R.id.magnetic_field_text_view);
        TextView gyroscopeTextView = findViewById(R.id.gyroscope_text_view);
        TextView airPressureTextView = findViewById(R.id.air_pressure_text_view);
        TextView timestampTextView = findViewById(R.id.date_text_view);
        TextView ownerTextView = findViewById(R.id.owner_text_view);

        if (null == acceleration) {
            acceleration = "Unavailable";
        } else {
            acceleration = convertToXYZ(acceleration);
        }

        if (null == airPressure) {
            airPressure = "Unavailable";
        } else {
            airPressure =
                    String.format(
                        Locale.ENGLISH,
                        "%.1f",
                        Float.parseFloat(airPressure.replace("[", "").replace("]", "")
                    )
            );
        }

        if (null == gyroscope) {
            gyroscope = "Unavailable";
        } else {
            gyroscope = convertToXYZ(gyroscope);
        }

        if (null == magneticField) {
            magneticField = "Unavailable";
        } else {
            magneticField = convertToXYZ(magneticField);
        }

        if (null == timestamp) {
            timestamp = "Unavailable";
        }

        if (null == owner) {
            owner = "Unavailable";
        }

        accelerationTextView.setText(acceleration);
        magneticFieldTextView.setText(magneticField);
        gyroscopeTextView.setText(gyroscope);
        airPressureTextView.setText(airPressure);
        timestampTextView.setText(timestamp);
        ownerTextView.setText(owner);
    }

    private String convertToXYZ(String values) {
        if (null == values) {
            return "";
        }
        values = values.replace("[", "").replace("]", "");
        String[] coords = values.split(", ");
        return String.format(
                Locale.ENGLISH,
                "(%.1f, %.1f, %.1f)",
                Float.parseFloat(coords[0]),
                Float.parseFloat(coords[1]),
                Float.parseFloat(coords[2])
        );
    }
}
