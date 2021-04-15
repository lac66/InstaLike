// Post.java
// Levi Carpenter

// class to handle images in individual accounts

package com.learn.instalike;

import android.text.format.DateFormat;

import com.google.firebase.Timestamp;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;

public class Post implements Serializable {
    String imgRef, caption, createdAt;
    HashSet<Account> likedBy;
    Timestamp createdAtTimestamp;
    StorageReference fullImgRef;

    Post(String imgRef, String caption) {
        this.imgRef = imgRef;
        this.caption = caption;
        this.likedBy = new HashSet<>();
    }

    Post(String imgRef, String caption, HashSet<Account> likedBy, Timestamp createdAt) {
        this.imgRef = imgRef;
        this.caption = caption;
        this.createdAtTimestamp = createdAt;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(createdAt.getSeconds() * 1000L);
        this.createdAt = DateFormat.format("MM-dd-yyyy hh:mm a", cal).toString();
        this.likedBy = likedBy;
    }

    public String getImgRef() {
        return imgRef;
    }

    public String getCaption() {
        return caption;
    }

    public StorageReference getFullImgRef() {
        return fullImgRef;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public HashSet<Account> getLikedBy() {
        return likedBy;
    }

    public Timestamp getCreatedAtTimestamp() {
        return createdAtTimestamp;
    }

    public void setFullImgRef(StorageReference imgRef) {
        this.fullImgRef = imgRef;
    }

    @Override
    public String toString() {
        return "Post{" +
                "imgRef='" + imgRef + '\'' +
                ", caption='" + caption + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", likedBy=" + likedBy +
                '}';
    }
}
