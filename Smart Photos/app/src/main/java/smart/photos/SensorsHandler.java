package smart.photos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SensorsHandler implements SensorEventListener {
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private final String TAG = "SensorsHandler";
    private HomePageActivity context;
    private Map<String, String> sensorData;
    private List<Sensor> sensorList;
    private ResultReceiver resultReceiver = new AddressResultReceiver(new Handler());
    private final LocationCallback locationCallback;

    public SensorsHandler(HomePageActivity context) {
        this.context = context;
        this.sensorData = new HashMap<>();
        this.sensorData.put("Owner", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        this.sensorList = new ArrayList<>();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    double latitude =
                            locationResult.getLocations().get(latestLocationIndex).getLatitude();
                    double longitude =
                            locationResult.getLocations().get(latestLocationIndex).getLongitude();
                    sensorData.put("Lat", Double.toString(latitude).replace(",","~"));
                    sensorData.put("Long", Double.toString(longitude).replace(",","~"));

                    Location location = new Location("providerNA");
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    fetchAddressFromLatLong(location);
                }

                String timestamp = new SimpleDateFormat("HH:mm  dd/MM", Locale.ENGLISH).format(new Date());
                sensorData.put("Timestamp", timestamp);
            }
        };
    }

    public void setupSensors() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorList.add(accelerometer);
            Log.d(TAG, "onCreate: Registered Accelerometer");
        }

        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyro != null) {
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorList.add(gyro);
            Log.d(TAG, "onCreate: Registered Gyro");
        }

        Sensor magno = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magno != null) {
            sensorManager.registerListener(this, magno, SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorList.add(magno);
            Log.d(TAG, "onCreate: Registered Magno");
        }

        Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (light != null) {
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorList.add(light);
            Log.d(TAG, "onCreate: Registered Light");
        }

        Sensor pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (pressure != null) {
            sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorList.add(pressure);
            Log.d(TAG, "onCreate: Registered Pressure");
        }

        Sensor temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (temp != null) {
            sensorManager.registerListener(this, temp, SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorList.add(temp);
            Log.d(TAG, "onCreate: Registered Temp");
        }

        Sensor humid = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (humid != null) {
            sensorManager.registerListener(this, humid, SensorManager.SENSOR_DELAY_NORMAL);
            this.sensorList.add(humid);
            Log.d(TAG, "onCreate: Registered Humidity");
        }
    }

    public void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = getSensor(event.sensor.getType());
        if (null == sensor) {
            return;
        }

        // Update sensor data
        sensorData.put(sensorTypeToString(sensor.getType()), Arrays.toString(event.values).replace(",","~"));

        // Update time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm-ss a", Locale.ENGLISH);
        String dateTime = dateFormat.format(calendar.getTime());
        sensorData.put("Time", dateTime.replace(",","~"));
    }

    private Sensor getSensor(int sensorType) {
        for (Sensor sensor : this.sensorList) {
            if (sensor.getType() == sensorType) {
                return sensor;
            }
        }
        return null;
    }

    public static String sensorTypeToString(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetic Field";
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Temperature";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Humidity";
            default:
                return null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* No implementation necessary */
    }

    public Map<String, String> getData() {
        return sensorData;
    }

    public void startLocationListener() {
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void removeLocationListener() {
        LocationServices.getFusedLocationProviderClient(context)
                .removeLocationUpdates(locationCallback);
    }

    private void fetchAddressFromLatLong(Location location) {
        Intent intent = new Intent(context, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        context.startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode == Constants.SUCCESS_RESULT) {
                sensorData.put("Location",resultData.getString(Constants.RESULT_DATA_KEY).replace(",","~"));
            } else {
                Toast.makeText(context, resultData.getString(Constants.RESULT_DATA_KEY), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
