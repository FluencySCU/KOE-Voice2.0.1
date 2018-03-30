package com.voice.activity;

import java.util.ArrayList;
//Microsoft Speech SDK
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import com.voice.MainActivity;
import com.voice.R;
import com.voice.wifi.foregin.Globals;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import org.junit.Test;

public class TestActivity extends Activity implements ISpeechRecognitionServerEvents,OnClickListener {
    private Context mContext;
    final MainActivity m=new MainActivity();
    private ImageButton last;//beforeone
    private ImageButton next;
    private int currentnum;
    private TextView num;
    private TextView diff;
    private TextView pinyin;
    private TextView hanzi;
    private TextView std_ans_text_view;
    private TextView user_ans_text_view;
    private TextView practice;
    private ImageButton stop_test_image_button;
    private int numoflist = 7;
    private Activity mActivity;
    int testnum = m.hornor.gettestnum();
    private ImageButton fayinBotton;
    private ImageView picture;
    private String resultarray[] = {"菠萝", "草莓", "橘子", "梨子", "芒果", "柠檬", "苹果"};
    private String pinyinarray[] = {"bō luó", "cǎo méi", "jú zǐ", "lí zǐ", "máng guǒ", "níng méng", "píng guǒ"};
    //private String picturename[] = {"boluo", "caomeng", "juzi", "lizi", "mangguo", "ningmeng", "pingguo"};
    private String result;
    private TextView score;
    int num_socre = 0;

    int m_waitSeconds = 0;
    final String PRIMARY_KEY = "5d68cddf319e4ae5ae184ebd6e7e4209";
    MicrophoneRecognitionClient micClient = null;//Client
    FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;

    private enum FinalResponseStatus {NotReceived, OK, Timeout}

    private String user_ans="";//user结果
    private boolean logTime = true;

    public TestActivity() {
    }

    /**
     * 使用的识别mode
     *
     * @return The speech recognition mode.
     */
    private SpeechRecognitionMode getMode() {
        return SpeechRecognitionMode.ShortPhrase;
    }

    /**
     * 使用的语言库
     *
     * @return The default locale.
     */
    private String getDefaultLocale() {
        return "zh-CN";
    }

    public String getUser_ans() {
        return user_ans;
    }

    public void setUser_ans(String user_ans) {
        this.user_ans = user_ans;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_two);

        m.hornor.settestnum(testnum + 1);
        mActivity = TestActivity.this;
        currentnum = 0;
        initWidgets();
        initPermission();


        UpdateView();
        // Set the application-wide context global, if not already set
        Context myContext = Globals.getContext();
        if (myContext == null) {
            myContext = mActivity.getApplicationContext();
            if (myContext == null) {
                throw new NullPointerException("Null context!?!?!?");
            }
            Globals.setContext(myContext);
        }

        mContext = mActivity.getApplicationContext();

    }

    private void initWidgets() {
        // TODO Auto-generated method stub
        this.last = (ImageButton) this.findViewById(R.id.practice_back);
        last.setOnClickListener(this);
        this.next = (ImageButton) this.findViewById(R.id.practice_test);
        next.setOnClickListener(this);

        this.picture = (ImageView) this.findViewById(R.id.testing);


        this.num = (TextView) this.findViewById(R.id.num);
        this.diff = (TextView) this.findViewById(R.id.diff);
        this.score = (TextView) this.findViewById(R.id.point);
        this.pinyin = (TextView) this.findViewById(R.id.pinyin);
        this.std_ans_text_view= (TextView) this.findViewById(R.id.std_ans_text_view);
        this.user_ans_text_view= (TextView) this.findViewById(R.id.user_ans_text_view);
        this.stop_test_image_button= (ImageButton) this.findViewById(R.id.stop_test_image_button);
        stop_test_image_button.setVisibility(View.INVISIBLE);//初始情况不能点击
        this.hanzi = (TextView) this.findViewById(R.id.hanzi);


        practice= (TextView) findViewById(R.id.goto_practice);
        practice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(TestActivity.this,PracticeActivity.class);
                startActivity(intent);
            }
        });

        fayinBotton = (ImageButton) findViewById(R.id.fayin);


        fayinBotton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"开始语音识别",Toast.LENGTH_SHORT).show();
                System.out.println("MainActivity---start()--------开始");
                user_ans_text_view.setText("");
                StartButton_Click();
            }
        });

        stop_test_image_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"发音结束.",Toast.LENGTH_SHORT).show();
                if(null!=micClient)
                    micClient.endMicAndRecognition();
            }
        });

        DisplayMetrics dm = new DisplayMetrics();

        dm = getApplicationContext().getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        last.setMaxWidth(screenWidth / 3);
        next.setMaxWidth(screenWidth / 3);
    }

    /**
     * 开始
     */
    private void StartButton_Click() {
        this.fayinBotton.setVisibility(View.INVISIBLE);//开始按钮禁止点击
        this.stop_test_image_button.setVisibility(View.VISIBLE);//停止按钮允许点击
        this.m_waitSeconds = this.getMode() == SpeechRecognitionMode.ShortPhrase ? 20 : 200;


        Toast.makeText(getApplicationContext(),"请发音...",Toast.LENGTH_SHORT).show();

        if (this.micClient == null) {
            this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                    this,
                    this.getMode(),
                    this.getDefaultLocale(),
                    this,
                    this.PRIMARY_KEY);
        }

        this.micClient.startMicAndRecognition();
    }



    private void printLog(String text) {
        if (logTime) {
            //text += "  ;time=" + System.currentTimeMillis();
        }


        int strlen=text.length();
        text=text.substring(0,strlen-1);
        System.out.println("printLog()-----------------text="+text+"------length="+strlen);
        //结果字符串
        user_ans_text_view.append(text);
        setUser_ans(text);
        judgePoint();
    }

    private void judgePoint() {
        String tempAns=getUser_ans();

        switch(currentnum){
            case 0:
                if(tempAns.equals("菠萝")){
                    score.setText("100");
                    score.setTextColor(Color.parseColor("#EE2C2C"));
                }
                else{
                    score.setText("0");
                    score.setTextColor(Color.parseColor("#ffcf0a"));
                }
                break;
            case 1:
                if(tempAns.equals("草莓")){
                    score.setText("100");
                    score.setTextColor(Color.parseColor("#EE2C2C"));
                }
                else
                {
                    score.setText("0");
                    score.setTextColor(Color.parseColor("#ffcf0a"));
                }
                break;
            case 2:
                if(tempAns.equals("橘子")){
                    score.setText("100");
                    score.setTextColor(Color.parseColor("#EE2C2C"));
                }
                else
                {
                    score.setText("0");
                    score.setTextColor(Color.parseColor("#ffcf0a"));
                }
                break;
            case 3:
                if(tempAns.equals("梨子")){
                    score.setText("100");
                    score.setTextColor(Color.parseColor("#EE2C2C"));
                }
                else
                {
                    score.setText("0");
                    score.setTextColor(Color.parseColor("#ffcf0a"));
                }
                break;
            case 4:
                if(tempAns.equals("芒果")){
                    score.setText("100");
                    score.setTextColor(Color.parseColor("#EE2C2C"));
                }
                else
                {
                    score.setText("0");
                    score.setTextColor(Color.parseColor("#ffcf0a"));
                }
                break;
            case 5:
                if(tempAns.equals("柠檬")){
                    score.setText("100");
                    score.setTextColor(Color.parseColor("#EE2C2C"));
                }
                else
                {
                    score.setText("0");
                    score.setTextColor(Color.parseColor("#ffcf0a"));
                }
                break;
            case 6:
                if(tempAns.equals("苹果")){
                    score.setText("100");
                    score.setTextColor(Color.parseColor("#EE2C2C"));
                }
                else
                {
                    score.setText("0");
                    score.setTextColor(Color.parseColor("#ffcf0a"));
                }
                break;
        }
    }

    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm :permissions){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()){
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    public void onClick(View v) {
        Log.i("3", "3");
        this.UpdateView();
        Log.i("3", "3");
        if (v == next) {
            if (currentnum < numoflist) {
                currentnum++;
                if (currentnum > 6)
                    currentnum = 0;
                this.UpdateView();
                setItem(currentnum);
            }
        } else if (v == last) {
            currentnum--;
            if (currentnum < 0)
                currentnum = 6;
            this.UpdateView();
            setItem(currentnum);

        }

        Log.i("3", "3");
    }



    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Dialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.qq_leba_list_seek_individuation)
                    .setTitle("测试未完成")
                    .setMessage("你确定现在结束测试吗？这将导致本次测试无效！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked OK so do some stuff */
                            TestActivity.this.finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked OK so do some stuff */
                        }
                    }).create();
            dialog.show();
        }

        return true;
    }

    private void UpdateView() {
        score.setText("暂无");
        score.setTextColor(Color.parseColor("#ffcf0a"));
        user_ans_text_view.setText("");
        if (currentnum == 0) {
            last.setEnabled(false);
        } else if (currentnum > 0) {
            last.setEnabled(true);
        }
        if (currentnum == 6) {
            next.setEnabled(false);
        } else if (currentnum > 0) {
            next.setEnabled(true);
        }
        SharedPreferences setting = getSharedPreferences("wordroid.model_preferences", MODE_PRIVATE);
        if (setting.getBoolean("iftts", false)) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }


        // TODO Auto-generated method stub
        if (currentnum < numoflist) {
            num.setText("第 " + (currentnum + 1) + " 个 / 共 " + numoflist + " 个");
            if (DifficultychoseActivity.test.getDifficulty() == 1) {
                diff.setText("难度：简单");
            } else if (DifficultychoseActivity.test.getDifficulty() == 2) {
                diff.setText("难度：一般");
            } else if (DifficultychoseActivity.test.getDifficulty() == 3) {
                diff.setText("难度：困难");
            }
        } else if (currentnum >= numoflist) {
            currentnum--;
            Dialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.qq_leba_list_seek_individuation)
                    .setTitle("测试已完成")
                    .setMessage("您可以依照复习计划进行本单元的复习")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            /* User clicked OK so do some stuff */
                            TestActivity.this.finish();
                        }
                    }).create();
            dialog.show();
        }


    }

    private void setItem(int num) {
        result = resultarray[num];
        StringBuffer sb = new StringBuffer(result);
        sb.insert(1, "  ");
        switch (num) {
            case 0:
                picture.setImageResource(R.drawable.boluo);
                pinyin.setText(pinyinarray[num]);
                hanzi.setText(sb);
                std_ans_text_view.setText(sb);
                break;
            case 1:
                picture.setImageResource(R.drawable.caomei);
                pinyin.setText(pinyinarray[num]);
                hanzi.setText(sb);
                std_ans_text_view.setText(sb);
                break;
            case 2:
                picture.setImageResource(R.drawable.juzi);
                pinyin.setText(pinyinarray[num]);
                hanzi.setText(sb);
                std_ans_text_view.setText(sb);
                break;
            case 3:
                picture.setImageResource(R.drawable.lizi);
                pinyin.setText(pinyinarray[num]);
                hanzi.setText(sb);
                std_ans_text_view.setText(sb);
                break;
            case 4:
                picture.setImageResource(R.drawable.mangguo);
                pinyin.setText(pinyinarray[num]);
                hanzi.setText(sb);
                std_ans_text_view.setText(sb);
                break;
            case 5:
                picture.setImageResource(R.drawable.ningmeng);
                pinyin.setText(pinyinarray[num]);
                hanzi.setText(sb);
                std_ans_text_view.setText(sb);
                break;
            case 6:
                picture.setImageResource(R.drawable.pingguo);
                pinyin.setText(pinyinarray[num]);
                hanzi.setText(sb);
                std_ans_text_view.setText(sb);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle results = data.getExtras();
            ArrayList<String> results_recognition = results.getStringArrayList("results_recognition");
            if (isright(results_recognition) == true) {
                num_socre++;
                score.setText("" + num_socre);
            }
        }
    }

    private boolean isright(ArrayList<String> results_recognition) {
        String heightArray[] = (String[]) results_recognition.toArray(new String[0]);
        return heightArray[0].equals(result);

    }

    //部分结果
    @Override
    public void onPartialResponseReceived(String s) {
        ;
    }

    //最终结果
    @Override
    public void onFinalResponseReceived(RecognitionResult response) {
        boolean isFinalDicationMessage = this.getMode() == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (null != this.micClient && ((this.getMode() == SpeechRecognitionMode.ShortPhrase) || isFinalDicationMessage)) {
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDicationMessage) {
            this.fayinBotton.setVisibility(View.VISIBLE);
            this.stop_test_image_button.setVisibility(View.INVISIBLE);
            this.isReceivedResponse = TestActivity.FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {
            if(response.Results.length==0){
                Toast.makeText(getApplicationContext(),"很抱歉，没有听到您的发音，请重试",Toast.LENGTH_SHORT).show();//如果结果为空，提醒用户重试
            }
            else{
                printLog(response.Results[0].DisplayText);//如果有结果，打印NormalText
            }
        }
    }

    //Intent
    @Override
    public void onIntentReceived(String s) {
        ;
    }

    //Error
    @Override
    public void onError(int i, String s) {
        fayinBotton.setVisibility(View.VISIBLE);
        stop_test_image_button.setVisibility(View.INVISIBLE);
        Toast.makeText(getApplicationContext(),"啊哦，语音识别失败啦！请重试",Toast.LENGTH_SHORT).show();
    }

    //回调
    @Override
    public void onAudioEvent(boolean b) {
        if (!b) {//如果没有记录了，关闭Client
            this.micClient.endMicAndRecognition();
            this.fayinBotton.setVisibility(View.VISIBLE);
            this.stop_test_image_button.setVisibility(View.INVISIBLE);
        }
    }
}
