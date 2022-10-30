package smart.photos.user;

import android.graphics.Bitmap;

import com.google.firebase.firestore.Exclude;

public class ImageBookmark {
    private String path;
    private Long expiry;

    @Exclude
    private Bitmap bitmap;

    public ImageBookmark(String path, Long expiry) {
        this.path = path;
        this.expiry = expiry;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public Long getExpiry() {
        return this.expiry;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
