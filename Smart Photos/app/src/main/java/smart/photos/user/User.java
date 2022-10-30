package smart.photos.user;

import android.media.Image;

import java.util.List;
import java.util.ArrayList;

public class User {
    private String name;
    private String email;
    private List<ImageBookmark> imageBookmarks;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.imageBookmarks = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setImageBookmarks(List<ImageBookmark> imageBookmarks) {
        this.imageBookmarks = imageBookmarks;
    }

    public List<ImageBookmark> getImageBookmarks() {
        return this.imageBookmarks;
    }

    public void addImageBookmark(ImageBookmark imageBookmark) {
        for (ImageBookmark existingImageBookmark: imageBookmarks) {
            if (existingImageBookmark.getPath().equals(imageBookmark.getPath())) {
                existingImageBookmark.setExpiry(imageBookmark.getExpiry());
                return;
            }
        }
        imageBookmarks.add(imageBookmark);
    }

    public boolean containsImageBookmark(ImageBookmark imageBookmark) {
        for (ImageBookmark existingImageBookmark: imageBookmarks) {
            if (existingImageBookmark.getPath().equals(imageBookmark.getPath())) {
                return true;
            }
        }
        return false;
    }
}
