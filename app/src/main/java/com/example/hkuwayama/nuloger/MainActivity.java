package com.example.hkuwayama.nuloger;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
//import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import static android.os.SystemClock.elapsedRealtime;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.firebase.auth.GoogleAuthProvider;

import android.os.Handler;

import java.io.BufferedReader;
//import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener,
																	   //	MyPreferenceFragment.OnFragmentInteractionListener,
																	   GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	public Toolbar toolbar;                        //このアクティビティのtoolBar
	public DrawerLayout drawer;
	public ActionBarDrawerToggle abdToggle;        //アニメーションインジケータ
	public NavigationView navigationView;

	public java.io.File wrDir;//自分のアプリ用の内部ディレクトリ
	public String wrDirName;
	public final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public final DateFormat dfs = new SimpleDateFormat("MM/dd HH:mm:ss");
	public final DateFormat dffn = new SimpleDateFormat("yyyyMMddHHmmss");
	public Date date;
	public boolean isServiceRunning = false;            //サービス実行中

	public int fragmentNo = -1;
	public MainFragment mainFragment = null;                            //メイン画面
	//	public FloatingActionButton fab;
	public int mainFragmentNo = 1;
	public MyPreferenceFragment myPreferenceFragment = null;        //設定画面
	public int myPreferenceFragmentNo = 2;
	public MyWebFragment webFragment = null;                        //web画面
	public int webFragmentNo = 3;
	public RecordService RS = null;
	private UpdateReceiver upReceiver;
	private IntentFilter intentFilter;

	public LocationManager locationManager;
	double latitudeVal = -1;// 緯度の表示
	public String latitudeStr = "";
	double longitudeVal = -1;       // 経度の表示
	public String LongtudeStr = "";
	double accuracyVal = -1;                //精度
	public String accuracyStr = "";
	double altitudeVal = -1;                //標高
	public String altitudeStr = "";
	double pinpointingTimeVal = 0;                //測位時刻
	public String pinpointingTimeStr = "";
	public String now_count = "0";


	public static SharedPreferences sharedPref;
	public SharedPreferences.Editor myEditor;
	public String student_id = "1234567890";                            //"実施者ID（学籍番号）
	public String accountName = "";                                //保存先サーバのアカウント"
	public String client_id = "";
	public String stock_count = "500";                            //自動送信するデータ数"
	public String waiting_scond = "5";              //記録間隔
	public String gps_sarch_span = "5";              //GPS情報更新間隔[秒]
	public String gps_mini_destance = "10";         //GPS通知最小距離[m]
	public String local_dir = "";              //端末内の保存先
	public String local_dir_size = "";              //保存先の空き容量
	public String max_file_size = "";              //これまでの最大ファイルサイズ="

	/**
	 * このアプリケーションの設定ファイル読出し
	 **/
	public void readPrif() {
		final String TAG = "readPrif[MA}";
		String dbMsg = "";//////////////////
		try {
			sharedPref = PreferenceManager.getDefaultSharedPreferences(this);            //	getActivity().getBaseContext()
			myEditor = sharedPref.edit();

			if ( checkSTORAGEParmission(rp_readPrif) && checkWiFiParmission(rp_readPrif) && checkAccountParmission(rp_readPrif) && checkGPSParmission(rp_readPrif) ) {
				Map< String, ? > keys = sharedPref.getAll();
				dbMsg = dbMsg + ",読み込み開始;keys=" + keys.size() + "件";

				int i = 0;
				for ( String key : keys.keySet() ) {
					i++;
					String rStr = keys.get(key).toString();
					dbMsg = dbMsg + "\n" + i + "/" + keys.size() + ")" + key + "は" + rStr;
					try {
						if ( key.equals("student_id_key") ) {
							student_id = keys.get(key).toString();
							dbMsg = dbMsg + ",実施者ID（学籍番号）=" + student_id;
						} else if ( key.equals("accountName_key") ) {
							accountName = keys.get(key).toString();
							dbMsg = dbMsg + ",保存先サーバのアカウント=" + accountName;
						} else if ( key.equals("client_id_key") ) {
							client_id = keys.get(key).toString();
							dbMsg = dbMsg + ",保存先サーバのclient Id=" + client_id;
						} else if ( key.equals("stock_count_key") ) {
							stock_count = keys.get(key).toString();
							dbMsg = dbMsg + ",自動送信するデータ数=" + stock_count;
						} else if ( key.equals("waiting_scond_key") ) {
							waiting_scond = keys.get(key).toString();
							dbMsg = dbMsg + ",記録間隔=" + waiting_scond;
						} else if ( key.equals("gps_sarch_span_key") ) {
							if ( !keys.get(key).toString().isEmpty() ) {
								gps_sarch_span = keys.get(key).toString();
							}
							dbMsg = dbMsg + ",GPS情報通知間隔[秒]=" + gps_sarch_span;
						} else if ( key.equals("gps_mini_destance_key") ) {
							if ( !keys.get(key).toString().isEmpty() ) {
								gps_mini_destance = keys.get(key).toString();
							}
							dbMsg = dbMsg + ",GPS通知最小距離[m]=" + gps_mini_destance;
						} else if ( key.equals("local_dir_key") ) {
							local_dir = keys.get(key).toString();
							dbMsg = dbMsg + ",端末内の保存先=" + local_dir;
						} else if ( key.equals("local_dir_size_key") ) {
							local_dir_size = keys.get(key).toString();
							dbMsg = dbMsg + ",保存先の空き容量=" + local_dir_size;
						} else if ( key.equals("max_file_size_key") ) {
							max_file_size = keys.get(key).toString();
							dbMsg = dbMsg + ",これまでの最大ファイルサイズ=" + max_file_size;
						}
					} catch (Exception e) {
						myLog(TAG, dbMsg + "で" + e.toString());
					}
				}
				//課題；保存先サーバのアカウント
				setSaveParameter();                 //保存可能上限の確認と修正
			}
			this.myLog(TAG, dbMsg);
		} catch (Exception e) {
			Log.e(TAG, dbMsg + "；" + e);
		}
	}                                                                     //プリファレンスの読込み

	//LifeCycle/////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		readPrif();
		setContentView(R.layout.activity_main);
		toolbar = ( Toolbar ) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		initDrawer();
		if ( isMyService(RecordService.class.getName()) ) {               // {                //サービス実行中
			isServiceRunning = true;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	/**
	 * onStart, onPauseの次
	 */
	@SuppressLint ( "MissingPermission" )
	@Override
	protected void onResume() {
		super.onResume();
		final String TAG = "onResume[MA]";
		String dbMsg = "";//////////////////E/ActivityThread: Performing stop of activity that is not resumed: {com.hijiyama_koubou.atare_kun/com.hijiyama_koubou.atare_kun.AtarekunnActivity
		try {
			callMain();
			myLog(TAG, dbMsg);
		} catch (Exception e) {
			myLog(TAG, dbMsg + "で" + e.toString());
		}
	}                                                                 // onStart, onPauseの次

	///NavigationDrorwer/////////////////////////////////////////////////////////////////LifeCycle//

	//ヘッダーアイコンのタップ
	@Override
	public void onBackPressed() {
		//	DrawerLayout drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
		if ( drawer.isDrawerOpen(GravityCompat.START) ) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		final String TAG = "onPostCreate[MA}";
		String dbMsg = "";
		try {
			abdToggle.syncState();    //NaviIconの回転アニメーションなど   Attempt to invoke virtual method 'void android.support.v7.app.ActionBarDrawerToggle.syncState()' on a null object reference
			myLog(TAG, dbMsg);
		} catch (Exception e) {
			myLog(TAG, dbMsg + "で" + e.toString());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		final String TAG = "onConfigurationChanged[MA}";
		String dbMsg = "";
		try {
			abdToggle.onConfigurationChanged(newConfig);
			myLog(TAG, dbMsg);
		} catch (Exception e) {
			myLog(TAG, dbMsg + "で" + e.toString());
		}
	}

	/**
	 * NaviViewの初期設定
	 * 開閉のイベント設定
	 **/
	public void initDrawer() {            //http://qiita.com/androhi/items/f12b566730d9f951b8ec
		final String TAG = "initDrawer[MA}";
		String dbMsg = "";
		try {
			//		nvh_img = ( ImageView ) findViewById(R.id.nvh_img);                //NaviViewヘッダーのアイコン
			drawer = ( DrawerLayout ) findViewById(R.id.drawer_layout);
			myLog(TAG, dbMsg);
			abdToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
			abdToggle.setDrawerIndicatorEnabled(true);
			drawer.setDrawerListener(abdToggle);    //Attempt to invoke virtual method 'void android.support.v4.widget.DrawerLayout.setDrawerListener(android.support.v4.widget.DrawerLayout$DrawerListener)' on a null object reference
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);            //左矢印←アイコンになる
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			myLog(TAG, dbMsg);
			navigationView = ( NavigationView ) findViewById(R.id.nav_view);
			//		navigationView.setNavigationItemSelectedListener(MainActivity.this);    //( NavigationView.OnNavigationItemSelectedListener )
			navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
				@Override
				public boolean onNavigationItemSelected(MenuItem menuItem) {
					final String TAG = "onNavigationItemSelected[MyActivity.initDrawer]";
					String dbMsg = "MenuItem" + menuItem.toString();/////////////////////////////////////////////////
					boolean retBool = false;
					try {
						retBool = funcSelected(menuItem);
						MainActivity.this.drawer.closeDrawers();
					} catch (Exception e) {
						myLog(TAG, dbMsg + "で" + e.toString());
						return false;
					}
					return retBool;
				}
			});
		} catch (Exception e) {
			myLog(TAG, dbMsg + "で" + e.toString());
		}
	}                                                                    //NaviViewの初期設定

	@SuppressWarnings ( "StatementWithEmptyBody" )
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();
		funcSelected(item);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	///メイン画面へメニュー追加///////////////////////////////////////////////////////////////////////////
	public static final int MENU_main = 0;                    //メイン画面	     <item android:id="@+id/mm_main"	android:orderInCategory="101"	android:title="@string/main_screen"/>
	public static final int MENU_conectedt = MENU_main + 1;    //現在の接続先       <item  android:id="@+id/mm_conected" android:orderInCategory="102"  android:title="@string/current_connection"/>
	public static final int MENU_PLC = MENU_conectedt + 1;    //現在地確認       <item android:id="@+id/mm_present_location_confirmation"  android:orderInCategory="103" android:title="@string/present_location_confirmation"　android:icon="@android:drawable/ic_dialog_map"-->
	public static final int MENU_share = MENU_PLC + 1;        //登録したログの確認   <item android:id="@+id/mm_share" android:orderInCategory="104" android:title="@string/Indication_of_the_registered_log"/>
	public static final int MENU_TC = MENU_share + 1;            //廃止；送信先変更    <item  android:id="@+id/mm_transmission_change" android:orderInCategory="107"  android:title="@string/transmission_change"/>
	public static final int MENU_disconect = MENU_TC + 1;        //回線切断              <item android:id="@+id/mm_" android:orderInCategory="108" android:title="@string/info_a_setudann"/>
	public static final int MENU_prefarence = MENU_disconect + 1;        //設定画面   <item android:id="@+id/mm_prefarence" android:title="@string/action_settings"  android:orderInCategory="189"/>
	public static final int MENU_quit = MENU_prefarence + 1;            //    <item android:id="@+id/mm_quit" android:orderInCategory="199" android:title="@string/menu_item_sonota_end"/>
	public static int mMenuType = MENU_main;                    //メニューレイアウト管理用変数

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu item) {
		final String TAG = "onPrepareOptionsMenu[MA}";
		String dbMsg = "開始" + item;                    //表記が返る
		try {
			dbMsg = dbMsg + " , mMenuType= " + mMenuType;
			switch ( mMenuType ) {
				case MENU_main:        //メイン画面	     <item android:id="@+id/mm_main"	android:orderInCategory="101"	android:title="@string/main_screen"/>
					//			itemListFragment.listPrepareOptionsMenu(item);  など
					break;
				case MENU_conectedt:    //現在地確認       <item android:id="@+id/mm_present_location_confirmation"  android:orderInCategory="103" android:title="@string/present_location_confirmation"　android:icon="@android:drawable/ic_dialog_map"-->
					break;
				case MENU_PLC:        //登録したログの確認   <item android:id="@+id/mm_share" android:orderInCategory="104" android:title="@string/Indication_of_the_registered_log"/>
					break;
				case MENU_prefarence://設定画面   <item android:id="@+id/mm_prefarence" android:title="@string/action_settings"  android:orderInCategory="189"/>
					break;
				default:
					break;
			}
			//		myLog(TAG, dbMsg);
		} catch (Exception er) {
			myLog(TAG, dbMsg + "で" + er.toString());
		}
		return true;        //	return super.onOptionsItemSelected ((MenuItem) item);でクラッシュ
	}                                            //状況に合わせたメニューアイテムの表示/非表示処理	再開時;⑨

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final String TAG = "onOptionsItemSelected[MA}";
		String dbMsg = "開始" + item;                    //表記が返る
		try {
			myLog(TAG, dbMsg);
			funcSelected(item);
		} catch (Exception er) {
			myLog(TAG, dbMsg + "で" + er.toString());
		}
		//本当は　return abdToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);			//アイコン回転
		return super.onOptionsItemSelected(item);
	}

	/**
	 * MainActivityのメニュー
	 * ドロワーと共通になるので関数化
	 */
//	Bundle bundle;
	public boolean funcSelected(MenuItem item) {
		final String TAG = "funcSelected[MA}";
		String dbMsg = "MenuItem" + item.toString();/////////////////////////////////////////////////
		try {
			Bundle bundle = new Bundle();
			int id = item.getItemId();
			dbMsg = "id=" + id;
			switch ( id ) {
				case R.id.mm_main:                        //メイン画面	     <item android:id="@+id/mm_main"	android:orderInCategory="101"	android:title="@string/main_screen"/>
				case R.id.nav_main:
					mMenuType = MENU_main;
					callMain();
					break;
				case R.id.mm_conected:    //現在の接続先       <item  android:id="@+id/" android:orderInCategory="102"  android:title="@string/current_connection"/>
				case R.id.nav_conected:
					mMenuType = MENU_conectedt;
					getNowConect();
					break;
				case R.id.mm_present_location_confirmation:    //現在地確認       <item android:id="@+id/"  android:orderInCategory="103" android:title="@string/present_location_confirmation"　android:icon="@android:drawable/ic_dialog_map"-->
				case R.id.nav_present_location_confirmation:
					mMenuType = MENU_PLC;
						presentLocation();
//					//			bundle.putInt ("dWidth", dWidth);												//ディスプレイ幅
//					//			bundle.putInt ("dHeight", dHeight);
//					//ヘルプwebの場合
//					bundle.putString("dataURI", "http://www.geocities.jp/hqu666/");
//					//			bundle.putString ("baseUrl", baseUrl);
//					//			bundle.putString ("fType", fType);
//					//			bundle.putString ("fName", fName);
//					repFragmentLoad(web_fragment, bundle);            //Flagmentの入れ替え
					break;
				case R.id.mm_share:        //登録したログの確認  android:orderInCategory="104" android:title="@string/Indication_of_the_registered_log"/>
				case R.id.nav_share:
					mMenuType = MENU_share;
					callOtherAplication("com.google.android.apps.plus", "com.google.android.apps.plus.phone.HomeActivity");
					//他のアプリを起動
			//		makeList();
//					Intent intent = new Intent(getApplicationContext(), RingStop.class);
//					Bundle bandle = new Bundle();
//					bandle.putBoolean("soundOn", false);
//					intent.putExtras(bandle);
//					startActivity(intent);
//					auto_start_fragment.notifOn = false;
//					quitMe();
					break;
				case R.id.mm_send:              //今すぐ保存する
				case R.id.nav_send:
					sendDatas();
					break;
//				case R.id.mm_transmission_change:            //廃止；送信先変更
//				case R.id.nav_transmission_change:
//					mMenuType = MENU_TC;
//					transmissionChange();
//					break;
				case R.id.mm_disconect:        //回線切断              <item android:id="@+id/" android:orderInCategory="108" android:title="@string/info_a_setudann"/>
				case R.id.nav_disconect:
					mMenuType = MENU_disconect;
					actionDisconnect();
					break;
				case R.id.mm_prefarence:                    //設定画面   <item android:id="@+id/" android:title="@string/action_settings"  android:orderInCategory="189"/>
				case R.id.nav_prefarence:
					mMenuType = MENU_prefarence;
					callSetting();
					break;
				case R.id.mm_quit:            //    <item android:id="@+id/" android:orderInCategory="199" android:title="@string/menu_item_sonota_end"/>
				case R.id.nav_quit:
					callQuit();//このActivtyの終了
					break;
				default:
					break;
			}
			myLog(TAG, dbMsg);
		} catch (Exception er) {
			myLog(TAG, dbMsg + "で" + er.toString());
		}
		return false;
	}                                        //メニューとDrowerからの画面/機能選択

	public void callQuit() {
		if ( googleApiClient != null ) {
			googleApiClient.disconnect();                  // 接続解除
		}
		if ( locationManager != null ) {
			locationManager.removeUpdates(this);                //listener削除
		}
		if ( isMyService(RecordService.class.getName()) ) {                                            //Activtyを閉じる時にサービス実行中なら
			CharSequence contentTitle = getResources().getString(R.string.app_name2);
			CharSequence contentText = "記録中";
			CharSequence tickerStr = "タップしてアプリケーションを開く";
			if ( RS == null ) {
				RS = new RecordService();
			}
			RS.showMyNotification(MainActivity.this, contentTitle, contentText, tickerStr);                              //サービス実行中のノティフィケーション作成
		}
		this.finish();
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
					if ( fragmentNo == mainFragmentNo ) {
						callQuit();
					} else {
						callMain();
					}
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

	///Fragment Call///////////////////////////////////////////////////////////////////////////////////
	public void callMain() {
		final String TAG = "callMain";
		String dbMsg = "";
		Bundle bundle = new Bundle();
		Location location = getGPSDatas();                   //メイン画面のリフレッシュ開始
		if ( location != null ) {
			latitudeVal = location.getLatitude();
			dbMsg = "latitudeVal=" + latitudeVal;// 緯度の表示
			longitudeVal = location.getLongitude();       // 経度の表示
			dbMsg += ",longitudeVal=" + longitudeVal;
			altitudeVal = location.getAltitude();                //標高
			dbMsg += ",altitudeVal=" + altitudeVal;
			accuracyVal = location.getAccuracy();                //精度
			dbMsg += ",accuracyVal=" + accuracyVal;
			pinpointingTimeVal = location.getTime();                //測位時刻
			dbMsg += ",pinpointingTimeVal=" + pinpointingTimeVal;
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  //yyyy-MM-dd HH:mm:ss.SSS
			pinpointingTimeStr = df.format(pinpointingTimeVal);
			pinpointingTimeStr = pinpointingTimeStr;
			dbMsg += "=" + pinpointingTimeStr;
		}
		bundle.putFloat("latitudeVal", ( float ) latitudeVal);  // 緯度の表示
		bundle.putFloat("longitudeVal", ( float ) longitudeVal); // 経度の表示
		bundle.putFloat("altitudeVal", ( float ) altitudeVal);  //標高
		bundle.putFloat("accuracyVal", ( float ) accuracyVal);  //精度
		bundle.putString("pinpointingTimeStr", pinpointingTimeStr);                //測位時刻

//		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();		// Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
		if ( mainFragment == null ) {
//			dbMsg +=",add";
			mainFragment = new MainFragment();                                        //メイン画面
		}
		repFragmentLoad(mainFragment, bundle);
		dbMsg += ",fragmentNo=" + fragmentNo;
		fragmentNo = mainFragmentNo;
		dbMsg += ">>" + fragmentNo;
		myLog(TAG, dbMsg);
	}

	/**
	 * GPSで表示された座標が正しいか確認し、修正する機能です
	 * https://developers.google.com/maps/documentation/android-api/intents?hl=ja
	 * https://akira-watson.com/android/google-map-2.html
	 * */
	public void presentLocation() {
		final String TAG = "presentLocation[MA}";
		String dbMsg = "開始";/////////////////////////////////////////////////
//		if ( webFragment == null ) {
//			webFragment = new MyWebFragment();            //web画面
//		}
	//	Bundle bundle = new Bundle();
		Location location = getGPSDatas();                   //メイン画面のリフレッシュ開始
		if ( location != null ) {
			latitudeVal = location.getLatitude();
			dbMsg = "latitudeVal=" + latitudeVal;// 緯度の表示
			longitudeVal = location.getLongitude();       // 経度の表示
			dbMsg += ",longitudeVal=" + longitudeVal;
			altitudeVal = location.getAltitude();                //標高
			dbMsg += ",altitudeVal=" + altitudeVal;
			accuracyVal = location.getAccuracy();                //精度
			dbMsg += ",accuracyVal=" + accuracyVal;
			pinpointingTimeVal = location.getTime();                //測位時刻
			dbMsg += ",pinpointingTimeVal=" + pinpointingTimeVal;
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  //yyyy-MM-dd HH:mm:ss.SSS
			pinpointingTimeStr = df.format(pinpointingTimeVal);
			dbMsg += "=" + pinpointingTimeStr;
		}
//		bundle.putFloat("latitudeVal", ( float ) latitudeVal);			// 緯度の表示
//		bundle.putFloat("longitudeVal", ( float ) longitudeVal);		// 経度の表示
//		bundle.putFloat("altitudeVal", ( float ) altitudeVal);			//標高
//		bundle.putFloat("accuracyVal", ( float ) accuracyVal);			//精度
//		bundle.putString("pinpointingTimeStr", pinpointingTimeStr);		//測位時刻
//		String urlStr = "https://www.google.co.jp/maps";
//		if ( -1 < latitudeVal ) {
//			urlStr += "/@" + latitudeVal + "," + longitudeVal + "z";
//		}
//		bundle.putString("urlStr", urlStr);
//		repFragmentLoad(webFragment, bundle);
		 String urlStr="geo:"+latitudeVal+","+longitudeVal +"?z="+18;
		dbMsg += ",urlStr=" + urlStr;
		Uri gmmIntentUri = Uri.parse(urlStr);
		//Create a Uri from an intent string. Use the result to create an Intent.
		//      +"?z="+ Uri.encode("15")
	//Create a Uri from an intent string. Use the result to create an Intent.
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);				// Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
		mapIntent.setPackage("com.google.android.apps.maps");							// Make the Intent explicit by setting the Google Maps package
		if (mapIntent.resolveActivity(getPackageManager()) != null) {
			startActivity(mapIntent);														// Attempt to start an activity that can handle the Intent
		} else{
			String titolStr="MAPが見つかりません";
			String mggStr="Googlマップをインストールして下さい。";
			messageShow( titolStr,  mggStr);
		}
		fragmentNo = webFragmentNo;
		myLog(TAG, dbMsg);
	}                       	///GPSで表示された座標が正しいか確認し、修正する機能です

	public void openPref() {
		if ( myPreferenceFragment == null ) {
			myPreferenceFragment = new MyPreferenceFragment();            //設定画面
		}
		Bundle bundle = new Bundle();
//		this.student_id="123456789";							//"実施者ID（学籍番号）
		this.accountName = "hkuwayama@coresoft.net.co.jp";        //保存先サーバのアカウント"
//		this. stock_count="500";								//自動送信するデータ数" />
		bundle.putString("student_id", student_id);
		bundle.putString("accountName", accountName);
		bundle.putString("stock_count", stock_count);
		repFragmentLoad(myPreferenceFragment, bundle);
		fragmentNo = myPreferenceFragmentNo;
//		mainFragment.student_id_tv.setText(student_id);
//		mainFragment.accountName_tv.setText(accountName);

	}

	private Fragment calentFragment;
	private FragmentManager fm;

	/**
	 * 呼出し元	reaDB() , funcSelected() , quitMe()
	 */
	public void repFragmentLoad(Fragment fragment, Bundle bundle) {
		final String TAG = "repFragmentLoad[MA}";
		String dbMsg = "開始";
		try {
			dbMsg = "fragment=" + fragment;
			if ( fragment != null && fragment.isAdded() == false ) {
				calentFragment = fragment;
				if ( bundle != null ) {
					dbMsg = dbMsg + ",bundle=" + bundle;
					fragment.setArguments(bundle);                                                                //bundleのデータをfragmentに渡して
				}
				fm = getFragmentManager();                // Fragmentを作成します
				FragmentTransaction ft = fm.beginTransaction();                                // Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
				ft.replace(R.id.main_content_frame, fragment);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				dbMsg = dbMsg + ",ft=" + ft;
				ft.commit();
			}
			//	myLog(TAG,dbMsg);
		} catch (Exception e) {
			myLog(TAG, dbMsg + "で" + e.toString());
		}
	}                            ///Flagmentの入れ替え

	//RuntimeParmission///////////////////////////////////////////////////////////////////////////////////
	public static int rp_inet = 0x11;        //アカウント変更
	public static int rp_sendDatas = 0x12;        //データ送信
	public static int rp_strage = 0x21;      //データ保存
	public static int rp_setting = 0x22;      //設定保存
	public static int rp_readPrif = 0x23;      //設定読み出し
	public static int rp_makeList = 0x24;      //保存したデータの読み込み
	public static int rp_saveLocalFile = 0x25; //端末内にファイル保存
	public static int rp_apListUp = 0x31;      //圏内のwifiAPの検出
	public static int rp_getNowConect = 0x32;        //接続先APの検出
	public static int rp_getGPSInfo = 0x41;          //GPS

	public boolean checkWiFiParmission(int reTry) {
		boolean retBool = false;
		int accessWifiState = -1;
		int changeWifiState = -1;
		accessWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE);
		changeWifiState = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CHANGE_WIFI_STATE);
		if ( accessWifiState != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CHANGE_WIFI_STATE ,}, reTry);
		} else if ( changeWifiState != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE ,}, reTry);
		} else {
			retBool = true;
		}
		return retBool;
	}

	public boolean checkSTORAGEParmission(int reTry) {
		boolean retBool = false;
		int readExternalStorage = -1;
		readExternalStorage = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
		int writeExternalStorage = -1;
		writeExternalStorage = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if ( readExternalStorage != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE ,}, reTry);
		} else if ( writeExternalStorage != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE ,}, reTry);
		} else {
			retBool = true;
		}
		return retBool;
	}

	//Google Drive,Accountへのアクセス
	public boolean checkAccountParmission(int reTry) {
		boolean retBool = false;
		int accessInternet = -1;
		accessInternet = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.INTERNET);
		int getAccount = -1;
		getAccount = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.GET_ACCOUNTS);
//        int useCredentials = -1;
//        useCredentials = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.USE_CREDENTIALS);

		if ( accessInternet != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET ,}, reTry);
		} else if ( getAccount != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS ,}, reTry);
		} else {
			retBool = true;
		}
		return retBool;
	}

	public boolean checkGPSParmission(int reTry) {
		final String TAG = "checkGPSParmission";
		String dbMsg = "開始";
		boolean retBool = false;
		int accessFineLocation = -1;
		accessFineLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
		dbMsg += ",accessFineLocation=" + accessFineLocation;
		int accessCoarseLocation = -1;
		accessCoarseLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);  //位置情報 ネットワークから
		dbMsg += ",accessCoarseLocation=" + accessCoarseLocation;
////		int accessCoarseUpdate = -1;
////		accessCoarseUpdate = ContextCompat.checkSelfPermission(this.getApplicationContext(),Manifest.permission.ACCESS_COARSE_UPDATES);
////		dbMsg += ",accessCoarseUpdate=" + accessCoarseUpdate;
		if ( accessFineLocation != PackageManager.PERMISSION_GRANTED ) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION ,}, reTry);
		} else if ( accessCoarseLocation != PackageManager.PERMISSION_GRANTED ) {    //
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION ,}, reTry);
////		} else if (accessCoarseUpdate != PackageManager.PERMISSION_GRANTED) {    //
////			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_UPDATES,}, reTry);
		} else {
			retBool = true;
		}
		this.myLog(TAG, dbMsg);
		return retBool;
	}


	public void getWiFiDatas() {
		if ( checkWiFiParmission(rp_apListUp) ) {
			apListUp();                //接続先のAP情報取得
		}
	}

	public void getNowConect() {
		if ( checkWiFiParmission(rp_getNowConect) ) {
			getNowConectInfo();                //接続先のAP情報取得
		}
	}

	public void makeList() {
		if ( checkSTORAGEParmission(rp_makeList) ) {
			String titolStr = "現在作成中";
			String mggStr = "ログのリストアップは現在考案中です";
			messageShow(titolStr, mggStr);
		}
	}

//    public void save2Local() {            // = 0x25; //端末内にファイル保存
//        if (checkSTORAGEParmission(rp_saveLocalFile)) {
//            saveLocalFile();
//        }
//    }

	///0104////////http://java-lang-programming.com/ja/articles/63
	public void callSetting() {
		if ( checkSTORAGEParmission(rp_setting) ) {
			openPref();
		}
	}

	public void sendDatasReady() {
		String titolStr = "今すぐ送信";
		String msgStr = "端末内に溜まったデータを手動送信します。";
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

	/***
	 * GoogleDriveへデータ転送
	 * */
	public void sendDatas() {
		if ( checkAccountParmission(rp_sendDatas) ) {
			sendAPData();
		}
	}

	//送信先変更
	public void transmissionChange() {
		if ( checkAccountParmission(rp_inet) ) {
			selectGoogleAccount();//onActivityResultのREQUEST_ACCOUNT_PICKER_ONLYで完結
		}
//		String titolStr = "接続先変更";
//		String mggStr = "現在の接続先:" + accountName + "を一旦消去します。次に送信操作を行う時にアカウント設定ダイアログが表示されます。";
//		new AlertDialog.Builder(this).setTitle(titolStr).setMessage(mggStr).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				MainActivity.this.accountName = "";
//				myEditor.putString("accountName_key", "");
//				boolean kakikomi = myEditor.commit();
//				//	dbMsg = dbMsg + ",書込み=" + kakikomi;//////////////////
//			}
//		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//			}
//		}).create().show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {                 //許可ダイアログの承認結果を受け取る（許可・不許可）
		if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
			//   switchが使えない？
			if ( requestCode == rp_inet ) {
				transmissionChange();
			} else if ( requestCode == rp_sendDatas ) {
				sendDatas();                                     //GoogleDriveへデータ転送
			} else if ( requestCode == rp_strage ) {
				makeList();                                        //保存済みファイルの読み込み
			} else if ( requestCode == rp_setting ) {
				callSetting();                                    //設定変更画面
			} else if ( requestCode == rp_readPrif ) {
				readPrif();                                        //設定読み出し
			} else if ( requestCode == rp_saveLocalFile ) {
				//          save2Local();                                       //端末内にファイル保存
			} else if ( requestCode == rp_makeList ) {
				makeList();                                        //保存したデータの読み込み
			} else if ( requestCode == rp_apListUp ) {
				getWiFiDatas();                                    //WiFiリストアップ
			} else if ( requestCode == rp_getNowConect ) {
				getNowConect();                                    //接続先APの検出
			} else if ( requestCode == rp_getGPSInfo ) {
				getGPSDatas();                                    //GPS利用
			}
		} else {
			Log.i("permission", "not permitted");
		}
	}

	//ap情報取得///////////////////////////////////////////////////////////////////////////////////
	public WifiManager wfManager;
	public String[] aps;
	public List< String > infoList;
	public List< ScanResult > apList;        // (imamnoWiFi実行後)スキャン結果を取得

	///参照　①　http://seesaawiki.jp/w/moonlight_aska/d/WiFi%C0%DC%C2%B3%BE%F0%CA%F3%A4%F2%BC%E8%C6%C0%A4%B9%A4%EB
	///Wifiの電波強度によって                   https://kokufu.blogspot.jp/2016/11/android-wi-fi-scanresult.html
	///Wi-Fi Aware(APIL26)                                   https://developer.android.com/guide/topics/connectivity/wifi-aware.html?hl=ja
	public List< ScanResult > apListUp() {                //圏内にあるSSIDをリストアップ		http://seesaawiki.jp/w/moonlight_aska/d/WiFi%A4%CEAP%A4%F2%A5%B9%A5%AD%A5%E3%A5%F3%A4%B9%A4%EB
		final String TAG = "apListUp";
		String dbMsg = "開始";
		try {
			wfManager = ( WifiManager ) MainActivity.this.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);        //(android.content.Context.WIFI_SERVICE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {                                                                         //APIL23以上
//                @SuppressLint("WifiManagerLeak") WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
//                this.wfManager = manager;
//            }
			if ( wfManager != null ) {
				dbMsg = ",isWifiEnabled=" + wfManager.isWifiEnabled();
				if ( wfManager.isWifiEnabled() == true ) {// if (wfManager.getWifiState() == wfManager.WIFI_STATE_ENABLED) {
					apList = null;//new List<ScanResult>() ;
					wfManager.startScan();                         // APをスキャン
					apList = wfManager.getScanResults();        // スキャン結果を取得
					dbMsg = "," + apList.size() + "件";
				}
			}
			Log.i(TAG, dbMsg);
		} catch (NullPointerException e) {
			Log.e(TAG, dbMsg + "で" + e.toString());// 適切な例外処理をしてください。
		}
		return apList;
	}

	public void showAPPropaty(AdapterView< ? > parent, View view, int position, long id) {
		final String TAG = "showAPPropaty";
		ListView listView = ( ListView ) parent;
		String dbMsg = position + ")";
		Log.i(TAG, dbMsg);
		String item = ( String ) listView.getSelectedItem();                            //選択されたアイテム;BSSID飲みにする可能性あり

		//String[] items = aps[position].split(",");
		String items = apList.get(position).toString();
		String bssid = apList.get(position).BSSID;// items[0];                            //"BSSID;
		// " + apList.get(position).BSSID;
		dbMsg = dbMsg + bssid;

		//                 String items = aps[position];
//                            String items = apList.get(position).toString().replace(",", "\n");
		dbMsg = dbMsg + ",items=" + items;
		Log.i(TAG, dbMsg);
		String Listitems = items.replace(",", "\n");

		new AlertDialog.Builder(MainActivity.this).setTitle(bssid + "の詳細").setMessage(Listitems).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create().show();
		myLog(TAG, dbMsg);
	}

//      public String makeSendList() {
//        final String TAG = "makeSendList";
//        String dbMsg = "開始";
//        String retStr = "";
//        try {
//            long currentTimeMillis = System.currentTimeMillis();
//            dbMsg = ",currentTimeMillis(現在時刻)=" + currentTimeMillis + "=" + df.format(currentTimeMillis); //1514721782830=2017/12/31 21:03:02,
//
//            long _elapsedRealtime = elapsedRealtime();
//            dbMsg += ",elapsedRealtime=" + _elapsedRealtime + "=" + df.format(_elapsedRealtime);   //800437756=1970/01/10 15:20:37システム起動時からの経過時間。システムがdeep sleepになってた間の分もカウントに含まれる。
//            long currentSysUpTime = currentTimeMillis - _elapsedRealtime;
//            dbMsg += ">システム起動時>" + currentSysUpTime;
//            dbMsg += "=" + df.format(currentSysUpTime);
//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {                                                                         //APIL23以上
//                @SuppressLint("WifiManagerLeak") WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
//                this.wfManager = manager;
//            }else{
//                 wfManager = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);        //(android.content.Context.WIFI_SERVICE);
//                 //  wfManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);        //(android.content.Context.WIFI_SERVICE);
//            }
//            if (wfManager != null) {
//                if (wfManager.isWifiEnabled() == true) {// if (wfManager.getWifiState() == wfManager.WIFI_STATE_ENABLED) {
//                    Date wrDt = new Date(System.currentTimeMillis());
//                    WifiInfo info = wfManager.getConnectionInfo();
//                    String conectedBSSID = info.getBSSID();        //String BSSID;APIL1;設定されている場合、このネットワーク構成エントリは、指定されたBSSIDを持つAPに関連付けるときにのみ使用する必要があります。値は、イーサネットMACアドレ
//                    String conectedSsID = info.getSSID();
//                    apList = null;//new List<ScanResult>() ;
//                    wfManager.startScan();                         // APをスキャン
//                    apList = wfManager.getScanResults();        // スキャン結果を取得
//                    int tSize = apList.size();
//                    mainFragment.list_up_count_tv.setText(tSize + "件");
//                    ;  //リストアップ件数
//                    dbMsg += "," + tSize + "件";
//                    retStr = "Student Id" + ",Record Time" + ",SSID" + ",BSSID" + ",frequency[MHz]" + ",level[dBm]" + ",capabilities" +                    //APIL1
//                            ",timestamp" + ",intcenterFreq0" + ",centerFreq1" + ",channelWidth" + ",operatorFriendlyName" + ",venueName" +        //APIL17,23
//                            ",latitude" + ",longitude" + ",altitude" + ",accuracy" + ",pinpointing Time" + ",isConected" + "\n";                    //GPS
//                    for (int i = 0; i < apList.size(); i++) {
//                        dbMsg += "\n(" + i + "/" + apList.size() + ")";
//                        String OneRecord = student_id + "," + df.format(wrDt);
////ScanResultのフィールド	https://developer.android.com/reference/android/net/wifi/ScanResult.html////
//                        String sSID = apList.get(i).SSID;
//                        OneRecord += "," + sSID;                                    //String SSID;APIL1;ネットワーク名
//                        dbMsg += ",SSID=" + sSID + " ; " + conectedSsID;
//                        String bSSID = apList.get(i).BSSID;                                    //String BSSID;APIL1;アクセスポイントのアドレス。
//                        OneRecord += "," + bSSID;                                    //String BSSID;APIL1;アクセスポイントのアドレス。
//                        dbMsg += ",BSSID=" + bSSID + " ; " + conectedBSSID;
//
//                        OneRecord += "," + apList.get(i).frequency;            //int ;APIL1;クライアントがアクセスポイントと通信しているチャネルの主要な20 MHz周波数（MHz単位）。
//                        OneRecord += "," + apList.get(i).level;    //int ;//APIL1;検出された信号レベル（dBm）。RSSIとも呼ばれます。calculateSignalLevel(int, int)この数値をユーザーに表示できる絶対信号レベルに変換するために使用します。
//                        OneRecord += "," + apList.get(i).capabilities;            //String ;APIL1;アクセスポイントでサポートされている認証、キー管理、および暗号化方式について説明します。
//                        long timeStamp = apList.get(i).timestamp / 1000;
//                        dbMsg += ",timeStamp=" + timeStamp + "=" + df.format(timeStamp);
//                        timeStamp = currentSysUpTime + timeStamp;
//                        OneRecord += "," + df.format(timeStamp);            //long ;APIL17;この結果が最後に確認されたときのタイムスタンプ（ブート以降）。
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                            OneRecord += "," + apList.get(i).centerFreq0;            //int ;APIL23:APの帯域幅が20 MHzの場合は使用されませんAPが40,80または160 MHzを使用する場合、APが80 + 80 MHzを使用する場合の中心周波数（MHz）です。これは最初のセグメントの中心周波数）
//                            OneRecord += "," + apList.get(i).centerFreq1;            //int ;APIL23;APの帯域幅が80 + 80 MHzの場合にのみ使用されます.APが80 + 80 MHzを使用する場合、これは2番目のセグメントの中心周波数（MHz単位）です。
//                            OneRecord += "," + apList.get(i).channelWidth;            //int ;//APIL23;APチャネル帯域幅。
////														//0;CHANNEL_WIDTH_20MHZ、1;CHANNEL_WIDTH_40MHZ、 2;CHANNEL_WIDTH_80MHZ、3;CHANNEL_WIDTH_160MHZ,4;CHANNEL_WIDTH_80MHZ_PLUS_MHZ。
//                            OneRecord += "," + apList.get(i).operatorFriendlyName;            //	CharSequence ;	//APIL23;アクセスポイントが発行したパスポイントオペレータ名を示します。
//                            OneRecord += "," + apList.get(i).venueName;            //	CharSequence ;APIL23;アクセスポイントから発行された会場名を示します。Passpointネットワークでのみ使用可能で、アクセスポイントで公開されている場合にのみ使用できます。
//                        }
//                        OneRecord += addGPSFeld();
//                        if (bSSID.equals(conectedBSSID)) {
//                            OneRecord += "," + "true".toString() + "\n";
//                        } else {
//                            OneRecord += "," + "false".toString() + "\n";
//                        }
//                        dbMsg += ",OneRecord=" + OneRecord;
//                        retStr += OneRecord;
//                    }            //for(int i=0; i<apList.size(); i++) {
//                }
//            }
////			dbMsg += ",retStr="+ retStr;
//            Log.i(TAG, dbMsg);
//        } catch (NullPointerException e) {
//            Log.e(TAG, dbMsg + "で" + e.toString());// 適切な例外処理をしてください。
//        }
//        return retStr;
//    }

//    public String addGPSFeld() {
//        final String TAG = "addGPSFeld";
//        String dbMsg = null;
//        String retStr = "";
//        if (-1 < latitudeVal) {         // 緯度の表示
//            retStr += "," + latitudeVal;
//            //	infoList.add(latitudeStr);// 緯度
//        } else {
//            retStr += ",";
//        }
//        if (-1 < longitudeVal) {         //   = -1;       // 経度の表示
//            retStr += "," + longitudeVal;
////			infoList.add(LongtudeStr);// 経度
//        } else {
//            retStr += ",";
//        }
//        if (-1 < altitudeVal) {         // = -1;                //標高
//            retStr += "," + altitudeVal;
//            //		infoList.add(altitudeStr);// 標高
//        } else {
//            retStr += ",";
//        }
//        if (-1 < accuracyVal) {         //精度
//            retStr += "," + accuracyVal;
//            //		infoList.add(accuracyStr);// 精度
//        } else {
//            retStr += ",";
//        }
//        if (0 < pinpointingTimeVal) {         // = 0;                //測位時刻
//            final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//            retStr += "," + df.format(pinpointingTimeVal);
//            //		infoList.add(pinpointingTimeStr);// 測位時刻
//        } else {
//            retStr += ",";
//        }
//
//        return retStr;
//    }

	//接続先APの検出
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

	// GPS/////////////////////////////////////////////////////////////////////////////////////////
	public Location getGPSDatas() {
		final String TAG = "getGPSDatas";
		String dbMsg = "開始";
		Location retLocation = null;
		try {
			if ( checkGPSParmission(rp_getGPSInfo) ) {
				//	retLocation = getGPSDatasBody();
				int accessFineLocation = -1;
				accessFineLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
				dbMsg += ",accessFineLocation=" + accessFineLocation;
				int accessCoarseLocation = -1;
				accessCoarseLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);  //位置情報 ネットワークから
				dbMsg += ",accessCoarseLocation=" + accessCoarseLocation;
				if ( accessFineLocation != PackageManager.PERMISSION_GRANTED && accessCoarseLocation != PackageManager.PERMISSION_GRANTED ) {                //   requestLocationUpdatesの前に必要
					return retLocation;
				}
				locationManager = ( LocationManager ) getSystemService(LOCATION_SERVICE);           // LocationManager インスタンス生成
				if ( locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
					dbMsg = "location manager Enabled";

				} else {
					Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);                    // GPSを設定するように促す
					startActivity(settingsIntent);
					dbMsg = "not gpsEnable, startActivity";
				}

				int gpsSarchSpan = Integer.parseInt(gps_sarch_span) * 1000;
				dbMsg += ",通知間隔=" + gpsSarchSpan;
				int gpsMiniDestance = Integer.parseInt(gps_mini_destance);
				dbMsg += ",GPS通知最小距離[m]=" + gpsMiniDestance;
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,      //                LocationManager.NETWORK_PROVIDER,
						gpsSarchSpan, // 通知のための最小時間間隔（ミリ秒）
						gpsMiniDestance, // 通知のための最小距離間隔（メートル）
						this);
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, rp_getGPSInfo, 50, this);
				List< String > AllProviders = locationManager.getAllProviders();
				dbMsg += ",AllProviders=" + AllProviders.toString();
				Criteria criteria = new Criteria();                                                                    //プロバイダを選ぶ上での基準を設定する
				criteria.setAccuracy(Criteria.ACCURACY_FINE);                                                    //精度が高い（WiFi等の情報を含むasisted）に設定
				String bestProvider = locationManager.getBestProvider(criteria, true);            //最適なプロバイダを取得する;gpsなど
				dbMsg += ",bestProvider=" + bestProvider;
				retLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);              //最後に取得できたLocationを取得	LocationManager.GPS_PROVIDER
				if ( retLocation == null ) {
					dbMsg += ",retLocation=null";
				}
			}
			this.myLog(TAG, dbMsg);
			writeLocationInfo(retLocation);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + "；" + er);
		}
		return retLocation;
	}

	public void writeLocationInfo(Location location) {
		final String TAG = "writeLocationInfo[MA}";
		String dbMsg = "開始";/////////////////////////////////////////////////
		if ( location != null ) {
			latitudeVal = location.getLatitude();
			dbMsg = "latitudeVal=" + latitudeVal;
			latitudeStr = "Latitude:" + latitudeVal;// 緯度の表示

			longitudeVal = location.getLongitude();       // 経度の表示
			dbMsg += ",longitudeVal=" + longitudeVal;
			LongtudeStr = "Longtude:" + longitudeVal;

			altitudeVal = location.getAltitude();                //標高
			dbMsg += ",altitudeVal=" + altitudeVal;
			altitudeStr = "Altitude:" + altitudeVal;

			accuracyVal = location.getAccuracy();                //精度
			dbMsg += ",accuracyVal=" + accuracyVal;
			accuracyStr = "Accuracy:" + accuracyVal;

			pinpointingTimeVal = location.getTime();                //測位時刻
			dbMsg += ",pinpointingTimeVal=" + pinpointingTimeVal;
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  //yyyy-MM-dd HH:mm:ss.SSS
			pinpointingTimeStr = df.format(pinpointingTimeVal);
			pinpointingTimeStr = pinpointingTimeStr;
			dbMsg += "=" + pinpointingTimeStr;
			if ( fragmentNo == mainFragmentNo ) {
				mainFragment.latitude_tv.setText(latitudeVal + "");
				mainFragment.longitude_tv.setText("" + longitudeVal);
				mainFragment.altitude_tv.setText("" + altitudeVal + "m");
				mainFragment.accuracy_tv.setText("" + accuracyVal);
				mainFragment.timestanp_tv.setText("" + pinpointingTimeStr);
				dbMsg += ">>表示更新";
			}
		}
		myLog(TAG, dbMsg);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		final String TAG = "onStatusChanged";
		String dbMsg = "provider=" + provider;
		dbMsg += ",status=" + status;
		switch ( status ) {
			case LocationProvider.AVAILABLE:
				dbMsg += "、LocationProvider.AVAILABLE";
				break;
			case LocationProvider.OUT_OF_SERVICE:
				dbMsg += "、LocationProvider.AVAILABLE";
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				dbMsg += "、LocationProvider.TEMPORARILY_UNAVAILABLE";
				break;
		}
		Log.d(TAG, dbMsg);
	}

	@Override
	public void onLocationChanged(Location location) {
		final String TAG = "onLocationChanged";
		String dbMsg = "開始";
		writeLocationInfo(location);
		locationManager.removeUpdates(this);                //listener削除
		Log.d(TAG, dbMsg);
	}

	@Override
	public void onProviderEnabled(String provider) {
		final String TAG = "onProviderEnabled";
		String dbMsg = "provider=" + provider;
		Log.d(TAG, dbMsg);
	}

	@Override
	public void onProviderDisabled(String provider) {
		final String TAG = "onProviderDisabled";
		String dbMsg = "provider=" + provider;
		Log.d(TAG, dbMsg);
	}

	//GoogleDriveにデータ送信////////////////////////////////////////////////////////////GPS

	/**
	 * http://vividcode.hatenablog.com/entry/20130908/1378613811
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

	//
	protected void sendAPData() {
		final String TAG = "sendAPData";
		String dbMsg = "";//"service=" + service.about();
		date = new Date(System.currentTimeMillis());
		mainFragment.accountName_tv.setText(accountName);
		mainFragment.send_time_tv.setText(dfs.format(date));
		FILE_TITLE = (student_id + "_" + dffn.format(date) + ".csv").toString();
		if ( service == null ) {                                                                                //orgは onStartで
			conectReady();
		} else {
			conectSaveStart();
		}
		myLog(TAG, dbMsg);
	}

	/**
	 * アカウントピッカーで端末に設定しているアカウントを選択
	 */
	public void selectGoogleAccount() {
		credential = GoogleAccountCredential.usingOAuth2(MainActivity.this.getApplicationContext(), Arrays.asList(DriveScopes.DRIVE));    //端末に設定してあるアカウントをリストアップし
		Intent chooseAccount = credential.newChooseAccountIntent();
		startActivityForResult(chooseAccount, REQUEST_ACCOUNT_PICKER_ONLY);                //アカウントの選択画面を表示
	}

	/**
	 * GoogleAccountCredentialオブジェクトを取得
	 * アカウントの選択画面を表示して onActivityResultに結果を返す
	 */
	protected void conectReady() {
		final String TAG = "conectReady";
		String dbMsg = "accountName=" + accountName;
		try {
			//保留；アカウント選択の保持//////////////////////////////////////
			dbMsg += ",client_id=" + client_id;
			if ( !client_id.isEmpty() ) {                                                            //アカウントが有れば
				//		credential = GoogleAccountCredential.usingAudience(MainActivity.this.getApplicationContext(), "server:client_id:" + client_id);
				//		credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);		//https://firebase.google.com/docs/auth/android/google-signin?hl=ja
			}
			//保留//////////////////////////////////////
			dbMsg += ",credential=" + credential;
			if ( credential == null ) {    //（アカウントが無効などで）credentialが取得できなければ
				credential = GoogleAccountCredential.usingOAuth2(MainActivity.this.getApplicationContext(), Arrays.asList(DriveScopes.DRIVE));    //端末に設定してあるアカウントをリストアップしアカウントの選択画面を表示
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);                //戻り値の処理
			} else {
				conectSaveStart();
			}
			myLog(TAG, dbMsg);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
	}

	/**
	 * http://vividcode.hatenablog.com/entry/20130908/1378613811////////////////
	 */
	private com.google.api.services.drive.Drive getDriveService(GoogleAccountCredential credential) {
		final String TAG = "getDriveService";
		String dbMsg = "credential=" + credential.toString();  //credential=com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential@88c3335
		myLog(TAG, dbMsg);
		return new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
	}

	protected void conectSaveStart() {
		final String TAG = "conectSaveStart";
		String dbMsg = "accountName=" + accountName;
		try {
			credential.setSelectedAccountName(accountName);
			dbMsg += ",credential=" + credential;
			service = getDriveService(credential);
			dbMsg += ",service=" + service;
			loadLocalFile();              //端末内に保存ファイルを呼び出しGoogleDriveへ送信
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
			//java.lang.NullPointerException: Attempt to invoke virtual method 'com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential.setSelectedAccountName(java.lang.String)' on a null object reference
		}
		myLog(TAG, dbMsg);
	}

	static final int REQUEST_ACCOUNT_PICKER = 1;                                    //アカウントピッカーで選択後保存動作へ
	static final int REQUEST_AUTHORIZATION = REQUEST_ACCOUNT_PICKER + 1;
	static final int REQUEST_ACCOUNT_PICKER_ONLY = REQUEST_AUTHORIZATION + 1;    //アカウントピッカーで選択後、グローバル変数とプリファレンス更新

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
							dbMsg += ",accountName=" + accountName;                 //requestCode=1,resultCode=-1,accountName=hkuwayama@coresoft-net.co.jp
							myEditor.putString("accountName_key", accountName);
							boolean kakikomi = myEditor.commit();
							dbMsg = dbMsg + ",書込み=" + kakikomi;//////////////////
							conectSaveStart();
						}
					}
					break;
				case REQUEST_AUTHORIZATION:
					if ( resultCode == Activity.RESULT_OK ) {
						loadLocalFile();              //端末内に保存ファイルを呼び出しGoogleDriveへ送信
					} else {
						startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
					}
					break;
				case REQUEST_ACCOUNT_PICKER_ONLY:
					if ( resultCode == Activity.RESULT_OK ) {
						this.accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
						dbMsg += ",accountName=" + accountName;                 //requestCode=1,resultCode=-1,accountName=hkuwayama@coresoft-net.co.jp
						GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
						//保留//////////////////////////////////////
						if ( result != null ) {
							if ( result.isSuccess() ) {    // Google Sign In was successful, authenticate with Firebase
								GoogleSignInAccount account = result.getSignInAccount();
								this.client_id = account.getId();
								dbMsg += ",getIdToken=" + account.getIdToken();
								dbMsg += ",getServerAuthCode=" + account.getServerAuthCode();
								dbMsg += ",zzaba=" + account.zzaba();
								dbMsg += ",zzaba=" + account.zzabc();
							}
							dbMsg += ",getStatus=" + result.getStatus().toString();
						}
						//保留//////////////////////////////////////
						dbMsg += ",client_id=" + client_id;                 //requestCode=1,resultCode=-1,accountName=hkuwayama@coresoft-net.co.jp
						mainFragment.accountName_tv.setText(accountName);
						myEditor.putString("accountName_key", accountName);
						boolean kakikomi = myEditor.commit();
						dbMsg = dbMsg + ",書込み=" + kakikomi;//////////////////
						String titolStr = "接続先変更";
						String mggStr = "Google Driveの保存先アカウントを\n" + accountName + "\nに変更しました。\n\n更にアカウントを登録する場合はこの端末の「設定」→「アカウント」→「Google」で追加してください。";
						messageShow(titolStr, mggStr);

					}
					break;
			}
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		myLog(TAG, dbMsg);
	}

	/**
	 * GooGle Driveへファイル送信
	 */
	public boolean retBool;

	private boolean saveTextToDrive(final String fileTitol, final String inputText) {       //GooGle Driveへファイル送信
		final String TAG = "saveTextToDrive";
		String dbMsg = "開始";
		try {
			retBool = false;
			//       dbMsg = "inputText=" + inputText;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					final String TAG = "saveTextToDrive_Thread";
					String dbMsg = "fileTitol=" + fileTitol;
					try {
						File body = new File();
						body.setTitle(fileTitol);//fileContent.getName());
						body.setMimeType("text/plain");
						ByteArrayContent content = new ByteArrayContent("text/plain", inputText.getBytes(Charset.forName("UTF-8")));

//						String fileIdOrNull = null;
//						FileList list = null;
//							list = service.files().list().execute();
//
//						dbMsg += ",GoogleDriveに=" +list.size()+"件";
//						for ( File f : list.getItems() ) {
//							if ( FILE_TITLE.equals(f.getTitle()) ) {
//								fileIdOrNull = f.getId();		//				 指定のタイトルのファイルの ID を取得
//							}
//						}
//						dbMsg += ",fileIdOrNull=" +fileIdOrNull;
//			//			if ( fileIdOrNull == null ) {									// 既存ファイルが無ければ
//						myLog(TAG, dbMsg);

//						// JSONデータをアップロードする
////						String folderId = projectFolder.getId();
////						ParentReference parent = new ParentReference();
////						parent.setId(folderId);
//					//	File json = new File().setTitle(FILE_TITLE).setMimeType("text/plain").setParents(Arrays.asList(parent));
//					//	ByteArrayContent jsonContent = new ByteArrayContent("text/plain", genchoJson.getBytes(Charset.forName("UTF-8")));
//						File uploadedJson = service.files().insert(body, content)
//								.setFields("id, parents")
//								.execute();				//https://github.com/bpm-tech/gencho_mobile/blob/master/GenchoMobile/app/src/main/java/jp/co/bpm_gr/genchomobile/activity/ProjectDetailActivity.java
						File uploadedJson = service.files().insert(body, content).execute();                //新規ファイル追加
						dbMsg += ">>insert";
						dbMsg += ";uploadedJson=" + uploadedJson;
						String permissionId = uploadedJson.getId();//				.get ("permissionId")	.toString();
						dbMsg += ";permissionId=" + permissionId;
//						if(! client_id.equals(permissionId)){
//							client_id =  permissionId;
////											   myEditor.remove("client_id");      //「ContentValues」クラスのオブジェクトに追加されたキーと値のペアを、キーを指定して削除する
////											   myEditor.putString("client_id", permissionId);
////											   boolean kakikomi = myEditor.commit();
////											dbMsg = dbMsg + ",書込み=" + kakikomi;
//						}

						retBool = true;

//						} else {														//既存ファイルが有れば//
//							service.files().update(fileIdOrNull, body, content).execute();
//							dbMsg += ">>update" ;
//						}
						dbMsg += ">retBool=" + retBool;
						myLog(TAG, dbMsg);

					} catch (UserRecoverableAuthIOException er) {                        // TODO 失敗時の処理?
						retBool = false;
						Log.e(TAG, dbMsg + ";でエラー発生；" + er);
						startActivityForResult(er.getIntent(), REQUEST_AUTHORIZATION);
					} catch (GoogleAuthIOException er) {
						retBool = false;
						Log.e(TAG, dbMsg + ";でエラー発生；" + er);// Developer ConsoleでClientIDを設定していない場合に発生する
					} catch (IOException er) {
						retBool = false;
						Log.e(TAG, dbMsg + ";でエラー発生；" + er);
						//: FILE_TITLE=123456789_20180110170335.csv;でエラー発生；com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
					}
				}
			});
			t.start();
		} catch (Exception er) {
			retBool = false;
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		dbMsg += ">>retBool=" + retBool;
		myLog(TAG, dbMsg);
		return retBool;
	}         //GooGle Driveへファイル送信

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
					for ( com.google.api.services.drive.model.File f : list.getItems() ) {
						if ( FILE_TITLE.equals(f.getTitle()) ) {
							fileIdOrNull = f.getId();
						}
					}

					InputStream is = null;
					if ( fileIdOrNull != null ) {
						com.google.api.services.drive.model.File f = service.files().get(fileIdOrNull).execute();
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
	private static InputStream downloadFile(com.google.api.services.drive.Drive service, com.google.api.services.drive.model.File file) {
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
		String dbMsg = "";
		if ( googleApiClient != null ) {
			dbMsg = ",isConnected=" + googleApiClient.isConnected();
			googleApiClient.disconnect();                  // 接続解除
			dbMsg += ">>" + googleApiClient.isConnected();
		}
		myLog(TAG, dbMsg);
	}

	//ローカルファイル///////////////////////////////////////////////////////////////////////////////////
	public void setSaveParameter() {                //端末内にファイル保存する為のパラメータ調整
		final String TAG = "setSaveParameter";
		String dbMsg = "開始";
		try {
			java.io.File wrDir = MainActivity.this.getApplicationContext().getFilesDir();//自分のアプリ用の内部ディレクトリ
			dbMsg = ",端末内の保存先=" + local_dir;
			if ( local_dir.isEmpty() ) {
				local_dir = wrDir.getPath();
				dbMsg = dbMsg + ",>>" + local_dir;
			}
			local_dir_size = wrDir.getFreeSpace() + "";// "5000000";
			dbMsg = dbMsg + ",保存先の空き容量=" + local_dir_size;
			if ( local_dir_size.isEmpty() ) {
				local_dir_size = "5000000";
				dbMsg = dbMsg + ">>" + local_dir_size;
			}
			if ( max_file_size.isEmpty() ) {
				max_file_size = "50";
				dbMsg = dbMsg + ",補足:これまでの最大ファイルサイズ=" + max_file_size;
			}
			//課題；自動送信するデータ数;保存先空き容量に応じて変更（1ファイル）5K
			java.io.File[] files;
			files = new java.io.File(local_dir).listFiles();
			if ( files != null ) {
				int fCount = files.length;
				now_count = fCount + "";
			}
			dbMsg += ",fCount=" + now_count + "件";
			long stockCount = Long.parseLong(stock_count);
			long localDirSize = Long.parseLong(local_dir_size);
			long maxFileSize = Long.parseLong(max_file_size);
			long jdge = localDirSize / maxFileSize;
			dbMsg = dbMsg + ",jdge=" + jdge;
			if ( jdge < stockCount ) {
				stock_count = (jdge / 2) + "";
				dbMsg = dbMsg + ">>stock_count=" + stock_count;
			}

			myLog(TAG, dbMsg);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
	}            //端末内にファイル保存する為のパラメータ調整


//    public void saveLocalFile() {                //端末内にファイル保存
//        final String TAG = "saveLocalFile";
//        String dbMsg = "開始";
//        try {
//            final String inputText = makeSendList();                //((EditText)findViewById(R.id.editText)).getText().toString();
//            dbMsg = "inputText=" + inputText;
//            Date wrDt = new Date(System.currentTimeMillis());
//            String WritetTimeFn = dffn.format(wrDt);
//            FILE_TITLE = (student_id + "_" + WritetTimeFn + ".csv").toString();
//            dbMsg += ",FILE_TITLE=" + FILE_TITLE;
//            FileOutputStream outputStream;
//            outputStream = openFileOutput(FILE_TITLE,android.content.Context.MODE_PRIVATE);
//            //Attempt to invoke virtual method 'java.io.FileOutputStream android.content.Context.openFileOutput(java.lang.String, int)' on a null object reference
//            outputStream.write(inputText.getBytes());
//            outputStream.close();
//            dbMsg += ",書き込み終了";
//            java.io.File wrDir = MainActivity.this.getApplicationContext().getFilesDir();//自分のアプリ用の内部ディレクトリ
//            String wrDirName = wrDir.getPath();
//            dbMsg += ",wrDir=" + wrDirName;            //wrDir=/data/user/0/com.example.hkuwayama.nuloger/files
//            java.io.File file = new java.io.File(wrDir, FILE_TITLE);
//            if (file.exists()) {
//                long wSize = file.length();
//                long maxFileSize = Long.parseLong(max_file_size);
//                dbMsg = dbMsg + ",wSize=" + wSize + "/max_file_size=" + maxFileSize;
//                if (maxFileSize < wSize) {
//                    max_file_size = wSize + "";
//                    dbMsg = dbMsg + ">>max_file_size=" + max_file_size;
//                    myEditor.putString("max_file_size_key", max_file_size);
//                    boolean kakikomi = myEditor.commit();
//                    dbMsg = dbMsg + ",書込み=" + kakikomi;
//                }
//                //         max_file_size
//                dbMsg += ",成功";
//                String WriteStartTimeStr = dfs.format(wrDt);
//                mainFragment.send_time_tv.setText(WriteStartTimeStr);
//                setSaveParameter();                //端末内にファイル保存する為のパラメータ調整
//                mainFragment.now_stock_tv.setText(now_count);      //ファイル蓄積件数
//                mainFragment.free_space_tv.setText(local_dir_size);     //空き容量
//
//            }
//            myLog(TAG, dbMsg);
//
//        } catch (IOException er) {
//            myLog(TAG, dbMsg + ";でエラー発生；" + er);
//        } catch (Exception er) {
//            Log.e(TAG, dbMsg + ";でエラー発生；" + er);
//        }
//    }                //端末内にファイル保存

	public void loadLocalFile() {                //端末内に保存ファイルを呼び出しGoogleDriveへ送信
		final String TAG = "loadLocalFile";
		String dbMsg = "開始";
		try {
			List< String > songList = new ArrayList< String >();
			java.io.File wrDir = MainActivity.this.getApplicationContext().getFilesDir();//自分のアプリ用の内部ディレクトリ
			String wrDirName = wrDir.getPath();
			dbMsg += ",wrDirName=" + wrDirName;
			java.io.File[] files;
			files = new java.io.File(wrDirName).listFiles();
			if ( files != null ) {
				int fCount = files.length;
				now_count = fCount + "";
				dbMsg += ",fCount=" + now_count + "件";
				for ( int i = 0 ; i < fCount ; i++ ) {
					dbMsg += "\n(" + i + "/" + fCount + ")";
					InputStream in;
					String lineBuffer;
					String rFName = files[i].getName();
					dbMsg += "rFName=" + rFName;
					in = openFileInput(rFName); //LOCAL_FILE = "log.txt";
					BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					String rStr = "";
					while ( (lineBuffer = reader.readLine()) != null ) {
						dbMsg += ",lineBuffer=" + lineBuffer;
						rStr += lineBuffer;
					}
					dbMsg += ",rStr=" + rStr;
					boolean sucsessBool = saveTextToDrive(rFName, rStr);  //GooGle Driveへファイル送信
					dbMsg += ",sucsessBool=" + sucsessBool;
//                    if (sucsessBool) {
					files[i].delete();
//                    }
					now_count = (fCount - i + 1) + "";
					mainFragment.now_stock_tv.setText(now_count);      //ファイル蓄積件数
				}
//                for(int i = 0; i < fCount; i++) {
//                    dbMsg += "\n(" + i + "/" + fCount + ")";
//                    files[i].delete();
//                }
				setSaveParameter();                //端末内にファイル保存する為のパラメータ調整
				mainFragment.now_stock_tv.setText(now_count);      //ファイル蓄積件数
				mainFragment.free_space_tv.setText(local_dir_size);     //空き容量
				String megStr = fCount + "件送信しました";
				Toast.makeText(this, megStr, Toast.LENGTH_LONG).show();
			}
			myLog(TAG, dbMsg);

		} catch (IOException er) {
			myLog(TAG, dbMsg + ";でエラー発生；" + er);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	public Intent serviceIntent;

	public void startMyService() {                //サービス呼出し
		final String TAG = "startMyService";
		String dbMsg = "開始";
		try {
			serviceIntent = new Intent(MainActivity.this, RecordService.class);
			startService(serviceIntent);
			isServiceRunning = true;
			upReceiver = new UpdateReceiver();
			intentFilter = new IntentFilter();
			intentFilter.addAction("UPDATE_ACTION");
			registerReceiver(upReceiver, intentFilter);

			upReceiver.registerHandler(updateHandler);
			myLog(TAG, dbMsg);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
	}

	public void stopMyService() {                //サービス停止
		final String TAG = "startMyService";
		String dbMsg = "開始";
		try {
			Intent serviceIntent = new Intent(MainActivity.this, RecordService.class);
			stopService(serviceIntent);
			myLog(TAG, dbMsg);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
	}

	public boolean isMyService(String sarchServiceName) {                //サービス実行中 RecordService.class.getName()
		final String TAG = "isMyService";
		String dbMsg = "開始";
		boolean retBool = false;
		try {
			ActivityManager manager = ( ActivityManager ) getSystemService(Context.ACTIVITY_SERVICE);
			dbMsg += ",実行中のサービス";
			for ( ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE) ) {
				String rServiceName = serviceInfo.service.getClassName();
				dbMsg += ";=" + rServiceName;
				if ( sarchServiceName.equals(rServiceName) ) {
					retBool = true;
				}
			}
			myLog(TAG, dbMsg);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
		return retBool;
	}

	// サービスから値を受け取ったら動かしたい内容を書く
	@SuppressLint ( "HandlerLeak" )
	private Handler updateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final String TAG = "startMyService";
			String dbMsg = "開始";
			try {
				Bundle bundle = msg.getData();
				String WriteStartTimeStr = bundle.getString("WriteStartTimeStr");
				dbMsg = "WriteStartTimeStr=" + WriteStartTimeStr;
				String now_count = bundle.getString("now_count");
				dbMsg += ",now_count=" + now_count;
				String local_dir_size = bundle.getString("local_dir_size");
				dbMsg += ",local_dir_size=" + local_dir_size;
				mainFragment.send_time_tv.setText(( CharSequence ) WriteStartTimeStr);
				mainFragment.now_stock_tv.setText(( CharSequence ) now_count);      //ファイル蓄積件数
				mainFragment.free_space_tv.setText(( CharSequence ) local_dir_size);     //空き容量
				myLog(TAG, dbMsg);
			} catch (Exception er) {
				Log.e(TAG, dbMsg + ";でエラー発生；" + er);
			}
		}
	};

	//他のアプリを起動///////////////////////////////////////////////////////////////////////
	public void callOtherAplication(String packageName, String className) {                   //"com.google
		// .android.apps.plus"    "com.google.android.apps.plus.phone.HomeActivity"
		final String TAG = "callOtherAplication";
		String dbMsg = "開始";
		try {
			Intent intent = new Intent(Intent.ACTION_MAIN); //act
			intent.setAction("android.intent.category.LAUNCHER"); // cat
			;

			intent.setClassName(packageName, className); // cmp 省略せずに書く
			//	intent.setFlgs(0x10200000); //flgs ここはIntentの定数を使用するのがいい
			startActivity(intent);

			myLog(TAG, dbMsg);
		} catch (Exception er) {
			Log.e(TAG, dbMsg + ";でエラー発生；" + er);
		}
	}

	////汎用関数///////////////////////////////////////////////////////////////////////
	public boolean isIntVar(String val) {
		try {
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException nfex) {
			return false;
		}
	}

	public boolean isLongVal(String val) {
		try {
			Long.parseLong(val);
			return true;
		} catch (NumberFormatException nfex) {
			return false;
		}
	}
//保留；入力ダイアログ
//    public String retStr = "";
//     public void inputShow(String titolStr, String mggStr, String defaultStr) {
//         retStr = defaultStr;
//         LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
//         final View layout = inflater.inflate( R.layout.step_setting,(ViewGroup) findViewById(R.id.ss_root));
//         EditText ss_stet_et = (EditText) layout.findViewById(R.id.ss_stet_et);
//         TextView ss_msg_tv = (TextView) layout.findViewById(R.id.ss_msg_tv);
//         // アラーとダイアログ を生成
//         AlertDialog.Builder builder = new AlertDialog.Builder(this);
//         builder.setTitle(titolStr);
//         builder.setMessage(mggStr);
//         builder.setView(layout);
//         ss_stet_et.setText(defaultStr);
//
//         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//             public void onClick(DialogInterface dialog, int which) {
//                 // OK ボタンクリック処理
//                 EditText text = (EditText) layout.findViewById(R.id.ss_stet_et);
//                 retStr = text.getText().toString();
//             }
//         });
//         builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//             public void onClick(DialogInterface dialog, int which) {
//               }
//         });
//
//         // 表示
//         builder.create().show();
//    }

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

	//	@Override
	public void onFragmentInteraction(Uri uri) {

	}
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
PW          		; cs06hiroshima→15580824
プロジェクト名      ;   NUloger
プロジェクト ID は	;   nuloger です
プロジェクト番号	;	1012140024872



アカウント			nagounilog01@gmail.com
PW          		; nuaplog01
プロジェクト名      ;   NUloger
プロジェクト ID は	;   nuloger です
プロジェクト番号	;	1012140024872
証明書[1]:
所有者: C=US, O=Android, CN=Android Debug
発行者: C=US, O=Android, CN=Android Debug
シリアル番号: 1
有効期間の開始日: Mon Dec 18 10:32:39 JST 2017終了日: Wed Dec 11 10:32:39 JST 2047
証明書のフィンガプリント:
		 MD5:  50:0C:E3:0F:27:2A:17:F5:32:76:3D:2F:8A:46:61:B6
		 SHA1: EC:D0:0E:1D:5B:85:BF:D2:F4:B4:C2:D0:8D:A6:5B:EB:E6:52:81:DF
		 SHA256: 8E:CC:F1:F4:84:84:CE:DD:A2:55:F1:EC:2D:DC:D9:47:8E:9B:85:45:FB:F6:4C:2F:C4:37:98:42:BA:45:A9:C2
		 署名アルゴリズム名: SHA1withRSA
		 バージョン: 1
Client ID	375687083608-sb1477pludkfq3aelgm9j6q4hb3vi6n8.apps.googleusercontent.com
Android クライアント 1	2018/01/10	Android	375687083608-sb1477pludkfq3aelgm9j6q4hb3vi6n8.apps.googleusercontent.com
APIキー	AIzaSyB4jeijrqrrH8cyGn9nxAUXvQ07VjFXkFA

SSID/a  Buffalo-A-02FA
SSID/g  Buffalo-G-02FA

暗号化キー　emxanek4xhi7k

OAuth 2.0 クライアント ID 	;	NUAndroid1
keytool -exportcert -keystore path-to-debug-or-production-keystore -list -v
 API key	2017/12/26	なし	AIzaSyAcW0we2TB_omDTlk-3qTGdtJ85TYXF2JM

ACriant01         com.example.hkuwayama.nuloger     3E:19:82:D8:6A:F4:98:11:DC:3C:83:80:D0:53:9E:F2:00:A1:A3:70
hkuwayama@coresoft-net.co.jp        com.example.hkuwayama.nuloger

ID: hiroshima_app@coresoft-net.co.jp
PW: cVrN6UDn


"permissionId":"07874823402801050504"
  parents":[{"id":"0AGbBmSMba1QDUk9PVA",

 01-16 22:45:09.481 8996-9384/? I/saveTextToDrive_Thread: fileTitol=1234567890_20180116223739.csv>>
 insert;
 uploadedJson={"alternateLink":"https://drive.google.com/file/d/1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA/view?usp=drivesdk",
 "appDataContents":false,"copyable":true,"createdDate":"2018-01-16T13:45:08.613Z",
 "downloadUrl":"https://doc-0s-8s-docs.googleusercontent.com/docs/securesc/o12bm5shhuu91nu0hue5dvr0vtdo2upj/nbthnsnrv9p47b4rk52mfq0vkb4qms92/1516104000000/07874823402801050504/07874823402801050504/1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA?e=download&gd=true",
 "editable":true,"embedLink":"https://drive.google.com/file/d/1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA/preview?usp=drivesdk",
 "etag":"\"08z8BR1n-DRPUqokV3fOX3Mqkto/MTUxNjExMDMwODYxMw\"",
 "explicitlyTrashed":false,
 "fileExtension":"csv",
 "fileSize":"897",
 "headRevisionId":"0B2bBmSMba1QDRUllUlRVcS94L1Zud0NSMlBUUFcwaHhFR1J3PQ",
 "iconLink":"https://drive-thirdparty.googleusercontent.com/16/type/text/plain",
 "id":"1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA",
 "kind":"drive#file",
 "labels":{"hidden":false,"restricted":false,"starred":false,"trashed":false,"viewed":true},"lastModifyingUser":{"displayName":"桑山博臣",
 "isAuthenticatedUser":true,"kind":"drive#user","permissionId":"07874823402801050504","emailAddress":"hkuwayama@coresoft-net.co.jp"},
 "lastModifyingUserName":"桑山博臣","lastViewedByMeDate":"2018-01-16T13:45:08.613Z",
 "md5Checksum":"c3dce4de1b6dfe9c61f3ab76c1a70317","mimeType":"text/plain",
 "modifiedByMeDate":"2018-01-16T13:45:08.613Z","modifiedDate":"2018-01-16T13:45:08.613Z",
 "originalFilename":"1234567890_20180116223739.csv","ownerNames":["桑山博臣"],"owners":[{"displayName":"桑山博臣","isAuthenticatedUser":true,
 "kind":"drive#user","permissionId":"07874823402801050504","emailAddress":"hkuwayama@coresoft-net.co.jp"}],
 "parents":[{"id":"0AGbBmSMba1QDUk9PVA",
 "isRoot":true,"kind":"drive#parentReference","parentLink":"https://www.googleapis.com/drive/v2/files/0AGbBmSMba1QDUk9PVA",
 "selfLink":"https://www.googleapis.com/drive/v2/files/1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA/parents/0AGbBmSMba1QDUk9PVA"}],
 "quotaBytesUsed":"897","selfLink":"https://www.googleapis.com/drive/v2/files/1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA",
 "shared":false,"title":"1234567890_20180116223739.csv","userPermission":{"etag":"\"08z8BR1n-DRPUqokV3fOX3Mqkto/qEjSLSYPW_BzHIj5Bhg0hIYJnYE\"",
 "id":"me","kind":"drive#permission","role":"owner","selfLink":"https://www.googleapis.com/drive/v2/files/1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA/permissions/me","type":"user"},
 "webContentLink":"https://drive.google.com/uc?id=1FE5pzvO1UMSNM-iHDW90ad_S_pjRCslA&export=download","writersCanShare":true,
 "markedViewedByMeDate":"1970-01-01T00:00:00.000Z","version":"2","capabilities":{"canCopy":true,"canEdit":true},"spaces":["drive"]}


		/////////////////////////////////////////////////////////////////
saveTextToDrive_Thread: fileTitol=1234567890_20180116223830.csv>>insert;
	  uploadedJson={"alternateLink":"https://drive.google.com/file/d/1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI/view?usp=drivesdk","appDataContents":false,"copyable":true,"createdDate":"2018-01-16T13:45:08.721Z","downloadUrl":"https://doc-0k-8s-docs.googleusercontent.com/docs/securesc/o12bm5shhuu91nu0hue5dvr0vtdo2upj/s99fioc8rfal099tq1jasojt0j4pgpfo/1516104000000/07874823402801050504/07874823402801050504/1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI?e=download&gd=true","editable":true,"embedLink":"https://drive.google.com/file/d/1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI/preview?usp=drivesdk","etag":"\"08z8BR1n-DRPUqokV3fOX3Mqkto/MTUxNjExMDMwODcyMQ\"","explicitlyTrashed":false,"fileExtension":"csv","fileSize":"1323","headRevisionId":"0B2bBmSMba1QDRXhHU2J3V3M5a24rbENxcE5BYlF0USt0ZFVvPQ","iconLink":"https://drive-thirdparty.googleusercontent.com/16/type/text/plain","id":"1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI","kind":"drive#file","labels":{"hidden":false,"restricted":false,"starred":false,"trashed":false,"viewed":true},"lastModifyingUser":{"displayName":"桑山博臣","isAuthenticatedUser":true,"kind":"drive#user","permissionId":"07874823402801050504","emailAddress":"hkuwayama@coresoft-net.co.jp"},"lastModifyingUserName":"桑山博臣","lastViewedByMeDate":"2018-01-16T13:45:08.721Z","md5Checksum":"5368d16fd3253dda87bdd60af016ac5c","mimeType":"text/plain","modifiedByMeDate":"2018-01-16T13:45:08.721Z","modifiedDate":"2018-01-16T13:45:08.721Z","originalFilename":"1234567890_20180116223830.csv","ownerNames":["桑山博臣"],"owners":[{"displayName":"桑山博臣","isAuthenticatedUser":true,"kind":"drive#user","permissionId":"07874823402801050504","emailAddress":"hkuwayama@coresoft-net.co.jp"}],"parents":[{"id":"0AGbBmSMba1QDUk9PVA","isRoot":true,"kind":"drive#parentReference","parentLink":"https://www.googleapis.com/drive/v2/files/0AGbBmSMba1QDUk9PVA","selfLink":"https://www.googleapis.com/drive/v2/files/1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI/parents/0AGbBmSMba1QDUk9PVA"}],"quotaBytesUsed":"1323","selfLink":"https://www.googleapis.com/drive/v2/files/1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI","shared":false,"title":"1234567890_20180116223830.csv","userPermission":{"etag":"\"08z8BR1n-DRPUqokV3fOX3Mqkto/my_If4Ac_uO8GzIasS7y4c2mW4o\"","id":"me","kind":"drive#permission","role":"owner","selfLink":"https://www.googleapis.com/drive/v2/files/1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI/permissions/me","type":"user"},"webContentLink":"https://drive.google.com/uc?id=1UvE2Ch9bUd6qd75Xq06U-J5YSce93qcI&export=download","writersCanShare":true,"markedViewedByMeDate":"1970-01-01T00:00:00.000Z","version":"2","capabilities":{"canCopy":true,"canEdit":true},"spaces":["drive"]}




	* */