// UserListFragment.java
// Levi Carpenter

package com.learn.instalike;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserListFragment extends Fragment {
    private static final String ACC = "account";
    private Account currentUser;

    ArrayList<Account> accounts;
    RecyclerView recyclerViewUsers;
    LinearLayoutManager layoutManager;
    AccountRecyclerViewAdapter adapter;

    UserListListener ulListener;

    public UserListFragment() {
        // Required empty public constructor
    }

    public static UserListFragment newInstance(Account param1) {
        UserListFragment fragment = new UserListFragment();
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
        getActivity().setTitle("Users");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        // initialize elements
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewUsers.setLayoutManager(layoutManager);

        accounts = new ArrayList<>();

        getAccounts();

        adapter = new AccountRecyclerViewAdapter(accounts);
        recyclerViewUsers.setAdapter(adapter);

        // click listeners
        adapter.setListener(new AccountRecyclerViewAdapter.IconClickListener() {
            @Override
            public void profileClicked(Account account) {
                ulListener.goToProfile(account);
            }
        });

        view.findViewById(R.id.buttonNewPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ulListener.goToNewPost(currentUser);
            }
        });

        view.findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ulListener.logout();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UserListListener) {
            this.ulListener = (UserListListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement UserListListener");
        }
    }

    // listener to change fragment
    interface UserListListener {
        void goToProfile(Account account);
        void goToNewPost(Account account);
        void logout();
    }

    //
    // Database methods
    //

    // list all users
    private void getAccounts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("accounts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        accounts.clear();
                        for (QueryDocumentSnapshot document : value) {
                            if (document.getId().equals(currentUser.getId())) {
                                accounts.add(0, new Account(document.getId(), document.getString("name"), document.getString("email")));
                            } else {
                                accounts.add(new Account(document.getId(), document.getString("name"), document.getString("email")));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}