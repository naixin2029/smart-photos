package smart.photos.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smart.photos.Constants;
import smart.photos.R;
import smart.photos.SensorsHandler;
import smart.photos.map.MapActivity;
import smart.photos.share.MetadataActivity;
import smart.photos.user.ImageBookmark;
import smart.photos.user.User;

import smart.photos.handler.DocumentSnapshotHandler;

public class ViewImageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private ProgressBar progressBar;
    private ImageView bookmarkIcon;
    private Map<String, String> metadataMap;
    private String[] latLon;
    private String path;
    private Long expiry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        progressBar = findViewById(R.id.view_loading_progress_bar);
        bookmarkIcon = findViewById(R.id.view_bookmark_button);

        path = getIntent().getStringExtra("Image Path");

        Log.d("PATH", path);

        if(null == path) {
            onInvalidImage(InvalidType.IMAGE_DELETED);
        }

        expiry = getIntent().getLongExtra("Expiry", 0);

        Date now = new Date();
        if(expiry < now.getTime()) {
            onInvalidImage(InvalidType.LINK_EXPIRED);
        }

        byte[] imageData = getIntent().getByteArrayExtra("Image Data");

        final ImageView imageView = findViewById(R.id.view_image_view);

        if (null == imageData) {
            bookmarkIcon.setImageDrawable(getDrawable(R.drawable.ic_outline_bookmark_border_24));

            // Get Image
            final long FIVE_MEGABYTE = 1024 * 1024 * 5;
            FirebaseStorage.getInstance().getReference(path)
                    .getBytes(FIVE_MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imageView.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Intent intent = new Intent();
                            intent.putExtra("Deleted Path", path);
                            setResult(Constants.RESULT_IMAGE_DELETED, intent);
                            onInvalidImage(InvalidType.IMAGE_DELETED);
                            finish();
                        }
                    });
        } else {
            bookmarkIcon.setImageDrawable(getDrawable(R.drawable.ic_baseline_bookmark_24));
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            imageView.setImageBitmap(bitmap);
        }

        metadataMap = new HashMap<>();

        // Get Image metadata
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

                progressBar.setVisibility(View.INVISIBLE);
                findViewById(R.id.view_loading).setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Intent intent = new Intent();
                intent.putExtra("Deleted Path", path);
                setResult(Constants.RESULT_IMAGE_DELETED, intent);
                onInvalidImage(InvalidType.IMAGE_DELETED);
                finish();
            }
        });
    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }

    private void onInvalidImage(InvalidType type) {
        Intent intent = new Intent(getApplicationContext(), ViewImageInvalidActivity.class);
        intent.putExtra("Type", type.ordinal());
        startActivity(intent);
        finish();
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

    public void onBookmarkButtonPress(View view) {
        progressBar.setVisibility(View.VISIBLE);
        if (null == mAuth.getCurrentUser()) {
            finish();
            return;
        }

        // TODO check if the image has already been bookmarked, delete the bookmark if it has

        bookmarkIcon.setImageDrawable(getDrawable(R.drawable.ic_baseline_bookmark_24));

        mFirestore.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            Log.d("ViewImageActivity", "Successfully download current user object in onBookmarkButtonPress");

                            User user = DocumentSnapshotHandler
                                            .convertDocumentSnapshotToCurrentUser(snapshot);

                            ImageBookmark imageBookmark = new ImageBookmark(path, expiry);

                            List<ImageBookmark> bookmarks = user.getImageBookmarks();
                            boolean removedBookmark = false;
                            for (ImageBookmark mBookmark: bookmarks) {
                                if (imageBookmark.getPath().equals(mBookmark.getPath())) {
                                    bookmarks.remove(mBookmark);
                                    removedBookmark = true;
                                    bookmarkIcon.setImageDrawable(getDrawable(R.drawable.ic_outline_bookmark_border_24));
                                    break;
                                }
                            }

                            if (!removedBookmark) {
                                user.addImageBookmark(imageBookmark);
                            }

                            mFirestore.collection("users")
                                    .document(mAuth.getCurrentUser().getUid())
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("ViewImageActivity", "Successfully uploaded new image bookmark");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("ViewImageActivity", e.getMessage());
                                            bookmarkIcon.setImageDrawable(getDrawable(R.drawable.ic_outline_bookmark_border_24));
                                        }
                                    })
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        }
                    }
                 )
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ViewImageActivity", e.getMessage());
                        bookmarkIcon.setImageDrawable(getDrawable(R.drawable.ic_outline_bookmark_border_24));
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
