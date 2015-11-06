package com.hitherejoe.vineyard.injection.component;

import com.hitherejoe.vineyard.injection.scope.PerActivity;
import com.hitherejoe.vineyard.ui.activity.ConnectActivity;
import com.hitherejoe.vineyard.ui.activity.LauncherActivity;
import com.hitherejoe.vineyard.ui.activity.MainActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.fragment.MainFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = ApplicationComponent.class)
public interface ActivityComponent {

    void inject(ConnectActivity connectActivity);
    void inject(LauncherActivity launcherActivity);
    void inject(MainActivity mainActivity);
    void inject(PlaybackActivity playbackActivity);
    void inject(MainFragment mainFragment);
}