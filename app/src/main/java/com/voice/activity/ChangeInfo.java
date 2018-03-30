package com.voice.activity;

//import com.qq.R;
import com.voice.R;
import com.voice.User;
import com.voice.login_database.MyDatabase;
import com.voice.sort.SelectPicPopupWindow;
import com.voice.view.CircleImageView;
import com.voice.view.TitleBarView;
import com.voice.MainActivity;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeInfo extends Activity {
	private boolean have=MainActivity.user.getHavaHead();
	private Bitmap bitmap=MainActivity.user.getHeadIcon();
	final static MainActivity m=new MainActivity();
	public static User usertemp=new User(m.user.getLogId(),m.user.getLogName(),"",m.user.getSign(),m.user.getSex(),m.user.getRegistDate(),m.user.getHeadPath());
	private Context mContext;
	private View mBaseView;
	public CircleImageView head_change;
	private TitleBarView mTitleBarView;
	public static final String[] SexChoice={"未知","男","女"};
	private Spinner SexSpiner;
	private ArrayAdapter<String> arrayAdapter;
	private TextView ID;
	private EditText Name;
	private EditText Sign;
	public RelativeLayout ChangeFinish;
	private MyDatabase myHelper;
	private SQLiteDatabase db;
	private String Sex="";
	private TextView SexShow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changeinfo);
		mContext=this;
		findView();
		init();
	}
	@Override
	protected void onResume(){
		super.onResume();
		init();
	}
	private void findView(){
		mTitleBarView=(TitleBarView)findViewById(R.id.title_bar);
		head_change=(CircleImageView)findViewById(R.id.head_change);
		ID=(TextView)findViewById(R.id.UsrID);
		Name=(EditText)findViewById(R.id.UsrName);
		Sign=(EditText)findViewById(R.id.Sign);
		SexShow=(TextView)findViewById(R.id.sex_show);
		myHelper=new MyDatabase(this,"dbName",null,1);
		ChangeFinish=(RelativeLayout) findViewById(R.id.ChangeFinish);
		SexSpiner=(Spinner)findViewById(R.id.sex_choice);
		arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,SexChoice);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SexSpiner.setAdapter(arrayAdapter);

	}

	private void init(){

		mTitleBarView.setCommonTitle(View.GONE, View.VISIBLE, View.GONE, View.GONE);
		mTitleBarView.setTitleText(R.string.mime);
		ID.setText("账号："+usertemp.getLogId());
		Sign.setText(usertemp.getSign());
		Name.setText(usertemp.getLogName());
		if(have){
			head_change.setImageBitmap(bitmap);
		}
		switch (usertemp.getSex()){
			case "":
				SexSpiner.setSelection(0,true);
				SexShow.setText("您的性别：未知");
				break;
			case "male":
				SexSpiner.setSelection(1,true);
				SexShow.setText("您的性别：男");
				break;
			case "female":
				SexSpiner.setSelection(2,true);
				SexShow.setText("您的性别：女");
				break;
		}

		SexSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				Sex=SexChoice[i];
				SexShow.setText("您的性别："+Sex);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});
		final String OldName=Name.getText().toString();
		/**改变头像
		 *
		 */
		head_change.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(),"请在上一界面更改头像",Toast.LENGTH_SHORT).show();
			}
		});

		ChangeFinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String NewName=Name.getText().toString();
				String NewSign=Sign.getText().toString();
				String HeadPath=usertemp.getHeadPath();
				int check=checkname(NewName);
				if(check==0)
					Toast.makeText(getApplicationContext(),
							"用户名不能为空", Toast.LENGTH_SHORT).show();
				else{
					db = myHelper.getWritableDatabase();
					ContentValues values=new ContentValues();
					values.put("name", NewName);
					values.put("sign", NewSign);
					switch (Sex){
						case "未知":
							values.put("sex","");
							usertemp.setSex("");
							break;
						case "男":
							values.put("sex","male");
							usertemp.setSex("male");
							break;
						case  "女":
							values.put("sex","female");
							usertemp.setSex("female");
							break;
						default:
							break;
					}
					values.put("head_path",HeadPath);
					db.update("User", values, "tele=?", new String[]{m.user.getLogId()});
					usertemp.setLogName(NewName);
					usertemp.setSign(NewSign);

					//SettingFragment sf=new SettingFragment();
					//sf.init();
					m.user.setLogName(usertemp.getLogName());
					m.user.setSex(usertemp.getSex());
					m.user.setSign(usertemp.getSign());
					m.user.setHeadPath(usertemp.getHeadPath());
					db.close();
					myHelper.close();
					m.finish();
					finish();

				}
			}
		});
	}
	private int checkname(String name){
		int flag=0;
		if(name.length()>0)
			flag=1;
		return flag;
	}
	/**
	 * 设置添加屏幕的背景透明度
	 * @param bgAlpha
	 */
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; //0.0-1.0
		getWindow().setAttributes(lp);
	}
	/**
	 * 添加PopupWindow关闭的事件，主要是为了将背景透明度改回来
	 *
	 */
	class poponDismissListener implements PopupWindow.OnDismissListener{

		@Override
		public void onDismiss() {
			// TODO Auto-generated method stub
			//Log.v("List_noteTypeActivity:", "我是关闭事件");
			backgroundAlpha(1f);
		}

	}
}
