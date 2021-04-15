// RegistrationFragment.java
// Levi Carpenter

package com.learn.instalike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import java.util.HashMap;

public class RegistrationFragment extends Fragment {
    private FirebaseAuth mAuth;
    EditText editTextRegistrationName, editTextRegistrationEmail, editTextRegistrationPassword;
    Button buttonRegistrationSubmit, buttonRegistrationCancel;
    RegistrationListener rListener;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Create New Account");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        // initialize elements
        editTextRegistrationName = view.findViewById(R.id.editTextRegistrationName);
        editTextRegistrationEmail = view.findViewById(R.id.editTextRegistrationEmail);
        editTextRegistrationPassword = view.findViewById(R.id.editTextRegistrationPassword);
        buttonRegistrationSubmit = view.findViewById(R.id.buttonRegistrationSubmit);
        buttonRegistrationCancel = view.findViewById(R.id.buttonRegistrationCancel);

        // listener for register button
        buttonRegistrationSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextRegistrationName.getText().toString();
                String email = editTextRegistrationEmail.getText().toString();
                String password = editTextRegistrationPassword.getText().toString();

                if (name.isEmpty()) {
                    Toast.makeText(getActivity(), "Name is not entered", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Email is not entered", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(getActivity(), "Password is not entered", Toast.LENGTH_SHORT).show();
                } else {
                    registerAccount(name, email, password);
                }
            }
        });

        // listener to go to login
        buttonRegistrationCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rListener.goToLogin();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof LoginFragment.LoginListener) {
            this.rListener = (RegistrationListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement LoginListener");
        }
    }

    interface RegistrationListener {
        void goToLogin();
        void goToUsers(Account account);
    }

    //
    // Database methods
    //

    // register account with firebaseAuth
    private void registerAccount(String name, String email, String password) {
        if (email == null || email.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Registration Error")
                    .setMessage("Email cannot be empty")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        } else if (password == null || password.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Registration Error")
                    .setMessage("Password cannot be empty")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        } else {
            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                addAccount(name, email);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Registration Error")
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

    // add account to firestore
    void addAccount(String name, String email) {
        if(name == null || name.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Registration Error")
                    .setMessage("Name cannot be empty")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        } else if(email == null || email.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Registration Error")
                    .setMessage("Email cannot be empty")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            builder.create().show();
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            HashMap<String, Object> user = new HashMap<>();
            user.put("name", name);
            user.put("email", email);

            db.collection("accounts").document(mAuth.getCurrentUser().getUid())
                    .set(user)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                rListener.goToUsers(new Account(mAuth.getUid(), name, email));
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Registration Error")
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
}