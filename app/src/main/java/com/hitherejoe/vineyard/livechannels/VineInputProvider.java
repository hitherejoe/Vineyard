package com.hitherejoe.vineyard.livechannels;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.felkertech.channelsurfer.LibraryUtils;
import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.MediaPlayerInputProvider;
import com.felkertech.channelsurfer.service.TvInputProvider;
import com.felkertech.channelsurfer.sync.SyncAdapter;
import com.hitherejoe.vineyard.data.remote.VineyardService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by guest1 on 1/6/2016.
 */
public class VineInputProvider extends MediaPlayerInputProvider {
    private static final String TAG = "VineInputProvider";
    private HashMap<Channel, List<Program>> epg;

    public VineInputProvider() {}

    @Override
    public List<Channel> getAllChannels() {
        List<Channel> topics = new ArrayList<>();
        topics.add(new Channel()
                .setName("Popular Vines")
                .setNumber("101"));
        topics.add(new Channel()
                .setName("Editor's Vines")
                .setNumber("102"));
        return topics;
    }

    @Override
    public void performCustomSync(SyncAdapter syncAdapter, String inputId) {

        super.performCustomSync(syncAdapter, inputId);
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        for(int i=0;i<programs;i++) {
            programList.add(new Program.Builder(getGenericProgram(channelInfo))
                            .setVideoHeight(720)
                            .setVideoWidth(720)
                            .setStartTimeUtcMillis((startTimeMs + SEGMENT * i))
                            .setEndTimeUtcMillis((startTimeMs + SEGMENT * (i + 1)))
                            .build()
            );
        }
        return programList;
    }

    @Override
    public View onCreateOverlayView() {
        Log.d(TAG, "onCreateOverlayView");
        return null;
    }

    private Channel mChannel;
    @Override
    public boolean onTune(Channel channel) {
        //Check the channels
        //Popular
        notifyVideoUnavailable(REASON_BUFFERING);
        Log.d(TAG, "Tuning to "+channel.getName());
        Log.d(TAG, "We are playing "+getProgramRightNow(channel));
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        VineyardService vineyardService = VineyardService.Creator.newVineyardService();

        mChannel = channel;
        if(channel.getNumber().equals("101")) {
            vineyardService.getPopularPosts().enqueue(new Callback<VineyardService.PostResponse>() {
                @Override
                public void onResponse(Response<VineyardService.PostResponse> response, Retrofit retrofit) {
                    tuneResponse(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        } else if(channel.getNumber().equals("102")) {
            vineyardService.getEditorsPicksPosts().enqueue(new Callback<VineyardService.PostResponse>() {
                @Override
                public void onResponse(Response<VineyardService.PostResponse> response, Retrofit retrofit) {
                    tuneResponse(response);
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        }
        return true;
    }

    public void tuneResponse(Response<VineyardService.PostResponse> response) {
        VineyardService.PostResponse postResponse = response.body();
        setOverlayEnabled(false);
        try {
            beginPlayingVine(postResponse, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void beginPlayingVine(final VineyardService.PostResponse response, final int index) throws IOException {
        String url = response.data.records.get(index).videoUrl;
        Log.d(TAG, "Switch to " + index + ", " + url);
        try {
            mediaPlayer.setDataSource(url);
        } catch(IllegalStateException exception) {
            Log.e(TAG, exception.getMessage()+" Illegal State Exception in switching channels");
            try {
                Log.d(TAG, "Release media player");
                mediaPlayer.stop();
                mediaPlayer.reset();
                beginPlayingVine(response, index);
                return;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        mediaPlayer.setSurface(mSurface);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d(TAG, "Prepared");
//                mp.setLooping(true);
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG, "Finished loop");
                        try {
                            mp.stop();
                            mp.reset();
                            if (index + 1 < response.data.records.size())
                                beginPlayingVine(response, index + 1);
                            else
                                onTune(mChannel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                notifyVideoAvailable();
                mp.start();
            }
        });
        mediaPlayer.prepare();
    }
}
