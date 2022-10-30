package smart.photos.handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import smart.photos.user.ImageBookmark;
import smart.photos.user.User;

public class DocumentSnapshotHandler {
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static User convertDocumentSnapshotToCurrentUser(DocumentSnapshot snapshot) {
        String name = mAuth.getCurrentUser().getDisplayName();
        String email = mAuth.getCurrentUser().getEmail();

        User user = new User(name, email);

        List<Map<String, Object>> mImageBookmarks = (List<Map<String, Object>>) snapshot.get("imageBookmarks");

        List<ImageBookmark> imageBookmarks = new ArrayList<>();

        if (null != mImageBookmarks) {
            for (Map<String, Object> mImageBookmark : mImageBookmarks) {

                String mPath = (String) mImageBookmark.get("path");
                Long mExpiry = (Long) mImageBookmark.get("expiry");

                imageBookmarks.add(new ImageBookmark(mPath, mExpiry));
            }
        }

        user.setImageBookmarks(imageBookmarks);

        return user;
    }
}
