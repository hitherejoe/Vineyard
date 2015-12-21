package com.hitherejoe.vineyard.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.BaseCardView;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hitherejoe.vineyard.R;

import timber.log.Timber;


public class VideoCardView extends BaseCardView {

    public static final int CARD_TYPE_FLAG_IMAGE_ONLY = 0;
    public static final int CARD_TYPE_FLAG_TITLE = 1;
    public static final int CARD_TYPE_FLAG_CONTENT = 2;
    public static final int CARD_TYPE_FLAG_ICON_RIGHT = 4;
    public static final int CARD_TYPE_FLAG_ICON_LEFT = 8;

    private PreviewCardView mPreviewCard;
    private ViewGroup mInfoArea;
    private TextView mTitleView;
    private TextView mContentView;
    private ImageView mBadgeImage;
    private boolean mAttachedToWindow;

    public VideoCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildImageCardView(styleResId);
    }

    public VideoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getStyledContext(context, attrs, defStyleAttr), attrs, defStyleAttr);
        buildImageCardView(getImageCardViewStyle(context, attrs, defStyleAttr));
    }

    private void buildImageCardView(int styleResId) {
        // Make sure the ImageCardView is focusable.
        setFocusable(true);
        setFocusableInTouchMode(true);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.lb_video_card_view, this);
        TypedArray cardAttrs = getContext().obtainStyledAttributes(styleResId, R.styleable.lbImageCardView);
        int cardType = cardAttrs.getInt(R.styleable.lbImageCardView_lbImageCardViewType, CARD_TYPE_FLAG_IMAGE_ONLY);
        boolean hasImageOnly = cardType == CARD_TYPE_FLAG_IMAGE_ONLY;
        boolean hasTitle = (cardType & CARD_TYPE_FLAG_TITLE) == CARD_TYPE_FLAG_TITLE;
        boolean hasContent = (cardType & CARD_TYPE_FLAG_CONTENT) == CARD_TYPE_FLAG_CONTENT;
        boolean hasIconRight = (cardType & CARD_TYPE_FLAG_ICON_RIGHT) == CARD_TYPE_FLAG_ICON_RIGHT;
        boolean hasIconLeft = !hasIconRight && (cardType & CARD_TYPE_FLAG_ICON_LEFT) == CARD_TYPE_FLAG_ICON_LEFT;

        mPreviewCard = (PreviewCardView) findViewById(R.id.layout_preview_card);

        mPreviewCard.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Timber.e("HAS FOCUS: " + hasFocus);
            }
        });

        mInfoArea = (ViewGroup) findViewById(R.id.info_field);
        if (hasImageOnly) {
            removeView(mInfoArea);
            cardAttrs.recycle();
            return;
        }
        // Create children
        if (hasTitle) {
            mTitleView = (TextView) inflater.inflate(R.layout.lb_image_card_view_themed_title, mInfoArea, false);
            mInfoArea.addView(mTitleView);
        }

        if (hasContent) {
            mContentView = (TextView) inflater.inflate(R.layout.lb_image_card_view_themed_content, mInfoArea, false);
            mInfoArea.addView(mContentView);
        }

        if (hasIconRight || hasIconLeft) {
            int layoutId = R.layout.lb_image_card_view_themed_badge_right;
            if (hasIconLeft) {
                layoutId = R.layout.lb_image_card_view_themed_badge_left;
            }
            mBadgeImage = (ImageView) inflater.inflate(layoutId, mInfoArea, false);
            mInfoArea.addView(mBadgeImage);
        }

        // Set up LayoutParams for children
        if (hasTitle && !hasContent && mBadgeImage != null) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mTitleView
                    .getLayoutParams();
            // Adjust title TextView if there is an icon but no content
            if (hasIconLeft) {
                relativeLayoutParams.addRule(RelativeLayout.END_OF, mBadgeImage.getId());
            } else {
                relativeLayoutParams.addRule(RelativeLayout.START_OF, mBadgeImage.getId());
            }
            mTitleView.setLayoutParams(relativeLayoutParams);
        }

        // Set up LayoutParams for children
        if (hasContent) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mContentView
                    .getLayoutParams();
            if (!hasTitle) {
                relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            // Adjust content TextView if icon is on the left
            if (hasIconLeft) {
                relativeLayoutParams.removeRule(RelativeLayout.START_OF);
                relativeLayoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START);
                relativeLayoutParams.addRule(RelativeLayout.END_OF, mBadgeImage.getId());
            }
            mContentView.setLayoutParams(relativeLayoutParams);
        }

        if (mBadgeImage != null) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) mBadgeImage
                    .getLayoutParams();
            if (hasContent) {
                relativeLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mContentView.getId());
            } else if (hasTitle) {
                relativeLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mTitleView.getId());
            }
            mBadgeImage.setLayoutParams(relativeLayoutParams);
        }

        // Backward compatibility: Newly created ImageCardViews should change
        // the InfoArea's background color in XML using the corresponding style.
        // However, since older implementations might make use of the
        // 'infoAreaBackground' attribute, we have to make sure to support it.
        // If the user has set a specific value here, it will differ from null.
        // In this case, we do want to override the value set in the style.
        Drawable background = cardAttrs.getDrawable(R.styleable.lbImageCardView_infoAreaBackground);
        if (null != background) {
            setInfoAreaBackground(background);
        }
        // Backward compatibility: There has to be an icon in the default
        // version. If there is one, we have to set it's visibility to 'GONE'.
        // Disabling 'adjustIconVisibility' allows the user to set the icon's
        // visibility state in XML rather than code.
        if (mBadgeImage != null && mBadgeImage.getDrawable() == null) {
            mBadgeImage.setVisibility(View.GONE);
        }
        cardAttrs.recycle();
    }

    private static Context getStyledContext(Context context, AttributeSet attrs, int defStyleAttr) {
        int style = getImageCardViewStyle(context, attrs, defStyleAttr);
        return new ContextThemeWrapper(context, style);
    }

    private static int getImageCardViewStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        // Read style attribute defined in XML layout.
        int style = null == attrs ? 0 : attrs.getStyleAttribute();
        if (0 == style) {
            // Not found? Read global ImageCardView style from Theme attribute.
            TypedArray styledAttrs = context.obtainStyledAttributes(R.styleable.LeanbackTheme);
            style = styledAttrs.getResourceId(R.styleable.LeanbackTheme_imageCardViewStyle, 0);
            styledAttrs.recycle();
        }
        return style;
    }

    public VideoCardView(Context context) {
        this(context, null);
    }

    public VideoCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageCardViewStyle);
    }

    /**
     * Returns the main image view.
     */
    public final ImageView getMainImageView() {
        return mPreviewCard.getImageView();
    }

    /**
     * Sets the layout dimensions of the ImageView.
     */
    public void setMainContainerDimensions(int width, int height) {
        ViewGroup.LayoutParams lp = mPreviewCard.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mPreviewCard.setLayoutParams(lp);
    }

    /**
     * Sets the info area background drawable.
     */
    public void setInfoAreaBackground(Drawable drawable) {
        if (mInfoArea != null) {
            mInfoArea.setBackground(drawable);
        }
    }

    public void setVideoUrl(String url) {
        mPreviewCard.setVideoUrl(url);
    }

    public void startVideo() {
        mPreviewCard.setLoading();
    }

    public void stopVideo() {
        mPreviewCard.setFinished();
    }

    /**
     * Sets the title text.
     */
    public void setTitleText(CharSequence text) {
        if (mTitleView == null) {
            return;
        }
        mTitleView.setText(text);
    }

    /**
     * Sets the content text.
     */
    public void setContentText(CharSequence text) {
        if (mContentView == null) {
            return;
        }
        mContentView.setText(text);
    }

    private void fadeIn() {
        ImageView mImageView = mPreviewCard.getImageView();
        mImageView.setAlpha(0f);
        if (mAttachedToWindow) {
            mImageView.animate().alpha(1f)
                    .setDuration(mImageView.getResources().getInteger(android.R.integer.config_shortAnimTime));
        }
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        ImageView mImageView = mPreviewCard.getImageView();
        if (mImageView.getAlpha() == 0) {
            fadeIn();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        ImageView mImageView = mPreviewCard.getImageView();
        mAttachedToWindow = false;
        mImageView.animate().cancel();
        mImageView.setAlpha(1f);
        super.onDetachedFromWindow();
    }

}
