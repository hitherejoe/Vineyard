package com.felkertech.channelsurfer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.felkertech.channelsurfer.service.TvInputProvider;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by guest1 on 1/6/2016.
 */
public class LibraryUtils {
    /**
        Returns the TvInputProvider that was defined by the project's manifest
     **/
    public static TvInputProvider getTvInputProvider(Context mContext) {
        ApplicationInfo app = null;
        try {
            app = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;
            String service = bundle.getString("TvInputService");
            TvInputProvider provider = (TvInputProvider) Class.forName(service).getConstructors()[0].newInstance();
            return provider;
        } catch (PackageManager.NameNotFoundException | ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
