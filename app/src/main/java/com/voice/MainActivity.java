package com.voice;

import java.io.File;
import java.io.IOException;

import com.imnjh.imagepicker.PickerConfig;
import com.imnjh.imagepicker.SImagePicker;
import com.voice.PickImg.FrescoImageLoader;
import com.voice.bbs.BBSFragment;
import com.voice.bbs.BBSFragmentTwo;
import com.voice.bbs.BBS_MainActivity;
import com.voice.fragment.DynamicFragment;
import com.voice.fragment.NewsFatherFragment;
import com.voice.fragment.SettingFragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.voice.sidebar.Feedback;
import com.voice.sidebar.ResideMenu;
import com.voice.sidebar.ResideMenuItem;
import com.voice.sidebar_calendarview.Characterset;
import com.voice.sidebar_calendarview.SetplanActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends FragmentActivity implements OnClickListener {

    protected static final String TAG = "MainActivity";
    public static Context mContext;
    private ImageButton mNews, mConstact, mDeynaimic, mSetting;
    private View mPopView;
    private View currentButton;
    private Handler handler;
    public static Bitmap normalHeadIcon;


    public static User user;
    public static Hornor hornor = new Hornor(0, "暂无", 0, 0, 0);


    private LinearLayout buttomBarGroup;

    //侧边栏
    ResideMenu resideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        normalHeadIcon=BitmapFactory.decodeResource(getResources(), R.drawable.channel_qq);
        mContext = this;
        handler=new Handler();
        findView();
        init();
        getHeadIcon(CONTEXT.url+"/head_image/" + user.getLogId() + ".jpg");
        //初始化图片选择器
        SImagePicker.init(new PickerConfig.Builder().setAppContext(mContext)
                .setImageLoader(new FrescoImageLoader())
                .setToolbaseColor(getResources().getColor(R.color.colorPrimary)).build());
        // attach to current activity;
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.wel_background);
        resideMenu.attachToActivity(this);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);


        // create menu items;
        String titles[] = {"我的计划", "个性学习", "我的收藏", "个性设置", "VIP特权", "关于"};
        int icon[] = {R.drawable.sidebar_icon_plan_64, R.drawable.sidebar_icon_profile, R.drawable.sidebar_icon_collect, R.drawable.sidebar_icon_individual, R.drawable.sidebar_icon_vip, R.drawable.sidebar_icon_calendar, R.drawable.sidebar_icon_settings};

        for (int i = 0; i < titles.length; i++) {
            ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i], i);
            OnClickListener myOnClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int temp = ((ResideMenuItem) v).getid();
                    if (temp == 0) {
                        Intent intent = new Intent(mContext, SetplanActivity.class);
                        startActivity(intent);
                    }
                    if (temp == 1) {
                        Intent intent = new Intent(mContext, Characterset.class);
                        startActivity(intent);
                    }
                    if (temp == 5) {
                        Intent intent = new Intent(mContext, Feedback.class);
                        startActivity(intent);
                    }
                    //   if(temp==3){
                    //	Intent intent = new Intent(mContext, Characterset.class);
                    //	startActivity(intent);
                    //		}
                }
            };

            item.setOnClickListener(myOnClickListener);
            resideMenu.addMenuItem(item, ResideMenu.DIRECTION_LEFT); // or  ResideMenu.DIRECTION_RIGHT
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void findView() {
        mPopView = LayoutInflater.from(mContext).inflate(R.layout.app_exit, null);
        buttomBarGroup = (LinearLayout) findViewById(R.id.buttom_bar_group);
        mDeynaimic = (ImageButton) findViewById(R.id.buttom_deynaimic);
        mConstact = (ImageButton) findViewById(R.id.buttom_constact);
        mNews = (ImageButton) findViewById(R.id.buttom_news);
        mSetting = (ImageButton) findViewById(R.id.buttom_setting);

    }

    /**
     * 获取用户头像
     *
     * @param url 用户头像对应的url地址
     */
    private void getHeadIcon(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"头像获取失败，请检查网络",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final byte[] head_bt = response.body().bytes();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(head_bt, 0, head_bt.length);
                if (null != bitmap) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            user.setHavaHead(true);
                            user.setHeadIcon(bitmap);
                        }
                    });
                }
                else{
                    user.setHavaHead(false);
                    user.setHeadIcon(null);
                }
            }
        });
    }

    private void init() {
        Intent intent = getIntent();
        Bundle u = intent.getExtras();
        if (u != null) {
            user = (User) u.getSerializable("user");
        }
        mDeynaimic.setOnClickListener(this);
        mConstact.setOnClickListener(this);
        mNews.setOnClickListener(this);
        mSetting.setOnClickListener(this);

        mDeynaimic.performClick();

        File dir = new File("data/data/com.voice/databases");
        File res = new File("data/data/com.voice/video");
        System.out.println("创建数据库。。。。");
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (dir.exists())
                System.out.println("创建文件夹成功");
            else
                System.out.println("创建文件夹失败");
        }
        if (!res.exists()) {
            try {
                res.mkdirs();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (res.exists())
                System.out.println("创建文件夹成功");
            else
                System.out.println("创建文件夹失败");
        }

    }

    private void setButton(View v) {
        if (currentButton != null && currentButton.getId() != v.getId()) {
            currentButton.setEnabled(true);
        }
        v.setEnabled(false);
        currentButton = v;
    }

    Fragment nowFragment;
    Fragment bbsFragment;
    public void onClick(View v) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();


        if (v == mDeynaimic) {
            nowFragment = new DynamicFragment();
        } else if (v == mNews) {
            if(null==bbsFragment)
                bbsFragment=new NewsFatherFragment();
            nowFragment = bbsFragment;
        } else if (v == mConstact) {
            nowFragment = BBSFragmentTwo.getInstance();
        } else {
            //this is a test
            nowFragment = new SettingFragment();
        }

        ft.replace(R.id.fl_content, nowFragment, MainActivity.TAG);
        ft.commit();
        setButton(v);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (nowFragment instanceof SettingFragment)
            nowFragment.onActivityResult(requestCode, resultCode, data);
        if(nowFragment instanceof BBSFragment)
            nowFragment.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 2s内连按两次退出程序
     */
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(resideMenu.isOpened()){
                resideMenu.closeMenu();
            }
            else if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_LONG).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
