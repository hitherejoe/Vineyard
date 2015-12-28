package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.ui.activity.PostGridActivity;
import com.hitherejoe.vineyard.ui.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostAdapter extends PaginationAdapter {

    public PostAdapter(Context context, String tag) {
        super(context, new CardPresenter(context), tag);
    }

    @Override
    public void addAllItems(List<?> items) {
        ArrayList<Post> posts = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Object object = items.get(i);
            if (object instanceof Post) posts.add((Post) object);
        }
        Collections.sort(posts);
        addPosts(posts);
    }

    @Override
    public List<Post> getAllItems() {
        List<Object> itemList = getItems();
        ArrayList<Post> posts = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            Object object = itemList.get(i);
            if (object instanceof Post) posts.add((Post) object);
        }
        return posts;
    }
}
