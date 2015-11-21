package com.hitherejoe.vineyard.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.GuidedStepActivity;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class AutoLoopStepFragment extends GuidedStepFragment {

    @Inject
    DataManager mDatamanager;

    private static final int ENABLED = 0;
    private static final int DISABLED = 1;
    private static final int OPTION_CHECK_SET_ID = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        boolean isAutoLoopEnabled = mDatamanager.getPreferencesHelper().getShouldAutoLoop();
        updateActions(isAutoLoopEnabled);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int onProvideTheme() {
        return R.style.Theme_Example_Leanback_GuidedStep_First;
    }

    private static void addCheckedAction(List<GuidedAction> actions, long id,
                                         String title, String desc, boolean checked) {
        GuidedAction guidedAction = new GuidedAction.Builder()
                .id(id)
                .title(title)
                .description(desc)
                .checkSetId(OPTION_CHECK_SET_ID)
                .build();
        guidedAction.setChecked(checked);
        actions.add(guidedAction);
    }

    private void updateActions(boolean shouldAutoLoop) {
        List<GuidedAction> actions = getActions();
        for (int i = 0; i < actions.size(); i++) {
            GuidedAction action = actions.get(i);
            if (action.getId() == ENABLED) {
                action.setChecked(shouldAutoLoop);
            } else if (action.getId() == DISABLED) {
                action.setChecked(!shouldAutoLoop);
            }
        }
    }

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        Timber.e(mDatamanager.getPreferencesHelper().getShouldAutoLoop() + "");
        String title = getString(R.string.guided_step_auto_loop_title);
        String description = getString(R.string.guided_step_auto_loop_description);
        Drawable icon = getActivity().getDrawable(R.drawable.lopp);
        return new GuidanceStylist.Guidance(title, description, "", icon);
    }

    @Override
    public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        if (getActivity() instanceof GuidedStepActivity) {
            addCheckedAction(actions, ENABLED,
                    getResources().getString(R.string.guided_step_auto_loop_enabled),
                    getResources().getString(R.string.guided_step_auto_loop_enabled_description),
                    false);
            addCheckedAction(actions, DISABLED,
                    getResources().getString(R.string.guided_step_auto_loop_disabled),
                    getResources().getString(R.string.guided_step_auto_loop_disabled_description),
                    false);
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action != null) {
            Timber.e(action.getTitle().toString() + " " + action.getId() + "");
            mDatamanager.getPreferencesHelper().putAutoLoop(action.getId() == ENABLED);
            Timber.e(mDatamanager.getPreferencesHelper().getShouldAutoLoop() + "");
            Intent output = new Intent();
            output.putExtra(MainFragment.RESULT_OPTION, action.getId() == ENABLED);
            getActivity().setResult(MainFragment.REQUEST_CODE_AUTO_LOOP, output);
            getActivity().finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

}