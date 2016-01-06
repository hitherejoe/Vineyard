package com.hitherejoe.vineyard.livechannels;

import android.net.Uri;
import android.view.Surface;
import android.view.View;

import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.TvInputProvider;

import java.util.List;

/**
 * Created by guest1 on 1/6/2016.
 */
public class VineInputProvider extends TvInputProvider {
    @Override
    public List<Channel> getAllChannels() {
        return null;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        return null;
    }

    @Override
    public boolean onSetSurface(Surface surface) {
        return false;
    }

    @Override
    public void onSetStreamVolume(float volume) {

    }

    @Override
    public void onRelease() {

    }

    @Override
    public View onCreateOverlayView() {
        return null;
    }

    @Override
    public boolean onTune(Channel channel) {
        return false;
    }
}
