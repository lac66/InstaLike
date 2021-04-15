// ProfileFragment.java
// Levi Carpenter

package com.learn.instalike;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private final int SPAN_COUNT = 3;
    private static final String ACC = "account";
    private Account pageOwner;

    ArrayList<Post> posts;
    RecyclerView recyclerViewImgGrid;
    GridLayoutManager layoutManager;
    ProfileRecyclerViewAdapter adapter;

    TextView textViewWelcome;

    ProfileListener pListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(Account param1) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(ACC, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pageOwner = (Account) getArguments().getSerializable(ACC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Profile");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // initialize elements
        textViewWelcome = view.findViewById(R.id.textViewWelcome);
        textViewWelcome.setText("Welcome to " + pageOwner.getName() + "'s Page");

        recyclerViewImgGrid = view.findViewById(R.id.recyclerViewImgGrid);
        layoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
        recyclerViewImgGrid.setLayoutManager(layoutManager);

        posts = new ArrayList<>();

        getAllPosts();

        adapter = new ProfileRecyclerViewAdapter(getActivity(), posts, pageOwner.getId());
        recyclerViewImgGrid.setAdapter(adapter);

        // set listener for adapter for element clicking
        adapter.setListener(new ProfileRecyclerViewAdapter.ClickListener() {
            @Override
            public void goToPost(Post post) {
                pListener.goToPost(pageOwner, post);
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof ProfileListener) {
            this.pListener = (ProfileListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ProfileListener");
        }
    }

    // listener to change fragment
    interface ProfileListener{
        void goToPost(Account account, Post post);
    }

    //
    // Database methods
    //

    // get all posts for user and listen for real-time updates
    private void getAllPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts").document(pageOwner.getId()).collection("photos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        posts.clear();
                        for (QueryDocumentSnapshot document : value) {
                            HashSet<Account> likedBySet = new HashSet<>();
                            HashMap<String, Object> likedByMap = (HashMap<String, Object>) document.get("likedBy");
                            if (likedByMap != null) {
                                for (Map.Entry mapElement : likedByMap.entrySet()) {
                                    String id = (String) mapElement.getKey();
                                    HashMap<String, Object> accountMap = (HashMap<String, Object>) mapElement.getValue();
                                    String name = (String) accountMap.get("name");
                                    String email = (String) accountMap.get("email");
                                    likedBySet.add(new Account(id, name, email));
                                }
                            }

                            posts.add(new Post(document.getId(), document.getString("caption"), likedBySet, (Timestamp) document.get("createdAt")));
                        }
                        // sort posts from newest to oldest
                        Collections.sort(posts, new Comparator<Post>() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public int compare(Post o1, Post o2) {
                                return Math.toIntExact(o2.getCreatedAtTimestamp().getSeconds() - o1.getCreatedAtTimestamp().getSeconds());
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}