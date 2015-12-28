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

import butterknife.Bind;
import butterknife.ButterKnife;

public class IconCardView extends BaseCardView {

    @Bind(R.id.layout_option_card)
    RelativeLayout mLayout;

    @Bind(R.id.image_option)
    ImageView mIcon;

    @Bind(R.id.text_option_title)
    TextView mTitle;

    @Bind(R.id.text_option_value)
    TextView mValue;

    public IconCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildptionCardView(styleResId);

    }

    public IconCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getStyledContext(context, attrs, defStyleAttr), attrs, defStyleAttr);
        buildptionCardView(getImageCardViewStyle(context, attrs, defStyleAttr));
    }

    private void buildptionCardView(int styleResId) {
        // Make sure the ImageCardView is focusable.
        setFocusable(true);
        setFocusableInTouchMode(true);
        //setCardType(CARD_TYPE_INFO_UNDER);
        setCardType(CARD_TYPE_MAIN_ONLY);
        setBackgroundResource(R.color.primary_dark);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_options_item, this);
        ButterKnife.bind(view);
        TypedArray cardAttrs =
                getContext().obtainStyledAttributes(
                        styleResId, android.support.v17.leanback.R.styleable.lbImageCardView);
        cardAttrs.recycle();
    }

    public void setMainImageDimensions(int width, int height) {
        ViewGroup.LayoutParams lp = mLayout.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mLayout.setLayoutParams(lp);
    }

    public void setOptionIcon(Drawable drawable) {
        mIcon.setImageDrawable(drawable);
    }

    public void setOptionTitleText(String titleText) {
        mTitle.setText(titleText);
    }

    public void setOptionValueText(String valueText) {
        mValue.setText(valueText);
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
            TypedArray styledAttrs =
                    context.obtainStyledAttributes(
                            android.support.v17.leanback.R.styleable.LeanbackTheme);
            style = styledAttrs.getResourceId(
                    android.support.v17.leanback.R.styleable.LeanbackTheme_imageCardViewStyle, 0);
            styledAttrs.recycle();
        }
        return style;
    }

    public IconCardView(Context context) {
        this(context, null);
    }

    public IconCardView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v17.leanback.R.attr.imageCardViewStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

}
