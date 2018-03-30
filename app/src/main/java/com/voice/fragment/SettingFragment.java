package com.voice.fragment;

import com.imnjh.imagepicker.PickerConfig;
import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.voice.CONTEXT;
import com.voice.PickImg.CacheManager;
import com.voice.PickImg.FrescoImageLoader;
import com.voice.R;
import com.voice.activity.LoginActivity;
import com.voice.view.CircleImageView;
import com.voice.view.TitleBarView;
import com.voice.MainActivity;
import com.voice.activity.ChangeInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.voice.MainActivity.user;

public class SettingFragment extends Fragment {

    private Context mContext;
    private View mBaseView;
    private TitleBarView mTitleBarView;
    private TextView Name;
    public static final int REQUEST_CODE_AVATAR = 100;
    public static final String AVATAR_FILE_NAME = "temp.jpg";
    private TextView Sign;
    private ImageView Sex;
    private TextView RegistDate;
    private RelativeLayout Logout;
    private ImageView ChangeInfo;
    private CircleImageView head;
    private Handler handler;

    //private CircleImageView Fresh;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        mBaseView = inflater.inflate(R.layout.fragment_mine, null);
        findView();
//		ProgressDialog progressDialog=new ProgressDialog(mContext);
//		progressDialog.show();
        init();
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    Toast.makeText(getActivity(), "获取头像成功", Toast.LENGTH_SHORT).show();
                } else if (msg.what == 3) {
                    Toast.makeText(getActivity(), "修改头像失败，请检查网络", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        return mBaseView;
    }


    @Override
    //public void onStart(){
    public void onResume() {
        init();
        super.onResume();
    }


    private void findView() {
        mTitleBarView = (TitleBarView) mBaseView.findViewById(R.id.title_bar);
        Name = (TextView) mBaseView.findViewById(R.id.UsrName);
        Sign = (TextView) mBaseView.findViewById(R.id.Sign);
        Logout = (RelativeLayout) mBaseView.findViewById(R.id.Logout);
        ChangeInfo = (CircleImageView) mBaseView.findViewById(R.id.myHeadImg);
        Sex = (ImageView) mBaseView.findViewById(R.id.sex);
        head = (CircleImageView) mBaseView.findViewById(R.id.pic);
        if (user.getHavaHead()) {
            head.setImageBitmap(user.getHeadIcon());
        }
        RegistDate = (TextView) mBaseView.findViewById(R.id.regist_date);
        //点击头像后弹出头像选择器，选择头像，得到文件副本绝对路径
        head.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SImagePicker
                        .from((MainActivity) getActivity())
                        .pickMode(SImagePicker.MODE_AVATAR)
                        .showCamera(true)
                        .cropFilePath(
                                CacheManager.getInstance().getImageInnerCache()
                                        .getAbsolutePath(AVATAR_FILE_NAME))
                        .forResult(REQUEST_CODE_AVATAR);
            }
        });
    }

    /**
     * 回调
     *
     * @param requestCode 请求code
     * @param resultCode  结果code
     * @param data        返回的intent结果对象
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            final ArrayList<String> list = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);//得到图片路径LIST
            String path = list.get(0);
            //得到path后，上传头像
            upLoadHead(path);
        }

    }

    /**
     * 上传头像图片
     *
     * @param imgPath
     */
    private void upLoadHead(String imgPath) {
        OkHttpClient okHttpClient = new OkHttpClient();
        File file = new File(imgPath);
        MediaType type = MediaType.parse("image/jpeg;charset=utf-8");
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("account", user.getLogId())
                .addFormDataPart("img", user.getLogId() + ".jpg", RequestBody.create(type, file));
        RequestBody body = builder.build();
        Request request = new Request.Builder().post(body).url(CONTEXT.url+"/uploadHead").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendEmptyMessage(3);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string().trim();
                Log.i("asdasdas", "???????");
                if (result.equals("success")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "头像上传成功", Toast.LENGTH_SHORT).show();
                            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                            head.setImageBitmap(bitmap);
                            user.setHavaHead(true);
                            user.setHeadIcon(bitmap);
                        }
                    });
                } else {
                    handler.sendEmptyMessage(3);
                }
            }
        });
    }

    public void init() {
        final MainActivity m = new MainActivity();//人才
        mTitleBarView.setCommonTitle(View.GONE, View.VISIBLE, View.GONE, View.GONE);
        mTitleBarView.setTitleText(R.string.mime);
//		ID.setText("账号："+m.user.getLogId());
//		Name.setText("用户名："+m.user.getLogName());
        Name.setText(user.getLogName());
        Sign.setText(user.getSign());
        String sex = user.getSex();
        RegistDate.setText("加入于" + user.getRegistDate());
        switch (sex) {
            case "":
                Sex.setVisibility(View.GONE);
                break;
            case "female":
                Sex.setVisibility(View.VISIBLE);
                Sex.setImageDrawable(getResources().getDrawable(R.drawable.ic_female));
                break;
            case "male":
                Sex.setVisibility(View.VISIBLE);
                Sex.setImageDrawable(getResources().getDrawable(R.drawable.ic_male));
                break;
        }

        Logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("提示").setMessage("确定退出当前账号吗？")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m.finish();
                                Intent intent = new Intent();
                                intent.setClass(mContext, LoginActivity.class);
                                SharedPreferences sp = getActivity().getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("PASSWORD", "");//清空保存的密码
                                editor.apply();
                                mContext.startActivity(intent);
                                getActivity().finish();
                            }
                        }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ;
                            }
                        }).create();
                dialog.show();
            }
        });
        ChangeInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, ChangeInfo.class);
                mContext.startActivity(intent);
            }
        });

    }

}
