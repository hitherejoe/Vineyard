package com.hitherejoe.vineyard.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.ui.fragment.MainFragment;


public class IconHeaderItemPresenter extends RowHeaderPresenter {
    private float mUnselectedAlpha;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        mUnselectedAlpha = viewGroup.getResources()
                .getFraction(R.fraction.lb_browse_header_unselect_alpha, 1, 1);
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_icon_header, null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object o) {
        HeaderItem headerItem = ((ListRow) o).getHeaderItem();
        View rootView = viewHolder.view;
        rootView.setAlpha(mUnselectedAlpha);
        setIconDrawable(headerItem.getName(), rootView);
        TextView label = (TextView) rootView.findViewById(R.id.header_label);
        label.setText(headerItem.getName());
    }

    public void setIconDrawable(String name, View rootView) {
        String[] categories = rootView.getResources().getStringArray(R.array.categories);
        TypedArray resources = rootView.getResources().obtainTypedArray(R.array.icons);
        int drawableResource = 0;

        if (name.equals(MainFragment.TAG_POPULAR)) {
            drawableResource = R.drawable.ic_fire;
        } else if (name.equals(MainFragment.TAG_TRENDING)) {
            drawableResource = R.drawable.ic_border_color_white_24dp;
        } else if (name.equals(MainFragment.TAG_OPTIONS)) {
            drawableResource = R.drawable.ic_settings_white_24dp;
        } else {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(name)) {
                    drawableResource = resources.getResourceId(i, 0);
                }
            }
        }
        resources.recycle();

        if (drawableResource != 0) {
            ImageView iconView = (ImageView) rootView.findViewById(R.id.header_icon);
            Drawable icon = rootView.getResources().getDrawable(drawableResource, null);
            iconView.setImageDrawable(icon);
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        // no op
    }

    // TODO: TEMP - remove me when leanback onCreateViewHolder no longer sets the mUnselectAlpha,AND
    // also assumes the xml inflation will return a RowHeaderView
    @Override
    protected void onSelectLevelChanged(RowHeaderPresenter.ViewHolder holder) {
        // this is a temporary fix
        holder.view.setAlpha(mUnselectedAlpha + holder.getSelectLevel() *
                (1.0f - mUnselectedAlpha));
    }

}