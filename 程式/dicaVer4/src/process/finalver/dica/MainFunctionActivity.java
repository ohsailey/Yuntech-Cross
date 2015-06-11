/**
 * �b�o�ӵe����TabHost�H�Ψ�L����|�ݨ쪺�`�n���s
 * 
 * �e�����ڭ̷|�b�I�����P���A���s�u���ʧ@
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
	
	/*--------------------�a��---------------------------------------------*/
//	private Location currentLocation;
//	private LocationManager locationManager;
//	private boolean enableTool;
//	private String strLocationPrivider;
//	Location location = null;
//	private GeoPoint currentGeoPoint;
	
	//e14_1:locationGPS
	private LocationManager mLocationManager;
	String locationStr = "666";
	
	/*----------------------------��Ʈw����----------------------------*/
	MySQLiteHelper myHelper;	//��Ʈw���U���O���󪺤ޥ�
	
	/*----------------------------�����s�u����-------------------------*/
	Socket clientSocket;	// �Ȥ��socket
	String tmp;				// �Ȧs��r�T��
	String userName = "";	//�]�w�ǰe�����ɧA��IP�W��
	
    /** Called when the activity is first created. */  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        
        /*--------------------------------------------------------------
         * ���U�o��O�������A�C�A�b�[�J���e�Х�import window
         * �o��n�bsetContentView�e���[�A���M�|���
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
        
        /*------------�]�wTab----------------------*/
        addOneTab();  
        addTwoTab();  
        //addThreeTab();  //���٬O����۳]�u�㭶��������
        addFourTab();  
        
        
        /*----------�]�w��l���------------------*/
        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//��lDatabase
        String name = text1.getText().toString();
        String result = queryData(myHelper);	//�V��Ʈw���d�߸��
        userName = result;
        name += result;
        text1.setText(name);
        
        /*------------OWN GPS SHOW--------------------*/
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        

      //��ܸӤ����IMEI�X
        try{
        	TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
    		//String IMEI = "IMEI: " + tm.getDeviceId() + "\n";
            
    		//String idTopic = text2.getText().toString();
    		char charArray[] = new char[10];
            String  idName = tm.getDeviceId();	//�V��Ʈw���d�߸��
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
		

        
        /*--------------------------- �������l��------------------------------------------------*/
     	final Thread t = new Thread(readData);	//Ū���
     	try{
			  //do what you want to do before sleeping
			  Thread.currentThread().sleep(2000);//sleep for 1000 ms
			  //do what you want to do after sleeptig
		}
			catch(InterruptedException ie){
			//If this thread was intrrupted by nother thread 
		}
     	
        t.start();
        
        
//        int whileCounter = 1;	//�Ψ���while loop�@���@�@�����ʧ@
//        while(clientSocket.isConnected()){
//        	 if(clientSocket.isConnected()){
//        		 
//        		 showToast("����!���A���w�s�u!");
//        		 whileCounter = 0;
////     			BufferedWriter bw;
////     			
////     			try {
////     				// ���o������X��y
////     				bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
////     				
////     				// �g�J�T���A���player n:says...
////     				bw.write(userName + ":" + "2234567" + "\n");
////     				
////     				// �ߧY�o�e
////     				bw.flush();
////     			} catch (IOException e) {
////     				
////     			}
////     			// �N��r����M��
////     			//EditText02.setText("");
//     		}
//        	
//        }
        

        /*----------------------------Button�]�m��-----------------------------------------------*/
        Button b1 = (Button) findViewById(R.id.button1);   	
    	 b1.setOnLongClickListener(new Button.OnLongClickListener() { 
		        @Override
		        public boolean onLongClick(View v) {
		            // TODO Auto-generated method stub
		        	
		        	try{
		        		showToast("���T���ǰe!");
			        	// �p�G�w�s���h
						if(clientSocket.isConnected()){
							
							BufferedWriter bw;
							
							try {
								// ���o������X��y
								bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
								
								// �g�J�T���A���player n:says...
								bw.write(userName + ":" +"1134567"+ "\n");
								
								// �ߧY�o�e
								bw.flush();
								
								showToast("Emerging messag sending sucess!");
							} catch (IOException e) {
								
							}
							// �N��r����M��
							//EditText02.setText("");
						}
			        	
						
//						showToast(locationStr);
		        	}catch(NullPointerException e){
		        		//showToast("Exception: " + e);
		        		showToast("Error linking! Please check linking status!");
		        	}catch(Exception e){
		        		showToast("Unkown error: " + e);
		        		//showToast("���~�s�u!�нT�{�s�u���A!");
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
    
    /*----------------------------------��Ʈw�ɯžާ@��ܰ�------------------------------*/
  	//��k�G�q��Ʈw���d�߸��
  	public String queryData(MySQLiteHelper myHelper){
  		String result="";
  		SQLiteDatabase db = myHelper.getReadableDatabase();		//��o��Ʈw����
  		Cursor cursor = db.query(" user_info", null, null, null, null, null, "id asc");	//�d�ߪ����
  		int nameIndex = cursor.getColumnIndex("name");	//���name�檺����
  		for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){	//�M�����G���A�������
  			result = result + cursor.getString(nameIndex);
  			/*result = result + cursor.getInt(ageIndex)+"    ";
  			result = result + cursor.getString(careerIndex)+"    ";
  			result = result + cursor.getString(major1Index)+"    ";
  			result = result + cursor.getString(major2Index)+"    ";
  			result = result + cursor.getString(major3Index)+"     \n";*/
  		}
  		cursor.close();		//�������G��
  		db.close();			//������Ʈw����
  		return result;
  	}
    /*End-------------------��Ʈw�ާ@--------------------------*/
    
    
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
//			new AlertDialog.Builder(MainFunctionActivity.this).setTitle("�a�Ϥu��").setMessage("�z�|���}�ҩw��A�ȡA�n�e���]�w�����Ұʩw��A�ȶܡH")
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
//							Toast.makeText(MainFunctionActivity.this, "���}�ҩw��A�ȡA�L�k�ϥΥ��u��!!", Toast.LENGTH_SHORT).show();
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
	
	
	/*--------------------------Map����------------------------------*/
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
//	 /* ���������m�ܧ�ɡA�Nlocation�ǤJ��s��UGeoPoint��MapView */
//	  private void processLocationUpdated(Location location)
//	  {
//	    /* �ǤJLocation����A���oGeoPoint�a�z�y�� */
//	    currentGeoPoint = getGeoByLocation(location);
//	    	    
//	    text2.setText
//	    (
//	      getResources().getText(R.string.str_my_location).toString()+"\n"+
//	      /* �����ǲߡG���XGPS�a�z�y�СG */
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
//	      /* ��Location�s�b */
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
//	      /* ��GeoPoint������null */
//	      if (gp != null)
//	      {
//	        /* �إ�Geocoder���� */
//	        Geocoder gc = new Geocoder(MainFunctionActivity.this, Locale.getDefault());
//	        
//	        /* ���X�a�z�y�иg�n�� */
//	        double geoLatitude = (int)gp.getLatitudeE6()/1E6;
//	        double geoLongitude = (int)gp.getLongitudeE6()/1E6;
//	        
//	        /* �۸g�n�ר��o�a�}�]�i�঳�h��a�}�^ */
//	        List<Address> lstAddress = gc.getFromLocation(geoLatitude, geoLongitude, 1);
//	        StringBuilder sb = new StringBuilder();
//	        
//	        /* �P�_�a�}�O�_���h�� */
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
//	        /* �N�^���쪺�a�}�A�զX���bStringBuilder���󤤿�X�� */
//	        strReturn = sb.toString();
//	      }
//	    }
//	    catch(Exception e)
//	    {
//	      e.printStackTrace();
//	    }
//	    return strReturn;
//	  }
    
    /*----------------------------���UToast�bThread�����X�T��-------------------------- */
	 public void showToast(final String msg) {
		  handler.post(new Runnable() {
		   public void run() {
		    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		   }
		  });
	}
	 
	 /*-----------------------------�Ψӥ��XalertDialog-------------------------------------*/
	 private void ShowMsgDialog(String title, String Msg, String btnTitle, final int flag)
	 {
		  android.app.AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
		  MyAlertDialog.setTitle(title);
		  MyAlertDialog.setMessage(Msg);
		  DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener()
		  {
				 public void onClick(DialogInterface dialog, int which) {
				  //�p�G��������Ʊ� �N�|�������� ��ܤ��
					 if(flag == 1){	//flag == 1�h��ܶi�J
						   
					 }
					 if(flag == 2){	//flag == 2�h��ܶi�J
						    
					 }
					    
				 }
		  };;
		  MyAlertDialog.setNeutralButton(btnTitle,OkClick );
		  MyAlertDialog.show();
	 }
	 
	 
	 /*---------------------------------------------------------------------------------------------------------------------------------------------------------
	  * �������A���s�u��
	  * 
	  * 
	  * 
	  * 
	  * 
	  * */
	 
	// ��ܧ�s�T��
		private Runnable updateText = new Runnable() {
			public void run() {
				// �[�J�s�T���ô���
				//TextView01.append(tmp + "\n");
			}
		};
		
		// ���o�������
		private Runnable readData = new Runnable() {
			public void run() {
				// server�ݪ�IP
				InetAddress serverIp;
				try {
					// �H���w(�����q����)IP��Server��
					serverIp = InetAddress.getByName("140.125.45.116");
					int serverPort = 8079;
					clientSocket = new Socket(serverIp, serverPort);
					
					// ���o������J��y
					BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
					
					// ��s�u��
					while (clientSocket.isConnected()) {
						showToast("Main: Link sucessful!");
						// ���o�����T��
						tmp = br.readLine();
						
						// �p�G���O�ŰT���h
						/* ��Ū�K�X��...
						 * �o�̭n��contains�����equal��indexof
						 * �]�����A���ݥ��X�Ӫ��T�����O��ª��K�X�L���X���٬O��Ӥ���ݪ�"player01: XXXXXXXXXXX"(���A���ݦ������������i�H�ѷ�)
						 * �ҥH�n�Υ]�t�r��A�Ӥ��O�r���]���O�۵�
						 * */
						if(tmp!=null)
							if(tmp.contains("1134567")){
								/*------------------------------------------------------------------------------------------------------------------
								 * ���U�N�O�W�����쪺Thread�����H�N����D�{�Ǫ��e�������D
								 * �ҥH�ڭ̥Υt�@�ؤ覡�h�B�z...�ξA�X�bThread����Toast����k
								 * ------------------------------------------------------------------------------------------------------------------*/
								showToast("Main: Some one say help!");
							}else if(tmp.contains("2234567")){
								//��J�\��
							}else{
								// ��ܷs���T��
								/* �]���b�o�̪���s����ݪ�Text view���覡�O�h���@�Ӧ@����String(tmp)
								 * �ҥH�̦������ڭ̬��F��ۥѪ�����o�ا�sText view���覡
								 * �ڦb�U����tmp�h�W�[�@�Ǧr��A��Function�h���T���X��
								 * 
								 * ���L�������G�N�|��tmp�갮�b(�ݦ���)�A�ҥH�N���ΦA��tmp�˦�null
								 * */
								tmp = tmp+"\nmHandler.post(updateText) is called.";
								//mHandler.post(updateText);
								//testMsgShow("mHandler.post(updateText) is called.\n");
								//TextView01.append("mHandler.post(updateText) is called.\n");
								//Toast.makeText(getApplicationContext(), "mHandler.post(updateText) is called", Toast.LENGTH_SHORT).show();
							}
							// ��ܷs���T��
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
		//�bPause���q����mLocationListener�����A���A��o�a�z��m����s���
	    @Override    
	    protected void onPause() {        
	    	if (mLocationManager != null) {            
	    		mLocationManager.removeUpdates(mLocationListener);        
	    		}                
	    	super.onPause();    
	    }
	    //��@mLocationListener����
	    public LocationListener mLocationListener = new LocationListener() 
	    { 
	    	//GPS��m��T�Q��s
	    	public void onLocationChanged(Location location) {        
//	    		mTextView01.setText("�n��-Latitude�G  " + String.valueOf(location.getLatitude()));
//	    		mTextView02.setText("�g��-Longitude�G  " + String.valueOf(location.getLongitude()));
	    		locationStr = "(" + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()) +")";
//	    		showToast(locationStr);

	    		}
	    	public void onProviderDisabled(String provider) {
	    	
	    	}     
	    	public void onProviderEnabled(String provider) {    
	    	
	    	}  
	    	//GPS��m��T�����A�Q��s
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
