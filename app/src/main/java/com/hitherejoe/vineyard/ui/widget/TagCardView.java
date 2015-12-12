package com.hitherejoe.vineyard.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.hitherejoe.vineyard.R;

public class TagCardView extends BaseCardView {

    private TextView mTagNameText;
    private ImageView mResultImage;

    public TagCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildLoadingCardView(styleResId);
    }

    public TagCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getStyledContext(context, attrs, defStyleAttr), attrs, defStyleAttr);
        buildLoadingCardView(getImageCardViewStyle(context, attrs, defStyleAttr));
    }

    private void buildLoadingCardView(int styleResId) {
        // Make sure the ImageCardView is focusable.
        setFocusable(false);
        setFocusableInTouchMode(false);
        setCardType(CARD_TYPE_MAIN_ONLY);
        setBackgroundResource(R.color.primary_light);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.view_tag_card, this);
        TypedArray cardAttrs = getContext().obtainStyledAttributes(styleResId, android.support.v17.leanback.R.styleable.lbImageCardView);

        mTagNameText = (TextView) findViewById(R.id.text_tag_name);
        mResultImage = (ImageView) findViewById(R.id.image_icon);
        cardAttrs.recycle();
    }

    public void setCardText(String string) {
        mTagNameText.setText(string);
    }

    public void setCardIcon(int resource) {
        mResultImage.setImageDrawable(ContextCompat.getDrawable(getContext(), resource));
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
            TypedArray styledAttrs = context.obtainStyledAttributes(android.support.v17.leanback.R.styleable.LeanbackTheme);
            style = styledAttrs.getResourceId(android.support.v17.leanback.R.styleable.LeanbackTheme_imageCardViewStyle, 0);
            styledAttrs.recycle();
        }
        return style;
    }

    public TagCardView(Context context) {
        this(context, null);
    }

    public TagCardView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v17.leanback.R.attr.imageCardViewStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

}
