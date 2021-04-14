// Comment.java
// Levi Carpenter

package com.learn.instalike;

// class to hold each comment

import android.text.format.DateFormat;

import com.google.firebase.Timestamp;

import java.util.Calendar;

public class Comment {
    String text;
    Account createdBy;
    String createdAt;
    String commentId;
    Timestamp createdAtTimestamp;

    public Comment(String commentId, String text, Account createdBy, Timestamp createdAt) {
        this.text = text;
        this.createdBy = createdBy;
        this.createdAtTimestamp = createdAt;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(createdAt.getSeconds() * 1000L);
        this.createdAt = DateFormat.format("MM-dd-yyyy hh:mm a", cal).toString();
        this.commentId = commentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Account getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Account createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Timestamp getCreatedAtTimestamp() {
        return createdAtTimestamp;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "text='" + text + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", commentId=" + commentId +
                '}';
    }
}
