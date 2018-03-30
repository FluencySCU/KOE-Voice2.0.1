package com.voice.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.imnjh.imagepicker.PickerConfig;
import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.voice.CONTEXT;
import com.voice.PickImg.FrescoImageLoader;
import com.voice.PickImg.PickAdapter;
import com.voice.PickImg.SingleFileLimitInterceptor;
import com.voice.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.voice.MainActivity.user;

public class NewArticleActivity extends Activity {
    private TextView percent;//显示输入字符百分比的textView
    private EditText text;//输入文字框，标题
    private TextView submit;//发布按钮
    private Button selectImg;//返回按钮
    private TextView back;
    private Handler handler;
    public static final int REQUEST_CODE_IMAGE = 101;
    private GridView gridView;
    private PickAdapter pickAdapter;
    private ArrayList<String> picPath = null;
    private ProgressDialog progressDialog;
    private String position="未知";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_article_layout);
//        SImagePicker.init(new PickerConfig.Builder().setAppContext(this)
//                .setImageLoader(new FrescoImageLoader())
//                .setToolbaseColor(getResources().getColor(R.color.colorPrimary)).build());
        new Thread(new Runnable() {
            @Override
            public void run() {
                getLocation();
            }
        }).start();
        initData();//初始化
    }

    /**
     * 初始化
     */
    private void initData() {
        handler = new Handler();
        pickAdapter = new PickAdapter(this);
        gridView = findViewById(R.id.image_grid);
        gridView.setAdapter(pickAdapter);
        percent = findViewById(R.id.text_newArticle_percent);//已输入的百分比
        text = findViewById(R.id.new_article_text);//输入框
        text.addTextChangedListener(new TextWatcher() {//文字框文字数量监听器
            int length = 0;
            int beforeLength = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                beforeLength = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                length = charSequence.length();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                percent.setText(length + "/500");
                if (length == 500)
                    percent.setTextColor(Color.RED);
                if (beforeLength == 500)
                    percent.setTextColor(Color.parseColor("#8A000000"));
            }
        });
        back = findViewById(R.id.button_newArticle_back);//返回按钮
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != picPath || !text.getText().toString().trim().equals("")) //按了返回键后,若选择图片或有输入文字都弹出对话框
                    showAlertDialog();
                else
                    finish();//如果有字或者图片，提示框，是否退出，若没有，则直接退出
            }
        });
        selectImg = findViewById(R.id.button_newArticle_selectImg);//选择图片
        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SImagePicker
                        .from(NewArticleActivity.this)
                        .maxCount(9)
                        .rowCount(3)
                        .showCamera(false)
                        .pickMode(SImagePicker.MODE_IMAGE)
                        .fileInterceptor(new SingleFileLimitInterceptor())//单个文件的限制
                        .forResult(REQUEST_CODE_IMAGE);
            }
        });
        submit = findViewById(R.id.button_newArticle_submit);//发布按钮
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Submit();
            }
        });
    }

    /**
     * 提交文字和图片以及用户信息
     */
    private void Submit() {
        String article = text.getText().toString();
        if (null == picPath && article.equals("")) {
            Toast.makeText(getApplicationContext(), "请至少上传一张图片或编写一段文字", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);//不可手动关闭
        progressDialog.show();
        progressDialog.setMessage("请稍后...");
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType type = MediaType.parse("image/jpeg;charset=utf-8");
        int picNum=0;
        if(null!=picPath)
            picNum=picPath.size();
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("account", user.getLogId())
                .addFormDataPart("name", user.getLogName())
                .addFormDataPart("content", article)
                .addFormDataPart("position", position)
                .addFormDataPart("picNum", picNum + "");
        for (int i = 1; i <= picNum; i++) {
            File file = new File(picPath.get(i - 1));
            builder.addFormDataPart(i + "", i + ".jpg", RequestBody.create(type, file));
        }//添加图片
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().post(requestBody).url(CONTEXT.url + "/uploadArticle").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "发布失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String code = response.body().string().trim();
                if (code.equals("success")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "发布成功", Toast.LENGTH_SHORT).show();
                            setResult(1);
                            finish();
                        }
                    });
                } else if (code.equals("fail")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "发布失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "发布失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            picPath =
                    data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);
            pickAdapter.setNewData(picPath);
        }
    }

    /**
     * 返回按钮监听
     *
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && (null != picPath || !text.getText().toString().trim().equals(""))) {//按了返回键后,若选择图片或有输入文字都弹出对话框
            showAlertDialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    //是否返回对话框
    private void showAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("还未发布，确定退出吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ;
                    }
                })
                .create();
        alertDialog.show();
    }

    /**
     * 获取城市
     */
    private void getLocation() {
        String provider = "";
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = locationManager.getProviders(true);

        if (list.contains(LocationManager.GPS_PROVIDER)) {
            //是否为GPS位置控制器
            provider = LocationManager.GPS_PROVIDER;
        } else if (list.contains(LocationManager.NETWORK_PROVIDER)) {
            //是否为网络位置控制器
            provider = LocationManager.NETWORK_PROVIDER;

        } else {
            return ;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "未开启获取定位权限", Toast.LENGTH_SHORT).show();
            return ;
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
        provider=locationManager.getBestProvider(criteria,true);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            //获取当前位置，这里只用到了经纬度
            Geocoder gc = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                position=result.get(0).getAdminArea()+result.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
