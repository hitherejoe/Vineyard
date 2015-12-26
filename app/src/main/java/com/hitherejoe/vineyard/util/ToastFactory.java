package com.hitherejoe.vineyard.util;

import android.content.Context;
import android.widget.Toast;

import com.hitherejoe.vineyard.R;

public class ToastFactory {

    public static Toast createWifiErrorToast(Context context) {
        return Toast.makeText(
                context,
                context.getString(R.string.error_message_wifi_needed),
                Toast.LENGTH_SHORT);
    }

}
