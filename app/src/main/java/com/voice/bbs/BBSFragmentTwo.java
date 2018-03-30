package com.voice.bbs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.voice.CONTEXT;
import com.voice.MainActivity;
import com.voice.R;
import com.voice.activity.LookPicActivity;
import com.voice.activity.NewArticleActivity;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.voice.MainActivity.mContext;
import static com.voice.MainActivity.normalHeadIcon;


public class BBSFragmentTwo extends Fragment {

    private View v;
    private RefreshLayout refreshLayout;
    private RecyclerView list;
    private static BBSFragmentTwo bbsFragmentTwo = null;
    private Handler handler;
    private Thread newThread = null;
    private int itemImgId[] = {R.id.article_list_item_img1, R.id.article_list_item_img2, R.id.article_list_item_img3
            , R.id.article_list_item_img4, R.id.article_list_item_img5, R.id.article_list_item_img6
            , R.id.article_list_item_img7, R.id.article_list_item_img8, R.id.article_list_item_img9};

    public static BBSFragmentTwo getInstance() {
        if (null == bbsFragmentTwo)
            bbsFragmentTwo = new BBSFragmentTwo();
        return bbsFragmentTwo;
    }//单例模式


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.article_fragment, null);
        init();
        initView();
        return v;
    }

    private void init() {
        ImageButton add = v.findViewById(R.id.newArticle);
        list = v.findViewById(R.id.article_list);
        list.setLayoutManager(new LinearLayoutManager(mContext));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(mContext, NewArticleActivity.class), 250);
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Toast.makeText(mContext, "获取列表失败，请检查网络", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(mContext, "社区还没有任何动态哦,赶紧去发布一个吧", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(mContext, "获取列表项失败，请重试", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(mContext, "获取部分图片失败，请重试", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        refreshLayout=v.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                initView();
                refreshlayout.finishRefresh(2000);
            }
        });
    }

    private void initView() {
        if (null != newThread)
            newThread.interrupt();
        newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().get().url(CONTEXT.url + "/getArticle").build();
                String result = "";
                try {
                    result = okHttpClient.newCall(request).execute().body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(1);
                    return;
                }
                if (result == null || result.equals("[]"))
                    handler.sendEmptyMessage(2);
                //-----------------------------初始化列表项数据-------------------------------------
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(3);
                    return;
                }
                int arrayLength = jsonArray.length();
                HashSet<String> id = new HashSet<>();
                JSONObject jsonObject = null;
                for (int i = 0; i < arrayLength; i++) {
                    try {
                        jsonObject = jsonArray.getJSONObject(i);
                        id.add(jsonObject.getString("account"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                        return;
                    }
                }
                HashMap<String, Bitmap> headIcon = new HashMap<>();
                if (!initIcon(id, headIcon)) {
                    handler.sendEmptyMessage(3);
                    return;
                }
                ArrayList<Map<String, Object>> mDatas = new ArrayList<Map<String, Object>>();//列表项的集合
                //设定每一项
                Map<String, Object> listItem = null;
                for (int i = 0; i < arrayLength; i++) {
                    listItem = new HashMap<String, Object>();//单个列表项
                    try {
                        jsonObject = jsonArray.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(3);
                        return;
                    }
                    try {
                        String user_id = jsonObject.getString("account");
                        listItem.put("id", user_id);
                        listItem.put("headIcon", headIcon.get(user_id));//头像
                        listItem.put("userName", jsonObject.get("name"));
                        listItem.put("time", jsonObject.get("date"));
                        listItem.put("content", jsonObject.get("content"));
                        listItem.put("position", jsonObject.get("position"));
                        listItem.put("lookTimes", "浏览168次");
                        listItem.put("picNum", jsonObject.get("picNum"));
                        mDatas.add(listItem);
                    } catch (JSONException e) {
                        handler.sendEmptyMessage(3);
                        e.printStackTrace();
                        return;
                    }
                }
                CommonAdapter commonAdapter = new CommonAdapter<Map<String, Object>>(mContext, R.layout.article_list_item, mDatas) {
                    @Override
                    protected void convert(ViewHolder holder, Map map, int position) {
                        holder.setOnClickListener(R.id.dianzan, new DianZanListener());
                        holder.setIsRecyclable(false);
                        ImageView head = holder.getView(R.id.article_list_item_userHead);
                        head.setImageBitmap((Bitmap) map.get("headIcon"));
                        String name = (String) map.get("userName");
                        holder.setText(R.id.article_list_item_userName, name);//用户名
                        String date = (String) map.get("time");
                        holder.setText(R.id.article_list_item_time, displayDateString(date));//时间
                        TextView content = holder.getView(R.id.article_list_item_content);
                        String context = (String) map.get("content");
                        if (context.length() == 0) {
                            content.setVisibility(View.GONE);
                        } else {
                            content.setText(context);
                        }//内容
                        holder.setText(R.id.article_list_item_position, (String) map.get("position"));//定位地点
                        holder.setText(R.id.article_list_item_lookTimes, (String) map.get("lookTimes"));//浏览次数
                        int num = (int) map.get("picNum");//图片数量
                        if (num < 7) {
                            LinearLayout l3 = holder.getView(R.id.img_list_3);
                            l3.setVisibility(View.GONE);
                        }//不足7个，隐藏第三排
                        if (num < 4) {
                            LinearLayout l2 = holder.getView(R.id.img_list_2);
                            l2.setVisibility(View.GONE);
                        }//不足4个，隐藏第二排
                        if (num == 0) {
                            LinearLayout l1 = holder.getView(R.id.img_list_1);
                            l1.setVisibility(View.GONE);
                        }//一张图片都没有,全部隐藏
                        else {
                            String id = (String) map.get("id");
                            for (int i = 0; i < num; i++) {
                                ImageView img = holder.getView(itemImgId[i]);
                                Glide.with(mContext).load(CONTEXT.url + "/article_image/" + id + "_" + date + "_" + (i + 1) + ".jpg")
                                        .asBitmap().thumbnail(0.1f).into(img);
                                img.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Bitmap bm =((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                                        Intent intent=new Intent(mContext, LookPicActivity.class);
                                        intent.putExtra("img",bm);
                                        startActivity(intent);
                                    }
                                });
                            }
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Bitmap bitmap = null;
//                                    for (int i = 0; i < num; i++) {
//                                        bitmap = getImage(id, date, i + 1);
//                                        if (null == bitmap) {
//                                            handler.sendEmptyMessage(4);
//                                            continue;
//                                        }
//                                        final Bitmap bitmap1 = bitmap;
//                                        ImageView img = holder.getView(itemImgId[i]);
//                                        handler.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                img.setImageBitmap(bitmap1);
//                                            }
//                                        });
//                                    }//设置所有图片
//                                }
//                            }).start();
                        }
                    }
                };
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(commonAdapter);
                    }
                });
            }
        });
        newThread.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            initView();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String displayDateString(String date) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date date1 = sdf.parse(date);
            Date date2 = new Date();
            long seconds = (date2.getTime() - date1.getTime()) / 1000;//秒
            if (seconds < 60)
                return "刚刚";
            long minutes = (date2.getTime() - date1.getTime()) / (1000 * 60);//分钟
            if (minutes < 60)
                return minutes + "分钟前";
            Calendar cld1 = Calendar.getInstance();
            Calendar cld2 = Calendar.getInstance();
            cld1.setTime(date1);
            cld2.setTime(date2);
            int day1 = cld1.get(Calendar.DAY_OF_MONTH);
            int day2 = cld2.get(Calendar.DAY_OF_MONTH);
            int day = day2 - day1;//日差
            int month1 = cld1.get(Calendar.MONTH) + 1;
            int month2 = cld2.get(Calendar.MONTH) + 1;
            int month = month2 - month1;//月差
            if (month > 0 || day > 2) {
                return month1 + "-" + day1;
            }
            int min = cld1.get(Calendar.MINUTE);
            int hour = cld1.get(Calendar.HOUR_OF_DAY);
            String result = "";
            if (day == 2) {
                result += "前天 ";
            } else if (day == 1) {
                result += "昨天 ";
            } else {
                result += "今天 ";
            }
            if (hour < 10) {
                result += 0 + "" + hour + ":";
            } else {
                result += hour + ":";
            }
            if (min < 10) {
                result += 0 + "" + min;
            } else {
                result += "" + min;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "时间未知";
        }
    }

    /**
     * 获取文章图片
     *
     * @param id
     * @param date
     * @param num
     * @return
     */
    private Bitmap getImage(String id, String date, int num) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(CONTEXT.url + "/article_image/" + id + "_" + date + "_" + num + ".jpg").build();
        byte[] head_b = new byte[0];
        Bitmap bitmap = null;
        try {
            head_b = okHttpClient.newCall(request).execute().body().bytes();
            bitmap = BitmapFactory.decodeByteArray(head_b, 0, head_b.length);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 因为头像都差不多，放在一个map里，一样的就不用重复加载了
     *
     * @param id
     * @param icon
     * @return
     */
    private boolean initIcon(HashSet<String> id, HashMap<String, Bitmap> icon) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = null;
        Bitmap normal = normalHeadIcon;
        Iterator iterator = id.iterator();
        while (iterator.hasNext()) {
            String userId = (String) iterator.next();
            Log.i("asdasdasd", CONTEXT.url + "/head_image/" + userId + ".jpg");
            request = new Request.Builder().url(CONTEXT.url + "/head_image/" + userId + ".jpg").build();
            Bitmap bitmap = null;
            try {
                byte[] head_b = okHttpClient.newCall(request).execute().body().bytes();
                bitmap = BitmapFactory.decodeByteArray(head_b, 0, head_b.length);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (null == bitmap) {
                bitmap = normal;
            }
            icon.put(userId, bitmap);
        }
        return true;
    }

    private class DianZanListener implements View.OnClickListener {
        boolean click = false;

        @Override
        public void onClick(View v) {
            ImageView imageView = (ImageView) v;
            if (click) {
                imageView.setBackgroundResource(R.drawable.dianzan);
                click = false;
            } else {
                imageView.setBackgroundResource(R.drawable.dianzan_1);
                click = true;
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (newThread != null) {
            newThread.interrupt();
        }
        super.onDestroyView();
    }
}
