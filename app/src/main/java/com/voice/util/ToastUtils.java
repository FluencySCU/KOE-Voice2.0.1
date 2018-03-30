package com.voice.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/3/2/002.
 */

public class ToastUtils {
    public static void show(Context context,CharSequence content){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }

}
