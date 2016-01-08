package com.hitherejoe.vineyard.livechannels;

import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.felkertech.channelsurfer.model.Channel;
import com.felkertech.channelsurfer.model.Program;
import com.felkertech.channelsurfer.service.MediaPlayerInputProvider;
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
public class VineInputProvider extends MediaPlayerInputProvider {
    private static final String TAG = "VineInputProvider";

    public VineInputProvider() {}

    @Override
    public List<Channel> getAllChannels() {
        List<Channel> topics = new ArrayList<>();
        for(int i = 101; i<=116; i++) {
            topics.add(buildChannel(i));
        }
        return topics;
    }

    public Channel buildChannel(int number) {
        String title = "";
        switch(number) {
            case 101:
                title = "Popular Vines";
                break;
            case 102:
                title = "Editor's Vines";
                break;
            case 103:
                title = "Scary Vines";
                break;
            case 104:
                title = "Comedy Vines";
                break;
            case 105:
                title = "Animal Vines";
                break;
            case 106:
                title = "Music Vines";
                break;
            case 107:
                title = "Art Vines";
                break;
            case 108:
                title = "Dance Vines";
                break;
            case 109:
                title = "Sports Vines";
                break;
            case 110:
                title = "OMG Vines";
                break;
            case 111:
                title = "Style Vines";
                break;
            case 112:
                title = "Family Vines";
                break;
            case 113:
                title = "Food Vines";
                break;
            case 114:
                title = "DIY Vines";
                break;
            case 115:
                title = "Places Vines";
                break;
            case 116:
                title = "News Vines";
                break;
        }
        return new Channel()
                .setName(title)
                .setNumber(number + "");
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channelInfo, long startTimeMs, long endTimeMs) {
        int programs = (int) ((endTimeMs-startTimeMs)/1000/60/60); //Hour long segments
        int SEGMENT = 1000*60*60; //Hour long segments
        List<Program> programList = new ArrayList<>();
        for(int i=0;i<programs;i++) {
            programList.add(new Program.Builder(getGenericProgram(channelInfo))
                            .setStartTimeUtcMillis((getNearestHour() + SEGMENT * i))
                            .setEndTimeUtcMillis((getNearestHour() + SEGMENT * (i + 1)))
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
        } else {
            String tag = channel.getName().substring(0, channel.getName().indexOf(" "));
            if(channel.getNumber().equals("105"))
                tag = "Animals";
            vineyardService.getPostsByTag(tag).enqueue(new Callback<VineyardService.PostResponse>() {
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
        notifyVideoUnavailable(REASON_BUFFERING);
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
