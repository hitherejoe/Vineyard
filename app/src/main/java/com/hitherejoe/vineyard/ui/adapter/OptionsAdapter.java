package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.hitherejoe.vineyard.data.model.Option;
import com.hitherejoe.vineyard.ui.presenter.OptionItemPresenter;

public class OptionsAdapter extends ArrayObjectAdapter {

    private OptionItemPresenter mCardPresenter;

    public OptionsAdapter(Context context) {
        mCardPresenter = new OptionItemPresenter(context);
        setPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                return mCardPresenter;
            }
        });
    }

    public void addOption(Option option) {
        add(option);
    }

    public void updateOption(Option option) {
        Option first = (Option) get(0);
        first.value = option.value;
        notifyChanged();
    }
}
