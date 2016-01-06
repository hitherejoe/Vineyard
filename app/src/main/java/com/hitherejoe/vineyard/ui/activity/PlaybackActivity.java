package com.hitherejoe.vineyard.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.ui.fragment.PlaybackOverlayFragment;
import com.hitherejoe.vineyard.util.DataUtils;
import com.hitherejoe.vineyard.util.NetworkUtil;
import com.hitherejoe.vineyard.util.ToastFactory;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * PlaybackActivity for video playback that loads PlaybackOverlayFragment and handles
 * the MediaSession object used to maintain the state of the media playback.
 */
public class PlaybackActivity extends BaseActivity {

    public static final String AUTO_PLAY = "auto_play";
    public static final String POST = "post";
    public static final String POST_LIST = "postList";
    public static final String EXTRA_IS_LOOP_ENABLED = "EXTRA_IS_LOOP_ENABLED";
    private boolean mWasSkipPressed;
    private boolean mIsAutoLoopEnabled;

    @Inject DataManager mDataManager;

    @Bind(R.id.videoView)
    VideoView mVideoView;

    public enum LeanbackPlaybackState {
        PLAYING, PAUSED, IDLE
    }

    private ArrayList<Post> mPostsList;
    private LeanbackPlaybackState mPlaybackState;
    private MediaPlayer mMediaPlayer;
    private MediaSession mSession;
    private Post mCurrentPost;

    private int mPosition;
    private int mCurrentItem;
    private long mStartTimeMillis;
    private long mDuration;

    public static Intent newStartIntent(Context context, Post post, ArrayList<Post> postList) {
        Intent intent = new Intent(context, PlaybackActivity.class);
        intent.putExtra(POST, post);
        intent.putParcelableArrayListExtra(POST_LIST, postList);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaybackState = LeanbackPlaybackState.IDLE;
        mPosition = 0;
        mDuration = -1;
        mWasSkipPressed = false;

        createMediaSession();
        setContentView(R.layout.playback_controls);
        ButterKnife.bind(this);
        getActivityComponent().inject(this);
        mIsAutoLoopEnabled = mDataManager.getPreferencesHelper().getShouldAutoLoop();

        mCurrentPost = getIntent().getParcelableExtra(PlaybackActivity.POST);
        if (mCurrentPost == null) {
            throw new IllegalArgumentException("PlaybackActivity requires a Post object!");
        }

        mPostsList = getIntent().getExtras().getParcelableArrayList(POST_LIST);
        if (mPostsList != null) {
            for (int i = 0; i < mPostsList.size(); i++) {
                if (mCurrentPost.equals(mPostsList.get(i))) mCurrentItem = i;
            }
        }
        loadViews();
        playPause(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView.isPlaying()) {
            if (!requestVisibleBehind(true)) {
                // Try to play behind launcher, but if it fails, stop playback.
                playPause(false);
            }
        } else {
            requestVisibleBehind(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        playPause(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayback();
        mVideoView.suspend();
        mVideoView.setVideoURI(null);
        mSession.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
            mWasSkipPressed = true;
            getMediaController().getTransportControls().skipToNext();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
            mWasSkipPressed = true;
            getMediaController().getTransportControls().skipToPrevious();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onVisibleBehindCanceled() {
        playPause(false);
        super.onVisibleBehindCanceled();
    }

    @Override
    public boolean onSearchRequested() {
        return true;
    }

    private void loadViews() {
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);
        setVideoPath(mCurrentPost.videoUrl);
        updateMetadata(mCurrentPost);
    }

    private void setPosition(int position) {
        if (position > mDuration) {
            mPosition = (int) mDuration;
        } else if (position < 0) {
            mPosition = 0;
        } else {
            mPosition = position;
        }
        mStartTimeMillis = System.currentTimeMillis();

    }

    private void createMediaSession() {
        if (mSession == null) {
            mSession = new MediaSession(this, getString(R.string.app_name));
            mSession.setCallback(new MediaSessionCallback());
            mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            mSession.setActive(true);
            setMediaController(new MediaController(this, mSession.getSessionToken()));
        }
    }

    private void playPause(boolean doPlay) {
        if (mPlaybackState == LeanbackPlaybackState.IDLE) setupCallbacks();

        if (doPlay && mPlaybackState != LeanbackPlaybackState.PLAYING) {
            mPlaybackState = LeanbackPlaybackState.PLAYING;
            if (mPosition > 0) {
                mVideoView.seekTo(mPosition);
            }
            mVideoView.start();
            mStartTimeMillis = System.currentTimeMillis();
        } else {
            mPlaybackState = LeanbackPlaybackState.PAUSED;
            int timeElapsedSinceStart = (int) (System.currentTimeMillis() - mStartTimeMillis);
            setPosition(mPosition + timeElapsedSinceStart);
            mVideoView.pause();
        }
        updatePlaybackState();
    }

    private void updatePlaybackState() {
        PlaybackState.Builder stateBuilder =
                new PlaybackState.Builder().setActions(getAvailableActions());
        int state = PlaybackState.STATE_PLAYING;
        if (mPlaybackState == LeanbackPlaybackState.PAUSED
                || mPlaybackState == LeanbackPlaybackState.IDLE) {
            state = PlaybackState.STATE_PAUSED;
        }
        stateBuilder.setState(state, mPosition, 1.0f);
        mSession.setPlaybackState(stateBuilder.build());
    }

    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH |
                PlaybackState.ACTION_SKIP_TO_NEXT |
                PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                PlaybackState.ACTION_FAST_FORWARD |
                PlaybackState.ACTION_REWIND;

        if (mPlaybackState == LeanbackPlaybackState.PLAYING) actions |= PlaybackState.ACTION_PAUSE;
        return actions;
    }

    private void updateMetadata(Post post) {
        final MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();

        String title = post.description.replace("_", " -");
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, post.postId);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
                post.username);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION,
                post.description);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI,
                post.avatarUrl);
        metadataBuilder.putLong(MediaMetadata.METADATA_KEY_DURATION, mDuration);

        // And at minimum the title and artist for legacy support
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, post.username);

        Glide.with(this)
                .load(post.avatarUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(500, 500) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                        mSession.setMetadata(metadataBuilder.build());
                    }
                });
    }

    private void setupCallbacks() {
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mVideoView.stopPlayback();
                mPlaybackState = LeanbackPlaybackState.IDLE;
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer = mp;
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    mVideoView.start();
                }
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!mIsAutoLoopEnabled) {
                    mPlaybackState = LeanbackPlaybackState.IDLE;
                } else {
                    //TODO: It'd be better to use the MediaPlayer looping functionality, but
                    // this broke the seek bar progress due to the gap between loops...
                    mMediaPlayer.start();
                    PlaybackState.Builder stateBuilder =
                            new PlaybackState.Builder().setActions(getAvailableActions());
                    stateBuilder.setState(PlaybackOverlayFragment.STATE_LOOPING, 0, 1.0f);
                    mSession.setPlaybackState(stateBuilder.build());
                }
            }
        });
    }

    private void stopPlayback() {
        if (mVideoView != null) mVideoView.stopPlayback();
    }

    private class MediaSessionCallback extends MediaSession.Callback {

        @Override
        public void onPlay() {
            playPause(true);
        }

        @Override
        public void onPause() {
            playPause(false);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            if (mWasSkipPressed || !mIsAutoLoopEnabled) {
                if (NetworkUtil.isNetworkConnected(PlaybackActivity.this)) {
                    for (Post post : mPostsList) {
                        if (post.postId.equals(mediaId)) {
                            setVideoPath(post.videoUrl);
                            mPlaybackState = LeanbackPlaybackState.PAUSED;
                            updateMetadata(post);
                            playPause(extras.getBoolean(AUTO_PLAY));
                        }
                    }
                    mWasSkipPressed = false;
                } else {
                    ToastFactory.createWifiErrorToast(PlaybackActivity.this).show();
                    finish();
                }
            }
        }

        @Override
        public void onSkipToNext() {
            if (mWasSkipPressed || !mIsAutoLoopEnabled) {
                PlaybackState.Builder stateBuilder =
                        new PlaybackState.Builder().setActions(getAvailableActions());
                stateBuilder.setState(PlaybackState.STATE_SKIPPING_TO_NEXT, 0, 1.0f);
                mSession.setPlaybackState(stateBuilder.build());
                if (mCurrentItem++ >= mPostsList.size()) {
                    mCurrentItem = 0;
                }
                Bundle bundle = new Bundle(1);
                bundle.putBoolean(PlaybackActivity.AUTO_PLAY, true);

                String nextId = mPostsList.get(mCurrentItem).postId;
                getMediaController().getTransportControls().playFromMediaId(nextId, bundle);
            }
        }

        @Override
        public void onSkipToPrevious() {
            if (mWasSkipPressed || !mIsAutoLoopEnabled) {
                PlaybackState.Builder stateBuilder =
                        new PlaybackState.Builder().setActions(getAvailableActions());
                stateBuilder.setState(PlaybackState.STATE_SKIPPING_TO_PREVIOUS, 0, 1.0f);
                mSession.setPlaybackState(stateBuilder.build());

                if (mCurrentItem-- < 0) mCurrentItem = mPostsList.size() - 1;
                Bundle bundle = new Bundle(1);
                bundle.putBoolean(PlaybackActivity.AUTO_PLAY, true);

                String prevId = mPostsList.get(mCurrentItem).postId;
                getMediaController().getTransportControls().playFromMediaId(prevId, bundle);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            setPosition((int) pos);
            mVideoView.seekTo(mPosition);
            updatePlaybackState();
        }

        @Override
        public void onFastForward() {
            if (mDuration != -1) {
                // Fast forward 2 seconds.
                setPosition(mVideoView.getCurrentPosition() + (2 * 1000));
                mVideoView.seekTo(mPosition);
                updatePlaybackState();
            }
        }

        @Override
        public void onRewind() {
            // rewind 2 seconds
            setPosition(mVideoView.getCurrentPosition() - (2 * 1000));
            mVideoView.seekTo(mPosition);
            updatePlaybackState();
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            if (action.equals(PlaybackOverlayFragment.CUSTOM_ACTION_LOOP)) {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mIsAutoLoopEnabled = extras.getBoolean(EXTRA_IS_LOOP_ENABLED);
                }
            } else if (action.equals(PlaybackOverlayFragment.CUSTOM_ACTION_SKIP_VIDEO)) {
                mWasSkipPressed = true;
            }
             super.onCustomAction(action, extras);
        }

    }

    private void setVideoPath(String videoUrl) {
        setPosition(0);
        mVideoView.setVideoPath(videoUrl);
        mStartTimeMillis = 0;
        mDuration = DataUtils.getDuration(videoUrl);
    }
}