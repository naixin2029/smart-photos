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

import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import smart.photos.ImageHandler;
import smart.photos.R;
import smart.photos.share.SharePageActivity;

public class MyPhotosRecyclerAdapter extends Adapter<MyPhotosRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final LayoutInflater mInflater;
    private final List<String> imageNames;

    public MyPhotosRecyclerAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.imageNames = new ArrayList<>();
        ImageHandler.fillImageNames(imageNames);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Bitmap imageBitmap = ImageHandler.getOrientedBitmap(imageNames.get(position));
        holder.imageView.setImageBitmap(imageBitmap);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SharePageActivity.class);

                // TODO: Send path only and load full quality image on the other side
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                String filePath = imageNames.get(position);

                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, bStream);
                byte[] byteArray = bStream.toByteArray();

                intent.putExtra("ImageBytes", byteArray);
                intent.putExtra("ImagePath", FirebaseAuth.getInstance().getCurrentUser().getUid() + "/images/" + filePath);

                context.startActivity(intent);
            }
        });

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Delete photo")
                        .setMessage("Are you sure you want to delete this photo?")
                        .setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String imagePath = imageNames.get(position);
                                        ImageHandler.removeImage(imagePath);
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
        return this.imageNames.size();
    }

    public void retrieveImageNames(){
        ImageHandler.fillImageNames(imageNames);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.grid_view_image);
        }
    }

}
