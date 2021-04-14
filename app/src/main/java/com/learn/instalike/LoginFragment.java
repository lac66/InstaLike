// LoginFragment.java
// Levi Carpenter

package com.learn.instalike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginFragment extends Fragment {
    FirebaseAuth mAuth;
    EditText editTextLoginEmail, editTextLoginPassword;
    Button buttonLogin, buttonCreateAccount;
    LoginListener lListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Login");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // associate elements with view
        editTextLoginEmail = view.findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = view.findViewById(R.id.editTextLoginPassword);
        buttonLogin = view.findViewById(R.id.buttonLogin);
        buttonCreateAccount = view.findViewById(R.id.buttonCreateAccount);

        // login button listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: ");
                String email = editTextLoginEmail.getText().toString();
                String password = editTextLoginPassword.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Email is not entered", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(getActivity(), "Password is not entered", Toast.LENGTH_SHORT).show();
                } else {
                    login(email, password);
                }
            }
        });

        // new account listener
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lListener.goToRegistration();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // assigns context to LoginListener
        if (context instanceof LoginListener) {
            this.lListener = (LoginListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LoginListener");
        }
    }

    // Listener to transfer data to fragments
    interface LoginListener {
        void goToRegistration();
        void goToUsers(Account currentUser);
    }

    // method to login user through firebase
    public void login(String email, String password){
        if(email == null || email.isEmpty() ){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Login Error")
                    .setMessage("Email cannot be empty")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        } else if(password == null || password.isEmpty() ){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Login Error")
                    .setMessage("Password cannot be empty")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        } else {
            Log.d("TAG", "login: start");
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                getAccount(mAuth.getCurrentUser().getUid());
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    }

    // create account object from user login
    void getAccount(String uid) {
        Log.d("TAG", "getAccount: ");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("accounts").get()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                Log.d("TAG", "onComplete: in for loop");
                                if (uid.equals(document.getId())) {
                                    lListener.goToUsers(new Account(document.getId(), document.getString("name"), document.getString("email")));
                                }
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
}