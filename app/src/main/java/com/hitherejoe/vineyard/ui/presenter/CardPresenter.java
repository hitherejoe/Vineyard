package com.hitherejoe.vineyard.ui.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.ui.activity.MainActivity;
import com.hitherejoe.vineyard.ui.activity.PostGridActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.widget.VideoCardView;


public class CardPresenter extends Presenter {

    private static final int CARD_WIDTH = 300;
    private static final int CARD_HEIGHT = 300;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private Drawable mDefaultCardImage;
    private Context mContext;

    public CardPresenter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        final Context context = parent.getContext();
        sDefaultBackgroundColor = ContextCompat.getColor(context, R.color.primary);
        sSelectedBackgroundColor = ContextCompat.getColor(context, R.color.primary_dark);
        mDefaultCardImage = ContextCompat.getDrawable(context, R.drawable.ic_card_default);

        final VideoCardView cardView = new VideoCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cardView.startVideo();
                } else {
                    if (mContext instanceof PostGridActivity) {
                        if (((PostGridActivity) mContext).isFragmentActive()) {
                            cardView.stopVideo();
                        }
                    } else if (mContext instanceof SearchActivity) {
                        if (((SearchActivity) mContext).isFragmentActive()) {
                            cardView.stopVideo();
                        }
                    } else if (mContext instanceof MainActivity) {
                        if (((MainActivity) mContext).isFragmentActive()) {
                            cardView.stopVideo();
                        }
                    } else {
                        cardView.stopVideo();
                    }
                }
            }
        });

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(VideoCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view's background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        if (item instanceof Post) {
            Post post = (Post) item;

            final VideoCardView cardView = (VideoCardView) viewHolder.view;
            if (post.videoUrl != null) {
                cardView.setTitleText(post.description);
                cardView.setContentText(post.username);
                cardView.setMainContainerDimensions(CARD_WIDTH, CARD_HEIGHT);
                cardView.setVideoUrl(post.videoUrl);

                Glide.with(cardView.getContext())
                        .load(post.thumbnailUrl)
                        .centerCrop()
                        .error(mDefaultCardImage)
                        .into(cardView.getMainImageView());
            }
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        if (viewHolder.view instanceof ImageCardView) {
            ImageCardView cardView = (ImageCardView) viewHolder.view;
            // Remove references to images so that the garbage collector can free up memory
            cardView.setBadgeImage(null);
            cardView.setMainImage(null);
        }
    }
}