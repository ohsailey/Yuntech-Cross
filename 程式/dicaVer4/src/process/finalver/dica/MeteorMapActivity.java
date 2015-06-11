package process.finalver.dica;

/* ���{���O���|�e�ϵ{��(�쥻)�A�ѪL�T�����H�X�R
 * 
 * �`�N!�A�C�������ʰT����Ƶ��ʧ@�Х[�Wtry&catch
 * 
 * �\��:
 * 1. ���|�W��
 * 2. �����ثe�������A
 * 3. ��ܥثe��m
 * 
 * ��������P���B:
 * 1. ���|�W���`�I�i�H�@���[(���ثe�̦h8��...�o�OGoogle direction������...���i�H��})
 * 2. ICON�`�I�Ϥ��i�H�̷Ӧۤv���w�@���[(�٨S����...�u�O�i�H�����A���ثe���O�ܻݭn)
 * 
 * �i�H�ǲߤ��B:
 * 1. AsyncTask
 * 2. String to URL to URI to solve encode problem
 * 3. �W�[�Ϥ�Layut
 * 4. �W�[���|Layout
 * 5. MessageFormat
 * 6. Google direction api �p��B�@�H�Χ⪱google direction url
 * 7. try&catch is important
 * 
 * �`�N:
 * ���{���M�Ω�Android���z�������
 * �]�����O��3.0UP�����������b�s�u�W�[�j�s�u
 * �ҥH�|�y���D�ϰT������L�s�u�T���X��
 * �ҥH���T�����k���ӬO�A�P�@��3.0�άO4.0���M�׬ۮe�󥭪O
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
	GeoPoint endPoint;	//�D�ϥت��ݦ�m
	GeoPoint pointCurrent ;
	private List<GeoPoint> _helperPoints = new ArrayList<GeoPoint>();		//���U�̮y�СA�Φb��ܼаO�W
	private List<GeoPoint> _tagPoints = new ArrayList<GeoPoint>();		//����q�L���y�СA�Φb��ܼаO�W
	private List<GeoPoint> _requestPoints = new ArrayList<GeoPoint>();		//�D�Ϫ̪��y�СA�Φb��ܼаO�W
	private List<String>_requesterTA = new ArrayList<String>();	//�@�ˬ������D�򤺮e
	private List<GeoPoint> _pathPoints = new ArrayList<GeoPoint>();		//�g�L���`�I���y�СA�Φb���|�W���W
	private List<GeoPoint>_ambulanceDepotPoints = new ArrayList<GeoPoint>();		//��ϯ��`�I���y�СA�Φb��ܼаO�W
	private List<String>_ambulanceDepotTA = new ArrayList<String>();		//��ϯ��`�I�����D��аO���e�A���Ƭ����D�_�Ƭ����e�A.get(i)����i/2��points list��.get(n)
	//private List<GeoPoint>_baseStationPoints = new ArrayList<GeoPoint>();		//�`�I���y��
	final GeoPoint pointDefault = new GeoPoint((int) (0 * 1000000),(int)(0 * 1000000));	//�Φb�eICON���y��
	
	Handler handler = new Handler();
	
	public static Handler mHandler = new Handler();
	
	/*----------------------------��Ʈw����----------------------------*/
	MySQLiteHelper myHelper;	//��Ʈw���U���O���󪺤ޥ�
	
	/*----------------------------�����s�u����-------------------------*/
	Socket clientSocket;	// �Ȥ��socket
	String tmp;				// �Ȧs��r�T��
	String userName = "";	//�]�w�ǰe�����ɧA��IP�W��
	Thread t;	//��l����� 
	
	/*----------------------------�e������----------------------------------*/
	TextView textview1;
	EditText EditText01;	// ��r���
	int quickMsgFlag = 0;	//0�h���alertDialogList���аO�a�ϡA1�h��ܬ��ֳt�T��

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findControl();
		
		
		
		/*----------�]�w��l���------------------*/
        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//��lDatabase
        String result = queryData(myHelper);	//�V��Ʈw���d�߸��
        userName = result;
        
        
        /**
         * ���Ҥ���o�̭n�θ�Ʈw�h�O��!!!!!!!!*/
        /*------------�]�w�ڭn�X�{���R�A�y��(��ϯ��B��L�Ϩa�̮y�СB�������q�y��)-------------------------*/
        GeoPoint tempPoint;
        /*----------------------------------�������ɥήy��------------------------------------------*/
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
//		/*----------------------------------�d����ɥήy��------------------------------------------*/
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
		
		/*----------------------------------------------���L�a��-------------------------------------------------------------*/
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.70053 * 1000000),(int)(120.53258 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.69472 * 1000000),(int)(120.52468 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.68541 * 1000000),(int)(120.53966 * 1000000)));
//		_ambulanceDepotPoints.add(tempPoint =  new GeoPoint((int) (23.69422 * 1000000),(int)(120.54384 * 1000000)));
//		
		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.69544 * 1000000),(int)(120.52834 * 1000000)));
		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.68797 * 1000000),(int)(120.53078 * 1000000)));
		_tagPoints.add(tempPoint =  new GeoPoint((int) (23.70191 * 1000000),(int)(120.52924 * 1000000)));
		/*-------------�]�w�y�а�END--------------------------------------------------*/
		if(!_tagPoints.isEmpty()){	//�h�P�_�O�_�S���H���XTAG�A�S�����ܴN���έ����F
			for(int i=0;i<_tagPoints.size();i++){
				showIcon(_tagPoints.get(i),"Here can't pass","Go other ways",2);		//2��ܥ��X�������q�аO
			}
		}
		if(!_ambulanceDepotPoints.isEmpty()){
			for(int i=0;i<_ambulanceDepotPoints.size();i++){
				showIcon(_ambulanceDepotPoints.get(i),"Ambulance Depot","Ambulance Depot base",3);		//3��ܥ��X�D�ϯ��аO
			}
		}
		if(!_helperPoints.isEmpty()){
			for(int i=0;i<_helperPoints.size();i++){
				showIcon(_helperPoints.get(i),"People","Relief worker",4);		//4��ܥ��X��L�Ϩa�аO
			}
		}
		
        /*--------------------------- �������l��------------------------------------------------*/
     	t = new Thread(readData);	//Ū���
     	try{
			  //do what you want to do before sleeping
			  Thread.currentThread().sleep(1000);//sleep for 1000 ms
			  //do what you want to do after sleeptig
		}
			catch(InterruptedException ie){
			//If this thread was intrrupted by nother thread 
		}
        t.start();
        
        /*�ګʦLù�L����]�O�]���s�u���D�|���ѨM*/
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
				quickMsgFlag = 0;//0��ܭnlist��ܼаO�M��
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
        /*--------------------------a�]�w�e����------------------------------------*/
        textview1 = (TextView) findViewById(R.id.textView1);
        
        
     // �]�w���s���ƥ�
    	Button buttonSned=(Button) findViewById(R.id.btnSend);
    	buttonSned.setOnClickListener(new Button.OnClickListener() {		
 			// ����U���s���ɭ�Ĳ�o�H�U����k
 			public void onClick(View v) {
 				// �p�G�w�s���h
 				if(clientSocket.isConnected()){
 					
 					BufferedWriter bw;
 					
 					try {
 						// ���o������X��y
 						bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
 						
 						// �g�J�T���A���player n:says...
 						bw.write(userName + ":" + EditText01.getText() + "\n");
 						
 						// �ߧY�o�e
 						bw.flush();
 					} catch (IOException e) {
 						
 					}
 					// �N��r����M��
 					EditText01.setText("");
 				}
 			}
 		});
    	
    	// �]�w���s���ƥ�
    	Button btnMsg=(Button) findViewById(R.id.btnMsg);
    	btnMsg.setOnClickListener(new Button.OnClickListener() {		
 			// ����U���s���ɭ�Ĳ�o�H�U����k
 			public void onClick(View v) {
 				quickMsgFlag = 1;	//1�h��ܬ���list������Y�T���M��
 				ShowAlertDialogAndList();
 				
 				quickMsgFlag = 0;	//�^�_
 			}
 		});
    	
        
	}	//End onCreate
	
	
	
	
	
	
	
    /*�������ĤG�ӭ���*/
    public void selectVersionPage(){
    	setContentView(R.layout.test);
    	Button b1 = (Button) findViewById(R.id.button1);
    	b1.setOnClickListener(new Button.OnClickListener()
        {
			@Override
			public void onClick(View v) {
				try{
	        		showToast("Emerging message send!");
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
							
							showToast("Emerging message send sucessful!");
						} catch (IOException e) {
							
						}
						// �N��r����M��
						//EditText02.setText("");
					}
	        	}catch(NullPointerException e){
	        		//showToast("Exception: " + e);
	        		showToast("Error linking! Please check linking status!");
	        	}catch(Exception e){
	        		showToast("Unkown error: " + e);
	        		//showToast("���~�s�u!�нT�{�s�u���A!");
	        	}
				
				
				
				
			}
        });

    }
	
	
	
	
	
	
	
	/*----------------------------------��Ʈw�ɯžާ@��ܰ�------------------------------*/
  	//��k�G�q��Ʈw���d�߸��
  	public String queryData(MySQLiteHelper myHelper){
  		String result="";
  		SQLiteDatabase db = myHelper.getReadableDatabase();		//��o��Ʈw����
  		Cursor cursor = db.query(" user_info", null, null, null, null, null, "id asc");	//�d�ߪ����
  		int nameIndex = cursor.getColumnIndex("name");	//���name�檺����
  		for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){	//�M�����G���A�������
  			result = result + cursor.getString(nameIndex);	//�^�ǦW�r�N�n
  		}
  		cursor.close();		//�������G��
  		db.close();			//������Ʈw����
  		return result;
  	}
    /*End-------------------��Ʈw�ާ@--------------------------*/

  	
	/*------------------------------------------------------------------------------------------------
	 * 
	 * 
	 * �����e��Ĳ�I��
	 * 
	 * 
	 * */
  	//���U�O���������שάO���ʵ����h�\��禡
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
////    	    ShowMsgDialog("���D","�z�T�w�n�аO�o�̬����q?",1);
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
  //���U�O���@�륿�`�H�g���������ʵ����h�\��禡�A�õL��������
//	@Override
//    // �Q�� MotionEvent �B�zĲ���{��
//    public boolean onTouchEvent(MotionEvent event) {
//		
//		
//		 //float touchX = event.getX();       // Ĳ���� X �b��m
//	     //float touchY = event.getY() - 50;  // Ĳ���� Y �b��m
//	     
//	        // �P�_Ĳ���ʧ@
//	        switch( event.getAction() ) {
//	      
//	            case MotionEvent.ACTION_DOWN:  // ���U
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
//	            case MotionEvent.ACTION_MOVE:  // �즲����
//	       
//	                
//	                break;
//	                
//	            case MotionEvent.ACTION_UP:  // ��}
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
	 * 																		Google map��
	 * -------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * */

	private void init()
	{
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{

			new AlertDialog.Builder(MeteorMapActivity.this).setTitle("Maop tool").setMessage("U still not enable location focus, r u want to enable location�H")
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
		
		/*----------------���ծy��---------------------------*/
		//final GeoPoint pointA = new GeoPoint((int) (23.707906 * 1000000),(int)(120.436581 * 1000000));	//�Φb�eICON���y�СAfor ����
		//final GeoPoint pointB = new GeoPoint((int) (23.680043 * 1000000),(int)(120.476267 * 1000000));	//�Φb�eICON���y�СAfor����
		//final GeoPoint pointC = new GeoPoint((int) (23.703788 * 1000000),(int)(120.399790 * 1000000));	//�Φb�eICON���y�СAfor����
		//
		
		final GeoPoint pointA = new GeoPoint((int) (23.55755 * 1000000),(int)(120.42378 * 1000000));	//�Φb�eICON���y�СAfor�d��
		final GeoPoint pointB = new GeoPoint((int) (23.55423 * 1000000),(int)(120.43249 * 1000000));	//�Φb�eICON���y�СAfor�d��
		final GeoPoint pointC = new GeoPoint((int) (23.703788 * 1000000),(int)(120.399790 * 1000000));	//�Φb�eICON���y�СAfor�d��

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, MeteorMapActivity.this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, MeteorMapActivity.this);
		final GeoPoint point = new GeoPoint((int)(23.706783 * 1000000),(int)(120.5397066 * 1000000));	//�Φb�eTemp�ؼбϴ����y��
		
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
				//new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(), "��鿤���c�����ظ��@�q267��");
				//new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(), "���L�������w�˨��@�F27��");
				//new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "," + point.getLatitudeE6() + "," + point.getLongitudeE6());
				new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(),a ,b, c);
				pointCurrent = new GeoPoint((int)(currentLocation.getLatitude() * 1000000),(int)(currentLocation.getLongitude() * 1000000));	//�Φb�eICON���y��
				//_tagPoints.set(0, pointCurrent);	//��s�������q���y��
				_points.add(pointCurrent);
				_points.add(pointA);
				_points.add(pointB);
//				_points.add(pointC);
				double distance = 0.0;
				
				distance += GetDistance(_points.get(0), _points.get(1)); 
				distance += GetDistance(_points.get(1), _points.get(2)); 
//				distance += GetDistance(_points.get(2), _points.get(3)); 
				
				//distance = Math.round(distance);
				
				/*---------------���p���I4��A�䤤#�O�N����ܼƦr�A��0�h�O��ܼƦr�p�G�S���Ʀr�h���0*/
				DecimalFormat df = new DecimalFormat( "#.0000"); 
				//System.out.println( df.format( distance ) ); 
				
				showToast("Dis: "+df.format( distance )+" KM");
				
				//new GoogleDirection().execute(b, c);
				//showToast(currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "," +a);
				endPoint = point;	//�Ψӵe�ؼбϴ��H���������I
								
			}
		});
	}

	private void enableMyLocation()
	{
		// �w���I
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
	 * 																			Activity�ͩR�g����
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
		
		/*--------�C��resume�N���ճs�u------------*/
		try{
    		
        	// �p�G�w�s���h
			if(clientSocket.isConnected()){
				
				BufferedWriter bw;
				
				try {
					
					showToast("Server linking sucessful.");
					// ���o������X��y
					bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
					
					// �g�J�T���A���player n:says...
					//bw.write(userName + ":" +"0034567"+"Test"+ "\n");
					bw.write("Test\n");
					
					// �ߧY�o�e
					bw.flush();
					
					
				} catch (IOException e) {
					showToast("Unkown error: " + e);
				}
				// �N��r����M��
				//EditText02.setText("");
			}

    	}catch(NullPointerException e){
    		//showToast("Exception: " + e);
    		showToast("Error link! Please check linking status!");
    	}catch(Exception e){
    		showToast("Unkown error: " + e);
    		//showToast("���~�s�u!�нT�{�s�u���A!");
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
					textview1.append(tmp + "\n");
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
							int serverPort = 8080;
							clientSocket = new Socket(serverIp, serverPort);
							
							// ���o������J��y
							BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
							
							// ��s�u��
							while (clientSocket.isConnected()) {
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
										//Toast.makeText(getApplicationContext(), "Someone say help!", Toast.LENGTH_SHORT).show();
										//tmp = "";
										//testMsgShow("tmp.equals(11356946) is called.\n");
										//tmp = tmp+"\nmHandler.post(Test) is called.";
										//mHandler.post(updateText);
										
										/*
										 * ���U�N�O�W�����쪺Thread�����H�N����D�{�Ǫ��e�������D
										 * �ҥH�ڭ̥Υt�@�ؤ覡�h�B�z...�ξA�X�bThread����Toast����k
										 * */
										showToast("Some one say help!");
										
									}else if(tmp.contains("2234567")){
										char tmpChar;
										tmpChar = tmp.charAt(7);
										//if((char)tmpChar < 10)	//����...
											userName = "player0" + tmpChar;
										//mHandler.post(updateText);
									}else{
										// ��ܷs���T��
										/* �]���b�o�̪���s����ݪ�Text view���覡�O�h���@�Ӧ@����String(tmp)
										 * �ҥH�̦������ڭ̬��F��ۥѪ�����o�ا�sText view���覡
										 * �ڦb�U����tmp�h�W�[�@�Ǧr��A��Function�h���T���X��
										 * 
										 * ���L�������G�N�|��tmp�갮�b(�ݦ���)�A�ҥH�N���ΦA��tmp�˦�null
										 * */
										//tmp = tmp+"\nmHandler.post(updateText) is called.";
										mHandler.post(updateText);
										//testMsgShow("mHandler.post(updateText) is called.\n");
										//TextView01.append("mHandler.post(updateText) is called.\n");
										//Toast.makeText(getApplicationContext(), "mHandler.post(updateText) is called", Toast.LENGTH_SHORT).show();
									}
									// ��ܷs���T��
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
	 * �򥻤W�N�O�ڥX�F�@��AsyncTask����n�]�n�T�ӿ�J�ܼƪ���ƫ��A
	 * �Ӭ���n�T�Ӫ���]�h�O�]���U�����T�ӱ`�n�禡(doInBackground, onProgressUpdate, onPostExecute)�ݭn��J�Ѽ�
	 * */
	
			
			
	/*-------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * -------------------------------------------------------------------------------------------------------------------
	 * 																					�W�����|��
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
		/* �o�̧ڭ̥i�H�ݨ�(String... params)
		 * �o�N��O���|��@�}�l��J���Ѽƥ����ꦨ�}�C
		 * �ҥH��new GoogleDirection().execute(currentLocation.getLatitude() + "," + currentLocation.getLongitude(), "���L�������w�˨��@�F27��");
		 * �i�H�o�{�o���O�Ѩ�զr��զ�(�{����m�y��, ���d���a�})
		 * �ҥH��i�ӲĤ@�ӱ`�n�٬O���ɭԥL�|��o��Ӧr��ꦨ�}�C�s��params[n], n����J���F��Ӽ�
		 * 
		 * */
		{
			if (params.length < 0)
				return null;
			/*
			_from = params[0];	//�_�I
			_to = params[1];	//���I
			_pass = params[2];	//�g�L
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
			
			/* �o��O�h�B�zGoogle direction api��BUG
			 * �]���b�@�몺URL�ǰe�W�A�p�G�O�ΥH�e����k�O�Φr�ꪺ��
			 * �ǲΪ��@�k�|��"|"�o�ӲŸ��X�{���~�s�X
			 * �ҥH�ڧ�쪺�Ѫk�O��uri�����u
			 * �ҥH����r���নURL�Φ��A��A��URL�নURI
			 * �M�ᨫHTTPPOST�����N������URI
			 * �Ѱ�BUG
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
		
		
		/* �ΨӰ��ѽX�ʧ@
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

		protected void onPostExecute(List<GeoPoint> points)	//�e�u�A�e��
		{
			if (points.size() > 0)
			{
				LineItemizedOverlay mOverlay = new LineItemizedOverlay(points);

				List<Overlay> overlays = mapView.getOverlays();
				overlays.clear();
				overlays.add(mylayer);
				overlays.add(0, mOverlay);
				
				
				/*���U�C���h���e�һݭn���аO
				 * �ثe�����аO: ��ϯ��B��L�Ϩa�H�B�������q�аO�B�Ϩa�ؼ�
				 * showIcon��flag:
				 * ��1���ɭԪ�ܭn�e�X���a�H(�Ϩa�ؼ�)
				 * ��2���ɭԪ�ܭn�e�X�������q
				 * ��3���ɭԪ�ܭn�e�X��ϯ�
				 * ��4���ɭԪ�ܭn�e�X��L�Ϩa�H
				 * 
				 * */
				//String[] token = params[1].split(",");
				showIcon(endPoint,"Helper1","Help~",1);		//1��ܥ��X�D�ϥؼЪ���m
				if(!_tagPoints.isEmpty()){	//�h�P�_�O�_�S���H���XTAG�A�S�����ܴN���έ����F
					for(int i=0;i<_tagPoints.size();i++){
						showIcon(_tagPoints.get(i),"Can't pass","Go other ways.",2);		//2��ܥ��X�������q�аO
					}
				}
				if(!_ambulanceDepotPoints.isEmpty()){
					for(int i=0;i<_ambulanceDepotPoints.size();i++){
						showIcon(_ambulanceDepotPoints.get(i),"Ambulance Depot","Ambulance Depot Base",3);		//3��ܥ��X�D�ϯ��аO
					}
				}
				if(!_helperPoints.isEmpty()){
					for(int i=0;i<_helperPoints.size();i++){
						showIcon(_helperPoints.get(i),"People","Relief worker.",4);		//3��ܥ��X�D�ϯ��аO
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
	
	
	/* ���o���I�����Z����method */
	  public double GetDistance(GeoPoint gp1,GeoPoint gp2)
	  {
	    double Lat1r = ConvertDegreeToRadians(gp1.getLatitudeE6()/1E6);
	    double Lat2r = ConvertDegreeToRadians(gp2.getLatitudeE6()/1E6);
	    double Long1r= ConvertDegreeToRadians(gp1.getLongitudeE6()/1E6);
	    double Long2r= ConvertDegreeToRadians(gp2.getLongitudeE6()/1E6);
	    /* �a�y�b�|(KM) */
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
	   * ���X�T����
	   * --------------------------------*/
	  
	  
	public void showToast(final String msg) {
		  handler.post(new Runnable() {
		   public void run() {
		    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		   }
		  });
	 }
	
	/*-----------------------------�Ψӥ��XalertDialog-------------------------------------*/
	 private void ShowMsgDialog(String title, String Msg, final int flag)
	 {
		  Builder MyAlertDialog = new AlertDialog.Builder(this);
		  MyAlertDialog.setTitle(title);
		  MyAlertDialog.setMessage(Msg);
		  DialogInterface.OnClickListener OkClick = new DialogInterface.OnClickListener()
		  {
				 public void onClick(DialogInterface dialog, int which) {
				  //�p�G��������Ʊ� �N�|�������� ��ܤ��
					 if(flag == 1){	//flag == 1�h��ܶi�J�аO�������q�A����!��showIcon��flag���P��!!!!���}!!!
						pointCurrent = new GeoPoint((int)(currentLocation.getLatitude() * 1000000),(int)(currentLocation.getLongitude() * 1000000));	//�Φb�eICON���y��
						_tagPoints.add(pointCurrent);
						 showIcon(pointCurrent,"Can't pass","Go other ways.", 2);	//2��ܬO���ثe��m���q
					 }
					 if(flag == 2){	//flag == 2�h��ܶi�J�\�୶��
						 //showIcon(_points.get(0),"Helper1","Help~~", 1);
					 }
					    
				 }
		  };;
		  
		  DialogInterface.OnClickListener CancelClick = new DialogInterface.OnClickListener()
		  {
				 public void onClick(DialogInterface dialog, int which) {
				  //�p�G��������Ʊ� �N�|�������� ��ܤ��
					 if(flag == 1){	//flag == 1�h��ܼаO�������q
						    
					 }
					 if(flag == 2){	//flag == 2�h��ܶi�J�\�୶��
						    
					 }
					    
				 }
		  };;
		  MyAlertDialog.setPositiveButton("OK",OkClick );
		  MyAlertDialog.setNeutralButton("Cancel",CancelClick );
		  MyAlertDialog.show();
	 }
	 
	 
	 /*-----------------------------�Ψӥ��XalertDialog-------------------------------------*/
	//����檺��ܤ��
	 final String[] titleListStrForBasestation = {"Tag to crash","Tag to mudd","Tag to need for help","Tab to house crack"};
	 final String[] titleListStrForBasestation1 = {"Soon there","Be stronger","Don't scare","Call me soon"};
	 final String[] articleListStr = {"Are u sure to tag?","Infact","Easy"};
	 
	 private void ShowAlertDialogAndList()
	 {
		 if(quickMsgFlag == 0){
			  Builder MyAlertDialog = new AlertDialog.Builder(this);
			  MyAlertDialog.setTitle("Quick message");
			  //�إ߿�ܪ��ƥ�
			  DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener()
			  {
				 public void onClick(DialogInterface dialog, int which) {
					 ShowMsgDialog(titleListStrForBasestation[which], "OK?", which);
				 }
			  };
			  
			  //�إ߫��U��������Ʊ����������ƥ�
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
			  //�إ߿�ܪ��ƥ�
			  DialogInterface.OnClickListener ListClick = new DialogInterface.OnClickListener()
			  {
				 public void onClick(DialogInterface dialog, int which) {
					 ShowMsgDialog(titleListStrForBasestation1[which], "OK?", which);
				 }
			  };
			  
			  //�إ߫��U��������Ʊ����������ƥ�
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
		/* ���U�O�h�s�W�@�Ӧa�Ϫ��h��(�i�Ѧ�zoon level��z...���ܦh�h)
		 * �M��ڭ̥u�O�h�[�@�h�a�ϼh�šA�æb�Ӧa�ϼh�ŤW���Y�Ӧ�m�[�W�@�ӹϮ�
		 * */

		if(iconNumber == 1){	//��ܥ��X�D�Ϫ̦�m
			List<Overlay> mapOverlays = mapView.getOverlays();
			Drawable drawable = this.getResources().getDrawable(R.drawable.sos);	//�]�w�Ϯ�
			CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//�h�snew�t�@�����O
			
			//GeoPoint point = new GeoPoint(latitudeE6, longitudeE6);
			//GeoPoint point = new GeoPoint((int)(23.707906 * 1000000),(int)(120.436581 * 1000000));	//�y��
			GeoPoint point = drawPoint;
			//GeoPoint point2 = new GeoPoint((int)(23.714707 * 1000000),(int)(120.43703 * 1000000));	//�y��
			//Location mLocation = new Location(120, 23);
			OverlayItem overlayitem = new OverlayItem(point, userName, article);	//�ͥX�C�Ӯy�Ъ̪���m��T
			//OverlayItem overlayitem2 = new OverlayItem(point2, "Helper2", "I'm in Athens, Greece!");
			
			
			itemizedOverlay.addOverlay(overlayitem);
			//itemizedOverlay.addOverlay(overlayitem2);
			mapOverlays.add(itemizedOverlay);
		}
		if(iconNumber == 2){	//��ܥ��ثe�������q�аO
			try{
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = this.getResources().getDrawable(R.drawable.tag);	//�]�w�Ϯ�
				CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//�h�snew�t�@�����O
				
				GeoPoint point = drawPoint;
				OverlayItem overlayitem = new OverlayItem(point, userName, article);	//�ͥX�C�Ӯy�Ъ̪���m��T

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);
			}catch(Exception e){
				showToast("Unkown error: "+e);
			}
			
		}
		
		if(iconNumber == 3){	//��ܥ���ϯ��аO
			try{
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = this.getResources().getDrawable(R.drawable.helthkeep);	//�]�w�Ϯ�
				CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//�h�snew�t�@�����O
				
				GeoPoint point = drawPoint;
				OverlayItem overlayitem = new OverlayItem(point, userName, article);	//�ͥX�C�Ӯy�Ъ̪���m��T

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);
			}catch(Exception e){
				showToast("Unkown error: "+e);
			}
			
		}
		
		if(iconNumber == 4){	//��ܥ���L�Ϩa�H�аO
			try{
				List<Overlay> mapOverlays = mapView.getOverlays();
				Drawable drawable = this.getResources().getDrawable(R.drawable.helper);	//�]�w�Ϯ�
				CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this);	//�h�snew�t�@�����O
				
				GeoPoint point = drawPoint;
				OverlayItem overlayitem = new OverlayItem(point, userName, article);	//�ͥX�C�Ӯy�Ъ̪���m��T

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
		      //ShowMsgDialog("���Ӥu","�w�s�u",1);
		      return null;
	     }
    }
	
	
}