package com.hitherejoe.vineyard.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;


public class Post implements Comparable<Post>, Parcelable {
    public String avatarUrl;
    public String created;
    public String description;
    public String postId;
    public String thumbnailUrl;
    public String username;
    public String videoUrl;
    public String tag;

    @Override
    public int compareTo(@NonNull Post another) {
        return another.getFormattedDate().compareTo(getFormattedDate());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.avatarUrl);
        dest.writeString(this.created);
        dest.writeString(this.description);
        dest.writeString(this.postId);
        dest.writeString(this.thumbnailUrl);
        dest.writeString(this.username);
        dest.writeString(this.videoUrl);
        dest.writeString(this.tag);
    }

    public Post() { }

    public Date getFormattedDate() {
        try {
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
            return dateFormat.parse(this.created);
        } catch (ParseException e) {
            Timber.e(e, "There was a problem parsing the Post Date.");
        }
        return null;
    }

    protected Post(Parcel in) {
        this.avatarUrl = in.readString();
        this.created = in.readString();
        this.description = in.readString();
        this.postId = in.readString();
        this.thumbnailUrl = in.readString();
        this.username = in.readString();
        this.videoUrl = in.readString();
        this.tag = in.readString();
    }

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (avatarUrl != null ? !avatarUrl.equals(post.avatarUrl) : post.avatarUrl != null)
            return false;
        if (created != null ? !created.equals(post.created) : post.created != null) return false;
        if (description != null ? !description.equals(post.description) : post.description != null)
            return false;
        if (postId != null ? !postId.equals(post.postId) : post.postId != null) return false;
        if (thumbnailUrl != null ? !thumbnailUrl.equals(post.thumbnailUrl) : post.thumbnailUrl != null)
            return false;
        if (username != null ? !username.equals(post.username) : post.username != null)
            return false;
        if (videoUrl != null ? !videoUrl.equals(post.videoUrl) : post.videoUrl != null)
            return false;
        return !(tag != null ? !tag.equals(post.tag) : post.tag != null);

    }

    @Override
    public int hashCode() {
        int result = avatarUrl != null ? avatarUrl.hashCode() : 0;
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (postId != null ? postId.hashCode() : 0);
        result = 31 * result + (thumbnailUrl != null ? thumbnailUrl.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (videoUrl != null ? videoUrl.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}