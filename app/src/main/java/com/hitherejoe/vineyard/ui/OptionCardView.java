package com.hitherejoe.vineyard.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v7.widget.CardView;
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

public class OptionCardView extends BaseCardView {

    RelativeLayout mCardLayout;

    ImageView mOptionIcon;

    TextView mOptionTitle;

    TextView mOptionValue;

    public OptionCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildptionCardView(styleResId);

    }

    public OptionCardView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mCardLayout = (RelativeLayout) view.findViewById(R.id.layout_option_card);
        mOptionIcon = (ImageView) view.findViewById(R.id.image_option);
        mOptionTitle = (TextView) view.findViewById(R.id.text_option_title);
        mOptionValue = (TextView) view.findViewById(R.id.text_option_value);
        TypedArray cardAttrs = getContext().obtainStyledAttributes(styleResId, android.support.v17.leanback.R.styleable.lbImageCardView);

        cardAttrs.recycle();
    }

    public void setMainImageDimensions(int width, int height) {
        ViewGroup.LayoutParams lp = mCardLayout.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mCardLayout.setLayoutParams(lp);
    }

    public void setOptionIcon(Drawable drawable) {
        mOptionIcon.setImageDrawable(drawable);
    }

    public void setOptionTitleText(String titleText) {
        mOptionTitle.setText(titleText);
    }

    public void setOptionValueText(String valueText) {
        mOptionValue.setText(valueText);
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

    public OptionCardView(Context context) {
        this(context, null);
    }

    public OptionCardView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v17.leanback.R.attr.imageCardViewStyle);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

}
