package com.hitherejoe.vineyard.ui.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.model.Post;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {

    public static final String ITEM_RELOAD = "item_reload";
    public static final String ITEM_TRY_AGAIN = "item_try_again";

    private static final int CARD_WIDTH = 470;
    private static final int CARD_HEIGHT = 264;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private Drawable mDefaultCardImage;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {

        Context context = parent.getContext();
        sDefaultBackgroundColor = ContextCompat.getColor(context, R.color.primary);
        sSelectedBackgroundColor = ContextCompat.getColor(context, R.color.primary_dark);
        mDefaultCardImage = ContextCompat.getDrawable(context, R.drawable.lb_ic_play);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
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
            ImageCardView cardView = (ImageCardView) viewHolder.view;

            if (post.videoUrl != null) {
                cardView.setTitleText(post.description);
                cardView.setContentText(post.username);
                cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
                Glide.with(viewHolder.view.getContext())
                        .load(post.thumbnailUrl)
                        .centerCrop()
                        .error(mDefaultCardImage)
                        .into(cardView.getMainImageView());
            }
        } else if (item instanceof String) {
            Context context = viewHolder.view.getContext();
            ImageCardView cardView = (ImageCardView) viewHolder.view;
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            cardView.getMainImageView().setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_refresh_white));

            if (item.equals(ITEM_TRY_AGAIN)) {
                cardView.setTitleText(context.getString(R.string.title_oops));
                cardView.setContentText(context.getString(R.string.message_try_again));
            } else if (item.equals(ITEM_RELOAD)) {
                cardView.setTitleText(context.getString(R.string.title_no_videos));
                cardView.setContentText(context.getString(R.string.message_check_again));
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