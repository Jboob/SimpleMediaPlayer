package com.ran.media.view;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author Ran
 * @date 2018/10/11.
 */

public class JToast {

    private static JToast jToast;

    private static Toast toast;

    private static Handler mHandler;

    private JToast() {
    }

    public synchronized static JToast getInstance() {
        if (null == jToast) {
            jToast = new JToast();
            mHandler = new Handler();
        }
        return jToast;
    }


    public void show(Context ctx, CharSequence content, int time) {
        time = time < 1000 ? 2000 : time;
        mHandler.removeCallbacks(runnable);
        showToast(ctx, content, time);
        mHandler.postDelayed(runnable, time);
    }


    private void showToast(Context ctx, CharSequence content, int time) {
        Context applicationContext = ctx.getApplicationContext();
        if (!TextUtils.isEmpty(content)) {
            if (null == toast) {
                toast = Toast.makeText(applicationContext, content, Toast.LENGTH_SHORT);
            }
            toast.setText(content);
            toast.show();
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            JToast.toast.cancel();
        }
    };

}
