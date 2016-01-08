package com.hitherejoe.vineyard.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.util.NetworkUtil;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ConnectActivity extends BaseActivity {

    @Bind(R.id.button_sign_in)
    Button mSignInButton;

    @Bind(R.id.edit_text_username)
    EditText mUsernameEditText;

    @Bind(R.id.edit_text_password)
    EditText mPasswordEditText;

    @Bind(R.id.progress)
    ProgressBar mSignInProgress;

    private Subscription mSubscription;

    @Inject
    DataManager mDataManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);

        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    @OnClick(R.id.button_sign_in)
    public void onSignInClicked() {
        if (NetworkUtil.isNetworkConnected(this)) {
            replaceButtonWithProgress(true);
            validateData();
        } else {
            showToast(R.string.error_message_network_connection);
        }
    }

    private void validateData() {
        String username = mUsernameEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if (username.length() == 0 && password.length() == 0) {
            showToast(R.string.error_message_sign_in_blank);
            replaceButtonWithProgress(false);
        } else if (username.length() == 0) {
            showToast(R.string.error_message_sign_in_blank_username);
            replaceButtonWithProgress(false);
        } else if (password.length() == 0) {
            showToast(R.string.error_message_sign_in_blank_password);
            replaceButtonWithProgress(false);
        } else {
            login(username, password);
        }
    }

    private void showToast(int messageResource) {
        Toast.makeText(this, getString(messageResource), Toast.LENGTH_SHORT).show();
    }

    private void replaceButtonWithProgress(boolean isLoading) {
        mSignInProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        mSignInButton.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        mUsernameEditText.setEnabled(!isLoading);
        mPasswordEditText.setEnabled(!isLoading);
    }

    private void login(String username, String password) {
        mSubscription = mDataManager.getAccessToken(username, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Authentication>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("error!" + e.getMessage());
                        showToast(R.string.error_message_sign_in);
                        replaceButtonWithProgress(false);
                    }

                    @Override
                    public void onNext(Authentication authentication) {
                        Timber.e(authentication.success + "");
                        if (authentication.success) {
                            startActivity(new Intent(ConnectActivity.this, MainActivity.class));
                        } else {
                            showToast(R.string.error_message_sign_in);
                            replaceButtonWithProgress(false);
                        }
                    }
                });
    }

    @Override
    public boolean onSearchRequested() {
        // Start search activity
        return true;
    }

}
