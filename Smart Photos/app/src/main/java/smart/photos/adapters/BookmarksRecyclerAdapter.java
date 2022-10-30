package smart.photos.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.List;

import smart.photos.R;
import smart.photos.handler.DocumentSnapshotHandler;
import smart.photos.user.ImageBookmark;
import smart.photos.user.User;
import smart.photos.view.ViewImageActivity;

public class BookmarksRecyclerAdapter extends Adapter<BookmarksRecyclerAdapter.ViewHolder> {
    private Context context;
    private final LayoutInflater mInflater;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private List<ImageBookmark> bookmarks;

    public BookmarksRecyclerAdapter(Context context, List<ImageBookmark> bookmarks) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.bookmarks = bookmarks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        final Bitmap bitmap = bookmarks.get(position).getBitmap();
        if (null != bitmap) {
            holder.imageView.setImageBitmap(bitmap);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ViewImageActivity.class);

                    // TODO: Send path only and load full quality image on the other side
                    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bStream);
                    byte[] byteArray = bStream.toByteArray();

                    intent.putExtra("Image Data", byteArray);
                    intent.putExtra("Image Path", bookmarks.get(position).getPath());
                    intent.putExtra("Expiry", bookmarks.get(position).getExpiry());
                    context.startActivity(intent);
                }
            });
        } else {
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle("Image Not Found")
                            .setMessage("This image has been deleted by the owner. Would you like to remove it?")
                            .setPositiveButton("yes",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ImageBookmark deletedBookmark = bookmarks.remove(position);
                                            notifyDataSetChanged();
                                            deleteBookmarkFromFirestore(deletedBookmark);
                                        }
                                    })
                            .setNegativeButton("no",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Do nothing
                                        }
                                    });
                    builder.create().show();
                }
            });
        }

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Remove photo")
                        .setMessage("Are you sure you want to remove this bookmarked photo?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ImageBookmark deletedBookmark = bookmarks.remove(position);
                                        notifyDataSetChanged();
                                        deleteBookmarkFromFirestore(deletedBookmark);
                                    }
                                })
                        .setNegativeButton("no",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                });
                builder.create().show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    public void notifyItemDeleted(String deletedPath) {
        for (ImageBookmark bookmark : bookmarks) {
            if (bookmark.getPath().equals(deletedPath)) {
                bookmark.setBitmap(null);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.grid_view_image);
        }
    }

    private void deleteBookmarkFromFirestore(final ImageBookmark bookmark) {
        mFirestore.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        // Downloaded snapshot successfully
                        User user = DocumentSnapshotHandler
                                .convertDocumentSnapshotToCurrentUser(snapshot);

                        // Remove bookmark
                        List<ImageBookmark> bookmarks = user.getImageBookmarks();
                        for (ImageBookmark mBookmark: bookmarks) {
                            if (bookmark.getPath().equals(mBookmark.getPath())) {
                                bookmarks.remove(mBookmark);
                                break;
                            }
                        }

                        // Upload deletion
                        mFirestore.collection("users")
                                .document(mAuth.getCurrentUser().getUid())
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("ViewImageActivity", "Successfully uploaded new user object");
                                    }
                                });
                    }
                });
    }

    private void deleteBookmarkLocally(ImageBookmark bookmark) {
        for (ImageBookmark mBookmark: bookmarks) {
            if (bookmark.getPath().equals(mBookmark.getPath())) {
                bookmarks.remove(mBookmark);
                return;
            }
        }
    }
}
