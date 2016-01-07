package channelsurfer.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.util.Log;

/**
 * Static helper methods for working with the SyncAdapter framework.
 */
public class SyncUtils {
    private static final String TAG = "cumulus:SyncUtils";
    private static final String CONTENT_AUTHORITY = TvContract.AUTHORITY;

    public static void setUpPeriodicSync(Context context, String inputId) {
        Account account = DummyAccountService.getAccount();
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (!accountManager.addAccountExplicitly(account, null, null)) {
            Log.w(TAG, "Account already exists.");
        }
        ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
        Bundle bundle = new Bundle();
        bundle.putString(SyncAdapter.BUNDLE_KEY_INPUT_ID, inputId);
        ContentResolver.addPeriodicSync(account, CONTENT_AUTHORITY, bundle,
                SyncAdapter.SYNC_FREQUENCY_SEC/6); //Sync every hour b/c why not?
    }

    public static void requestSync(String inputId) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putString(SyncAdapter.BUNDLE_KEY_INPUT_ID, inputId);
        Log.d(TAG, "Request sync");
        ContentResolver.requestSync(DummyAccountService.getAccount(), CONTENT_AUTHORITY,
                bundle);
    }
}
