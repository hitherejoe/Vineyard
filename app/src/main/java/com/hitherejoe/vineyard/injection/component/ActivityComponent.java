package com.hitherejoe.vineyard.injection.component;

import com.hitherejoe.vineyard.injection.PerActivity;
import com.hitherejoe.vineyard.injection.module.ActivityModule;
import com.hitherejoe.vineyard.ui.activity.ConnectActivity;
import com.hitherejoe.vineyard.ui.activity.GuidedStepActivity;
import com.hitherejoe.vineyard.ui.activity.MainActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.fragment.AutoLoopStepFragment;
import com.hitherejoe.vineyard.ui.fragment.MainFragment;
import com.hitherejoe.vineyard.ui.fragment.PlaybackOverlayFragment;
import com.hitherejoe.vineyard.ui.fragment.PostGridFragment;
import com.hitherejoe.vineyard.ui.fragment.SearchFragment;

import dagger.Component;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);

    void inject(MainFragment mainFragment);

    void inject(SearchFragment searchFragment);

    void inject(PostGridFragment postGridFragment);

    void inject(ConnectActivity connectActivity);

    void inject(GuidedStepActivity guidedStepActivity);

    void inject(PlaybackActivity playbackActivity);

    void inject(AutoLoopStepFragment autoLoopStepFragment);

    void inject(PlaybackOverlayFragment playbackOverlayFragment);

}
