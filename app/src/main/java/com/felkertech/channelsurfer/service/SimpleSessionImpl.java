package com.felkertech.channelsurfer.service;

import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.felkertech.channelsurfer.model.Channel;

/**
 * Simple session implementation which plays local videos on the application's tune request.
 */
public class SimpleSessionImpl extends TvInputService.Session {
    private String TAG = "SimpleSession";
    private TvInputProvider tvInputProvider;
    SimpleSessionImpl(TvInputProvider tvInputProvider) {
        super(tvInputProvider);
        this.tvInputProvider = tvInputProvider;
    }
    @Override
    public void onRelease() {
        tvInputProvider.onRelease();
    }
    @Override
    public boolean onSetSurface(Surface surface) {
        return tvInputProvider.onSetSurface(surface);
    }
    @Override
    public void onSetStreamVolume(float volume) {
        tvInputProvider.onSetStreamVolume(volume);
    }

    @Override
    public void onSetCaptionEnabled(boolean enabled) {
        // The sample content does not have caption. Nothing to do in this sample input.
        // NOTE: If the channel has caption, the implementation should turn on/off the caption
        // based on {@code enabled}.
        // For the example implementation for the case, please see {@link RichTvInputService}.
    }
    @Override
    public View onCreateOverlayView() {
        return tvInputProvider.onCreateOverlayView();
    }
    @Override
    public boolean onTune(Uri channelUri) {
        notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
        setOverlayViewEnabled(true);
        Log.d(TAG, "Tuning to " + channelUri.toString());
        String[] projection = {TvContract.Channels.COLUMN_SERVICE_ID, TvContract.Channels.COLUMN_INPUT_ID, TvContract.Channels.COLUMN_DISPLAY_NUMBER};
        //Now look up this channel in the DB
        try (Cursor cursor = tvInputProvider.getContentResolver().query(channelUri, projection, null, null, null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
            cursor.moveToNext();
            Channel channel = new Channel()
                    .setNumber(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NUMBER)))
                    .setName(cursor.getString(cursor.getColumnIndex(TvContract.Channels.COLUMN_DISPLAY_NAME)))
                    .setOriginalNetworkId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID)))
                    .setTransportStreamId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID)))
                    .setServiceId(cursor.getInt(cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID)))
                    .setVideoHeight(1080)
                    .setVideoWidth(1920);
            return tvInputProvider.onTune(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}