package com.hitherejoe.vineyard.livechannels;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import channelsurfer.model.Channel;
import channelsurfer.model.Program;
import channelsurfer.service.TvInputProvider;
import com.hitherejoe.vineyard.data.remote.VineyardService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by guest1 on 1/6/2016.
 */
public class VineInputProvider extends TvInputProvider {
    private MediaPlayer mediaPlayer;
    private Surface mSurface;
    private static final String TAG = "VineInputProvider";

    public VineInputProvider() {}

    @Override
    public List<Channel> getAllChannels() {
        List<Channel> topics = new ArrayList<>();
        topics.add(new Channel()
                .setName("Popular Vines")
                .setServiceId(1)
                .setOriginalNetworkId(1)
                .setTransportStreamId(1)
                .setNumber("101"));
        topics.add(new Channel()
                .setName("Editor's Vines")
                .setServiceId(2)
                .setOriginalNetworkId(2)
                .setTransportStreamId(2)
                .setNumber("102"));
        return topics;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        for(int i=0;i<programs;i++) {
            programList.add(getGenericProgram(channelInfo)
                    .setStartTimeUtcMillis((startTimeMs + SEGMENT * i))
                    .setEndTimeUtcMillis((startTimeMs + SEGMENT * (i + 1)))
            );
        }
        return programList;
    }

    @Override
    public boolean onSetSurface(Surface surface) {
        mSurface = surface;
        return true;
    }

    @Override
    public void onSetStreamVolume(float volume) {
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        mediaPlayer.setVolume(volume, volume);
    }

    @Override
    public void onRelease() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
    }

    @Override
    public View onCreateOverlayView() {
        Log.d(TAG, "onCreateOverlayView");
        return null;
    }

    @Override
    public boolean onTune(Channel channel) {
        //Check the channels
        //Popular
        notifyVideoUnavailable(REASON_BUFFERING);
        Log.d(TAG, "Tuning to "+channel.getName());
        if(mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        VineyardService vineyardService = VineyardService.Creator.newVineyardService();

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
        String url = postResponse.data.records.get(0).videoUrl;
//        Log.d(TAG, "Play " + url);
        notifyVideoAvailable();
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
//                mediaPlayer = null;
                beginPlayingVine(response, index);
                return;
            } catch(Exception e) {
                e.printStackTrace();
            }
//            mSurface = null;
//            return;
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
                                beginPlayingVine(response, 0); //Loop to the beginning. Changing channels will reset data (or we can do that ourselves later TODO)
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mp.start();
            }
        });
        mediaPlayer.prepare();
    }
}
