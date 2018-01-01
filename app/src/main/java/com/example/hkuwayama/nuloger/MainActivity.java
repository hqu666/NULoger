package com.example.hkuwayama.nuloger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import static android.os.SystemClock.elapsedRealtime;
import static android.os.SystemClock.elapsedRealtimeNanos;
import static android.os.SystemClock.uptimeMillis;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	public final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public final DateFormat dffn = new SimpleDateFormat("yyyyMMddHHmmss");
	public Date date;
	public int WaitingScond = 5;
	public String  student_id="1234567890";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Toolbar toolbar = ( Toolbar ) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = ( FloatingActionButton ) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String titolStr = "1レコードだけ送信";
				String msgStr = "連続送信する場合は「記録開始」ボタンをONにして下さい。";
				new AlertDialog.Builder(MainActivity.this).setTitle(titolStr).setMessage(msgStr).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendDatas();
					}
				}).
						  setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							  @Override
							  public void onClick(DialogInterface dialog, int which) {
								  //					Toast.makeText(MainActivity.this, " 必要が生じましたら再度お尋ねします。", Toast.LENGTH_LONG).show();
							  }
						  }).create().show();
			}
		});

		DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = ( NavigationView ) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
		if ( drawer.isDrawerOpen(GravityCompat.START) ) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if ( id == R.id.action_prefarence ) {               //設定
			callSetting();
			return true;
		} else if ( id == R.id.action_disconnect ) {               //回線切断
			actionDisconnect();
			return true;

		} else if ( id == R.id.action_quit ) {               //終了
			callQuit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void callQuit() {
		if ( null != timer ) {
			timer.cancel();                                // タイマーをキャンセル
			timer = null;
		}
		if ( googleApiClient != null ) {
			googleApiClient.disconnect();                  // 接続解除
		}
		this.finish();
	}

	@SuppressWarnings ( "StatementWithEmptyBody" )
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if ( id == R.id.nav_main ) {
		} else if ( id == R.id.nav_conected ) {
			getNowConect();
		} else if ( id == R.id.nav_prefarence ) {
			callSetting();
		} else if ( id == R.id.nav_send ) {
			sendDatas();
		} else if ( id == R.id.nav_share ) {
			makeList();
		} else if ( id == R.id.nav_quit ) {
			callQuit();
		}
		DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		setMainVeiw();          //メイン画面の設定
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final String TAG = "onKeyDown";
		String dbMsg = "開始";
		try {
			dbMsg = "keyCode=" + keyCode;//+",getDisplayLabel="+String.valueOf(MyEvent.getDisplayLabel())+",getAction="+MyEvent.getAction();////////////////////////////////
			myLog(TAG, dbMsg);
			switch ( keyCode ) {    //キーにデフォルト以外の動作を与えるもののみを記述★KEYCODE_MENUをここに書くとメニュー表示されない
				case KeyEvent.KEYCODE_BACK:            //4KEYCODE_BACK :keyCode；09SH: keyCode；4,MyEvent=KeyEvent{action=0 code=4 repeat=0 meta=0 scancode=158 mFlags=72}
					callQuit();
					return true;
//				case KeyEvent.KEYCODE_HOME:            //3
////					ComponentName compNmae = startService(new Intent(MainActivity.this, NotificationChangeService.class));                           //     makeNotificationを持つクラスへ
////					dbMsg = "compNmae=" + compNmae;     //compNmae=ComponentInfo{hijiyama_koubou.com.residualquantityofthesleep/hijiyama_koubou.com.residualquantityofthesleep.NotificationChangeService}
////						NotificationManager mNotificationManager = ( NotificationManager ) mainActivity.getSystemService(NOTIFICATION_SERVICE);
////						mNotificationManager.cancel(NOTIFICATION_ID);            //サービスの停止時、通知内容を破棄する
//					myLog(TAG, dbMsg);
//					return true;
				default:
					return false;
			}
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
			return false;
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		final String TAG = "dispatchKeyEvent";
		int keyCode = event.getKeyCode();
		String dbMsg = "keyCode=" + String.valueOf(keyCode);
		boolean retBool = false;
		try {
			dbMsg = dbMsg + ",getAction=" + event.getAction();////////////////////////////////
			dbMsg = dbMsg + ",retBool=" + retBool;
			myLog(TAG, dbMsg);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		if ( retBool ) {
			return true;        //Activityを終了せずに何もしないようにするには親クラスのdispatchKeyEvent()を呼び出さずにtrueを返すようにします。
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	//RuntimeParmission///////////////////////////////////////////////////////////////////////////////////
	public static int rp_inet = 0x11;        //データ送受信
	public static int rp_strage = 0x21;      //データ保存
	public static int rp_setting = 0x22;      //設定保存
	public static int rp_apListUp = 0x31;      //wifi
	public static int rp_getNowConect = 0x32;
	public static int rp_getGPSInfo = 0x41;          //GPS

	public void getWiFiDatas() {
		int accessWifiState = -1;
		int changeWifiState = -1;
		accessWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE);
		changeWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CHANGE_WIFI_STATE);
		if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE ,}, rp_apListUp);
		} else if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE ,}, rp_apListUp);
		} else {
			apListUp();                //接続先のAP情報取得
		}
	}

	public void getNowConect() {
		int accessWifiState = -1;
		int changeWifiState = -1;
		accessWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE);
		changeWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CHANGE_WIFI_STATE);
		if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE ,}, rp_apListUp);
		} else if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE ,}, rp_apListUp);
		} else {
			getNowConectInfo();                //接続先のAP情報取得
		}
	}

	public void getGPSDatas() {
		int accessFineLocation = -1;
		accessFineLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION); //ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
		if ( accessFineLocation != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION ,}, rp_getGPSInfo);
		} else {
			locationStart();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, rp_getGPSInfo, 50, ( LocationListener ) this);
		}
	}

	public void makeList() {
		int readExternalStorage = -1;
		readExternalStorage = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
		int writeExternalStorage = -1;
		writeExternalStorage = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if ( readExternalStorage != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE ,}, rp_getGPSInfo);
		} else if ( writeExternalStorage != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE ,}, rp_getGPSInfo);
		} else {
			loadTextFromDrive();                //http://vividcode.hatenablog.com/entry/20130908/1378613811/
			String titolStr = "現在作成中";
			String mggStr = "ログのリストアップは現在考案中です";
			messageShow(titolStr, mggStr);
		}
	}

	public void callSetting() {
		int readExternalStorage = -1;
		readExternalStorage = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
		int writeExternalStorage = -1;
		writeExternalStorage = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if ( readExternalStorage != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE ,}, rp_getGPSInfo);
		} else if ( writeExternalStorage != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE ,}, rp_getGPSInfo);
		} else {
			String titolStr = "現在作成中";
			String mggStr = "設定画面は現在考案中です";
			messageShow(titolStr, mggStr);
		}
	}

	public void sendDatas() {
		int accessInternet = -1;
		accessInternet = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.INTERNET);
		int getAccount = -1;
		getAccount = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.GET_ACCOUNTS);
		if ( accessInternet != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET ,}, rp_inet);
		} else if ( getAccount != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS ,}, rp_inet);
		} else {
			sendAPData();
		}
	}

	protected void myParmission() {
		final String TAG = "myParmission[MainActivity]";
		String dbMsg = "開始";
		try {
			dbMsg = "VERSION=" + Build.VERSION.SDK_INT;
			dbMsg = dbMsg + ",READ_CALENDAR=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
			dbMsg = dbMsg + "=" + PackageManager.PERMISSION_GRANTED;
			if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ) {                //APIL23以降はActivityCompat.requestPermissionsで了承を得た場合
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, rp_apListUp);
				dbMsg = dbMsg + ">>=" + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
				//		getLocalCalendar();
			}
			//	myLog(TAG, dbMsg);
		} catch (Exception e) {
			//		myLog(TAG, dbMsg + "で" + e.toString(), "e");
		}
	}

	// https://techbooster.org/android/application/17223/
	public boolean checkWifiParmission() {
		boolean retBool = false;
		int accessWifiState = -1;
		int changeWifiState = -1;
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {                                                                         //APIL23以上
			accessWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE);
			changeWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CHANGE_WIFI_STATE);
			if ( accessWifiState != PackageManager.PERMISSION_GRANTED || changeWifiState != PackageManager.PERMISSION_GRANTED ) {  // 権限の取得状況を確認する
				if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE) == false || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CHANGE_WIFI_STATE) == false ) {  //初めてか「今後は確認しない」がチェックされたらfalseが返る
					new AlertDialog.Builder(this).setTitle("パーミッションの追加説明").setMessage("Wifiの情報を得るパーミッションが必要です。\n\n（一度許可頂ければこのダイアログは表示されません。）").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE , Manifest.permission.ACCESS_WIFI_STATE}, rp_apListUp);
							// MY_PERMISSIONS_REQUEST_READ_CONTACTSはアプリ内で独自定義したrequestCodeの値
						}
					}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(MainActivity.this, " 必要が生じましたら再度お尋ねします。", Toast.LENGTH_LONG).show();
						}
					}).create().show();
					return false;
				}
				requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE , Manifest.permission.CHANGE_WIFI_STATE}, rp_apListUp);         //,Manifest.permission.CHANGE_WIFI_STATE
			}               //if ( accessWifiState != PackageManager.PERMISSION_GRANTED || changeWifiState!= PackageManager.PERMISSION_GRANTED ) {
		} else {           //if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			retBool = true;
		}
		return retBool;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {                 //許可ダイアログの承認結果を受け取る（許可・不許可）
		if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
			//   switchが使えない？
			if ( requestCode == rp_inet ) {
			} else if ( requestCode == rp_inet ) {
				sendDatas();
			} else if ( requestCode == rp_strage ) {
				makeList();
			} else if ( requestCode == rp_setting ) {
				callSetting();
			} else if ( requestCode == rp_apListUp ) {
				getWiFiDatas();
			} else if ( requestCode == rp_getNowConect ) {
				getNowConect();
			} else if ( requestCode == rp_getGPSInfo ) {
				getGPSDatas();
			}
		} else {
			Log.i("permission", "not permitted");
		}
	}

	//メイン画面の設定///////////////////////////////////////////////////////////////////////////////////
	public ListView ap_lv;
	public TextView accountName_tv;   //送信先
	public TextView student_id_tv;      //送信者；学籍番号
	public TextView send_time_tv;   //送信時刻
	public TextView timestanp_tv;       //送信時刻；もしくはGPS
	public TextView latitude_tv;        //緯度
	public TextView longitude_tv;       //経度
	public TextView accuracy_tv;        //GPS測位制度
	public TextView altitude_tv;        //標高
	public TextView list_up_count_tv;  //リストアップ件数

	public Switch record_start_sw;     //開始ボタン
	public Spinner step_spinner;       //測定間隔

	public void setMainVeiw() {
		ap_lv = ( ListView ) findViewById(R.id.ap_lv);
		accountName_tv = ( TextView ) findViewById(R.id.accountName_tv);   //送信先
		accountName_tv.setText("");
		student_id_tv = ( TextView ) findViewById(R.id.student_id_tv);   //送信者；学籍番号
		student_id_tv.setText(student_id);
		send_time_tv = ( TextView ) findViewById(R.id.send_time_tv);   //送信時刻
		send_time_tv.setText("");
		timestanp_tv = ( TextView ) findViewById(R.id.timestanp_tv);     //送信時刻；もしくはGPS
		latitude_tv = ( TextView ) findViewById(R.id.latitude_tv);       //緯度
		longitude_tv = ( TextView ) findViewById(R.id.longitude_tv);     //
		accuracy_tv = ( TextView ) findViewById(R.id.accuracy_tv);       //GPS測位制度
		altitude_tv = ( TextView ) findViewById(R.id.altitude_tv);       //標高
		list_up_count_tv = ( TextView ) findViewById(R.id.list_up_count_tv);       //リストアップ件数

		step_spinner = ( Spinner ) findViewById(R.id.step_spinner);       //測定間隔
		step_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView< ? > parent, View viw, int position, long id) {                    //　アイテムが選択された時
				final String TAG = "step_spinner";
				String dbMsg = "position=" + position + ",id=" + id;
				try {
					String[] ids = getResources().getStringArray(R.array.step_list);
					WaitingScond = Integer.parseInt(ids[position]);
					dbMsg += ",WaitingScond=" + WaitingScond;
				} catch (Exception er) {
					Log.e(TAG, dbMsg + ";でエラー発生；" + er);
				}
				myLog(TAG, dbMsg);
			}

			public void onNothingSelected(AdapterView< ? > parent) {                //　アイテムが選択されなかった
			}
		});
		record_start_sw = ( Switch ) findViewById(R.id.record_start_sw);   //開始ボタン
		record_start_sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			//     @Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final String TAG = "record_start_sw";
				String dbMsg = "isChecked=" + isChecked;
				try {
					if ( isChecked ) {
						getGPSDatas();
						if ( null != timer ) {                        // 稼働中の場合は止める
							timer.cancel();
							timer = null;
						}
						timer = new Timer();                // タイマーインスタンスを作成
						timerTask = new MyTimerTask();                // タイマータスクインスタンスを作成
						dbMsg += ",WaitingScond=" + WaitingScond;
						timer.schedule(timerTask, 0, WaitingScond * 1000);                // タイマースケジュールを設定
					} else {
						if ( null != timer ) {
							timer.cancel();                                // タイマーをキャンセル
							timer = null;
						}
					}
				} catch (Exception er) {
					Log.e(TAG, dbMsg + ";でエラー発生；" + er);
				}
				myLog(TAG, dbMsg);
			}
		});


		getGPSDatas();
//        getWiFiDatas();
//接続先の無線LANスポットを変える方法        http://web-terminal.blogspot.jp/2013/12/wifilan.html
		///WifiConfigController
	}

	//ap情報取得///////////////////////////////////////////////////////////////////////////////////
	public WifiManager wfManager;
	public String[] aps;
	public List< String > infoList;
	public List< ScanResult > apList;        // (imamnoWiFi実行後)スキャン結果を取得

	///参照　①　http://seesaawiki.jp/w/moonlight_aska/d/WiFi%C0%DC%C2%B3%BE%F0%CA%F3%A4%F2%BC%E8%C6%C0%A4%B9%A4%EB
	///Wifiの電波強度によって                   https://kokufu.blogspot.jp/2016/11/android-wi-fi-scanresult.html
	///Wi-Fi Aware(APIL26)                                   https://developer.android.com/guide/topics/connectivity/wifi-aware.html?hl=ja
	public void apListUp() {                //圏内にあるSSIDをリストアップ		http://seesaawiki.jp/w/moonlight_aska/d/WiFi%A4%CEAP%A4%F2%A5%B9%A5%AD%A5%E3%A5%F3%A4%B9%A4%EB
		final String TAG = "apListUp";
		String dbMsg = null;
		try {            // WiFi機能が無効の状態で呼び出されるとSSID検索の所でnullとなるので念のため例外処理を行なう

			wfManager = ( WifiManager ) MainActivity.this.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);        //(android.content.Context.WIFI_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {                                                                         //APIL23以上
//                @SuppressLint("WifiManagerLeak") WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
//                this.wfManager = manager;
//            }
			if ( wfManager != null ) {
				if ( wfManager.isWifiEnabled() == true ) {// if (wfManager.getWifiState() == wfManager.WIFI_STATE_ENABLED) {
					apList = null;//new List<ScanResult>() ;
					wfManager.startScan();                         // APをスキャン
					apList = wfManager.getScanResults();        // スキャン結果を取得
					int tSize = apList.size();
					list_up_count_tv.setText(tSize + "件");
					;  //リストアップ件数

					dbMsg = tSize + "件";
					aps = new String[apList.size()];
					int setuzokuCount = 0;
					for ( int i = 0 ; i < apList.size() ; i++ ) {
						dbMsg = i + "/" + apList.size() + ")";
						aps[i] = ",SSID:" + apList.get(i).SSID;
						aps[i] = aps[i] + "BSSID:" + apList.get(i).BSSID;

						if ( !apList.get(i).SSID.equals("") ) {                    //SSIDが拾えないものは空白文字が入る
							setuzokuCount++;
						} else {
						}

						aps[i] = aps[i] + apList.get(i).frequency + getResources().getString(R.string.comon_mfz) + " , " + apList.get(i).level + getResources().getString(R.string.comon_dbm);
						aps[i] = aps[i] + apList.get(i).toString();
						///			aps[i] = "SSID:" + apList.get(i).SSID + "\n"+ apList.get(i).frequency + "MHz " + apList.get(i).level + "dBm" + "\nMACアドレス" + apList.get(i).BSSID + "\n暗号化情報" + apList.get(i).capabilities;
						dbMsg = dbMsg + aps[i];
						//				Log.i(TAG, dbMsg);
					}            //for(int i=0; i<apList.size(); i++) {
					dbMsg = dbMsg + setuzokuCount + "件";
					//       setuzokukanou_tv.setText(String.valueOf(setuzokuCount)); 				//接続可能機器数
					ArrayAdapter< String > adapter = new ArrayAdapter< String >(this, android.R.layout.simple_list_item_1, aps);
					ap_lv.setAdapter(adapter);
					ap_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView< ? > parent, View view, int position, long id) {
							final String TAG = "onItemClick";
							ListView listView = ( ListView ) parent;
							String dbMsg = position + ")";
							Log.i(TAG, dbMsg);
							String item = ( String ) listView.getSelectedItem();                            //選択されたアイテム;BSSID飲みにする可能性あり

							String[] items = aps[position].split(",");

							String bssid = items[0];                            //"BSSID;" + apList.get(position).BSSID;
							dbMsg = dbMsg + bssid;

							//                 String items = aps[position];
//                            String items = apList.get(position).toString().replace(",", "\n");
							dbMsg = dbMsg + ",items=" + items;
							Log.i(TAG, dbMsg);
							String Listitems = aps[position].replace(",", "\n");

							new AlertDialog.Builder(MainActivity.this).setTitle(bssid + "の詳細").setMessage(Listitems).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}).create().show();
							// dlogHyouji(position);                //カスタムダイアログ表示
						}
					});
				}
			}
			Log.i(TAG, dbMsg);
		} catch (NullPointerException e) {
			Log.e(TAG, dbMsg + "で" + e.toString());// 適切な例外処理をしてください。
		}
	}

	public String makeSendList() {
		final String TAG = "makeSendList";
		String dbMsg = null;
		String retStr = "";
		try {
//			final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//			final Date date = new Date(System.currentTimeMillis());
//	//		dbMsg += ",uptimeMillis=" + uptimeMillis();        //システム起動時からの経過時間。システムがdeep sleepになったりした間はカウントが止まる。     uptimeMillis=636705137
//					long currentSysUpTime = currentTimeMillis - uptimeMillis();
//			dbMsg += ">currentSysUpTime>" +currentSysUpTime;                //currentTimeMillis=1514713190995>currentSysUpTime>1514073455131
//			dbMsg += "="+ df.format(currentSysUpTime);
//
//			dbMsg += ",elapsedRealtimeNanos=" + elapsedRealtimeNanos();   //elapsedRealtimeNanos=788815193626873
//			 currentSysUpTime = currentTimeMillis - elapsedRealtimeNanos();
//			dbMsg += ">>" +currentSysUpTime;
//			dbMsg += "="+ df.format(currentSysUpTime);			dbMsg = ",date=" + date ;                                           //,date=Sun Dec 31 18:39:47 GMT+09:00 2017
			long currentTimeMillis = System.currentTimeMillis();
			dbMsg += ",currentTimeMillis(現在時刻)=" + currentTimeMillis + "=" + df.format(currentTimeMillis); //1514721782830=2017/12/31 21:03:02,

			long _elapsedRealtime = elapsedRealtime();
			dbMsg += ",elapsedRealtime=" + _elapsedRealtime + "=" + df.format(_elapsedRealtime);   //800437756=1970/01/10 15:20:37システム起動時からの経過時間。システムがdeep sleepになってた間の分もカウントに含まれる。
			long currentSysUpTime = currentTimeMillis - _elapsedRealtime;
			dbMsg += ">システム起動時>" + currentSysUpTime;
			dbMsg += "=" + df.format(currentSysUpTime);
			wfManager = ( WifiManager ) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);        //(android.content.Context.WIFI_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {                                                                         //APIL23以上
//                @SuppressLint("WifiManagerLeak") WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
//                this.wfManager = manager;
//            }
			if ( wfManager != null ) {
				if ( wfManager.isWifiEnabled() == true ) {// if (wfManager.getWifiState() == wfManager.WIFI_STATE_ENABLED) {
					WifiInfo info = wfManager.getConnectionInfo();
					String conectedBSSID = info.getBSSID();        //String BSSID;APIL1;設定されている場合、このネットワーク構成エントリは、指定されたBSSIDを持つAPに関連付けるときにのみ使用する必要があります。値は、イーサネットMACアドレ
					String conectedSsID = info.getSSID();
					apList = null;//new List<ScanResult>() ;
					wfManager.startScan();                         // APをスキャン
					apList = wfManager.getScanResults();        // スキャン結果を取得
					int tSize = apList.size();
					list_up_count_tv.setText(tSize + "件");
					;  //リストアップ件数
					dbMsg += "," + tSize + "件";
					retStr = "Student Id" + ",Record Time" + ",SSID" + ",BSSID" + ",frequency[MHz]" + ",level[dBm]" + ",capabilities" +                    //APIL1
							         ",timestamp" + ",intcenterFreq0" + ",centerFreq1" + ",channelWidth" + ",operatorFriendlyName" + ",venueName" +        //APIL17,23
							         ",latitude" + ",longitude" + ",altitude" + ",accuracy" + ",pinpointing Time" + ",isConected" + "\n";                    //GPS
					for ( int i = 0 ; i < apList.size() ; i++ ) {
						dbMsg += "\n(" + i + "/" + apList.size() + ")";
						String OneRecord = student_id + "," + df.format(date);
//ScanResultのフィールド	https://developer.android.com/reference/android/net/wifi/ScanResult.html////
						String sSID = apList.get(i).SSID;
						OneRecord += "," + sSID;                                    //String SSID;APIL1;ネットワーク名
						dbMsg += ",SSID=" + sSID + " ; " + conectedSsID;
						String bSSID = apList.get(i).BSSID;                                    //String BSSID;APIL1;アクセスポイントのアドレス。
						OneRecord += "," + bSSID;                                    //String BSSID;APIL1;アクセスポイントのアドレス。
						dbMsg += ",BSSID=" + bSSID + " ; " + conectedBSSID;

						OneRecord += "," + apList.get(i).frequency;            //int ;APIL1;クライアントがアクセスポイントと通信しているチャネルの主要な20 MHz周波数（MHz単位）。
						OneRecord += "," + apList.get(i).level;    //int ;//APIL1;検出された信号レベル（dBm）。RSSIとも呼ばれます。calculateSignalLevel(int, int)この数値をユーザーに表示できる絶対信号レベルに変換するために使用します。
						OneRecord += "," + apList.get(i).capabilities;            //String ;APIL1;アクセスポイントでサポートされている認証、キー管理、および暗号化方式について説明します。
						long timeStamp = apList.get(i).timestamp / 1000;
						dbMsg += ",timeStamp=" + timeStamp + "=" + df.format(timeStamp);
						timeStamp = currentSysUpTime + timeStamp;
						OneRecord += "," + df.format(timeStamp);            //long ;APIL17;この結果が最後に確認されたときのタイムスタンプ（ブート以降）。
						if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
							OneRecord += "," + apList.get(i).centerFreq0;            //int ;APIL23:APの帯域幅が20 MHzの場合は使用されませんAPが40,80または160 MHzを使用する場合、APが80 + 80 MHzを使用する場合の中心周波数（MHz）です。これは最初のセグメントの中心周波数）
							OneRecord += "," + apList.get(i).centerFreq1;            //int ;APIL23;APの帯域幅が80 + 80 MHzの場合にのみ使用されます.APが80 + 80 MHzを使用する場合、これは2番目のセグメントの中心周波数（MHz単位）です。
							OneRecord += "," + apList.get(i).channelWidth;            //int ;//APIL23;APチャネル帯域幅。
//														//0;CHANNEL_WIDTH_20MHZ、1;CHANNEL_WIDTH_40MHZ、 2;CHANNEL_WIDTH_80MHZ、3;CHANNEL_WIDTH_160MHZ,4;CHANNEL_WIDTH_80MHZ_PLUS_MHZ。
							OneRecord += "," + apList.get(i).operatorFriendlyName;            //	CharSequence ;	//APIL23;アクセスポイントが発行したパスポイントオペレータ名を示します。
							OneRecord += "," + apList.get(i).venueName;            //	CharSequence ;APIL23;アクセスポイントから発行された会場名を示します。Passpointネットワークでのみ使用可能で、アクセスポイントで公開されている場合にのみ使用できます。
						}
						OneRecord += addGPSFeld();
						if ( bSSID.equals(conectedBSSID) ) {
							OneRecord += "," + "true".toString() + "\n";
						} else {
							OneRecord += "," + "false".toString() + "\n";
						}
						dbMsg += ",OneRecord=" + OneRecord;
						retStr += OneRecord;
					}            //for(int i=0; i<apList.size(); i++) {
				}
			}
//			dbMsg += ",retStr="+ retStr;
			Log.i(TAG, dbMsg);
		} catch (NullPointerException e) {
			Log.e(TAG, dbMsg + "で" + e.toString());// 適切な例外処理をしてください。
		}
		return retStr;
	}

	public String addGPSFeld() {
		final String TAG = "addGPSFeld";
		String dbMsg = null;
		String retStr = "";
		if ( -1 < latitudeVal ) {         // 緯度の表示
			retStr += "," + latitudeVal;
			//	infoList.add(latitudeStr);// 緯度
		} else {
			retStr += ",";
		}
		if ( -1 < longitudeVal ) {         //   = -1;       // 経度の表示
			retStr += "," + longitudeVal;
//			infoList.add(LongtudeStr);// 経度
		} else {
			retStr += ",";
		}
		if ( -1 < altitudeVal ) {         // = -1;                //標高
			retStr += "," + altitudeVal;
			//		infoList.add(altitudeStr);// 標高
		} else {
			retStr += ",";
		}
		if ( -1 < accuracyVal ) {         //精度
			retStr += "," + accuracyVal;
			//		infoList.add(accuracyStr);// 精度
		} else {
			retStr += ",";
		}
		if ( 0 < pinpointingTimeVal ) {         // = 0;                //測位時刻
			final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			retStr += "," + df.format(pinpointingTimeVal);
			//		infoList.add(pinpointingTimeValStr);// 測位時刻
		} else {
			retStr += ",";
		}

		return retStr;
	}

	public void getNowConectInfo() {                //接続先情報
		final String TAG = "getNowConect";
		String dbMsg = null;
		try {
			String mggStr = "表示できる情報はありません。";
			wfManager = ( WifiManager ) MainActivity.this.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);        //(android.content.Context.WIFI_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {                                                                         //APIL23以上
//                @SuppressLint("WifiManagerLeak") WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
//                this.wfManager = manager;
//            }
			if ( wfManager != null ) {
				if ( wfManager.isWifiEnabled() == true ) {// if (wfManager.getWifiState() == wfManager.WIFI_STATE_ENABLED) {
					WifiInfo info = wfManager.getConnectionInfo();
					if ( infoList == null ) {
						infoList = new ArrayList< String >();
					}


//                    infoList.add(SsId);
					int ipAdr = info.getIpAddress();
					String IpAddress = String.format("IP Adrress : %02d.%02d.%02d.%02d", (ipAdr >> 0) & 0xff, (ipAdr >> 8) & 0xff, (ipAdr >> 16) & 0xff, (ipAdr >> 24) & 0xff);
//                    infoList.add(IpAddress);
//                    String MacAddress = String.format("MAC Address : %s", info.getMacAddress());    // MACアドレスを取得
//                    infoList.add(MacAddress);
//                    int rssi = info.getRssi();    // 受信信号強度&信号レベルを取得
//                    String rssiStr = String.format("rssi : %s", rssi);    // 受信信号強度&信号レベルを取得
//                    infoList.add(rssiStr);
//                    int level = WifiManager.calculateSignalLevel(rssi, 5);
//                    String levelStr = String.format("level : %s", level);    // 受信信号強度&信号レベルを取得
//                    infoList.add(levelStr);
//                    String Rssi = String.format("RSSI : %d / Level : %d/4", rssi, level);
//                    infoList.add(Rssi);
//                    mggStr = infoList.toString();
					mggStr = info.toString();
					mggStr = mggStr + "," + IpAddress;
					mggStr = mggStr.replace(",", "\n");
				}
			}
			String titolStr = "現在の接続;WifiInfo";
			messageShow(titolStr, mggStr);
			Log.i(TAG, dbMsg);
		} catch (NullPointerException e) {
			Log.e(TAG, dbMsg + "で" + e.toString());// 適切な例外処理をしてください。
		}
	}
//                public void onClick(View v) {
////                    final String TAG = "onClick[sai_scan_bt]";
////                    String dbMsg=null;
////                    try{
////                        imamnoWiFi();		//wifiStateなどの取得
////                    }catch (Exception e) {
////                        Log.e(TAG , dbMsg +"で"+e.toString());
////                    }
////                }
////            });
////            imamnoWiFi();		//wifiStateなどの取得
////        }catch (Exception e) {
////            Log.e(TAG , dbMsg +"で"+e.toString());
////        }
////    }
//
//

//
//    public void dlogHyouji(int selID) {                //カスタムダイアログ表示
//        final String TAG = "dlogHyouji[]";
//        String dbMsg = null;
//        try {
//            dbMsg = selID + ")" + apList.get(selID).BSSID;
//            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);                 // カスタムビューを設定
//            View layout = inflater.inflate(R.layout.info, (ViewGroup) findViewById(R.id.info_root));
//            TableLayout infoTable = (TableLayout) layout.findViewById(R.id.infoTable);                                        //情報テーブル
//            TextView info_name_tv = (TextView) layout.findViewById(R.id.info_name_tv);                            //設定した名称
//            TextView info_ssid_tv = (TextView) layout.findViewById(R.id.kr_slash);                                //ネットワーク名
//            TextView info_bssid_tv = (TextView) layout.findViewById(R.id.info_bssid_tv);                            //MACアドレス
//            TextView info_frq_tv = (TextView) layout.findViewById(R.id.info_frq_tv);                                    //チャンネル周波数
//            TextView info_level_tv = (TextView) layout.findViewById(R.id.info_level_tv);                            //信号レベル
//            TextView info_capabilities_tv = (TextView) layout.findViewById(R.id.info_capabilities_tv);            //暗号化情報
//            TextView info_other_tv = (TextView) layout.findViewById(R.id.other_tv);            //その他
//
//            Button info_tojiru_bt = (Button) layout.findViewById(R.id.info_tojiru_bt);                                    //閉じるボタン
//            //		info_tojiru_bt.setVisibility(View.GONE);
//            Button info_setuzoku_bt = (Button) layout.findViewById(R.id.info_setuzoku_bt);                            //接続ボタン
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);                                                     // アラーとダイアログ を生成
//            infoTable.setColumnStretchable(1, true);                                                    //横幅いっぱいに広げる
//
//            builder.setTitle("情報表示");
//            builder.setView(layout);
//            String ssidTx = apList.get(selID).SSID.toString();            //getSSID()	SSIDを取得します。
//            if (!ssidTx.equals("")) {
//                info_ssid_tv.setText(ssidTx);                                //ネットワーク名
//            } else {
//                info_setuzoku_bt.setVisibility(View.GONE);
//            }
//            String bsssidTx = apList.get(selID).BSSID.toString();                    //getBSSID()	BSSIDを取得します。	getMacAddress()	MACアドレスを取得します。
//            if (!bsssidTx.equals("")) {
//                info_bssid_tv.setText(bsssidTx);                            //MACアドレス
//            }
//            String frqTx = String.valueOf(apList.get(selID).frequency);                    //getBSSID()	BSSIDを取得します。	getMacAddress()	MACアドレスを取得します。
//            dbMsg = dbMsg + ",チャンネル周波数= " + frqTx;
//            info_frq_tv.setText(frqTx);                    //チャンネル周波数
//            String levelTx = String.valueOf(apList.get(selID).level);                //getBSSID()	BSSIDを取得します。	getMacAddress()	MACアドレスを取得します。
//            dbMsg = dbMsg + ",信号レベル= " + levelTx;
//            info_level_tv.setText(levelTx);                                //信号レベル
//            info_capabilities_tv.setText(apList.get(selID).capabilities.toString());                    //暗号化情報
//            String otherStr = "timestamp=" + apList.get(selID).timestamp + "\r";
//            info_other_tv.setText(otherStr);
//
//
//            info_tojiru_bt.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    final String TAG = "[info_tojiru_bt]";
//                    String dbMsg = " Cancel ボタンクリック";
//                    try {
//                        Log.i(TAG, dbMsg);
//                    } catch (Exception e) {
//                        Log.e(TAG, dbMsg + "で" + e.toString());
//                    }
//                }
//            });
//            info_setuzoku_bt.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    final String TAG = "[info_setuzoku_bt]";
//                    String dbMsg = " 接続ボタン";
//                    try {
//                        Log.i(TAG, dbMsg);
//                    } catch (Exception e) {
//                        Log.e(TAG, dbMsg + "で" + e.toString());
//                    }
//                }
//            });
//
//            builder.create().show();                // 表示
//
//        } catch (NullPointerException e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }
//
//    //接続実績表示/////////////////Wifiの電波強度によって、接続先の無線LANスポットを変える方法	http://web-terminal.blogspot.jp/2013/12/wifilan.html
//    public void rirekiHyouji() {            //接続実績表示へ
//        final String TAG = "rirekiHyouji[]";
//        String dbMsg = null;
//        try {
//            //   titolTable.setVisibility(View.GONE);;													//消す
//            rirekiSyutoku();            //接続実績情報取得
//            //		Log.i(TAG, dbMsg);
//        } catch (Exception e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }
//
//    public void rirekiSyutoku() {            //接続実績情報取得
//        final String TAG = "rirekiHyouji[]";
//        String dbMsg = null;
//        try {
//            List<WifiConfiguration> config_list = wfManager.getConfiguredNetworks();            // 接続実績のあるwifi一覧を取得;サプリカントに設定されているすべてのネットワークのリスト
//            dbMsg = config_list.size() + "件";
//            String[] aps = new String[config_list.size()];
//            for (int i = 0; i < config_list.size(); i++) {            // 接続経験のあるスポットを順に確認する
//                dbMsg = i + "/" + config_list.size() + ")";
//                aps[i] = getResources().getString(R.string.info_a_bssid) + " " + config_list.get(i).BSSID;            //BSSID	MACアドレス
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_ssid) + " ; " + " " + config_list.get(i).SSID;            //
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_hidden_ssid) + " " + config_list.get(i).networkId;            //ネットワークID
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_status) + " " + config_list.get(i).status;            //"">ネットワークの状態</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_hidden_ssid) + " " + config_list.get(i).hiddenSSID;            //"ステルスモードの状態
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_allowed_auth_algorithms) + " " + config_list.get(i).allowedAuthAlgorithms;            //認証プロトコルセット
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_allowed_allowed_group_ciphers) + " " + config_list.get(i).allowedGroupCiphers;            //"">グループの暗号セット</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_allowed_allowed_key_management) + " " + config_list.get(i).allowedKeyManagement;            //">キー管理プロトコルのセット</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_allowed_allowed_pairwise_ciphers) + " " + config_list.get(i).allowedPairwiseCiphers;            //"">WPAのための暗号とのペア情報</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_allowed_protocols) + " " + config_list.get(i).allowedProtocols;            //"">セキュリティプロトコルのセット</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_pre_shared_key) + " " + config_list.get(i).preSharedKey;            //"">WPA-PSKのキー</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a__prioritye) + " " + config_list.get(i).priority;            //e="">優先度</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_wep_keys) + " " + config_list.get(i).wepKeys;            //"">WEPキー</string>
//                aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_wep_txKey_index) + " " + config_list.get(i).wepTxKeyIndex;            //">WEPキーのインデックス</string>
//                if (Integer.parseInt(Build.VERSION.SDK) >= 18) {
//                    aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_allowed_enterprise_config) + " " + config_list.get(i).enterpriseConfig;            //APIL18 ;EAP関連の詳細情報</string>
//                }
//                aps[i] = aps[i] + "\n";            //一行空白
//                dbMsg = dbMsg + aps[i];
//            }
////			</WifiConfiguration>
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, aps);
//            ap_lv.setAdapter(adapter);
//            ap_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view,
//                                        int position, long id) {
//                    ListView listView = (ListView) parent;
//                    String dbMsg = position + ")";
//                    Log.i(TAG, dbMsg);
//                    String item = (String) listView.getSelectedItem();                            //選択されたアイテムを取得します
//                    dbMsg = dbMsg + item;
//                    dbMsg = dbMsg + "BSSID" + apList.get(position).BSSID;
//                    Log.i(TAG, dbMsg);
//                }
//            });
//
////			Log.i(TAG, dbMsg);
//        } catch (Exception e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }
//
//    //この端末の情報表示/////////////////Wifiの電波強度によって、接続先の無線LANスポットを変える方法	http://web-terminal.blogspot.jp/2013/12/wifilan.html
//    @SuppressWarnings("static-access")
//    public void konoHyouji() {            //この端末の情報
//        final String TAG = "konoHyouji[]";
//        String dbMsg = null;
//        try {
//            //     titolTable.setVisibility(View.GONE);;													//消す
//            WifiInfo info = wfManager.getConnectionInfo();                    //http://www.adakoda.com/adakoda/2009/03/android-wi-fiip-address.html
//            int ipAddress = info.getIpAddress();
//            String strIPAddess =
//                    ((ipAddress >> 0) & 0xFF) + "." +
//                            ((ipAddress >> 8) & 0xFF) + "." +
//                            ((ipAddress >> 16) & 0xFF) + "." +
//                            ((ipAddress >> 24) & 0xFF);
//            int rssi = info.getRssi();                                                    // 受信信号強度
//            int level = wfManager.calculateSignalLevel(rssi, 5);            // 信号レベルを取得
//            android.net.wifi.SupplicantState suppState = wfManager.getConnectionInfo().getSupplicantState();
//            String suppStateStr = String.valueOf(info.getDetailedStateOf(suppState));
//            String suppStateInfo = null;            //http://d.hatena.ne.jp/unagi_brandnew/20120403/1333453240
//            if (suppStateStr.equals("AUTHENTICATING")) {
//                suppStateInfo = getResources().getString(R.string.supp_authnticating);    //確立されるネットワーク・リンク（認証を実行する）  Network link established, performing authentication.
//            } else if (suppStateStr.equals("BLOCKED")) {
//                suppStateInfo = getResources().getString(R.string.supp_bloced);    //="">アクセスしたらブロックされました</string><!--		Access to this network is blocked. -->
//            } else if (suppStateStr.equals("CONNECTED")) {
//                suppStateInfo = getResources().getString(R.string.supp_connected);    //">	IP trafficは利用できなければなりません</string><!--	IP traffic should be available.-->
//            } else if (suppStateStr.equals("CONNECTING")) {
//                suppStateInfo = getResources().getString(R.string.supp_connecting);    //="">データ接続準備中。</string><!--	Currently setting up data connection.-->
//            } else if (suppStateStr.equals("DISCONNECTED")) {
//                suppStateInfo = getResources().getString(R.string.supp_disconnected);    //">IP trafficは御利用頂けません。.</string><!--		IP traffic not available.-->
//            } else if (suppStateStr.equals("DISCONNECTING")) {
//                suppStateInfo = getResources().getString(R.string.supp_disconnecting);    //">データ接続を解除中です。</string><!--Currently tearing down data connection..-->
//            } else if (suppStateStr.equals("FAILED")) {
//                suppStateInfo = getResources().getString(R.string.supp_failed);    //="">接続に失敗しました。</string><!--Attempt to connect failed..-->
//            } else if (suppStateStr.equals("IDLE")) {
//                suppStateInfo = getResources().getString(R.string.supp_idle);    //="">データ接続セットアップを始める準備をさせてください。</string><!--	Ready to start data connection setup.-->
//            } else if (suppStateStr.equals("OBTAINING_IPADDR")) {
//                suppStateInfo = getResources().getString(R.string.supp_obtaing_ipaddr);    //="">IPアドレスに情報を割り当てるためにDHCPサーバーからの返答を待っています。</string><!--	Awaiting response from DHCP server in order to assign IP address information.-->
//            } else if (suppStateStr.equals("SCANNING")) {
//                suppStateInfo = getResources().getString(R.string.supp_scanning);    //="">利用できるアクセス・ポイントを捜しています。</string><!--		Searching for an available access point.-->
//            } else if (suppStateStr.equals("SUSPENDED")) {
//                suppStateInfo = getResources().getString(R.string.supp_suspended);    //"">切断されました</string><!--			IP traffic is suspended-->
//            }
//
//            String[] aps = new String[1];
//            int i = 0;
//            aps[i] = getResources().getString(R.string.info_a_ssid) + " ;  " + info.getSSID().substring(1, info.getSSID().length() - 1);                                                                    //SSID
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_bssid) + " ; " + info.getBSSID();                                //	MAアドレス
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_ip_address) + " ; " + strIPAddess;                                            //IPアドレス
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_net_work_id) + " ; " + " " + info.getNetworkId();                            //	ネットワークID
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_get_rssi) + " ; " + info.getRssi();                                                //受信信号強度
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_signal_level) + " ; " + level;                                                //信号レベルを取得
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_hidden_ssid) + " ; " + info.getHiddenSSID();                                //ステルスモード
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_link_speed) + " ; " + info.getLinkSpeed();                                    //リンクスピード
//            aps[i] = aps[i] + "\n" + getResources().getString(R.string.info_a_supp_state) + " " + info.getDetailedStateOf(suppState);                //アプリカントの状態をマッピングします。
//            aps[i] = aps[i] + "\n(" + suppStateInfo + ")";
//            dbMsg = dbMsg + aps[i];
////			</WifiConfiguration>
//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, aps);
//            ap_lv.setAdapter(adapter);
//            ap_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view,
//                                        int position, long id) {
//                    ListView listView = (ListView) parent;
//                    String dbMsg = position + ")";
//                    Log.i(TAG, dbMsg);
//                    String item = (String) listView.getSelectedItem();                            //選択されたアイテムを取得します
//                    dbMsg = dbMsg + item;
//                    dbMsg = dbMsg + "BSSID" + apList.get(position).BSSID;
//                    Log.i(TAG, dbMsg);
//                }
//            });
//            //		Log.i(TAG, dbMsg);
//        } catch (Exception e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }
//
//    ////SSIDを端末に設定する	http://blog.dtdweb.com/2013/03/08/android-wifi-network////////////
//    public void scNasi_ssid() {                //セキュリティなしのSSIDを設定する
//        final String TAG = "ssidNasi[WiFiActivity]";
//        String dbMsg = null;
//        try {
//            String ssid = "SSID_NAME";
//            dbMsg = ssid;
//            WifiConfiguration config = new WifiConfiguration();
//            config.SSID = "\"" + ssid + "\"";
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//            config.allowedAuthAlgorithms.clear();
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            int networkId = wfManager.addNetwork(config); // 失敗した場合は-1となります
//            wfManager.saveConfiguration();
//            wfManager.updateNetwork(config);
//            Log.i(TAG, dbMsg);
//        } catch (Exception e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }

//    //現在の状況表示///////////////////////////////////////////////////////////////////////////////////////////////
////    protected void jyoukyouHyouji() {			//現在の状況表示へ
////        final String TAG = "focusGoSettei";
////        String dbMsg=null;
////        try{
////
////
////            dbMsg="テーブル幅の設定";
////            titolTable.setVisibility(View.VISIBLE);;													//表示する
////            titolTable.setColumnStretchable(1, true);													//横幅いっぱいに広げる	http://www.javadrive.jp/android/tablelayout/index5.html
////
////            dbMsg="ボタンの割付け";
////            wifi_onOff_bt.setOnClickListener(new OnClickListener() {								//wifiOn/Offボタン
////                public void onClick(View v) {
////                    final String TAG = "onClick[wifi_onOff_bt]" ;
////                    String dbMsg=null;
////                    try{
////                        if(wfManager.isWifiEnabled()) {
////                            wfManager.setWifiEnabled(false);		// WifiをOnからOffに	http://seesaawiki.jp/w/moonlight_aska/d/WiFi%A4%F2ON/OFF%A4%B9%A4%EB
////                            wifi_onOff_bt.setText(getResources().getString(R.string.wifi_off).toString());
////                            ssid_setuzoku() ;				//作成したSSIDに接続する
////                        }else {
////                            wfManager.setWifiEnabled(true);		// WifiをOffからOnに
////                            wifi_onOff_bt.setText(getResources().getString(R.string.wifi_on).toString());
////                        }
////                        imamnoWiFi();		//wifiStateなどの取得
////                    }catch (Exception e) {
////                        Log.e(TAG , dbMsg +"で"+e.toString());
////                    }
////                }
////            });
////            sai_scan_bt.setOnClickListener(new OnClickListener() { 									//再スキャンボタン
//    protected void imamnoWiFi() {            //wifiStateなどの取得
//        final String TAG = "imamnoWiF";
//        String dbMsg = null;
//        try {
//            /////////////
//            String infoTx = "";
////			if( info_tv.getText().toString() !=null){
////				infoTx = info_tv.getText().toString();
////			}
//            dbMsg = "infoTx=" + infoTx;/////////////
//            int wifiState = wfManager.getWifiState();            // android.permission.ACCESS_WIFI_STATE
//            dbMsg = dbMsg + ",wifiState=" + wifiState;/////////////
//            switch (wifiState) {                                                                    //	http://www.syboos.jp/android/doc/wifi-summary.html
//                case wfManager.WIFI_STATE_DISABLING:
//                    infoTx = "使用不可に変更中";               //getResources().getString(R.string.wifistate_disabling);		//0;「使用不可にする」をしている状態
//                    break;
//                case wfManager.WIFI_STATE_DISABLED:
//                    infoTx = "使用不可";        // getResources().getString(R.string.wifistate_disabled);			//1;使用不可
//                    //        wifi_onOff_bt.setText(getResources().getString(R.string.wifi_on).toString());
//                    break;
//                case wfManager.WIFI_STATE_ENABLING:
//                    infoTx = "使用可能に変更中";            // getResources().getString(R.string.wifistate_enabling);			//2; 「使用可能にする」をしている状態</string>
//                    break;
//                case wfManager.WIFI_STATE_ENABLED:
//                    infoTx = "使用可能";       //getResources().getString(R.string.wifistate_enabled);			//3;使用可能
////                    wifi_onOff_bt.setText(getResources().getString(R.string.wifi_off).toString());
//                    break;
//                case wfManager.WIFI_STATE_UNKNOWN:
//                    infoTx = "不明";    // getResources().getString(R.string.comon_fumei);				//4;不明</string>
//                    break;
//            }
//            dbMsg = dbMsg + ",infoTx=" + infoTx;////////////android.content.res.Resources$NotFoundException: String resource ID #0x17
//            //    wifiState_tv.setText(infoTx);
//            Log.i(TAG, dbMsg);
//            ssid_kensaku();                //圏内にあるSSIDをリストアップ
//        } catch (Exception e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }
//
//    public void wep_ssid() {                //WEPのSSID設定
//        final String TAG = "wep_ssid[WiFiActivity]";
//        String dbMsg = null;
//        try {
//            String ssid = "SSID_NAME";
//            dbMsg = ssid;
//            WifiConfiguration config = new WifiConfiguration();
//            config.SSID = "\"" + ssid + "\"";
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//            config.wepKeys[0] = "\"password\"";
//            config.wepTxKeyIndex = 0;
//            int networkId = wfManager.addNetwork(config); // 失敗した場合は-1となります
//            wfManager.saveConfiguration();
//            wfManager.updateNetwork(config);
//            Log.i(TAG, dbMsg);
//        } catch (Exception e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }
//
//    public void wpa_ssid() {                //WPA/WPA2-PSKのSSID設定
//        final String TAG = "wpa_ssid[WiFiActivity]";
//        String dbMsg = null;
//        try {
//            String ssid = "SSID_NAME";
//            dbMsg = ssid;
//            WifiConfiguration config = new WifiConfiguration();
//            config.SSID = "\"" + ssid + "\"";
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.preSharedKey = "\"password\"";
//            int networkId = wfManager.addNetwork(config); // 失敗した場合は-1となります
//            wfManager.saveConfiguration();
//            wfManager.updateNetwork(config);
//            Log.i(TAG, dbMsg);
//        } catch (Exception e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());
//        }
//    }
//
//    public void ssid_setuzoku() {                //作成したSSIDに接続する
//        final String TAG = "ssid_setuzoku[]";
//        String dbMsg = null;
//        int networkId = 0; // 上記設定で取得できたものを使用
//        String targetSSID = "hoge-ssid";
//        try {            // WiFi機能が無効の状態で呼び出されるとSSID検索の所でnullとなるので念のため例外処理を行なう
//            wfManager.startScan();                // ssidの検索を開始
//            for (ScanResult result : wfManager.getScanResults()) {
//                dbMsg = "level=" + result.level;                                    //信号レベル(dBm)
//                dbMsg = dbMsg + ",BSSID=" + result.BSSID;                    //APのMACアドレス
//                String resultSSID = result.SSID.replace("\"", "");            // Android4.2以降よりダブルクォーテーションが付いてくるので除去
//                dbMsg = dbMsg + ",SSID=" + result.SSID;                            //ネットワーク名
//                Log.i(TAG, dbMsg);
//                if (resultSSID.equals(targetSSID)) {
//                    if (networkId > 0) {                                                // 接続を行う
//                        for (WifiConfiguration c0 : wfManager.getConfiguredNetworks()) {        // 先に既存接続先を無効にしてから接続します
//                            wfManager.enableNetwork(c0.networkId, false);
//                        }
//                        wfManager.enableNetwork(networkId, true);
//                    }
//                    break;
//                }
//            }
//        } catch (NullPointerException e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());// 適切な例外処理をしてください。
//        }
//    }
//    //メニューボタンで表示するメニュー/////////////////////////////////////////////////////////////////////////////SSIDを端末に設定する//
////    @Override
////    public boolean onCreateOptionsMenu(Menu flMenu) {
////        //	//Log.d("onCreateOptionsMenu","NakedFileVeiwActivity;mlMenu="+flMenu);
////        getMenuInflater().inflate(R.menu.main_act_menu , flMenu);		//メニューリソースの使用
////        return super.onCreateOptionsMenu(flMenu);
////    }
////
////    public boolean makeOptionsMenu(Menu flMenu) {	//ボタンで表示するメニューの内容
////        return true;
////    }
////
////    @Override
////    public boolean onPrepareOptionsMenu(Menu flMenu) {			//表示直前に行う非表示や非選択設定
////        //	//Log.d("onPrepareOptionsMenu","NakedFileVeiwActivity;mlMenu="+flMenu);
////        flMenu.findItem(R.id.menu_kinu_saikensaku).setEnabled(true);		//再検索
////
////        flMenu.findItem(R.id.menu_item_sonota_settei).setEnabled(false);		//設定	MENU_SETTEI
////        flMenu.findItem(R.id.menu_item_sonota_help).setEnabled(true);		//ヘルプ表示	MENU_HELP
////        flMenu.findItem(R.id.menu_item_sonota_end).setEnabled(true);	//終了	MENU_END
////        return true;
////    }
////
////    @Override
////    public boolean onOptionsItemSelected(MenuItem item) {
////        final String TAG = "onOptionsItemSelected[]";
////        String dbMsg=ORGUT.nowTime(true,true,true);/////////////////////////////////////
////        try{
////            Intent intentPRF;
////            dbMsg = "MenuItem"+item.getItemId()+"="+item.toString();////////////////////////////////////////////////////////////////////////////
////            Log.d("onOptionsItemSelected",dbMsg);
////
////            int nowSelectMenu = item.getItemId();
////            switch (nowSelectMenu) {
////                case R.id.menu_kinu_saikensaku:		//.).setEnabled(true);		//再検索
////                    imamnoWiFi();		//wifiStateなどの取得
////                    return true;
//////
////                case R.id.menu_item_sonota_settei:		//設定
//////				prefHyouji(R.id.menu_item_sonota_settei);												//プリファレンス表示
////                    //			startActivityForResult(new Intent(WiFiActivity.this, MyPreferences.class) , R.id.menu_item_sonota_settei);
////                    return true;
////                case R.id.menu_item_sonota_help:						//ヘルプ表示	MENU_HELP
////                    Intent intentWV = new Intent(WiFiActivity.this,wKit.class);			//webでヘルプ表示
////                    intentWV.putExtra("dataURI",helpURL);		//"file:///android_asset/index.html"
////                    startActivity(intentWV);
////                    return true;
////                case R.id.menu_item_sonota_end:					//終了	MENU_END
////                    quitMe();		//このアプリを終了する
////                    return true;
//////
//////			case MENU_MP_BT_jyoukyou:						////550="Bluetooth接続状況";
//////				setuzokuBT_RetST="この端末のBluetoothの利用状況を確認します・";
//////				Log.d("onOptionsItemSelected",setuzokuBT_RetST);
//////				intentA3 = new Intent(MuPlayer.this,Alart3BT.class);	//最大3ボタンのダイアログ
//////				intentA3.setAction(Intent.ACTION_VIEW);
//////				intentA3.putExtra("dTitol", "Bluetooth接続状況");		//ダイアログタイトル
//////				intentA3.putExtra("dMessage",setuzokuBT_RetST );			//アラート文
//////				intentA3.putExtra("Msg1", "了解");						//RESULT_OKのキーフェイス
//////				intentA3.putExtra("Msg3","再接続");						//RESULT_CANCELEDのキーフェイス
////////					intentA3.putExtra("Msg2","中立ボタン");							//ボタン2のキーフェイス
//////				startActivityForResult(intentA3, MENU_MP_BT_jyoukyou); //正常に戻れば
//////				return true;
//////			case MENU_MP_BT_yuukouka:						//551;Bluetoothの有効化
////////				MBT.checkBT();					//再接続
////////					Intent intentBT = new Intent(MuPlayer.this,myBTooth.class);			//BlueTooth
////////					startActivity(intentBT);
//////				return true;
//////			case MENU_MP_SETUP:
//////				dbMsg="設定";///////////////////////////////////////////////////////////////////
////////			Intent intent = new Intent(this,com.sen.gdpc.nakidfileveiwlite.NfvPref.class);
////////			startActivity(intent);
//////				return true;
//////			case MENU_MU_FINISH:
//////				dbMsg="音楽機能終了";///////////////////////////////////////////////////////////////////
//////				saiseiCyuu();			//再生中の状況保存
//////				MuPlayer.this.finish();
////////				NML.quitMe();
//////				return true;
//////			case MENU_MP_FINISH:						//リストに戻る
//////				dbMsg="再生中の状況保存";///////////////////////////////////////////////////////////////////
//////				saiseiCyuu();			//再生中の状況保存
////////				back2List(artistID,albumID,titolID, NakedFileVeiwActivity.MENU_MUSCK_TITOL);		//第4引数に指定したリスト//曲リスト作成
//////				return true;
//////			case MENU_MP_FINISH_STOP:					//すべて終了
//////				dbMsg="PlayerService";///////////////////////////////////////////////////////////////////
//////				saiseiCyuu();			//再生中の状況保存
//////				if (isServiceRunning(psSarviceUri)) {
//////					if(PlayerService.mPlayer !=null){
//////						if(PlayerService.mPlayer.isPlaying()){
//////							PlayerService.mPlayer.stop();
//////						}
//////						PlayerService.mPlayer.release();
//////						PlayerService.mPlayer=null;
//////					}
//////				}
//////				dbMsg="再生中の状況保存";///////////////////////////////////////////////////////////////////
////////				setResult(NfvMusic.closeMeFlag);
//////				pNFVeditor.putString("prefZenkaisyuuryou_sum","音楽再生").commit();							//前回終了時の状態
//////				dbMsg="finish";///////////////////////////////////////////////////////////////////
//////				MuPlayer.this.finish();
//////	//			MuPlayer.this.getParent().finish();
//////	//			MuPlayer.this.getParent().getParent().finish();
//////	//			back2List(artistID,albumID,titolID, NakedFileVeiwActivity.MENU_MUSCK_TITOL);		//第4引数に指定したリスト//曲リスト作成
//////				return true;
////            }
////            return false;
////        } catch (Exception e) {
////            Log.e(TAG,dbMsg +"で"+e.toString());
////            return false;
////        }
////    }
//
////    @Override
////    public void onOptionsMenuClosed(Menu flMenu) {
////        //Log.d("onOptionsMenuClosed","NakedFileVeiwActivity;mlMenu="+flMenu);
////    }
////
////    ///ライフサイクル/////////////////////////////////////////////////////////メニューボタンで表示するメニュー//
////
////    @Override
////    public void onWindowFocusChanged(boolean hasFocus) {            //Activityから、フォーカスが移った直後か失った直後
////        final String TAG = "onWindowFocusChanged[]";
////        String dbMsg = null;
////        try {
////            /////////////
//////            Log.i(TAG, dbMsg);
//////            dbMsg ="tugino_sousa=" + tugino_sousa + ",hasFocus=" +hasFocus ;/////////////////////////////////////
//////            if(tugino_sousa > 0){
//////                switch(tugino_sousa) {							//onWindowFocusChangedで次にフォーカスが切り替わった時の操作
//////                    case syoki_start_up:						//100Activityに置かれたアイテムのプロパティ取得
//////                        jyoukyouHyouji();		//現在の状況表示へ
//////                        break;
//////                    case syoki_start_up_rireki:
//////                        rirekiHyouji();		//接続実績表示へ
//////                        break;
//////                    case syoki_start_up_kore:
//////                        konoHyouji();			//この端末の情報
//////                        break;
//////                    default:
//////                        break;
//////                }
//////				tugino_sousa=0;
////
////
//////				dbMsg = dbMsg + "プレビュー=" + view_eria_ll.getLeft() +"～w;" +view_eria_ll.getWidth() + "ボタン=" + v_sizeBT.getLeft() +"～w;" +v_sizeBT.getWidth() ;//// ;				//エリア/
//////				dbMsg = dbMsg + ",web=" + web_eria_ll.getLeft() +"～w;" +web_eria_ll.getWidth()  + ",bkSurface=" + bkSurfaceview.getLeft() +"～w;" +bkSurfaceview.getWidth();/////////////////////////////////////////////
////            //	  		Log.i( TAG ,dbMsg);
//////            }
////        } catch (Exception e) {
////            Log.e(TAG, dbMsg + "；" + e);
////        }
////    }
////
////    @Override
////    protected void onRestart() {        //ActivityがonStop()の後、復活
////        super.onRestart();
////        final String TAG = "onRestart[WiFiActivity]";
////        String dbMsg = null;
////        try {
////            /////////////
////            Log.i(TAG, dbMsg);
////        } catch (Exception e) {
////            Log.e(TAG, dbMsg + "で" + e.toString());
////        }
////    }
////
////    @Override
////    protected void onResume() {            //Activityが前面になる時
////        super.onResume();
////        final String TAG = "onResume[WiFiActivity]";
////        String dbMsg = null;
////        try {
////            /////////////
////            Log.i(TAG, dbMsg);
////        } catch (Exception e) {
////            Log.e(TAG, dbMsg + "で" + e.toString());
////        }
////    }
////
////    @Override
////    protected void onStart() {        //Activityが画面に表示されるときに
////        super.onStart();
////        final String TAG = "onStart[WiFiActivity]";
////        String dbMsg = null;
////        try {
////            /////////////
////            Log.i(TAG, dbMsg);
////        } catch (Exception e) {
////            Log.e(TAG, dbMsg + "で" + e.toString());
////        }
////    }
////
////    @Override
////    protected void onPause() {        //Activityがバックグラウンドに移動
////        super.onPause();
////        final String TAG = "onPause[WiFiActivity]";
////        String dbMsg = null;
////        try {
////            /////////////
////            Log.i(TAG, dbMsg);
////        } catch (Exception e) {
////            Log.e(TAG, dbMsg + "で" + e.toString());
////        }
////    }
////
////    @Override
////    protected void onStop() {            // Activityが画面から見えなくなる時
////        super.onStop();
////        final String TAG = "onStop[WiFiActivity]";
////        String dbMsg = null;
////        try {
////            /////////////
////            Log.i(TAG, dbMsg);
////        } catch (Exception e) {
////            Log.e(TAG, dbMsg + "で" + e.toString());
////        }
////    }
////
////    @Override
////    protected void onDestroy() {            //Activityが終わる時
////        super.onDestroy();
////        final String TAG = "onDestroy[WiFiActivity]";
////        String dbMsg = null;
////        try {
////
////            Log.i(TAG, dbMsg);
////        } catch (Exception e) {
////            Log.e(TAG, dbMsg + "で" + e.toString());
////        }
////    }


	//GPS/////////////////////////////////////////////////////////////////////////////////////////////////
	LocationManager locationManager;
	double latitudeVal = -1;// 緯度の表示
	String latitudeStr = "";
	double longitudeVal = -1;       // 経度の表示
	String LongtudeStr = "";
	double accuracyVal = -1;                //精度
	String accuracyStr = "";
	double altitudeVal = -1;                //標高
	String altitudeStr = "";
	double pinpointingTimeVal = 0;                //測位時刻
	String pinpointingTimeValStr = "";

	private void locationStart() {
		Log.d("debug", "locationStart()");

		// LocationManager インスタンス生成
		locationManager = ( LocationManager ) getSystemService(LOCATION_SERVICE);

		if ( locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
			Log.d("debug", "location manager Enabled");
		} else {
			// GPSを設定するように促す
			Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(settingsIntent);
			Log.d("debug", "not gpsEnable, startActivity");
		}

		if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION ,}, rp_getGPSInfo);
			Log.d("debug", "checkSelfPermission false");
			return;
		}

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, rp_getGPSInfo, 50, ( LocationListener ) this);
		List< String > AllProviders = locationManager.getAllProviders();

		//プロバイダを選ぶ上での基準を設定する
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);   //精度が高いを設定
		String bestProvider = locationManager.getBestProvider(criteria, true);  //最適なプロバイダを取得する;gpsなど

		Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		writeLocationInfo(lastKnownLocation);

		getWiFiDatas();
	}

	public void writeLocationInfo(Location location) {
		if ( location != null ) {
			latitudeVal = location.getLatitude();
			latitudeStr = "Latitude:" + latitudeVal;// 緯度の表示
			latitude_tv.setText(":" + latitudeVal);

			longitudeVal = location.getLongitude();       // 経度の表示
			LongtudeStr = "Longtude:" + longitudeVal;
			longitude_tv.setText("/" + longitudeVal);

			altitudeVal = location.getAltitude();                //標高
			altitudeStr = "Altitude:" + altitudeVal;
			altitude_tv.setText("/" + altitudeVal + "m");

			accuracyVal = location.getAccuracy();                //精度
			accuracyStr = "Accuracy:" + accuracyVal;
			accuracy_tv.setText(";" + accuracyVal);

			pinpointingTimeVal = location.getTime();                //測位時刻
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  //yyyy-MM-dd HH:mm:ss.SSS
			pinpointingTimeValStr = df.format(pinpointingTimeVal);
			timestanp_tv.setText("" + pinpointingTimeValStr);
			pinpointingTimeValStr = "Altitude:" + pinpointingTimeValStr;
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch ( status ) {
			case LocationProvider.AVAILABLE:
				Log.d("debug", "LocationProvider.AVAILABLE");
				break;
			case LocationProvider.OUT_OF_SERVICE:
				Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
				break;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		writeLocationInfo(location);
	}

	//   @Override
	public void onProviderEnabled(String provider) {

	}

	//  @Override
	public void onProviderDisabled(String provider) {

	}

	//GoogleDriveにデータ送信/////////////////////////////////////////////////////////////////////////////////////////////
	////http://vividcode.hatenablog.com/entry/20130908/1378613811/  /////////////
	/**
	 * Google API Consoleでプロジェクト作成
	 * プロジェクト名      ;   NUloger
	 * プロジェクト ID は    ;   nuloger です
	 * <p>
	 * アカウント　hkuwayama@coresoft-net.co.jp
	 * PW          ; cs06hiroshima
	 */
	private static final String TAG = "DriveSample";
	private static final int REQUEST_CODE_RESOLUTION = 1;
	private GoogleApiClient googleApiClient = null;
	private com.google.api.services.drive.Drive service = null;
	private GoogleAccountCredential credential = null;
	private String FILE_TITLE = "google_drive_test";                //static final
	private String accountName = "";

	protected void sendAPData() {
		final String TAG = "sendAPData";
		String dbMsg = "";//"service=" + service.about();
		date = new Date(System.currentTimeMillis());
		send_time_tv.setText(df.format(date));
		accountName_tv.setText(accountName);
		FILE_TITLE = (student_id+ "_" + dffn.format(date) + ".csv").toString();
		if ( service == null ) {                                                                                //orgは onStartで
			conectReady();
		} else {
			conectSaveStart();
		}
		myLog(TAG, dbMsg);
	}

	protected void conectReady() {
		final String TAG = "conectReady";
		String dbMsg = "accountName=" + accountName;
		try {
			myLog(TAG, dbMsg);
			if ( accountName.equals("") ) {
				credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));   //アカウントの選択画面を表示
//				if(credential.){
//
//				}
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			} else {
				conectSaveStart();
			}
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
	}

	/////http://vividcode.hatenablog.com/entry/20130908/1378613811////////////////////////////////////////////////////////////////////////////////////////////
	protected void conectSaveStart() {
		final String TAG = "conectSaveStart";
		String dbMsg = "accountName=" + accountName;
		try {
			credential.setSelectedAccountName(accountName);
			service = getDriveService(credential);
			saveTextToDrive();
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		myLog(TAG, dbMsg);
	}


	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		final String TAG = "onActivityResult";
		String dbMsg = "requestCode=" + requestCode + ",resultCode=" + resultCode;
		try {
			switch ( requestCode ) {
				case REQUEST_ACCOUNT_PICKER:
					if ( accountName != null ) {
						if ( resultCode == RESULT_OK && data != null && data.getExtras() != null ) {
							this.accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
							dbMsg += ",accountName=" + accountName;
							conectSaveStart();
						}
					}
					break;
				case REQUEST_AUTHORIZATION:
					if ( resultCode == Activity.RESULT_OK ) {
						saveTextToDrive();
					} else {
						startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
					}
					break;
			}
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		myLog(TAG, dbMsg);
	}

	private void saveTextToDrive() {
		final String TAG = "saveTextToDrive";
		String dbMsg = "開始";
		try {
			final String inputText = makeSendList();                //((EditText)findViewById(R.id.editText)).getText().toString();
			dbMsg = "inputText=" + inputText;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "saveTextToDrive_Thread";
					String dbMsg = "FILE_TITLE=" + FILE_TITLE;
					try {

						File body = new File();
						body.setTitle(FILE_TITLE);//fileContent.getName());
						body.setMimeType("text/plain");
						ByteArrayContent content = new ByteArrayContent("text/plain", inputText.getBytes(Charset.forName("UTF-8")));

//						// 指定のタイトルのファイルの ID を取得
//						String fileIdOrNull = null;
//						FileList list = service.files().list().execute();
//						for ( File f : list.getItems() ) {
//							if ( FILE_TITLE.equals(f.getTitle()) ) {
//								fileIdOrNull = f.getId();
//							}
//						}
//						if ( fileIdOrNull == null ) {
						service.files().insert(body, content).execute();
//				//		Toast.makeText(getApplicationContext(), "insert!", Toast.LENGTH_SHORT).show();
//							dbMsg += ">>insert" ;
//						} else {
//							service.files().update(fileIdOrNull, body, content).execute();
//						Toast.makeText(getApplicationContext(), "update!", Toast.LENGTH_SHORT).show();
//							dbMsg += ">>update" ;
//						}
						// TODO 失敗時の処理?
					} catch (UserRecoverableAuthIOException er) {
						Log.e(TAG, dbMsg + ";でエラー発生；" + er);
						startActivityForResult(er.getIntent(), REQUEST_AUTHORIZATION);
					} catch (IOException er) {
						Log.e(TAG, dbMsg + ";でエラー発生；" + er);
						//			Toast.makeText(getApplicationContext(), "error occur..", Toast.LENGTH_SHORT).show();
						//			e.printStackTrace();
					}
				}
			});
			t.start();
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		myLog(TAG, dbMsg);
	}

	private void loadTextFromDrive() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				final String TAG = "loadTextFromDrive";
				String dbMsg = "開始";
				try {
					// 指定のタイトルのファイルの ID を取得
					String fileIdOrNull = null;
					FileList list = service.files().list().execute();
					for ( File f : list.getItems() ) {
						if ( FILE_TITLE.equals(f.getTitle()) ) {
							fileIdOrNull = f.getId();
						}
					}

					InputStream is = null;
					if ( fileIdOrNull != null ) {
						File f = service.files().get(fileIdOrNull).execute();
						is = downloadFile(service, f);
					}
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					try {
						StringBuffer sb = new StringBuffer();
						sb.append(br.readLine());

						final String text = sb.toString();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								//					((EditText)findViewById(R.id.editText)).setText(text);
							}
						});
					} finally {
						if ( br != null ) br.close();
					}
					myLog(TAG, dbMsg);
					// TODO 失敗時の処理?
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), "error occur..", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	// https://developers.google.com/drive/v2/reference/files/get より
	private static InputStream downloadFile(com.google.api.services.drive.Drive service, File file) {
		final String TAG = "downloadFile";
		String dbMsg = "開始";
		if ( file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0 ) {
			try {
				HttpResponse resp = service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl())).execute();
				return resp.getContent();
			} catch (IOException e) {
				// An error occurred.
				e.printStackTrace();
				return null;
			}
		} else {
			// The file doesn't have any content stored on Drive.
			return null;
		}
	}

	private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
		return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
	}

	/*
	 * ①無線LAN情報の取得方法    http://seesaawiki.jp/w/moonlight_aska/d/WiFi%C0%DC%C2%B3%BE%F0%CA%F3%A4%F2%BC%E8%C6%C0%A4%B9%A4%EB

上記Xamarin用ではなくAndroidStudio用のソースですが、同様のAPIがXamarinでも使えるはずです。
多少APIやプロパティの名前が異なっていると思います。
(getSystemServiceがXamarinだとGetSystemServiceとか、manager.getConnectionInfo()の代わりにmanager.ConnectionInfoとか
微妙な違いがあります)

なお、Androidの場合、デバイスの利用許可のためにはマニフェストファイルに設定が必要なので注意してください。
利用許可を付与していないとアプリがいきなり終了することがほとんどです。

たぶん、無線LAN情報の取得はそれほど苦労せず実装できると思いますが、ただし、実装には下記の③の対応が必要になるかと思います。


②GoogleDriveに保存する方法
日本語の文献はほとんど無いようです。ちょっと苦戦するかもしれません。

https://xamarindev.blog/2017/03/22/google-drive-api-with-xamarin-forms/

https://github.com/xamarin/google-apis

https://codexample.org/questions/168187/xamarin-forms-upload-to-google-drive.c

https://github.com/google/google-api-dotnet-client-samples/blob/master/Drive.Sample/Program.cs
  https://qiita.com/linquanstudio/items/23fca582ba6ae0d6d328
  
※下記はXamarinではなくAndroidStudioを使ってjavaで実装する例
　https://qiita.com/hituziando/items/dfbc64ed104e3cf2e431
　https://www.isus.jp/smartphones/connecting-to-google-drive-from-an-android-app/
　http://vividcode.hatenablog.com/entry/20130908/1378613811

③プラットフォーム依存処理(DependencyService利用)
基本的に共通部分のXamarin.Fromsで画面表示を行い、各プラットフォーム依存部分はインタフェースを定義して、AndroidやiOSで処理を分ける形になります。
今回はAndroidだけとはいえ、Androidプロジェクト側でしか処理できない部分が必ず発生しますので紹介しておきます。

		名古屋大	https://coresoft-z.backlog.jp/git/NAGOYA_U

Google API Consoleでプロジェクト作成
アカウント			hkuwayama@coresoft-net.co.jp
PW          		; cs06hiroshima
プロジェクト名      ;   NUloger
プロジェクト ID は	;   nuloger です
プロジェクト番号	;	1012140024872


OAuth 2.0 クライアント ID 	;	NUAndroid1
keytool -exportcert -keystore path-to-debug-or-production-keystore -list -v
 API key	2017/12/26	なし	AIzaSyAcW0we2TB_omDTlk-3qTGdtJ85TYXF2JM

ACriant01         com.example.hkuwayama.nuloger     3E:19:82:D8:6A:F4:98:11:DC:3C:83:80:D0:53:9E:F2:00:A1:A3:70
hkuwayama@coresoft-net.co.jp        com.example.hkuwayama.nuloger

    * */

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		final String TAG = "onConnected";
		String dbMsg = "開始";
		try {
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		myLog(TAG, dbMsg);
	}

	@Override
	public void onConnectionSuspended(int i) {
		final String TAG = "onConnectionSuspended";
		String dbMsg = "開始";
		try {
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		myLog(TAG, dbMsg);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		final String TAG = "onConnectionFailed";
		String dbMsg = "開始";
		try {
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		myLog(TAG, dbMsg);
	}

	protected void actionDisconnect() {
		final String TAG = "actionDisconnect";
		String dbMsg = ",isConnected=" + googleApiClient.isConnected();
		googleApiClient.disconnect();                  // 接続解除
		dbMsg += ">>" + googleApiClient.isConnected();
		myLog(TAG, dbMsg);
	}


	public void messageShow(String titolStr, String mggStr) {
		new AlertDialog.Builder(MainActivity.this).setTitle(titolStr).setMessage(mggStr).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create().show();
	}

	public boolean debugNow = true;

	public void myLog(String TAG, String dbMsg) {
		try {
			if ( debugNow ) {
				Log.i(TAG, dbMsg);
			}
		} catch (Exception e) {
			Log.e(TAG, dbMsg + "で" + e.toString());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//http://m-shige1979.hatenablog.com/entry/2015/01/17/080000
	private int count = 0;

	private MyTimerTask timerTask = null;
	private Timer timer = null;
	private Handler handler = new Handler();

	// タイマータスク用のクラス
	class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			handler.post(new Runnable() {
				public void run() {
					final String TAG = "MyTimerTask";
					String dbMsg = "開始";
					try {
						sendAPData();
					} catch (Exception er) {
						Log.e(TAG, dbMsg + ";でエラー発生；" + er);
					}
					myLog(TAG, dbMsg);
				}
			});
		}
	}
}
