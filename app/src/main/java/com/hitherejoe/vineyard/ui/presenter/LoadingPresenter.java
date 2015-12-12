package com.hitherejoe.vineyard.ui.presenter;

import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.hitherejoe.vineyard.ui.widget.LoadingCardView;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Loading CardView
 */
public class LoadingPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LoadingCardView cardView = new LoadingCardView(parent.getContext());
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof LoadingCardView){
            LoadingCardView cardView = (LoadingCardView) viewHolder.view;
            cardView.isLoading(true);
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        if (viewHolder.view instanceof LoadingCardView) {
            LoadingCardView cardView = (LoadingCardView) viewHolder.view;
            cardView.isLoading(false);
        }
    }
}