package smart.photos;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import smart.photos.adapters.BookmarksRecyclerAdapter;
import smart.photos.adapters.MyPhotosRecyclerAdapter;
import smart.photos.handler.DocumentSnapshotHandler;
import smart.photos.user.ImageBookmark;
import smart.photos.user.User;
import smart.photos.view.ViewImageActivity;

public class HomePageActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAPTURE_NEW_IMAGE = 601;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private ProgressBar progressBar;
    private RecyclerView myPhotosRecyclerView, bookmarksRecyclerView;
    private MyPhotosRecyclerAdapter myPhotosRecyclerAdapter;
    private BookmarksRecyclerAdapter bookmarksRecyclerAdapter;
    private SensorsHandler sensorsHandler;
    private TabLayout tabs;

    private List<ImageBookmark> bookmarks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null == FirebaseAuth.getInstance().getCurrentUser()) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Check if app has been opened by a deep link
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }

                        if (null != deepLink) {
                            String fullLink = deepLink.toString();
                            int indexOfPath = fullLink.indexOf('?') + 1;
                            int indexOfExpiry = fullLink.indexOf('?', indexOfPath) + 1;

                            String imagePath = fullLink.substring(indexOfPath, indexOfExpiry - 1);
                            String expiry = fullLink.substring(indexOfExpiry);

                            Log.d("FULL LINK", deepLink.toString());

                            Intent intent = new Intent(getApplicationContext(), ViewImageActivity.class);
                            intent.putExtra("Image Path", imagePath);
                            intent.putExtra("Expiry", Long.valueOf(expiry));
                            startActivityForResult(intent, Constants.REQUEST_VIEW_IMAGE);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });

        setContentView(R.layout.activity_home_page);

        sensorsHandler = new SensorsHandler(this);
        sensorsHandler.requestLocationPermission();
        sensorsHandler.setupSensors();

        /* Required to store files... for now  need to look into file providers and policy authorising? */
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        sensorsHandler.startLocationListener();

        this.progressBar = findViewById(R.id.progress_bar);

        ImageHandler.setUp(this, new Observer() {
            @Override
            public void update() {
                onFirebaseTransactionFinished();
            }
        });
        ImageHandler.syncImages();

        bookmarksRecyclerView = findViewById(R.id.bookmarks_recycler_view);
        myPhotosRecyclerView = findViewById(R.id.home_grid_recycler_view);

        bookmarksRecyclerView.addItemDecoration(new SpacesItemDecoration(4, 3));
        myPhotosRecyclerView.addItemDecoration(new SpacesItemDecoration(4, 3));

        // Don't initialise the bookmark adapter until we first click on the bookmarks tab
        setupMyPhotosRecyclerView();

        // Set user data
        TextView displayName = findViewById(R.id.display_name);
        ImageView profileImage = findViewById(R.id.profile_picture);
        if (null != mAuth.getCurrentUser()) {
            displayName.setText(mAuth.getCurrentUser().getDisplayName());
            Bitmap image = ImageHandler.getProfileImage();
            if(image != null) {
                profileImage.setImageBitmap(image);
            }
        }

        tabs = findViewById(R.id.home_tabs);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: // My photos tab
                        myPhotosRecyclerView.setVisibility(View.VISIBLE);
                        bookmarksRecyclerView.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        myPhotosRecyclerView.setVisibility(View.INVISIBLE);
                        bookmarksRecyclerView.setVisibility(View.VISIBLE);

                        // Download bookmarked photos into the bookmarks adapter
                        if (null == bookmarksRecyclerAdapter) {
                            setupBookmarksRecyclerView();
                        }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        ImageHandler.updateGrid();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null == mAuth.getCurrentUser()) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (null != sensorsHandler) {
                sensorsHandler.startLocationListener();
            }

            // Set user data
            TextView displayName = findViewById(R.id.display_name);
            ImageView profileImage = findViewById(R.id.profile_picture);
            displayName.setText(mAuth.getCurrentUser().getDisplayName());
            Bitmap image = ImageHandler.getProfileImage();
            if(image != null) {
                profileImage.setImageBitmap(image);
            }
        }
    }

    // Called when the + button is pressed
    public void takePhoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photoFile = ImageHandler.saveFile();

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(HomePageActivity.this,
                    "smart.photos.provider",
                    photoFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, CAPTURE_NEW_IMAGE);
        } else {
            Log.e(TAG, "Could not save a new image to your device.");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_NEW_IMAGE) {
                progressBar.setVisibility(View.VISIBLE);
                ImageHandler.uploadFromCamera(sensorsHandler.getData().toString());
            } else {
                Log.e(TAG, "Error taking the image");
            }
        }

        if (resultCode == Constants.RESULT_IMAGE_DELETED) {
            if (requestCode == Constants.REQUEST_VIEW_IMAGE) {
                if (null != bookmarksRecyclerAdapter) {
                    bookmarksRecyclerAdapter.notifyItemDeleted(data.getStringExtra("Deleted Path"));
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SensorsHandler.REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sensorsHandler.startLocationListener();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onEditButtonClick(View view) {
        startActivity(new Intent(this, EditProfileActivity.class));
    }

    private void setupMyPhotosRecyclerView() {
        myPhotosRecyclerAdapter = new MyPhotosRecyclerAdapter(this);
        myPhotosRecyclerView.setAdapter(myPhotosRecyclerAdapter);
    }

    private void setupBookmarksRecyclerView() {
        bookmarksRecyclerAdapter = new BookmarksRecyclerAdapter(this, bookmarks);
        bookmarksRecyclerView.setAdapter(bookmarksRecyclerAdapter);

        // Set progress bar visibility until transactions are complete
        progressBar.setVisibility(View.VISIBLE);

        // Download the users document containing bookmarks
        mFirestore.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        // Downloaded snapshot successfully
                        User user = DocumentSnapshotHandler
                                .convertDocumentSnapshotToCurrentUser(snapshot);

                        // After downloading snapshots successfully, download the bookmarked images
                        loadBookmarkedImages(user.getImageBookmarks());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void loadBookmarkedImages(final List<ImageBookmark> imageBookmarkList) {
        if (imageBookmarkList.size() == 0) {
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        // Anonymous class used to detect when all images are downloaded, successfully or not.
        final Observer observer = new Observer() {
            int calls = 0;

            @Override
            public synchronized void update() {
                calls += 1;

                if (calls == imageBookmarkList.size()) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        };

        for (final ImageBookmark bookmark: imageBookmarkList) {
            final String path = bookmark.getPath();

            byte[] buffer = new byte[1024 * 1024 * 5];
            mStorage.getReference(path)
                    .getBytes(buffer.length)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            ImageBookmark download = new ImageBookmark(path, bookmark.getExpiry());
                            download.setBitmap(bitmap);
                            bookmarks.add(0, download);
                            bookmarksRecyclerAdapter.notifyDataSetChanged();
                            observer.update();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            bookmarks.add(0, new ImageBookmark(path, bookmark.getExpiry()));
                            bookmarksRecyclerAdapter.notifyDataSetChanged();
                            observer.update();
                        }
                    });
        }
    }

    public void onFirebaseTransactionFinished() {
        progressBar.setVisibility(View.INVISIBLE);
        if(myPhotosRecyclerAdapter != null){
            myPhotosRecyclerAdapter.retrieveImageNames();
            myPhotosRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        // No action when the user presses back.
    }

    @Override
    protected void onStop() {
        if (null != sensorsHandler) {
            sensorsHandler.removeLocationListener();
        }
        super.onStop();
    }
}