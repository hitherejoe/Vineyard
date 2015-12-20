package com.hitherejoe.vineyard.data;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.app.recommendation.ContentRecommendation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/*
 * This class builds up to MAX_RECOMMENDATIONS of ContentRecommendations and defines what happens
 * when they're selected from Recommendations section on the Home screen by creating an Intent.
 */
public class UpdateRecommendationsService extends IntentService {
    private static final String TAG = "UpdateRecommendationsService";
    private static final int MAX_RECOMMENDATIONS = 3;

    private NotificationManager mNotificationManager;

    public UpdateRecommendationsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DataManager mDataManager = VineyardApplication.get(this).getComponent().dataManager();

        mDataManager.getPopularPosts("", "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VineyardService.PostResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("There was an error loading the recommendations", e);
                    }

                    @Override
                    public void onNext(VineyardService.PostResponse postResponse) {
                        handleRecommendations(postResponse.data.records);
                    }
                });
    }

    private void handleRecommendations(List<Post> recommendations) {
        Resources res = getResources();
        int cardWidth = res.getDimensionPixelSize(R.dimen.card_width);
        int cardHeight = res.getDimensionPixelSize(R.dimen.card_height);

        if (recommendations == null) {
            return;
        }

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }

        // This will be used to build up an object for your content recommendation that will be
        // shown on the TV home page along with other provider's recommendations.
        final ContentRecommendation.Builder builder = new ContentRecommendation.Builder()
                .setBadgeIcon(R.drawable.vineyard);
        Collections.sort(recommendations);

        for (int i = 0; i < recommendations.size() && i < MAX_RECOMMENDATIONS; i++) {
            Post post = recommendations.get(i);
            builder.setIdTag("Post" + i + 1)
                    .setTitle(post.description)
                    .setText(getString(R.string.header_text_popular))
                    .setContentIntentData(ContentRecommendation.INTENT_TYPE_ACTIVITY,
                            buildPendingIntent((ArrayList<Post>) recommendations, post), 0, null);

                Timber.e(post.thumbnailUrl);
                Glide.with(getApplication())
                        .load(post.thumbnailUrl)
                        .asBitmap()
                        .centerCrop()
                        .into(new SimpleTarget<Bitmap>(cardWidth, cardHeight) {
                            @Override
                            public void onResourceReady(Bitmap resource,
                                                        GlideAnimation<? super Bitmap>
                                                                glideAnimation) {
                                builder.setContentImage(resource);
                            }
                        });

                // Create an object holding all the information used to recommend the content.
                ContentRecommendation rec = builder.build();
                Notification notification = rec.getNotificationObject(getApplicationContext());

                // Recommend the content by publishing the notification.
                mNotificationManager.notify(i + 1, notification);

        }
    }

    private Intent buildPendingIntent(ArrayList<Post> recommendations, Post post) {
        Intent detailsIntent = new Intent(this, PlaybackActivity.class);
        detailsIntent.putExtra(PlaybackActivity.POST, post);
        detailsIntent.putParcelableArrayListExtra(PlaybackActivity.POST_LIST, recommendations);
        detailsIntent.setAction(post.postId);

        return detailsIntent;
    }
}