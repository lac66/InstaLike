// NewPostFragment.java
// Levi Carpenter

package com.learn.instalike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;

public class NewPostFragment extends Fragment {
    private static final int PICK_IMAGE = 100;
    private static final String ACC = "account";
    private Account currentUser;

    Uri imageUri;

    ImageView imageViewSelect;
    EditText editTextCaption;

    NewPostListener npListener;

    public NewPostFragment() {
        // Required empty public constructor
    }

    public static NewPostFragment newInstance(Account param1) {
        NewPostFragment fragment = new NewPostFragment();
        Bundle args = new Bundle();
        args.putSerializable(ACC, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = (Account) getArguments().getSerializable(ACC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("New Post");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);

        // initialize values
        imageUri = null;

        imageViewSelect = view.findViewById(R.id.imageViewSelect);
        editTextCaption = view.findViewById(R.id.editTextCaption);

        // listener to allow user to choose image for upload
        view.findViewById(R.id.buttonSelectImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        // listener to post chosen image
        view.findViewById(R.id.buttonPostImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = editTextCaption.getText().toString();
                if (imageUri == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("No Image Error")
                            .setMessage("Image must be selected")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                } else if (caption.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Warning: No Caption")
                            .setMessage("No text found for your caption. If you wish to add a caption, click Cancel. Otherwise, select Ok")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    postImage(caption);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.create().show();
                } else {
                    postImage(caption);
                }
            }
        });

        // listener to return to user list
        view.findViewById(R.id.buttonNewPostCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                npListener.goToUsers();
            }
        });

        return view;
    }

    // set chosen image to ImageView
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == -1 && data != null) {
            Log.d("TAG", "onActivityResult: ");
            imageUri = data.getData();
            imageViewSelect.setImageURI(imageUri);
        }
    }

    // method to store image in firebase storage
    void postImage(String caption) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference userDirectoryReference = storage.getReference().child(currentUser.getId());

        StorageReference selectImgRef = userDirectoryReference.child(imageUri.getLastPathSegment());
        UploadTask uploadTask = selectImgRef.putFile(imageUri);
        uploadTask.addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    refImgInDb(selectImgRef, caption);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Post Error")
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

    // method to reference image in firestore
    void refImgInDb(StorageReference imgRef, String caption) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> data = new HashMap<>();
        data.put("caption", caption);
        data.put("createdAt", Calendar.getInstance().getTime());
        data.put("likedBy", new HashMap<String, Object>());

        db.collection("accounts").document(currentUser.getId())
                .collection("photos").document(imgRef.getName())
                .set(data).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Posted", Toast.LENGTH_SHORT).show();
                    npListener.goToProfileFromNewPost(currentUser);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Post Error")
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof NewPostListener) {
            npListener = (NewPostListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NewPostListener");
        }
    }

    // listener to change fragment
    interface NewPostListener {
        void goToUsers();
        void goToProfileFromNewPost(Account account);
    }
}