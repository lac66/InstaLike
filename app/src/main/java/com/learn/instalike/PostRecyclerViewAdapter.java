// PostRecyclerViewAdapter.java
// Levi Carpenter

package com.learn.instalike;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder> {
    Account currentUser, postOwner;
    ArrayList<Comment> comments;
    PostAdapterListener paListener;

    PostRecyclerViewAdapter(Account currentUser, Account postOwner, ArrayList<Comment> comments) {
        this.currentUser = currentUser;
        this.postOwner = postOwner;
        this.comments = comments;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row_item, parent, false);
        PostRecyclerViewAdapter.PostViewHolder postViewHolder = new PostRecyclerViewAdapter.PostViewHolder(view);

        return postViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.comment = comment;
        holder.textViewCommentCreator.setText(comment.getCreatedBy().getName());
        holder.textViewComment.setText(comment.getText());
        holder.textViewCommentDate.setText(comment.getCreatedAt());

        // hide delete icon for comment if current user does not own comment
        if (!currentUser.getId().equals(comment.getCreatedBy().getId())) {
            holder.imageViewDeleteComment.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        Comment comment;
        TextView textViewCommentCreator, textViewComment, textViewCommentDate;
        ImageView imageViewDeleteComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewCommentCreator = itemView.findViewById(R.id.textViewCommentCreator);
            textViewComment = itemView.findViewById(R.id.textViewComment);
            textViewCommentDate = itemView.findViewById(R.id.textViewCommentDate);
            imageViewDeleteComment = itemView.findViewById(R.id.imageViewDeleteComment);

            // listener to delete comment
            imageViewDeleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    paListener.deleteComment(comment);
                }
            });
        }
    }

    interface PostAdapterListener{
        void deleteComment(Comment comment);
    }

    void setListener(PostAdapterListener listener) {
        this.paListener = listener;
    }
}
