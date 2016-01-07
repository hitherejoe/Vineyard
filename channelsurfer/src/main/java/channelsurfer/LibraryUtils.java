package channelsurfer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import channelsurfer.service.TvInputProvider;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by guest1 on 1/6/2016.
 */
public class LibraryUtils {
    private static String TAG = "LibraryUtils";
    /**
        Returns the TvInputProvider that was defined by the project's manifest
     **/
    public static TvInputProvider getTvInputProvider(Context mContext, final TvInputProviderCallback callback) {
        ApplicationInfo app = null;
        try {
            app = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            final String service = bundle.getString("TvInputService");
            try {
                Log.d(TAG, "Constructors: "+Class.forName(service).getConstructors().length);
                Log.d(TAG, "Constructor 1: " + Class.forName(service).getConstructors()[0].toString());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        TvInputProvider provider = null;
                        try {
                            provider = (TvInputProvider) Class.forName(service).getConstructors()[0].newInstance();
                            Log.d(TAG, provider.toString());
                            callback.onTvInputProviderCallback(provider);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface TvInputProviderCallback {
        void onTvInputProviderCallback(TvInputProvider provider);
    }
}
