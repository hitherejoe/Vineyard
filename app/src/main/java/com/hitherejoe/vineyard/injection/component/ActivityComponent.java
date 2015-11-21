package com.hitherejoe.vineyard.injection.component;

import com.hitherejoe.vineyard.injection.scope.PerActivity;
import com.hitherejoe.vineyard.ui.activity.ConnectActivity;
import com.hitherejoe.vineyard.ui.activity.GuidedStepActivity;
import com.hitherejoe.vineyard.ui.activity.LauncherActivity;
import com.hitherejoe.vineyard.ui.activity.MainActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.fragment.AutoLoopStepFragment;
import com.hitherejoe.vineyard.ui.fragment.MainFragment;
import com.hitherejoe.vineyard.ui.fragment.PlaybackOverlayFragment;
import com.hitherejoe.vineyard.ui.fragment.SearchFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = ApplicationComponent.class)
public interface ActivityComponent {

    void inject(ConnectActivity connectActivity);
    void inject(LauncherActivity launcherActivity);
    void inject(MainActivity mainActivity);
    void inject(PlaybackActivity playbackActivity);
    void inject(GuidedStepActivity guidedStepActivity);
    void inject(MainFragment mainFragment);
    void inject(SearchFragment searchFragment);
    void inject(AutoLoopStepFragment autoLoopStepFragment);
    void inject(PlaybackOverlayFragment playbackOverlayFragment);
}