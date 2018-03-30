package com.voice.activity;

/**
 * 自由练习功能，用户可以自己输入文本，然后开始练习自己所输的
 */

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import com.voice.R;

public class PracticeActivity extends Activity implements ISpeechRecognitionServerEvents {

    private EditText edit;
    private TextView text, text_pinyin, reText, reText_pinyin, result;
    private Button clearButton, startButton, stopButton,back;

    int m_waitSeconds = 0;
    final String PRIMARY_KEY = "5d68cddf319e4ae5ae184ebd6e7e4209";
    MicrophoneRecognitionClient micClient = null;//Client
    PracticeActivity.FinalResponseStatus isReceivedResponse = PracticeActivity.FinalResponseStatus.NotReceived;

    private enum FinalResponseStatus {NotReceived, OK, Timeout}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);
        initView();
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    /**
     * 初始化控件，绑定监听器
     */
    private void initView() {
        edit = (EditText) findViewById(R.id.practice_edit);//输入框
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    setTextAndTextPinyin();
                } catch (PinyinException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    setTextAndTextPinyin();
                } catch (PinyinException e) {
                    e.printStackTrace();
                }
            }
        });
        back= (Button) findViewById(R.id.practice_reback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        clearButton = (Button) findViewById(R.id.practice_clear);//清空按钮
        //清空按钮点击，EditText内容清空
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.setText("");
                reText.setText("");
                reText_pinyin.setText("");
                result.setText("");
            }
        });
        text = (TextView) findViewById(R.id.practice_text);//需要测试的文本
        text_pinyin = (TextView) findViewById(R.id.practice_text_pinyin);//文本拼音
        reText = (TextView) findViewById(R.id.practice_reText);//结果文本
        reText_pinyin = (TextView) findViewById(R.id.practice_reText_pinyin);//结果文本拼音
        result = (TextView) findViewById(R.id.practice_result);//练习结果
        startButton = (Button) findViewById(R.id.practice_start);//开始说话
        stopButton = (Button) findViewById(R.id.practice_stop);//结束说话
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "请先输入你想练习的文本内容！", Toast.LENGTH_SHORT).show();
                } else {
                    reText.setText("");
                    reText_pinyin.setText("");
                    result.setText("");
                    StartButton_Click();
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "发音结束.", Toast.LENGTH_SHORT).show();
                if (null != micClient)
                    micClient.endMicAndRecognition();
            }
        });
    }

    //在EditText输入过程中不断更新Text内容
    private void setTextAndTextPinyin() throws PinyinException {
        String content = edit.getText().toString();//获取内容
        text.setText(content);
        if (content.length() == 0)
            text_pinyin.setText("");
        else {
            //为文本中的汉字生成拼音
            String fomart = PinyinHelper.convertToPinyinString(content.trim().replaceAll("(?i)[^\\u4E00-\\u9FA5]", ""), " ", PinyinFormat.WITH_TONE_MARK);
            text_pinyin.setText(fomart);
        }
    }

    //设置结果文本
    private void setRTextAndTextPinyin(String result) throws PinyinException {
        reText.setText(result);
        if (result.length() == 0)
            reText_pinyin.setText("");
        else {
            //为文本中的汉字生成拼音
            String fomart = PinyinHelper.convertToPinyinString(result.trim().replaceAll("(?i)[^\\u4E00-\\u9FA5]", ""), " ", PinyinFormat.WITH_TONE_MARK);
            reText_pinyin.setText(fomart);
        }
        judgePoint();//打分
    }

    /**
     * 打分,结果
     */
    private void judgePoint() {
        int point = 0;
        String testText = text.getText().toString();
        String resultText = reText.getText().toString();
        if (testText.length() == 0 ||text_pinyin.getText().toString().length()==0|| resultText.length() == 0) {
            ;//任何一个为0，匹配度为0
        } else {
            char[] str1 = testText.toCharArray();
            char[] str2 = resultText.toCharArray();
            int count = 0;//计数，对了几个
            int i = 0;
            for (i = 0; i < str1.length && i < str2.length; i++) {
                if (str1[i] == str2[i])
                    count++;
            }
            point += count * 20 / str1.length;//文字匹配度
            count = 0;
            char[] str3 = text_pinyin.getText().toString().toCharArray();
            char[] str4 = reText_pinyin.getText().toString().toCharArray();
            for (i = 0; i < str3.length && i < str4.length; i++) {
                if (str3[i] == str4[i])
                    count++;
            }
            point += count * 80 / str3.length;//总匹配度
        }
        String practice_result="匹配度约为 " + point + "% ";
        //按得分显示不同的结果
        if(point==100)
            practice_result+=",完美！给你点赞！";
        else if(point>=80)
            practice_result+=",真是太棒啦！";
        else if(point>=50)
            practice_result+=",真厉害！继续加油！";
        else
            practice_result+=",再多练习几次，你能行！";
        result.setText(practice_result);
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

    /**
     * 开始
     */
    private void StartButton_Click() {
        this.startButton.setEnabled(false);//开始按钮禁止点击
        this.stopButton.setEnabled(true);//停止按钮允许点击
        this.m_waitSeconds = this.getMode() == SpeechRecognitionMode.ShortPhrase ? 20 : 200;


        Toast.makeText(getApplicationContext(), "请发音...", Toast.LENGTH_SHORT).show();

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
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDicationMessage) {
            this.startButton.setEnabled(true);
            this.stopButton.setEnabled(false);
            this.isReceivedResponse = PracticeActivity.FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {
            if (response.Results.length == 0) {
                Toast.makeText(getApplicationContext(), "很抱歉，没有听到您的发音，请重试", Toast.LENGTH_SHORT).show();//如果结果为空，提醒用户重试
            } else {
                try {
                    setRTextAndTextPinyin(response.Results[0].DisplayText);//如果有结果，打印NormalText
                } catch (PinyinException e) {
                    e.printStackTrace();
                }
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
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        Toast.makeText(getApplicationContext(), "啊哦，语音识别失败啦！请重试", Toast.LENGTH_SHORT).show();
    }

    //回调
    @Override
    public void onAudioEvent(boolean b) {
        if (!b) {//如果没有记录了，关闭Client
            this.micClient.endMicAndRecognition();
            this.startButton.setEnabled(true);
            this.stopButton.setEnabled(false);
        }
    }
}
