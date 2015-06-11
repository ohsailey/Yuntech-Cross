/**
 * 在這個畫面放TabHost以及其他絕對會看到的常駐按鈕
 * 
 * 畫面中我們會在背景做與伺服器連線的動作
 */
package process.finalver.dica;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

/**
 * @author Bruce
 *
 */
public class MainFunctionActivity  extends TabActivity {
	private TabHost m_tabHost;
	TextView text1;
	TextView text2;
	TextView text3;
	private Handler handler = new Handler();
	
	/*--------------------地圖---------------------------------------------*/
//	private Location currentLocation;
//	private LocationManager locationManager;
//	private boolean enableTool;
//	private String strLocationPrivider;
//	Location location = null;
//	private GeoPoint currentGeoPoint;
	
	//e14_1:locationGPS
	private LocationManager mLocationManager;
	String locationStr = "666";
	
	/*----------------------------資料庫部分----------------------------*/
	MySQLiteHelper myHelper;	//資料庫輔助類別物件的引用
	
	/*----------------------------網路連線部分-------------------------*/
	Socket clientSocket;	// 客戶端socket
	String tmp;				// 暫存文字訊息
	String userName = "";	//設定傳送網路時你的IP名稱
	
    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        
        /*--------------------------------------------------------------
         * 底下這行是移除狀態列，在加入之前請先import window
         * 這行要在setContentView前面加，不然會當機
         *-------------------------------------------------------------- */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        setContentView(R.layout.mainfunctionlayout);  
           
        m_tabHost = getTabHost();  
        
        text1=(TextView) findViewById(R.id.textViewName);
        text2=(TextView) findViewById(R.id.textView2);
        text3=(TextView) findViewById(R.id.textView3);
        
        /*------------設定Tab----------------------*/
        addOneTab();  
        addTwoTab();  
        //addThreeTab();  //我還是先把自設工具頁面取消掉
        addFourTab();  
        
        
        /*----------設定初始資料------------------*/
        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//初始Database
        String name = text1.getText().toString();
        String result = queryData(myHelper);	//向資料庫中查詢資料
        userName = result;
        name += result;
        text1.setText(name);
        
        /*------------OWN GPS SHOW--------------------*/
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        

      //顯示該手機的IMEI碼
        try{
        	TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
    		//String IMEI = "IMEI: " + tm.getDeviceId() + "\n";
            
    		//String idTopic = text2.getText().toString();
    		char charArray[] = new char[10];
            String  idName = tm.getDeviceId();	//向資料庫中查詢資料
            idName.getChars(2, 12, charArray, 0);
            String id = text2.getText().toString();
            for(int i=0;i<charArray.length;i++){
            	id += charArray[i];
            }
            //idTopic += id;
            text2.setText(id);
        }catch(Exception e){
        	showToast("Error:"+e);
        }
		

        
        /*--------------------------- 執行緒初始區------------------------------------------------*/
     	final Thread t = new Thread(readData);	//讀資料
     	try{
			  //do what you want to do before sleeping
			  Thread.currentThread().sleep(2000);//sleep for 1000 ms
			  //do what you want to do after sleeptig
		}
			catch(InterruptedException ie){
			//If this thread was intrrupted by nother thread 
		}
     	
        t.start();
        
        
//        int whileCounter = 1;	//用來讓while loop作為作一次的動作
//        while(clientSocket.isConnected()){
//        	 if(clientSocket.isConnected()){
//        		 
//        		 showToast("恭喜!伺服器已連線!");
//        		 whileCounter = 0;
////     			BufferedWriter bw;
////     			
////     			try {
////     				// 取得網路輸出串流
////     				bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
////     				
////     				// 寫入訊息，顯示player n:says...
////     				bw.write(userName + ":" + "2234567" + "\n");
////     				
////     				// 立即發送
////     				bw.flush();
////     			} catch (IOException e) {
////     				
////     			}
////     			// 將文字方塊清空
////     			//EditText02.setText("");
//     		}
//        	
//        }
        

        /*----------------------------Button設置區-----------------------------------------------*/
        Button b1 = (Button) findViewById(R.id.button1);   	
    	 b1.setOnLongClickListener(new Button.OnLongClickListener() { 
		        @Override
		        public boolean onLongClick(View v) {
		            // TODO Auto-generated method stub
		        	
		        	try{
		        		showToast("緊急訊息傳送!");
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
								
								showToast("Emerging messag sending sucess!");
							} catch (IOException e) {
								
							}
							// 將文字方塊清空
							//EditText02.setText("");
						}
			        	
						
//						showToast(locationStr);
		        	}catch(NullPointerException e){
		        		//showToast("Exception: " + e);
		        		showToast("Error linking! Please check linking status!");
		        	}catch(Exception e){
		        		showToast("Unkown error: " + e);
		        		//showToast("錯誤連線!請確認連線狀態!");
		        	}

		            return true;
		        }
		    });
    	 
//    	 StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()     
//         .detectDiskReads()     
//         .detectDiskWrites()     
//         .detectNetwork()   // or .detectAll() for all detectable problems     
//         .penaltyLog()     
//         .build());     
//         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()     
//         .detectLeakedSqlLiteObjects()     
//         .detectLeakedSqlLiteObjects()     
//         .penaltyLog()     
//         .penaltyDeath()     
//         .build());
        
    }  //End onCreate
    
    /*----------------------------------資料庫升級操作函示區------------------------------*/
  	//方法：從資料庫中查詢資料
  	public String queryData(MySQLiteHelper myHelper){
  		String result="";
  		SQLiteDatabase db = myHelper.getReadableDatabase();		//獲得資料庫物件
  		Cursor cursor = db.query(" user_info", null, null, null, null, null, "id asc");	//查詢表中資料
  		int nameIndex = cursor.getColumnIndex("name");	//獲取name欄的索引
  		for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){	//遍歷結果集，提取資料
  			result = result + cursor.getString(nameIndex);
  			/*result = result + cursor.getInt(ageIndex)+"    ";
  			result = result + cursor.getString(careerIndex)+"    ";
  			result = result + cursor.getString(major1Index)+"    ";
  			result = result + cursor.getString(major2Index)+"    ";
  			result = result + cursor.getString(major3Index)+"     \n";*/
  		}
  		cursor.close();		//關閉結果集
  		db.close();			//關閉資料庫物件
  		return result;
  	}
    /*End-------------------資料庫操作--------------------------*/
    
    
    public void addOneTab(){  
        Intent intent = new Intent();  
        intent.setClass(MainFunctionActivity.this, MeteorMapActivity.class);  
          
        TabSpec spec = m_tabHost.newTabSpec("One");  
        spec.setIndicator(getString(R.string.one), null);  
        spec.setContent(intent);          
        m_tabHost.addTab(spec);  
    }  
      
    public void addTwoTab(){  
        Intent intent = new Intent();  
        intent.setClass(MainFunctionActivity.this, ToolsActivity.class);  
          
        TabSpec spec = m_tabHost.newTabSpec("Two");  
        spec.setIndicator(getString(R.string.two), null);  
        spec.setContent(intent);          
        m_tabHost.addTab(spec);  
    }  
    public void addThreeTab(){  
        Intent intent = new Intent();  
        intent.setClass(MainFunctionActivity.this, ShortpathActivity.class);  
          
        TabSpec spec = m_tabHost.newTabSpec("Three");  
        spec.setIndicator(getString(R.string.three), null);  
        spec.setContent(intent);          
        m_tabHost.addTab(spec);  
    }  
    public void addFourTab(){  
        Intent intent = new Intent();  
        intent.setClass(MainFunctionActivity.this, SetActivity.class);  
          
        TabSpec spec = m_tabHost.newTabSpec("Four");  
        spec.setIndicator(getString(R.string.four), null);  
        spec.setContent(intent);          
        m_tabHost.addTab(spec);  
    }
    
    
//    private void init()
//	{
//		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
//		{
//
//			new AlertDialog.Builder(MainFunctionActivity.this).setTitle("地圖工具").setMessage("您尚未開啟定位服務，要前往設定頁面啟動定位服務嗎？")
//					.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
//					{
//
//						public void onClick(DialogInterface dialog, int which)
//						{
//							startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//						}
//					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
//					{
//						public void onClick(DialogInterface dialog, int which)
//						{
//							Toast.makeText(MainFunctionActivity.this, "未開啟定位服務，無法使用本工具!!", Toast.LENGTH_SHORT).show();
//						}
//					}).show();
//
//		}
//		else
//		{
//			if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null)
//			{
//				currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//			}
//			else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
//			{
//				currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//			}
//			enableTool = true;
//		}
//
//	}
    
//    protected void onResume()
//	{
//		super.onResume();
//		if (enableTool)
//		{
//			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MainFunctionActivity.this);
//			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, MainFunctionActivity.this);
//		}
//		else
//		{
//			init();
//		}
//	}
	
//	@Override
//	public void onLocationChanged(Location location) {
//		// TODO Auto-generated method stub
//		//currentLocation = location;
//	}
//
//	@Override
//	public void onProviderDisabled(String provider) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onProviderEnabled(String provider) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//		// TODO Auto-generated method stub
//		
//	}  
	
	
	/*--------------------------Map相關------------------------------*/
//	public Location getLocationPrivider(LocationManager lm)
//	  {
//	    Location retLocation = null;
//	    try
//	    {
//	      Criteria mCriteria01 = new Criteria();
//	      mCriteria01.setAccuracy(Criteria.ACCURACY_FINE);
//	      mCriteria01.setAltitudeRequired(false);
//	      mCriteria01.setBearingRequired(false);
//	      mCriteria01.setCostAllowed(true);
//	      mCriteria01.setPowerRequirement(Criteria.POWER_LOW);
//	      strLocationPrivider = lm.getBestProvider(mCriteria01, true);
//	      retLocation = lm.getLastKnownLocation(strLocationPrivider);
//	    }
//	    catch(Exception e)
//	    {
//	      //mTextView01.setText(e.toString());
//	      e.printStackTrace();
//	    }
//	    return retLocation;
//	  }
//	
//	 /* 當手機收到位置變更時，將location傳入更新當下GeoPoint及MapView */
//	  private void processLocationUpdated(Location location)
//	  {
//	    /* 傳入Location物件，取得GeoPoint地理座標 */
//	    currentGeoPoint = getGeoByLocation(location);
//	    	    
//	    text2.setText
//	    (
//	      getResources().getText(R.string.str_my_location).toString()+"\n"+
//	      /* 延伸學習：取出GPS地理座標： */
//	      
//	      getResources().getText(R.string.str_longitude).toString()+
//	      String.valueOf((int)currentGeoPoint.getLongitudeE6()/1E6)+"\n"+
//	      getResources().getText(R.string.str_latitude).toString()+
//	      String.valueOf((int)currentGeoPoint.getLatitudeE6()/1E6)+"\n"+
//	      
//	      getAddressbyGeoPoint(currentGeoPoint)
//	    );
//	  }
//	
//	private GeoPoint getGeoByLocation(Location location)
//	  {
//	    GeoPoint gp = null;
//	    try
//	    {
//	      /* 當Location存在 */
//	      if (location != null)
//	      {
//	        double geoLatitude = location.getLatitude()*1E6;
//	        double geoLongitude = location.getLongitude()*1E6;
//	        gp = new GeoPoint((int) geoLatitude, (int) geoLongitude);
//	      }
//	    }
//	    catch(Exception e)
//	    {
//	      e.printStackTrace();
//	    }
//	    return gp;
//	  }
//	public String getAddressbyGeoPoint(GeoPoint gp)
//	  {
//	    String strReturn = "";
//	    try
//	    {
//	      /* 當GeoPoint不等於null */
//	      if (gp != null)
//	      {
//	        /* 建立Geocoder物件 */
//	        Geocoder gc = new Geocoder(MainFunctionActivity.this, Locale.getDefault());
//	        
//	        /* 取出地理座標經緯度 */
//	        double geoLatitude = (int)gp.getLatitudeE6()/1E6;
//	        double geoLongitude = (int)gp.getLongitudeE6()/1E6;
//	        
//	        /* 自經緯度取得地址（可能有多行地址） */
//	        List<Address> lstAddress = gc.getFromLocation(geoLatitude, geoLongitude, 1);
//	        StringBuilder sb = new StringBuilder();
//	        
//	        /* 判斷地址是否為多行 */
//	        if (lstAddress.size() > 0)
//	        {
//	          Address adsLocation = lstAddress.get(0);
//
//	          for (int i = 0; i < adsLocation.getMaxAddressLineIndex(); i++)
//	          {
//	            sb.append(adsLocation.getAddressLine(i)).append("\n");
//	          }
//	          sb.append(adsLocation.getLocality()).append("\n");
//	          sb.append(adsLocation.getPostalCode()).append("\n");
//	          sb.append(adsLocation.getCountryName());
//	        }
//	        
//	        /* 將擷取到的地址，組合後放在StringBuilder物件中輸出用 */
//	        strReturn = sb.toString();
//	      }
//	    }
//	    catch(Exception e)
//	    {
//	      e.printStackTrace();
//	    }
//	    return strReturn;
//	  }
    
    /*----------------------------幫助Toast在Thread中打出訊息-------------------------- */
	 public void showToast(final String msg) {
		  handler.post(new Runnable() {
		   public void run() {
		    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		   }
		  });
	}
	 
	 /*-----------------------------用來打出alertDialog-------------------------------------*/
	 private void ShowMsgDialog(String title, String Msg, String btnTitle, final int flag)
	 {
		  android.app.AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
		  MyAlertDialog.setTitle(title);
		  MyAlertDialog.setMessage(Msg);
		  DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener()
		  {
				 public void onClick(DialogInterface dialog, int which) {
				  //如果不做任何事情 就會直接關閉 對話方塊
					 if(flag == 1){	//flag == 1則表示進入
						   
					 }
					 if(flag == 2){	//flag == 2則表示進入
						    
					 }
					    
				 }
		  };;
		  MyAlertDialog.setNeutralButton(btnTitle,OkClick );
		  MyAlertDialog.show();
	 }
	 
	 
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
				//TextView01.append(tmp + "\n");
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
					int serverPort = 8079;
					clientSocket = new Socket(serverIp, serverPort);
					
					// 取得網路輸入串流
					BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
					
					// 當連線後
					while (clientSocket.isConnected()) {
						showToast("Main: Link sucessful!");
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
								/*------------------------------------------------------------------------------------------------------------------
								 * 底下就是上面說到的Thread不能隨意控制主程序的畫面的問題
								 * 所以我們用另一種方式去處理...用適合在Thread中打Toast的方法
								 * ------------------------------------------------------------------------------------------------------------------*/
								showToast("Main: Some one say help!");
							}else if(tmp.contains("2234567")){
								//填入功能
							}else{
								// 顯示新的訊息
								/* 因為在這裡的更新手機端的Text view的方式是去做一個共有的String(tmp)
								 * 所以依此類推我們為了能自由的控制這種更新Text view的方式
								 * 我在下面幫tmp去增加一些字後再由Function去打訊息出來
								 * 
								 * 但他打完似乎就會把tmp刷乾淨(待考證)，所以就不用再把tmp弄成null
								 * */
								tmp = tmp+"\nmHandler.post(updateText) is called.";
								//mHandler.post(updateText);
								//testMsgShow("mHandler.post(updateText) is called.\n");
								//TextView01.append("mHandler.post(updateText) is called.\n");
								//Toast.makeText(getApplicationContext(), "mHandler.post(updateText) is called", Toast.LENGTH_SHORT).show();
							}
							// 顯示新的訊息
							//mHandler.post(updateText);
							//Toast.makeText(getApplicationContext(), "Someone say help!",Toast.LENGTH_SHORT).show();
							
					}
					
//					if(switchFlag == true){
//						Thread.sleep(9999);
//					}

				} catch (IOException e) {
					
				}
			}
		};
		
		
		@Override
		protected void onResume()
		{
			showToast("Resume");
			
			/*------GPS reset---------------------------------*/
			if (mLocationManager != null) {            
	    		mLocationManager.requestLocationUpdates(                
	    				LocationManager.GPS_PROVIDER,                
	    				0,                
	    				0,                
	    				mLocationListener);        
	    		}            

			super.onResume();
			
		}
		
		
		/*-------------------------------------GPS---------------------------------*/
		//在Pause階段關閉mLocationListener介面，不再獲得地理位置的更新資料
	    @Override    
	    protected void onPause() {        
	    	if (mLocationManager != null) {            
	    		mLocationManager.removeUpdates(mLocationListener);        
	    		}                
	    	super.onPause();    
	    }
	    //實作mLocationListener介面
	    public LocationListener mLocationListener = new LocationListener() 
	    { 
	    	//GPS位置資訊被更新
	    	public void onLocationChanged(Location location) {        
//	    		mTextView01.setText("緯度-Latitude：  " + String.valueOf(location.getLatitude()));
//	    		mTextView02.setText("經度-Longitude：  " + String.valueOf(location.getLongitude()));
	    		locationStr = "(" + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) +")";
//	    		showToast(locationStr);

	    		}
	    	public void onProviderDisabled(String provider) {
	    	
	    	}     
	    	public void onProviderEnabled(String provider) {    
	    	
	    	}  
	    	//GPS位置資訊的狀態被更新
	    	public void onStatusChanged(String provider, int status, Bundle extras) {        
	    		switch (status) {        
	    			case LocationProvider.AVAILABLE:            
	    				Log.v("Status", "AVAILABLE");            
	    				break;        
	    			case LocationProvider.OUT_OF_SERVICE:            
	    				Log.v("Status", "OUT_OF_SERVICE");            
	    				break;        
	    			case LocationProvider.TEMPORARILY_UNAVAILABLE:            
	    				Log.v("Status", "TEMPORARILY_UNAVAILABLE");            
	    				break;        
	    				}    
	    		}
	    };
		
	 
}
