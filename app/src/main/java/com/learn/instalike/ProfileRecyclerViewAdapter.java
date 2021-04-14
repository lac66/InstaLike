// HW 07
// ProfileRecyclerViewAdapter.java
// Levi Carpenter

package com.learn.instalike;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ProfileRecyclerViewAdapter extends RecyclerView.Adapter<ProfileRecyclerViewAdapter.ImageViewHolder> {
    Context context;
    StorageReference storageReference;
    ArrayList<Post> posts;
    ClickListener cListener;

    ProfileRecyclerViewAdapter(Context context, ArrayList<Post> posts, String userId) {
        this.context = context;
        this.posts = posts;
        this.storageReference = FirebaseStorage.getInstance().getReference(userId);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.img_grid_item, parent, false);
        ImageViewHolder imageViewHolder = new ImageViewHolder(view);

        return imageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.post = post;
        holder.imgRef = storageReference.child(post.getImgRef());
        GlideApp.with(context).load(holder.imgRef).into(holder.imageViewGrid);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        Post post;
        ImageView imageViewGrid;
        StorageReference imgRef;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewGrid = itemView.findViewById(R.id.imageViewGrid);

            // click listener for pictures
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    post.setFullImgRef(imgRef);
                    cListener.goToPost(post);
                }
            });
        }
    }

    // listener and set method to send data on which image
    void setListener(ClickListener clickListener){
        this.cListener = clickListener;
    }

    interface ClickListener{
        void goToPost(Post post);
    }
}
