// PostFragment.java
// Levi Carpenter

package com.learn.instalike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class PostFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static final String CURR = "current";
    private static final String ACC = "acc";
    private static final String POST = "post";
    private Account currentUser;
    private Account pageOwner;
    private Post currentPost;

    ArrayList<Comment> comments;
    RecyclerView recyclerViewComments;
    LinearLayoutManager layoutManager;
    PostRecyclerViewAdapter adapter;

    ImageView imageViewPost, imageViewLikePost, imageViewDeletePost;
    TextView textViewPostCaption, textViewPostDate;
    EditText editTextComment;

    boolean liked;
    PostListener pListener;

    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(Account currentUser, Account account, Post post) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putSerializable(CURR, currentUser);
        args.putSerializable(ACC, account);
        args.putSerializable(POST, post);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = (Account) getArguments().getSerializable(CURR);
            pageOwner = (Account) getArguments().getSerializable(ACC);
            currentPost = (Post) getArguments().getSerializable(POST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Post");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        // initialize values
        mAuth = FirebaseAuth.getInstance();

        textViewPostCaption = view.findViewById(R.id.textViewPostCaption);
        textViewPostDate = view.findViewById(R.id.textViewPostDate);
        editTextComment = view.findViewById(R.id.editTextComment);
        imageViewPost = view.findViewById(R.id.imageViewPost);
        imageViewLikePost = view.findViewById(R.id.imageViewLikePost);
        imageViewDeletePost = view.findViewById(R.id.imageViewDeletePost);

        GlideApp.with(getActivity()).load(currentPost.getFullImgRef()).into(imageViewPost);
        textViewPostCaption.setText(currentPost.getCaption());
        textViewPostDate.setText(currentPost.getCreatedAt());
        imageViewDeletePost.setImageResource(R.drawable.rubbish_bin);

        // hide delete icon if current user is not owner of post
        if (!mAuth.getUid().equals(pageOwner.getId())) {
            imageViewDeletePost.setVisibility(View.INVISIBLE);
        }

        // check if post is liked by current user and set icon accordingly
        if (currentPost.getLikedBy().contains(new Account(mAuth.getUid()))) {
            liked = true;
            imageViewLikePost.setImageResource(R.drawable.like_favorite);
        } else {
            liked = false;
            imageViewLikePost.setImageResource(R.drawable.like_not_favorite);
        }

        // setup recycler view for comments
        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewComments.setLayoutManager(layoutManager);

        comments = new ArrayList<>();

        getAllComments();

        adapter = new PostRecyclerViewAdapter(currentUser, pageOwner, comments);
        recyclerViewComments.setAdapter(adapter);

        // listener for like button
        imageViewLikePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liked) {
                    unlikePost();
                    liked = false;
                    imageViewLikePost.setImageResource(R.drawable.like_not_favorite);
                } else {
                    likePost();
                    liked = true;
                    imageViewLikePost.setImageResource(R.drawable.like_favorite);
                }
            }
        });

        // listener for delete button
        imageViewDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });

        // listener for post comment button
        view.findViewById(R.id.buttonPostComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editTextComment.getText().toString();
                if (text.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Comment Post Error")
                            .setMessage("Comment cannot be empty")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                } else {
                    postComment(text);
                }
            }
        });

        // set adapter listener to transmit data
        adapter.setListener(new PostRecyclerViewAdapter.PostAdapterListener() {
            @Override
            public void deleteComment(Comment comment) {
                deleteCommentFromDb(currentPost, comment);
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof PostListener) {
            pListener = (PostListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement PostListener");
        }
    }

    // listener to change fragment
    interface PostListener {
        void goToProfileFromPost();
    }

    //
    // Database methods
    //

    // retrieve all comments for the post and listen for real-time updates
    private void getAllComments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts").document(pageOwner.getId())
                .collection("photos").document(currentPost.getImgRef())
                .collection("comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        comments.clear();
                        for (QueryDocumentSnapshot document: value) {
                            HashMap<String, Object> accountMap = (HashMap<String, Object>) document.get("createdBy");
                            Account account = new Account((String) accountMap.get("id"), (String) accountMap.get("name"), (String) accountMap.get("email"));

                            comments.add(new Comment(document.getId(), document.getString("text"), account, (Timestamp) document.get("createdAt")));
                        }
                        // sort comments from oldest to newest
                        Collections.sort(comments, new Comparator<Comment>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public int compare(Comment o1, Comment o2) {
                                return Math.toIntExact(o1.getCreatedAtTimestamp().getSeconds() - o2.getCreatedAtTimestamp().getSeconds());
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // set like on post in db
    private void likePost() {
        HashSet<Account> likedBy = currentPost.getLikedBy();
        likedBy.add(currentUser);
        HashMap<String, Object> data = new HashMap<>();
        data.put("caption", currentPost.getCaption());
        data.put("createdAt", currentPost.getCreatedAtTimestamp());

        HashMap<String, Object> likedByMap = new HashMap<>();
        Iterator<Account> iterator = likedBy.iterator();
        while (iterator.hasNext()) {
            HashMap<String, Object> accountMap = new HashMap<>();
            Account account = iterator.next();
            accountMap.put("name", account.getName());
            accountMap.put("email", account.getEmail());
            likedByMap.put(account.getId(), accountMap);
        }
        data.put("likedBy", likedByMap);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts").document(pageOwner.getId())
                .collection("photos").document(currentPost.getImgRef())
                .set(data).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Liked", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Like Error")
                            .setMessage(task.getException().getMessage())
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                }
            }
        });
    }

    // unlike post in db
    private void unlikePost() {
        HashSet<Account> likedBy = currentPost.getLikedBy();
        likedBy.remove(currentUser);
        HashMap<String, Object> data = new HashMap<>();
        data.put("caption", currentPost.getCaption());
        data.put("createdAt", currentPost.getCreatedAtTimestamp());

        HashMap<String, Object> likedByMap = new HashMap<>();
        Iterator<Account> iterator = likedBy.iterator();
        while (iterator.hasNext()) {
            HashMap<String, Object> accountMap = new HashMap<>();
            Account account = iterator.next();
            accountMap.put("name", account.getName());
            accountMap.put("email", account.getEmail());
            likedByMap.put(account.getId(), accountMap);
        }
        data.put("likedBy", likedByMap);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts").document(pageOwner.getId())
                .collection("photos").document(currentPost.getImgRef())
                .set(data).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Unliked", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Unlike Error")
                            .setMessage(task.getException().getMessage())
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                }
            }
        });
    }

    // delete post from db
    private void deletePost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts").document(currentUser.getId())
                .collection("photos").document(currentPost.getImgRef())
                .delete().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                    pListener.goToProfileFromPost();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete Error")
                            .setMessage(task.getException().getMessage())
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                }
            }
        });
    }

    // post comment to db
    private void postComment(String comment) {
        editTextComment.setText("");
        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
        editTextComment.clearFocus();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> commentMap = new HashMap<>();
        commentMap.put("text", comment);
        commentMap.put("createdAt", Calendar.getInstance().getTime());

        HashMap<String, Object> accountMap = new HashMap<>();
        accountMap.put("name", currentUser.getName());
        accountMap.put("email", currentUser.getEmail());
        accountMap.put("id", currentUser.getId());

        commentMap.put("createdBy", accountMap);

        db.collection("accounts").document(pageOwner.getId())
                .collection("photos").document(currentPost.getImgRef())
                .collection("comments").add(commentMap)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Posted", Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Comment Post Error")
                                    .setMessage(task.getException().getMessage())
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            builder.create().show();
                        }
                    }
                });
    }

    // delete comment ...
    void deleteCommentFromDb(Post post, Comment comment) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts").document(pageOwner.getId())
                .collection("photos").document(post.getImgRef())
                .collection("comments").document(comment.getCommentId())
                .delete().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Comment Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete Comment Error")
                            .setMessage(task.getException().getMessage())
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                }
            }
        });
    }
}