// AccountRecyclerViewAdapter.java
// Levi Carpenter

// Adapter to list users in RecycerView

package com.learn.instalike;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.AccountViewHolder>{
    FirebaseAuth mAuth;
    ArrayList<Account> accounts;
    IconClickListener icListener;

    public AccountRecyclerViewAdapter(ArrayList<Account> data) {
        this.accounts = data;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_row_item, parent, false);
        AccountViewHolder accountViewHolder = new AccountViewHolder(view);

        return accountViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        // set text
        Account account = accounts.get(position);
        holder.account = account;
        holder.textViewName.setText(account.getName());
        holder.textViewEmail.setText(account.getEmail());
    }

    @Override
    public int getItemCount() {
        return this.accounts.size();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewEmail;
        Account account;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    icListener.profileClicked(account);
                }
            });
        }
    }

    // listener to go to profile page and set method for listener
    void setListener(IconClickListener listener) {
        this.icListener = listener;
    }

    interface IconClickListener {
        void profileClicked(Account account);
    }
}
