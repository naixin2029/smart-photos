package smart.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;

public class ImageHandler {

    private static String LOCAL_SAVE_DIR;
    // Stores list of paths to local images
    // Can be append to LOCAL_SAVE_DIR to retrieve an image
    // or used with firebase too, since image names are consistent
    private static List<String> imageNames;
    // Observer notified when we successfully upload image
    private static Observer observer;
    // Tracks the file of the last image taken from camera
    private static File lastCameraFile;
    private static String lastImagePath;

    final private static long DOWNLOAD_BUFFER = 1024 * 1024 * 5; // 5 mega bytes

    static void setUp(Context context, Observer o){
        observer = o;
        imageNames = new ArrayList<>();
        LOCAL_SAVE_DIR = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/";
    }

    /**
     * Used when about to take a photo.
     * Creates a new file on the users phone to store an image, and returns that file object.
     * Registers this file as the lastCameraFile.
     */
    static File saveFile(){
        // Name image with current time/date
        String timestamp = new SimpleDateFormat("dd_MM_yyyy_HH-mm-ss", Locale.ENGLISH).format(new Date());
        String image_name = "image_" + timestamp + ".JPEG";

        File file = new File(LOCAL_SAVE_DIR, image_name);
        if(!Objects.requireNonNull(file.getParentFile()).exists()){
            file.getParentFile().mkdirs();
        }
        lastCameraFile = file;
        lastImagePath = image_name;
        return file;
    }

    /**
     * Used by the photoRecycler to update its list of imageNames.
     * */
    public static void fillImageNames(List<String> fileNames){
        fileNames.clear();
        ArrayList<String> copy = new ArrayList<>(imageNames);
        fileNames.addAll(copy);
    }

    /**
     * Uploads the last registered camera image to firebase.
     */
    static void uploadFromCamera(final String metadata){

        Bitmap bm = getOrientedBitmap(lastImagePath);
        if (null != bm) {
            saveBitmap(bm, lastImagePath);
        }

        final Uri picURI = Uri.fromFile(lastCameraFile);

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        final String savePath = userId + "/images/" + picURI.getLastPathSegment();
        // Save image to firebase, then upload its metadata
        final StorageReference storageLocation = FirebaseStorage.getInstance().getReference().child(savePath);

        storageLocation.putFile(picURI)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("ImageHandler", "FAIL! Could not save photo to firebase");
                        deleteFile(lastCameraFile);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    final String meta = metadata;
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("ImageHandler", "Photo saved to firebase!");
                        // Create metadata object
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setContentType("image/jpeg")
                                .setCustomMetadata("sensorData", meta)
                                .build();
                        // Update metadata
                        storageLocation.updateMetadata(metadata)
                                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        imageNames.add(0, lastCameraFile.getName());
                                        observer.update();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Uh-oh, an error occurred!
                                        Log.e("ImageHandler", "Unable to save image metadata to firebase!");
                                        deleteFile(lastCameraFile);
                                    }
                                });
                }
            });
    }


    /**
     * Downloads any images from firebase that are not stored locally, and saves them.
     * Removes any locally stored images that are not in firebase.
     * Also downloads firebase stored profile image.
     * */
    static public void syncImages() {
        // Retrieve all the users images and show them on the grid
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final StorageReference imagesFolder = FirebaseStorage.getInstance().getReference().child(uid+"/images/");

        final List<String> remote_file_names = new ArrayList<>();

        // Get all images in the users unique images folder
        Task<ListResult> listResultTask = imagesFolder.listAll();
        listResultTask.addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult result) {
                for(final StorageReference fileRef : result.getItems()) {
                    final String filePath = fileRef.getPath();

                    String[] x = filePath.split("/");
                    final String fileName = x[x.length-1];
                    remote_file_names.add(fileName);
                    // Check if the file exists locally
                    File localFile = new File(LOCAL_SAVE_DIR, fileName);
                    if(localFile.exists()) continue;

                    // Otherwise, download the image and save it locally
                    fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            imageNames.add(0, fileName);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("ImageHandler", e.getMessage());
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                // Remove any local files that are not in the cloud.
                File folder = new File(LOCAL_SAVE_DIR);
                final File[] localFiles = folder.listFiles();
                if(localFiles != null && localFiles.length > 0){
                    for(File local_file : localFiles){
                        String local_file_name = local_file.getName();
                        // Profile pic syncing happens in syncProfileImage() dont delete here
                        if (local_file_name.contains("profile_picture")){
                            continue;
                        }
                        // Delete local file if it does not exist in firebase
                        if(!remote_file_names.contains(local_file_name)) {
                            local_file.delete();
                        }
                    }
                }
            }
        });
    }

    /**
     * Removes an image from BOTH firebase and local storage.
     * */
    static public void removeImage(final String imageName) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(uid + "/images/" + imageName);

        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully from firebase, so we can safely delete it locally
                Log.i("ImageHandler", "File successfully deleted from firebase");
               deleteFile(imageName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("ImageHandler", exception.getMessage());
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                observer.update();
            }
        });

    }

    /**
     * Updates the grid to display all locally stored images, and updates the list of fileNames.
     */
    static public void updateGrid(){
        imageNames.clear();

        File folder = new File(LOCAL_SAVE_DIR);

        if(folder.exists()){
            for(File f : Objects.requireNonNull(folder.listFiles())){
                String fname = f.getName();
                if(!fname.contains("profile_picture")){
                    // Do not add profile image to grid
                    imageNames.add(0, fname);
                }
            }
        }else{
            folder.mkdirs();
        }
        observer.update();
    }

    /**
     * Saves a bitmap to a file with the given name.
     * */
    static void saveBitmap(Bitmap bm, String fileName){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(LOCAL_SAVE_DIR + fileName);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a file from the local storage.
     * returns true if it was removed.
     * */
    static public boolean deleteFile(File file){
        if(file.exists()){
            boolean result = file.delete();
            if(result){
                imageNames.remove(file.getName());
            }
            return result;
        }
        return false;
    }

    /**
     * Deletes a file from the local storage.
     * returns true if it was removed.
     * */
    static public boolean deleteFile(String imageName){
        File file = new File(LOCAL_SAVE_DIR + imageName);
        if(file.exists()){
            boolean result = file.delete();
            if(result){
                imageNames.remove(file.getName());
            }
            return result;
        }
        return false;
    }

    /**
     * Returns a bitmap from a locally saved image.
     * */
    static public Bitmap getBitmap(String imageName){
        return BitmapFactory.decodeFile(LOCAL_SAVE_DIR + imageName);
    }

    /**
     * Takes a bitmap and saves it as the users current profile picture.
     * Saves both locally and to firebase.
     * Will return true if it saved successfully to BOTH locations.
     * Will return false if either save location failed. In such case it will not be saved anywhere.
     * */
    static void saveProfileImage(Bitmap bm){

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String timestamp = new SimpleDateFormat("dd_MM_yyyy_HH-mm-ss", Locale.ENGLISH).format(new Date());
        String pictureName = "profile_picture_" + timestamp;

        // Delete current profile pic
        File[] localFiles = (new File(LOCAL_SAVE_DIR)).listFiles();
        for(File f : localFiles){
            if(f.getName().contains("profile_picture")){
                f.delete();
                break;
            }
        }

        // Save new profile pic to local device
        final File file = new File(LOCAL_SAVE_DIR, pictureName);
        if(file.exists()){
            file.delete();
        }else if(!Objects.requireNonNull(file.getParentFile()).exists()){
            file.getParentFile().mkdirs();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file.getAbsolutePath(), false);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            file.delete();
        }

        // Save to firebase
        final String remote_save_dir = userId + "/" + pictureName;
        final StorageReference remote_save_ref = FirebaseStorage.getInstance().getReference().child(remote_save_dir);

        // Delete firebase stored profile picture, and upload new one
        FirebaseStorage.getInstance().getReference().child(userId).listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult result) {
                        for (final StorageReference fileRef : result.getItems()) {
                            final String filePath = fileRef.getPath();

                            String[] x = filePath.split("/");
                            final String fileName = x[x.length - 1];

                            if(fileName.contains("profile_picture")){
                                // Delete old profile pics
                                fileRef.delete();
                            }
                        }
                        // Upload new one
                        remote_save_ref.putFile(Uri.fromFile(file));
                    }
        });

    }

    /**
     * Syncs local version of profile picture with whatevers in firebase
     * */
    static void syncProfilePicture(){
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        final StorageReference storageLocation = FirebaseStorage.getInstance().getReference().child(userId);

        File current_pic = null;
        // Find current profile pic
        File[] localFiles = (new File(LOCAL_SAVE_DIR)).listFiles();
        for(File f : localFiles){
            if(f.getName().contains("profile_picture")){
                current_pic = f;
                break;
            }
        }

        final File current_profile_pic = current_pic;

        // Check any files in firebase userID folder for one whos name contains "profile_picture"
        // Then downloads it only if its different to current one.
        storageLocation.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult result) {
                for (final StorageReference fileRef : result.getItems()) {
                    final String filePath = fileRef.getPath();

                    String[] x = filePath.split("/");
                    final String fileName = x[x.length - 1];

                    if(fileName.contains("profile_picture")){
                        // Only download if the file names are diff (or if there is no local profile pic, null)
                        if(current_profile_pic != null || !fileName.equals(current_profile_pic.getName())){
                            if(current_profile_pic != null){
                                current_profile_pic.delete();
                            }
                            File local_file = new File(LOCAL_SAVE_DIR, fileName);
                            fileRef.getFile(local_file);
                            return;
                        }
                    }
                }
                // If we do not return before getting here, no pic was found in cloud
                // So delete current pic anyway to match
                if(current_profile_pic != null){
                    current_profile_pic.delete();
                }
            }
        });
    }

    /**
     * Returns a bitmap of the users current locally saved profile picture
     * */
    static Bitmap getProfileImage(){

        if (null == LOCAL_SAVE_DIR) {
            return null;
        }

        File saveDir = new File(LOCAL_SAVE_DIR);

        File[] localFiles = saveDir.listFiles();

        if (null == localFiles) {
            return null;
        }

        for(File f : localFiles){
            if(f.getName().contains("profile_picture")){
                return getBitmap(f.getName());
            }
        }
        return null;
    }

    public static int getOrientation(String imagePath) {
        imagePath = LOCAL_SAVE_DIR + imagePath;

        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (rotation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }

        } catch (IOException ignored) {

        }

        return 0;
    }

    public static Bitmap getOrientedBitmap(String imagePath) {
        Bitmap bm = getBitmap(imagePath);
        int rotation = getOrientation(imagePath);

        Log.d("Rotation", rotation + "");

        if (rotation == 0) {
            return bm;
        }

        Matrix matrix = new Matrix();
        matrix.preRotate(rotation);
        return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
    }
}
