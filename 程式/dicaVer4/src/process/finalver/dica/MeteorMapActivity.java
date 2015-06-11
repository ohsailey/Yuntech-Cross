package process.finalver.dica;

/* 本程式是路徑畫圖程式(原本)，由林俊佑本人擴充
 * 
 * 注意!，每次有互動訊息資料等動作請加上try&catch
 * 
 * 功能:
 * 1. 路徑規劃
 * 2. 偵測目前網路狀態
 * 3. 顯示目前位置
 * 
 * 跟網路不同之處:
 * 1. 路徑規劃節點可以一直加(但目前最多8個...這是Google direction的限制...但可以突破)
 * 2. ICON節點圖片可以依照自己喜歡一直加(還沒完成...只是可以完成，但目前不是很需要)
 * 
 * 可以學習之處:
 * 1. AsyncTask
 * 2. String to URL to URI to solve encode problem
 * 3. 增加圖片Layut
 * 4. 增加路徑Layout
 * 5. MessageFormat
 * 6. Google direction api 如何運作以及把玩google direction url
 * 7. try&catch is important
 * 
 * 注意:
 * 本程式專用於Android智慧手機版本
 * 因為平板的3.0UP的版本必須在連線上加強連線
 * 所以會造成求救訊息等其他連線訊息出錯
 * 所以正確的做法應該是再闢一個3.0或是4.0的專案相容於平板
 * */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import process.finalver.dica.MeteorMapActivityForRequester.asyncTaskUpdateProgress;



import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MeteorMapActivity extends MapActivity implements LocationListener
{
	private LocationManager locationManager;

	private MapView mapView;
	private MapController mapController;
	private MyLocationOverlay mylayer;
	private Button btnShowRoute;

	private boolean enableTool;
	private Location currentLocation;
	private List<GeoPoint> _points = new ArrayList<GeoPoint>();	//Test
	GeoPoint endPoint;	//求救目的端位置
	GeoPoint pointCurrent ;
	private List<GeoPoint> _helperPoints = new ArrayList<GeoPoint>();		//幫助者座標，用在顯示標記上
	private List<GeoPoint> _tagPoints = new ArrayList<GeoPoint>();		//不能通過的座標，用在顯示標記上
	private List<GeoPoint> _requestPoints = new ArrayList<GeoPoint>();		//求救者的座標，用在顯示標記上
	private List<String>_requesterTA = new ArrayList<String>();	//一樣紀錄標題跟內容
	private List<GeoPoint> _pathPoints = new ArrayList<GeoPoint>();		//經過的節點的座標，用在路徑規劃上
	private List<GeoPoint>_ambulanceDepotPoints = new ArrayList<GeoPoint>();		//急救站節點的座標，用在顯示標記上
	private List<String>_ambulanceDepotTA = new ArrayList<String>();		//急救站節點的標題跟標記內容，偶數為標題奇數為內容，.get(i)中的i/2為points list的.get(n)
	//private List<GeoPoint>_baseStationPoints = new ArrayList<GeoPoint>();		//節點的座標
	final GeoPoint pointDefault = new GeoPoint((int) (0 * 1000000),(int)(0 * 1000000));	//用在畫ICON的座標
	
	Handler handler = new Handler();
	
	public static Handler mHandler = new Handler();
	
	/*----------------------------資料庫部分----------------------------*/
	MySQLiteHelper myHelper;	//資料庫輔助類別物件的引用
	
	/*----------------------------網路連線部分-------------------------*/
	Socket clientSocket;	// 客戶端socket
	String tmp;				// 暫存文字訊息
	String userName = "";	//設定傳送網路時你的IP名稱
	Thread t;	//初始執行緒 
	
	/*----------------------------畫面相關----------------------------------*/
	TextView textview1;
	EditText EditText01;	// 文字方塊
	int quickMsgFlag = 0;	//0則表示alertDialogList為標記地圖，1則表示為快速訊息

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findControl();
		
		
		
		/*----------設定初始資料------------------*/
        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//初始Database
        String result = queryData(myHelper);	//向資料庫中查詢資料
        userName = result;
        
        
        /**
         * 切忌之後這裡要用資料庫去記喔!!!!!!!!*/
        /*------------設定我要出現的靜態座標(急救站、其他救災者座標、此路不通座標)-------------------------*/
        GeoPoint tempPoint;
        /*----------------------------------中央比賽用座標------------------------------------------*/
//        _ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (24.96024 * 1000000),(int)(121.20313 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (24.96849 * 1000000),(int)(121.19221 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (24.95969 * 1000000),(int)(121.18063 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (24.96482 * 1000000),(int)(121.23657 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.98450 * 1000000),(int)(121.21426 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.99327 * 1000000),(int)(121.24186 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.98468 * 1000000),(int)(121.18811 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.96711 * 1000000),(int)(121.24466 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.93550 * 1000000),(int)(121.23462 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.93324 * 1000000),(int)(121.19928 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.93628 * 1000000),(int)(121.18963 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (24.98068 * 1000000),(int)(121.25023 * 1000000)));
//		
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (24.96878 * 1000000),(int)(121.19237 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (24.95791 * 1000000),(int)(121.21660 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (24.98782 * 1000000),(int)(121.21126 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (24.98208 * 1000000),(int)(121.18383 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (24.95855 * 1000000),(int)(121.16795 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (24.97882 * 1000000),(int)(121.23977 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (24.98318 * 1000000),(int)(121.22298 * 1000000)));
//        
//		/*----------------------------------吳鳳比賽用座標------------------------------------------*/
		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.53900 * 1000000),(int)(120.42597 * 1000000)));
		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.54851 * 1000000),(int)(120.43059 * 1000000)));
		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.53170 * 1000000),(int)(120.43447 * 1000000)));
		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.55682 * 1000000),(int)(120.42033 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.55252 * 1000000),(int)(120.41737 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.56741 * 1000000),(int)(120.42138 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.56269 * 1000000),(int)(120.45604 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.55180 * 1000000),(int)(120.45992 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.54247 * 1000000),(int)(120.45808 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.52510 * 1000000),(int)(120.46998 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.52072 * 1000000),(int)(120.41712 * 1000000)));
//		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.54461 * 1000000),(int)(120.45016 * 1000000)));
//		
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (23.55755 * 1000000),(int)(120.42378 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (23.55423 * 1000000),(int)(120.43249 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (23.53512 * 1000000),(int)(120.44225 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (23.53397 * 1000000),(int)(120.42490 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (23.52727 * 1000000),(int)(120.42837 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (23.52689 * 1000000),(int)(120.44412 * 1000000)));
//		_helperPoints.add(tempPoint =  new GeoPoint((int) (23.54026 * 1000000),(int)(120.43451 * 1000000)));
		
		/*----------------------------------------------雲林地圖-------------------------------------------------------------*/
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.70053 * 1000000),(int)(120.53258 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.69472 * 1000000),(int)(120.52468 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.68541 * 1000000),(int)(120.53966 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.69422 * 1000000),(int)(120.54384 * 1000000)));
//		
		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.69544 * 1000000),(int)(120.52834 * 1000000)));
		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.68797 * 1000000),(int)(120.53078 * 1000000)));
		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.70191 * 1000000),(int)(120.52924 * 1000000)));
		/*-------------設定座標區END--------------------------------------------------*/
		if(!_tagPoints.isEmpty()){	//去判斷是否沒有人打出TAG，沒有的話就不用重劃了
			for(int i=0;i<_tagPoints.size();i++){
				showIcon(_tagPoints.get(i),"Here can't pass","Go other ways",2);		//2表示打出此路不通標記
			}
		}
		if(!_ambulanceDepotPoints.isEmpty()){
			for(int i=0;i<_ambulanceDepotPoints.size();i++){
				showIcon(_ambulanceDepotPoints.get(i),"Ambulance Depot","Ambulance Depot base",3);		//3表示打出求救站標記
			}
		}
		if(!_helperPoints.isEmpty()){
			for(int i=0;i<_helperPoints.size();i++){
				showIcon(_helperPoints.get(i),"People","Relief worker",4);		//4表示打出其他救災標記
			}
		}
		
        /*--------------------------- 執行緒初始區------------------------------------------------*/
     	t = new Thread(readData);	//讀資料
     	try{
			  //do what you want to do before sleeping
			  Thread.currentThread().sleep(1000);//sleep for 1000 ms
			  //do what you want to do after sleeptig
		}
			catch(InterruptedException ie){
			//If this thread was intrrupted by nother thread 
		}
        t.start();
        
        /*我封印羅盤的原因是因為連線問題尚未解決*/
        ImageButton tabBtn = (ImageButton)findViewById(R.id.imageButton1);
        tabBtn.setOnClickListener(new ImageButton.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Intent intent = new Intent();
//				intent.setClass(MeteorMapActivity.this, CompassActivity.class);
//				startActivity(intent);
//				MeteorMapActivity.this.finish();
//				selectVersionPage();
				quickMsgFlag = 0;//0表示要list顯示標記清單
				ShowAlertDialogAndList();
			}
        	
        });
        
        ImageButton tagBtn = (ImageButton)findViewById(R.id.imageButton2);
        tagBtn.setOnClickListener(new ImageButton.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ShowMsgDialog("Alert","Are U sure tag here to Can't pass?",1);
			}
        	
        });
        
        //_tagPoints.add(pointDefault);
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()     
//        .detectDiskReads()     
//        .detectDiskWrites()     
//        .detectNetwork()   // or .detectAll() for all detectable problems     
//        .penaltyLog()     
//        .build());     
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()     
//        .detectLeakedSqlLiteObjects()     
//        .detectLeakedSqlLiteObjects()     
//        .penaltyLog()     
//        .penaltyDeath()     
//        .build());
        
        
        
        /*-------------------------------------------View Item-------------------------------------------------------------------------*/
        EditText01=(EditText) findViewById(R.id.editText1);
        /*--------------------------a設定畫面區------------------------------------*/
        textview1 = (TextView) findViewById(R.id.textView1);
        
        
     // 設定按鈕的事件
    	Button buttonSned=(Button) findViewById(R.id.btnSend);
    	buttonSned.setOnClickListener(new Button.OnClickListener() {		
 			// 當按下按鈕的時候觸發以下的方法
 			public void onClick(View v) {
 				// 如果已連接則
 				if(clientSocket.isConnected()){
 					
 					BufferedWriter bw;
 					
 					try {
 						// 取得網路輸出串流
 						bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
 						
 						// 寫入訊息，顯示player n:says...
 						bw.write(userName + ":" + EditText01.getText() + "\n");
 						
 						// 立即發送
 						bw.flush();
 					} catch (IOException e) {
 						
 					}
 					// 將文字方塊清空
 					EditText01.setText("");
 				}
 			}
 		});
    	
    	// 設定按鈕的事件
    	Button btnMsg=(Button) findViewById(R.id.btnMsg);
    	btnMsg.setOnClickListener(new Button.OnClickListener() {		
 			// 當按下按鈕的時候觸發以下的方法
 			public void onClick(View v) {
 				quickMsgFlag = 1;	//1則表示為讓list顯示罐頭訊息清單
 				ShowAlertDialogAndList();
 				
 				quickMsgFlag = 0;	//回復
 			}
 		});
    	
        
	}	//End onCreate
	
	
	
	
	
	
	
    /*切換成第二個頁面*/
    public void selectVersionPage(){
    	setContentView(R.layout.test);
    	Button b1 = (Button) findViewById(R.id.button1);
    	b1.setOnClickListener(new Button.OnClickListener()
        {
			@Override
			public void onClick(View v) {
				try{
	        		showToast("Emerging message send!");
		        	// 如果已連接則
					if(clientSocket.isConnected()){
						
						BufferedWriter bw;
						
						try {
							// 取得網路輸出串流
							bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
							
							// 寫入訊息，顯示player n:says...
							bw.write(userName + ":" +"1134567"+ "\n");
							
							// 立即發送
							bw.flush();
							
							showToast("Emerging message send sucessful!");
						} catch (IOException e) {
							
						}
						// 將文字方塊清空
						//EditText02.setText("");
					}
	        	}catch(NullPointerException e){
	        		//showToast("Exception: " + e);
	        		showToast("Error linking! Please check linking status!");
	        	}catch(Exception e){
	        		showToast("Unkown error: " + e);
	        		//showToast("錯誤連線!請確認連線狀態!");
	        	}
				
				
				
				
			}
        });

    }
	
	
	
	
	
	
	
	/*----------------------------------資料庫升級操作函示區------------------------------*/
  	//方法：從資料庫中查詢資料
  	public String queryData(MySQLiteHelper myHelper){
  		String result="";
  		SQLiteDatabase db = myHelper.getReadableDatabase();		//獲得資料庫物件
  		Cursor cursor = db.query(" user_info", null, null, null, null, null, "id asc");	//查詢表中資料
  		int nameIndex = cursor.getColumnIndex("name");	//獲取name欄的索引
  		for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){	//遍歷結果集，提取資料
  			result = result + cursor.getString(nameIndex);	//回傳名字就好
  		}
  		cursor.close();		//關閉結果集
  		db.close();			//關閉資料庫物件
  		return result;
  	}
    /*End-------------------資料庫操作--------------------------*/

  	
	/*------------------------------------------------------------------------------------------------
	 * 
	 * 
	 * 偵測畫面觸碰區
	 * 
	 * 
	 * */
  	//底下是做偵測長案或是移動等等多功能函式
//	private boolean isPotentialLongPress;
//	
//	@Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        handleLongPress(event);
//        return super.dispatchTouchEvent(event);
//    }
//	
//	private void handleLongPress(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            // A new touch has been detected
//        	
//        	GeoPoint tapPoint = mapView.getProjection().fromPixels((int)event.getX(),(int)event.getY());
//    	    
//    	    int latitude = tapPoint.getLatitudeE6();
//    	    int longitude = tapPoint.getLongitudeE6();
//    	    showToast(latitude +","+ longitude);
////    	    ShowMsgDialog("標題","您確定要標記這裡為不通?",1);
//    	    
//            new Thread(new Runnable() {
//                public void run() {
//                    Looper.prepare();
//                    if (isLongPressDetected()) {
//                        // We have a longpress! Perform your action here
//                    	
//                    	showToast("Long click map detected!!");
//                    	
//                    	
//                    	
//                    }
//                }
//            }).start();
//        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            /*
//             * Only MotionEvent.ACTION_MOVE could potentially be regarded as
//             * part of a longpress, as this event is trigged by the finger
//             * moving slightly on the device screen. Any other events causes us
//             * to cancel this events status as a potential longpress.
//             */
//            if (event.getHistorySize() < 1)
//                return; // First call, no history
// 
//            // Get difference in position since previous move event
//            float diffX = event.getX()
//                    - event.getHistoricalX(event.getHistorySize() - 1);
//            float diffY = event.getY()
//                    - event.getHistoricalY(event.getHistorySize() - 1);
//            
//    	   
//            /* If position has moved substatially, this is not a long press but
//               probably a drag action */
//            if (Math.abs(diffX) > 0.5f || Math.abs(diffY) > 0.5f) {
//                isPotentialLongPress = false;
//            }
//        } else {
//            // This motion is something else, and thus not part of a longpress
//            isPotentialLongPress = false;
//        }
//    }
// 
//    /**
//     * Loops for an amount of time while checking if the state of the
//     * isPotentialLongPress variable has changed. If it has, this is regarded as
//     * if the longpress has been canceled. Else it is regarded as a longpress.
//     */
//    public boolean isLongPressDetected() {
//        isPotentialLongPress = true;
//        try {
//            for (int i = 0; i < (50); i++) {
//                Thread.sleep(10);
//                if (!isPotentialLongPress) {
//                    return false;
//                }
//            }
//            return true;
//        } catch (InterruptedException e) {
//            return false;
//        } finally {
//            isPotentialLongPress = false;
//        }
//    }
 
    
    /*-------------------------------------------------------------------------------------------------------------------------*/
  //底下是做一般正常人寫的偵測移動等等多功能函式，並無偵測長按
//	@Override
//    // 利用 MotionEvent 處理觸控程序
//    public boolean onTouchEvent(MotionEvent event) {
//		
//		
//		 //float touchX = event.getX();       // 觸控的 X 軸位置
//	     //float touchY = event.getY() - 50;  // 觸控的 Y 軸位置
//	     
//	        // 判斷觸控動作
//	        switch( event.getAction() ) {
//	      
//	            case MotionEvent.ACTION_DOWN:  // 按下
//	            	Projection p = mapView.getProjection();
//	        	    GeoPoint geoPoint = p.fromPixels((int) event.getX(), (int) event.getY());
//	        	    // You can now pull lat/lng from geoPoint
//	        	    //int latitude = geoPoint.getLatitudeE6();
//	        	    //int longitude = geoPoint.getLongitudeE6();
//	        	    
//	        	    GeoPoint tapPoint = mapView.getProjection().fromPixels((int)event.getX(),(int)event.getY());
//	        	    
//	        	    int latitude = tapPoint.getLatitudeE6();
//	        	    int longitude = tapPoint.getLongitudeE6();
//	        	    //showToast(latitude +","+ longitude);
//	                
//	                break;
//	                
//	            case MotionEvent.ACTION_MOVE:  // 拖曳移動
//	       
//	                
//	                break;
//	                
//	            case MotionEvent.ACTION_UP:  // 放開
//	            	
//	                break;
//	        }
//		
//		
//		
//	    
//        // TODO Auto-generated method stub
//        return super.onTouchEvent(event);
//	}
	
	/*-------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * 																		Google map區
	 * -------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * */

	private void init()
	{
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{

			new AlertDialog.Builder(MeteorMapActivity.this).setTitle("Maop tool").setMessage("U still not enable location focus, r u want to enable location？")
					.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
					{

						public void onClick(DialogInterface dialog, int which)
						{
							startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
							Toast.makeText(MeteorMapActivity.this, "Unenable location, Can't use this tool.", Toast.LENGTH_SHORT).show();
						}
					}).show();

		}
		else
		{
			if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
			{
				currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
			{
				currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			enableMyLocation();
			enableTool = true;
		}

	}

	private void findControl()
	{
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);

		mapController = mapView.getController();
		mapController.setZoom(16);
		
		/*----------------測試座標---------------------------*/
		//final GeoPoint pointA = new GeoPoint((int) (23.707906 * 1000000),(int)(120.436581 * 1000000));	//用在畫ICON的座標，for 中央
		//final GeoPoint pointB = new GeoPoint((int) (23.680043 * 1000000),(int)(120.476267 * 1000000));	//用在畫ICON的座標，for中央
		//final GeoPoint pointC = new GeoPoint((int) (23.703788 * 1000000),(int)(120.399790 * 1000000));	//用在畫ICON的座標，for中央
		//
		
		final GeoPoint pointA = new GeoPoint((int) (23.55755 * 1000000),(int)(120.42378 * 1000000));	//用在畫ICON的座標，for吳鳳
		final GeoPoint pointB = new GeoPoint((int) (23.55423 * 1000000),(int)(120.43249 * 1000000));	//用在畫ICON的座標，for吳鳳
		final GeoPoint pointC = new GeoPoint((int) (23.703788 * 1000000),(int)(120.399790 * 1000000));	//用在畫ICON的座標，for吳鳳

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, MeteorMapActivity.this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MeteorMapActivity.this);
		final GeoPoint point = new GeoPoint((int)(23.706783 * 1000000),(int)(120.5397066 * 1000000));	//用在畫Temp目標救援的座標
		
		//pointCurrent = new GeoPoint((int)(currentLocation.getLatitude() * 1000000),(int)(currentLocation.getLongitude() * 1000000));
		
		btnShowRoute = (Button) findViewById(R.id.btnShowRoute);
		btnShowRoute.setOnClickListener(new Button.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				String a = 23.7112202+","+120.5443198;
				String b = 23.7142391+","+120.5508997;
				String c = 23.7079219+","+120.5521570;
				//new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(), "桃園縣中壢市中華路一段267號");
				//new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(), "雲林縣虎尾鎮安溪里一鄰27號");
				//new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "," + point.getLatitudeE6() + "," + point.getLongitudeE6());
				new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(),a ,b, c);
				pointCurrent = new GeoPoint((int)(currentLocation.getLatitude() * 1000000),(int)(currentLocation.getLongitude() * 1000000));	//用在畫ICON的座標
				//_tagPoints.set(0, pointCurrent);	//更新此路不通的座標
				_points.add(pointCurrent);
				_points.add(pointA);
				_points.add(pointB);
//				_points.add(pointC);
				double distance = 0.0;
				
				distance += GetDistance(_points.get(0), _points.get(1)); 
				distance += GetDistance(_points.get(1), _points.get(2)); 
//				distance += GetDistance(_points.get(2), _points.get(3)); 
				
				//distance = Math.round(distance);
				
				/*---------------取小數點4位，其中#是代表顯示數字，而0則是顯示數字如果沒有數字則顯示0*/
				DecimalFormat df = new DecimalFormat( "#.0000"); 
				//System.out.println( df.format( distance ) ); 
				
				showToast("Dis: "+df.format( distance )+" KM");
				
				//new GoogleDirection().execute(b, c);
				//showToast(currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "," +a);
				endPoint = point;	//用來畫目標救援人員的測試點
								
			}
		});
	}

	private void enableMyLocation()
	{
		// 定位點
		List<Overlay> overlays = mapView.getOverlays();
		mylayer = new MyLocationOverlay(this, mapView);
		mylayer.enableCompass();
		mylayer.enableMyLocation();
		mylayer.runOnFirstFix(new Runnable()
		{

			public void run()
			{
				GeoPoint point = mylayer.getMyLocation();
				mapController.animateTo(point);
			}
		});
		overlays.add(mylayer);
	}
	
	
	
	/*-------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * 																			Activity生命週期區
	 * -------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * */
//	@Override
//	protected void onStart(){
//		showToast("Start");
//		super.onStart();
//	}
	
	
	
	@Override
	protected void onResume()
	{
		showToast("Resume");
		//t.start();
		
		super.onResume();
		if (enableTool)
		{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MeteorMapActivity.this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, MeteorMapActivity.this);
			mylayer.enableMyLocation();
			mylayer.enableCompass();
		}
		else
		{
			init();
		}
		
		/*--------每次resume就測試連線------------*/
		try{
    		
        	// 如果已連接則
			if(clientSocket.isConnected()){
				
				BufferedWriter bw;
				
				try {
					
					showToast("Server linking sucessful.");
					// 取得網路輸出串流
					bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
					
					// 寫入訊息，顯示player n:says...
					//bw.write(userName + ":" +"0034567"+"Test"+ "\n");
					bw.write("Test\n");
					
					// 立即發送
					bw.flush();
					
					
				} catch (IOException e) {
					showToast("Unkown error: " + e);
				}
				// 將文字方塊清空
				//EditText02.setText("");
			}

    	}catch(NullPointerException e){
    		//showToast("Exception: " + e);
    		showToast("Error link! Please check linking status!");
    	}catch(Exception e){
    		showToast("Unkown error: " + e);
    		//showToast("錯誤連線!請確認連線狀態!");
    	}
    	
		
	}

	@Override
	protected void onPause()
	{
		
		showToast("Pause");
		//t.interrupt();
		super.onPause();
		if (enableTool)
		{
			locationManager.removeUpdates(MeteorMapActivity.this);
			mylayer.disableCompass();
			mylayer.disableMyLocation();
			
		}
	}
	
//	@Override
//	protected void onDestroy() {
//		showToast("Exit");
//		super.onDestroy();
//	}
	
	
//	@Override
//	protected void onStop(){
//		showToast("Stop");
//		super.onStop();
//	}
	
	
	 /*---------------------------------------------------------------------------------------------------------------------------------------------------------
	  * 網路伺服器連線區
	  * 
	  * 
	  * 
	  * 
	  * 
	  * */
	// 顯示更新訊息
			private Runnable updateText = new Runnable() {
				public void run() {
					// 加入新訊息並換行
					textview1.append(tmp + "\n");
				}
			};
		
		// 取得網路資料
				private Runnable readData = new Runnable() {
					public void run() {
						// server端的IP
						InetAddress serverIp;

						
						try {
							// 以內定(本機電腦端)IP為Server端
							serverIp = InetAddress.getByName("140.125.45.116");
							int serverPort = 8080;
							clientSocket = new Socket(serverIp, serverPort);
							
							// 取得網路輸入串流
							BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
							
							// 當連線後
							while (clientSocket.isConnected()) {
								// 取得網路訊息
								tmp = br.readLine();
								
								// 如果不是空訊息則
								/* 解讀密碼端...
								 * 這裡要用contains不能用equal跟indexof
								 * 因為伺服器端打出來的訊號不是單純的密碼他打出來還是原來手機端的"player01: XXXXXXXXXXX"(伺服器端有對應的解釋可以參照)
								 * 所以要用包含字串，而不是字元也不是相等
								 * */
								if(tmp!=null)
									if(tmp.contains("1134567")){
										//Toast.makeText(getApplicationContext(), "Someone say help!", Toast.LENGTH_SHORT).show();
										//tmp = "";
										//testMsgShow("tmp.equals(11356946) is called.\n");
										//tmp = tmp+"\nmHandler.post(Test) is called.";
										//mHandler.post(updateText);
										
										/*
										 * 底下就是上面說到的Thread不能隨意控制主程序的畫面的問題
										 * 所以我們用另一種方式去處理...用適合在Thread中打Toast的方法
										 * */
										showToast("Some one say help!");
										
									}else if(tmp.contains("2234567")){
										char tmpChar;
										tmpChar = tmp.charAt(7);
										//if((char)tmpChar < 10)	//實驗...
											userName = "player0" + tmpChar;
										//mHandler.post(updateText);
									}else{
										// 顯示新的訊息
										/* 因為在這裡的更新手機端的Text view的方式是去做一個共有的String(tmp)
										 * 所以依此類推我們為了能自由的控制這種更新Text view的方式
										 * 我在下面幫tmp去增加一些字後再由Function去打訊息出來
										 * 
										 * 但他打完似乎就會把tmp刷乾淨(待考證)，所以就不用再把tmp弄成null
										 * */
										//tmp = tmp+"\nmHandler.post(updateText) is called.";
										mHandler.post(updateText);
										//testMsgShow("mHandler.post(updateText) is called.\n");
										//TextView01.append("mHandler.post(updateText) is called.\n");
										//Toast.makeText(getApplicationContext(), "mHandler.post(updateText) is called", Toast.LENGTH_SHORT).show();
									}
									// 顯示新的訊息
									//mHandler.post(updateText);
									//Toast.makeText(getApplicationContext(), "Someone say help!",Toast.LENGTH_SHORT).show();
									
							}

						} catch (IOException e) {
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println("Error: "+e);
						}
					}
				};
	
	
	
	
	/* AsyncTask<The Params input data type, Progress input data type, Result input data type>
	 * The three types used by an asynchronous task are the following:
	 * 1. Params, 	the type of the parameters sent to the task upon execution.
	 * 2. Progress, the type of the progress units published during the background computation.
	 * 3. Result, 	the type of the result of the background computation.
	 * 基本上就是我出了一個AsyncTask之後要設好三個輸入變數的資料型態
	 * 而為何要三個的原因則是因為下面有三個常駐函式(doInBackground, onProgressUpdate, onPostExecute)需要輸入參數
	 * */
	
			
			
	/*-------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * 																					規劃路徑區
	 * -------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * */
	private class GoogleDirection extends AsyncTask<String, Integer, List<GeoPoint>>
	{
		//private final String mapAPI = "http://maps.google.com/maps/api/directions/json?" + "origin={0}&destination={1}&waypoints={2}&language=zh-TW&sensor=true";
		//private final String mapAPI = "http://maps.googleapis.com/maps/api/directions/json?origin=Boston,MA&destination=Concord,MA&waypoints=Charlestown,MA|Lexington,MA&sensor=false";
		//String add = mapAPI.replace("|", "%124");
		//add = mapAPI.replace("\n", "");
		
		private final String mapAPI2 = "http://maps.google.com/maps/api/directions/json?";
		
		private String _from;
		private String _to;
		private String _pass;
		private List<GeoPoint> _points = new ArrayList<GeoPoint>();
		
		@Override
		protected List<GeoPoint> doInBackground(String... params)
		/* 這裡我們可以看到(String... params)
		 * 這意思是指會把一開始輸入的參數全部串成陣列
		 * 所以當new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(), "雲林縣虎尾鎮安溪里一鄰27號");
		 * 可以發現這當中是由兩組字串組成(現今位置座標, 欲查之地址)
		 * 所以當進來第一個常駐還是的時候他會把這兩個字串串成陣列叫做params[n], n為輸入的東西個數
		 * 
		 * */
		{
			if (params.length < 0)
				return null;
			/*
			_from = params[0];	//起點
			_to = params[1];	//終點
			_pass = params[2];	//經過
			//_pass = null;
			
			//showToast("_from: " + params[0] + ", _to: " + params[1]);
			
			String url = MessageFormat.format(mapAPI, _from, _to, _pass);
			//showToast("url: " + url);
			*/
			String strurl = mapAPI2;
			for(int i=0;i<params.length;i++){
				if(i == 0){
					strurl = strurl + "origin=" + params[i];
				}
				if(i == 1){
					strurl = strurl + "&destination=" + params[i];
				}
				if(i == 2){
					strurl = strurl + "&waypoints=" + params[i];
				}
				if(i > 2){
					strurl += "|" + params[i];
				}
			}
			strurl += "&language=en&sensor=true";
			
			showToast("url: " + strurl);
			
			//String url3 = "http://maps.google.com/maps/api/directions/json?" + "origin="+params[0]+"&destination={1}&waypoints={2}&language=zh-TW&sensor=true"; 
			Log.i("map", strurl);
			
			//HttpGet get = new HttpGet(url);
			
			/* 這邊是去處理Google direction api的BUG
			 * 因為在一般的URL傳送上，如果是用以前的方法是用字串的話
			 * 傳統的作法會讓"|"這個符號出現錯誤編碼
			 * 所以我找到的解法是走uri的路線
			 * 所以先把字串轉成URL形式，後再把URL轉成URI
			 * 然後走HTTPPOST的路就直接用URI
			 * 解除BUG
			 * */
			URL url = null;
			URI uri = null;
			try {
				url = new URL(strurl);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			HttpGet get = new HttpGet(uri);
			String strResult = "";
			try
			{
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				HttpClient httpClient = new DefaultHttpClient(httpParameters);

				HttpResponse httpResponse = null;
				httpResponse = httpClient.execute(get);

				if (httpResponse.getStatusLine().getStatusCode() == 200)
				{
					strResult = EntityUtils.toString(httpResponse.getEntity());

					JSONObject jsonObject = new JSONObject(strResult);
					JSONArray routeObject = jsonObject.getJSONArray("routes");
					String polyline = routeObject.getJSONObject(0).getJSONObject("overview_polyline")
							.getString("points");

					if (polyline.length() > 0)
					{
						decodePolylines(polyline);

					}

				}
			}
			catch (Exception e)
			{
				Log.e("map", e.toString());
			}
			
			
			
			return _points;
		}
		
		
		/* 用來做解碼動作
		 * */
		private void decodePolylines(String poly)
		{
			int len = poly.length();
			int index = 0;
			int lat = 0;
			int lng = 0;

			while (index < len)
			{
				int b, shift = 0, result = 0;
				do
				{
					b = poly.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lat += dlat;

				shift = 0;
				result = 0;
				do
				{
					b = poly.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
				_points.add(p);

			}
		}

		protected void onPostExecute(List<GeoPoint> points)	//畫線，畫圖
		{
			if (points.size() > 0)
			{
				LineItemizedOverlay mOverlay = new LineItemizedOverlay(points);

				List<Overlay> overlays = mapView.getOverlays();
				overlays.clear();
				overlays.add(mylayer);
				overlays.add(0, mOverlay);
				
				
				/*底下每次去重畫所需要的標記
				 * 目前有的標記: 急救站、其他救災人、此路不通標記、救災目標
				 * showIcon的flag:
				 * 當為1的時候表示要畫出受災人(救災目標)
				 * 當為2的時候表示要畫出此路不通
				 * 當為3的時候表示要畫出急救站
				 * 當為4的時候表示要畫出其他救災人
				 * 
				 * */
				//String[] token = params[1].split(",");
				showIcon(endPoint,"Helper1","Help~",1);		//1表示打出求救目標的位置
				if(!_tagPoints.isEmpty()){	//去判斷是否沒有人打出TAG，沒有的話就不用重劃了
					for(int i=0;i<_tagPoints.size();i++){
						showIcon(_tagPoints.get(i),"Can't pass","Go other ways.",2);		//2表示打出此路不通標記
					}
				}
				if(!_ambulanceDepotPoints.isEmpty()){
					for(int i=0;i<_ambulanceDepotPoints.size();i++){
						showIcon(_ambulanceDepotPoints.get(i),"Ambulance Depot","Ambulance Depot Base",3);		//3表示打出求救站標記
					}
				}
				if(!_helperPoints.isEmpty()){
					for(int i=0;i<_helperPoints.size();i++){
						showIcon(_helperPoints.get(i),"People","Relief worker.",4);		//3表示打出求救站標記
					}
				}
				
				asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
				task.execute();
				
			}

		}

	}
	
	
	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLocationChanged(Location location)
	{
		Log.v("map", location.toString());
		currentLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO Auto-generated method stub

	}
	
	
	/* 取得兩點間的距離的method */
	  public double GetDistance(GeoPoint gp1,GeoPoint gp2)
	  {
	    double Lat1r = ConvertDegreeToRadians(gp1.getLatitudeE6()/1E6);
	    double Lat2r = ConvertDegreeToRadians(gp2.getLatitudeE6()/1E6);
	    double Long1r= ConvertDegreeToRadians(gp1.getLongitudeE6()/1E6);
	    double Long2r= ConvertDegreeToRadians(gp2.getLongitudeE6()/1E6);
	    /* 地球半徑(KM) */
	    double R = 6371;
	    double d = Math.acos(Math.sin(Lat1r)*Math.sin(Lat2r)+
	               Math.cos(Lat1r)*Math.cos(Lat2r)*
	               Math.cos(Long2r-Long1r))*R;
	    //return d*1000;
	    return d;
	  }
	  private double ConvertDegreeToRadians(double degrees)
	  {
	    return (Math.PI/180)*degrees;
	  }
	
	  
	  
	  /*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	   * 打出訊息區
	   * --------------------------------*/
	  
	  
	public void showToast(final String msg) {
		  handler.post(new Runnable() {
		   public void run() {
		    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		   }
		  });
	 }
	
	/*-----------------------------用來打出alertDialog-------------------------------------*/
	 private void ShowMsgDialog(String title, String Msg, final int flag)
	 {
		  Builder MyAlertDialog = new AlertDialog.Builder(this);
		  MyAlertDialog.setTitle(title);
		  MyAlertDialog.setMessage(Msg);
		  DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener()
		  {
				 public void onClick(DialogInterface dialog, int which) {
				  //如果不做任何事情 就會直接關閉 對話方塊
					 if(flag == 1){	//flag == 1則表示進入標記此路不通，切忌!跟showIcon的flag不同喔!!!!分開!!!
						pointCurrent = new GeoPoint((int)(currentLocation.getLatitude() * 1000000),(int)(currentLocation.getLongitude() * 1000000));	//用在畫ICON的座標
						_tagPoints.add(pointCurrent);
						 showIcon(pointCurrent,"Can't pass","Go other ways.", 2);	//2表示是打目前位置不通
					 }
					 if(flag == 2){	//flag == 2則表示進入功能頁面
						 //showIcon(_points.get(0),"Helper1","Help~~", 1);
					 }
					    
				 }
		  };;
		  
		  DialogInterface.OnClickListener CancelClick = new DialogInterface.OnClickListener()
		  {
				 public void onClick(DialogInterface dialog, int which) {
				  //如果不做任何事情 就會直接關閉 對話方塊
					 if(flag == 1){	//flag == 1則表示標記此路不通
						    
					 }
					 if(flag == 2){	//flag == 2則表示進入功能頁面
						    
					 }
					    
				 }
		  };;
		  MyAlertDialog.setPositiveButton("OK",OkClick );
		  MyAlertDialog.setNeutralButton("Cancel",CancelClick );
		  MyAlertDialog.show();
	 }
	 
	 
	 /*-----------------------------用來打出alertDialog-------------------------------------*/
	//有選單的對話方塊
	 final String[] titleListStrForBasestation = {"Tag to crash","Tag to mudd","Tag to need for help","Tab to house crack"};
	 final String[] titleListStrForBasestation1 = {"Soon there","Be stronger","Don't scare","Call me soon"};
	 final String[] articleListStr = {"Are u sure to tag?","Infact","Easy"};
	 
	 private void ShowAlertDialogAndList()
	 {
		 if(quickMsgFlag == 0){
			  Builder MyAlertDialog = new AlertDialog.Builder(this);
			  MyAlertDialog.setTitle("Quick message");
			  //建立選擇的事件
			  DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener()
			  {
				 public void onClick(DialogInterface dialog, int which) {
					 ShowMsgDialog(titleListStrForBasestation[which], "OK?", which);
				 }
			  };
			  
			  //建立按下取消什麼事情都不做的事件
			  DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener()
			  {
				  public void onClick(DialogInterface dialog, int which) {
				  }
			  };  
			  
			  MyAlertDialog.setItems(titleListStrForBasestation, ListClick);
			  MyAlertDialog.setNeutralButton("Cancel",OkClick );
			  MyAlertDialog.show();
		  }
		  if(quickMsgFlag == 1){
			  Builder MyAlertDialog = new AlertDialog.Builder(this);
			  MyAlertDialog.setTitle("Quick message");
			  //建立選擇的事件
			  DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener()
			  {
				 public void onClick(DialogInterface dialog, int which) {
					 ShowMsgDialog(titleListStrForBasestation1[which], "OK?", which);
				 }
			  };
			  
			  //建立按下取消什麼事情都不做的事件
			  DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener()
			  {
				  public void onClick(DialogInterface dialog, int which) {
				  }
			  };  
			  
			  MyAlertDialog.setItems(titleListStrForBasestation1, ListClick);
			  MyAlertDialog.setNeutralButton("Cancel",OkClick );
			  MyAlertDialog.show();
		  }
	 }

	
	
	public void showIcon(GeoPoint drawPoint,String userName, String article, int iconNumber){
		/* 底下是去新增一個地圖的層級(可參考zoon level原理...有很多層)
		 * 然後我們只是去加一層地圖層級，並在該地圖層級上的某個位置加上一個圖案
		 * */

		if(iconNumber == 1){	//表示打出求救者位置
			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = this.getResources().getDrawable(R.drawable.sos);	//設定圖案
			CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//去叫new另一個類別
			
			//GeoPoint point = new GeoPoint(latitudeE6, longitudeE6);
			//GeoPoint point = new GeoPoint((int)(23.707906 * 1000000),(int)(120.436581 * 1000000));	//座標
			GeoPoint point = drawPoint;
			//GeoPoint point2 = new GeoPoint((int)(23.714707 * 1000000),(int)(120.43703 * 1000000));	//座標
			//Location mLocation = new Location(120, 23);
			OverlayItem overlayitem = new OverlayItem(point, userName, article);	//生出每個座標者的位置資訊
			//OverlayItem overlayitem2 = new OverlayItem(point2, "Helper2", "I'm in Athens, Greece!");
			
			
			itemizedOverlay.addOverlay(overlayitem);
			//itemizedOverlay.addOverlay(overlayitem2);
			mapOverlays.add(itemizedOverlay);
		}
		if(iconNumber == 2){	//表示打目前此路不通標記
			try{
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = this.getResources().getDrawable(R.drawable.tag);	//設定圖案
				CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//去叫new另一個類別
				
				GeoPoint point = drawPoint;
				OverlayItem overlayitem = new OverlayItem(point, userName, article);	//生出每個座標者的位置資訊

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);
			}catch(Exception e){
				showToast("Unkown error: "+e);
			}
			
		}
		
		if(iconNumber == 3){	//表示打急救站標記
			try{
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = this.getResources().getDrawable(R.drawable.helthkeep);	//設定圖案
				CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//去叫new另一個類別
				
				GeoPoint point = drawPoint;
				OverlayItem overlayitem = new OverlayItem(point, userName, article);	//生出每個座標者的位置資訊

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);
			}catch(Exception e){
				showToast("Unkown error: "+e);
			}
			
		}
		
		if(iconNumber == 4){	//表示打其他救災人標記
			try{
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = this.getResources().getDrawable(R.drawable.helper);	//設定圖案
				CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//去叫new另一個類別
				
				GeoPoint point = drawPoint;
				OverlayItem overlayitem = new OverlayItem(point, userName, article);	//生出每個座標者的位置資訊

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);
			}catch(Exception e){
				showToast("Unkown error: "+e);
			}
			
		}
		
	}
	
	public class asyncTaskUpdateProgress extends AsyncTask<Void, Integer, Void> {
   	 
        int progress;
        ProgressDialog progressDialog;
         
	     @Override
	     protected void onPostExecute(Void result) {
	      // TODO Auto-generated method stub
	    	 progressDialog.dismiss();
	    	 ShowMsgDialog("Task: Rescing","Path planning finish.",2);
	     }
	    
		@Override
	    protected void onPreExecute() {
	      // TODO Auto-generated method stub
		      progress = 0;
		      progressDialog = ProgressDialog.show(MeteorMapActivity.this, "Wait moment", "Task assignning...");
		      //((Object) progressDialog).doInBackground();
	    }
	    
	     @Override
	     protected Void doInBackground(Void... arg0) {
		      // TODO Auto-generated method stub
		      while(progress<100){
			       progress++;
			       SystemClock.sleep(50); 
			       //sleep(3000);
		      }
		      //ShowMsgDialog("找到志工","已連線",1);
		      return null;
	     }
    }
	
	
}