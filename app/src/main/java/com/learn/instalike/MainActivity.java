// MainActivity.java
// Levi Carpenter

// acts as a fragment manager file
// handles fragment transitions

package com.learn.instalike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener,
        RegistrationFragment.RegistrationListener, UserListFragment.UserListListener,
        NewPostFragment.NewPostListener, ProfileFragment.ProfileListener, PostFragment.PostListener {
    FirebaseAuth mAuth;
    Account currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            setCurrentUserAndGoToUserList();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.containerView, new LoginFragment())
                    .commit();
        }
    }

    // method to create account object if user is already logged in
    void setCurrentUserAndGoToUserList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts").get()
                .addOnCompleteListener(this, new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Log.d("TAG", "onComplete: in for loop");
                                if (mAuth.getCurrentUser().getUid().equals(document.getId())) {
                                    currentUser = new Account(document.getId(), document.getString("name"), document.getString("email"));

                                    getSupportFragmentManager().beginTransaction()
                                            .add(R.id.containerView, UserListFragment.newInstance(currentUser))
                                            .commit();
                                }
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Login Error")
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
    public void goToRegistration() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new RegistrationFragment())
                .commit();
    }

    @Override
    public void goToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .commit();
    }

    @Override
    public void goToUsers(Account currentUser) {
        this.currentUser = currentUser;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, UserListFragment.newInstance(currentUser))
                .commit();
    }

    @Override
    public void goToUsers() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void goToProfileFromNewPost(Account account) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, ProfileFragment.newInstance(account))
                .commit();
    }

    @Override
    public void goToProfile(Account account) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, ProfileFragment.newInstance(account))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToNewPost(Account account) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, NewPostFragment.newInstance(account))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void logout() {
        this.currentUser = null;
        mAuth.signOut();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new LoginFragment())
                .commit();
    }

    @Override
    public void goToPost(Account account, Post post) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, PostFragment.newInstance(currentUser, account, post))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToProfileFromPost() {
        getSupportFragmentManager().popBackStack();
    }
}