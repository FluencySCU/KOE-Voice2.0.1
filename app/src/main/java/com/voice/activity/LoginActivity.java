/*package com.qq.activity;

import com.qq.MainActivity;
import com.qq.R;
import com.qq.login.MyDatabase;
import com.qq.view.TextURLView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private Context mContext;
	private RelativeLayout rl_user;
	private Button mLogin;
	private Button register;
	private TextURLView mTextViewURL;
	
	private EditText telenumber;
	private EditText passworded;
	
	private MyDatabase myHelper;
	private SQLiteDatabase db;
	Cursor cursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		mContext=this;
		findView();
		initTvUrl();
		init();
	}
	
	private void findView(){
		rl_user=(RelativeLayout) findViewById(R.id.rl_user);
		mLogin=(Button) findViewById(R.id.login);
		register=(Button) findViewById(R.id.register);
		mTextViewURL=(TextURLView) findViewById(R.id.tv_forget_password);
		telenumber=(EditText)findViewById(R.id.account);
		passworded=(EditText)findViewById(R.id.password);
		myHelper=new MyDatabase(this);
	}

	private void init(){
		Animation anim=AnimationUtils.loadAnimation(mContext, R.anim.login_anim);
		anim.setFillAfter(true);
		rl_user.startAnimation(anim);
		
		mLogin.setOnClickListener(loginOnClickListener);
		register.setOnClickListener(registerOnClickListener);
	}
	
	private void initTvUrl(){
		mTextViewURL.setText(R.string.forget_password);
	}
	
	private OnClickListener loginOnClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String account =telenumber.getText().toString();
			String password=passworded.getText().toString();
			
			db = myHelper.getReadableDatabase();
			cursor = db.query("User", null, "tele=?", new String[]{account}, null, null, null);
			    		if(cursor.getCount()==0){
							Toast.makeText(getApplicationContext(),
									"账号不存在，请从新输入", Toast.LENGTH_SHORT).show();
							telenumber.setText("");
							passworded.setText("");
							cursor.close();
							db.close();
							myHelper.close();
						}
						else if(cursor.moveToFirst()&&!(cursor.getString(cursor.getColumnIndex("password")).equals(password))){
							Toast.makeText(getApplicationContext(),
									"输入密码错误，请从新输入", Toast.LENGTH_SHORT).show();
							telenumber.setText("");
							passworded.setText("");
							cursor.close();
							db.close();
							myHelper.close();
						}
						else{
							Toast.makeText(getApplicationContext(),
									"登录成功", Toast.LENGTH_SHORT).show();	
						Intent intent =new Intent(mContext,MainActivity.class);
						startActivity(intent);
						finish();
						}  
			
		}
	};
	
	private OnClickListener registerOnClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent=new Intent(mContext, RegisterPhoneActivity.class);
			startActivity(intent);
			
		}
	};
}
*/
package com.voice.activity;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.voice.CONTEXT;
import com.voice.MainActivity;
import com.voice.R;
import com.voice.User;
import com.voice.login_database.MyDatabase;
import com.voice.util.ToastUtils;
import com.voice.view.TextURLView;
import com.voice.register.RegisterPhoneActivity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class LoginActivity extends Activity {


    private Context mContext;
    //控件
    private RelativeLayout rl_user;
    private Button mLogin;
    private Button register;
    private TextURLView mTextViewURL;
    private EditText telenumber;
    private EditText passworded;
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    private MyDatabase myHelper;
    private SQLiteDatabase db;
    Cursor cursor;

    //jetzhouxl
    //private RequestQueue requestQueue;
    private String account;
    private String password;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //requestQueue = Volley.newRequestQueue(getApplicationContext());
        mContext = this;
        sp = this.getSharedPreferences("ACCOUNT", MODE_PRIVATE);
        editor = sp.edit();
        String savedAccount = sp.getString("PHONE", "");
        String savedPassword = sp.getString("PASSWORD", "");
        findView();
        initTvUrl();
        CONTEXT.setContext(getApplicationContext());
        init();
        telenumber.setText(savedAccount);
        passworded.setText(savedPassword);
        if (savedAccount.length() != 0 && savedPassword.length() != 0)
            login();
        telenumber.setSelection(telenumber.length());
    }

    private void findView() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("提示信息");
        progressDialog.setMessage("登录中，请稍后......");
        progressDialog.setCancelable(false);

        rl_user = (RelativeLayout) findViewById(R.id.rl_user);
        mLogin = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        mTextViewURL = (TextURLView) findViewById(R.id.tv_forget_password);
        telenumber = (EditText) findViewById(R.id.account);
        passworded = (EditText) findViewById(R.id.password);
        myHelper = new MyDatabase(this, "dbName", null, 1);
    }

    private void init() {
        Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.login_anim);
        anim.setFillAfter(true);
        rl_user.startAnimation(anim);

        mLogin.setOnClickListener(loginOnClickListener);
        register.setOnClickListener(registerOnClickListener);
    }

    private void initTvUrl() {
        mTextViewURL.setText(R.string.forget_password);
    }

    private void login() {
        account = telenumber.getText().toString();
        password = passworded.getText().toString();

        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            ToastUtils.show(LoginActivity.this, "账户或者密码不能为空哦");
            return;
        }
        progressDialog.show();
        RequestParams params = new RequestParams();
        params.put("account", account);
        params.put("password", password);
//		params.put("action", "login");
        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(5000);
        RequestHandle post = client.post(CONTEXT.url+"/LoginServlet", params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                String code = new String(bytes);
                if (code.trim().equals("success")) {
                    Log.e("LoginActivity", code);
                    progressDialog.dismiss();
                    //ToastUtils.show(mContext,"登录成功");

                    db = myHelper.getReadableDatabase();
                    cursor = db.query("User", null, "tele=?", new String[]{account}, null, null, null);
                    if (!cursor.moveToFirst()) {
                        Toast.makeText(getApplicationContext(),
                                "账号不存在，请重新输入", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        telenumber.setText("");
                        passworded.setText("");
                        cursor.close();
                        db.close();
                        myHelper.close();
                    } else if (cursor.moveToFirst() && !(cursor.getString(cursor.getColumnIndex("password")).equals(password))) {
                        Toast.makeText(getApplicationContext(),
                                "输入密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        telenumber.setText("");
                        passworded.setText("");
                        cursor.close();
                        db.close();
                        myHelper.close();
                    } else {
                        Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                        String UserName = cursor.getString(cursor.getColumnIndex("name"));
                        String Sign = cursor.getString(cursor.getColumnIndex("sign"));
                        String Sex = cursor.getString(cursor.getColumnIndex("sex"));
                        String RegistDate = cursor.getString(cursor.getColumnIndex("regist_date"));
                        String HeadPath = cursor.getString(cursor.getColumnIndex("head_path"));
                        User user = new User(account, UserName, "", Sign, Sex, RegistDate, HeadPath);
                        cursor.close();
                        db.close();
                        myHelper.close();
                        progressDialog.dismiss();
                        Intent intent = new Intent(mContext, MainActivity.class);
                        Bundle u = new Bundle();
                        u.putSerializable("user", user);
                        intent.putExtras(u);
                        startActivity(intent);
                        editor.putString("PHONE", account);
                        editor.putString("PASSWORD", password);
                        editor.commit();
                        finish();
                    }
                } else {
                    ToastUtils.show(LoginActivity.this, "密码或者账号错误");
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });


    }

    private OnClickListener loginOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            login();


//            StringRequest stringRequest = new StringRequest(Request.Method.GET,"http://192.168.43.11:8080/VoiceHelper/LoginServlet", new Response.Listener<String>() {
//                @Override
//                public void onResponse(String s) {
//                    //String s即为服务器返回的数据
//                    //Log.d("cylog", s);
//                }
//            },new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    //Log.e("cylog", volleyError.getMessage(),volleyError);
//                }
//            }){
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                       // return super.getHeaders();
//                     Map<String,String> headers=new HashMap<>();
//                    headers.put("account",account);
//                    headers.put("password",password);
//                    return headers;
//                }
//            };
//
//            requestQueue.add(stringRequest);


        }
    };


    private OnClickListener registerOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, RegisterPhoneActivity.class);
            startActivity(intent);

        }
    };
}

