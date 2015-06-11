package process.finalver.dica;


import process.finalver.dica.MeteorMapActivityForRequester.asyncTaskUpdateProgress;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class DicaVer4Activity extends Activity {
    /** Called when the activity is first created. */
	
	/*----------------------------計時器部分---------------------------*/
	private Long startTime;
	private Handler handler = new Handler();
	public ProgressDialog myDialog;
	
	/*----------------------------資料庫部分----------------------------*/
	MySQLiteHelper myHelper;	//資料庫輔助類別物件的引用
	boolean flagForCheckDB = false;
	
	/*----------------------------切換頁面用----------------------------
	 * flag對應到ShowMsgDialog()裡面的if判斷式
	 * 用於幫助asyncTaskUpdateProgress對應到對的alertDialog
	 * */
	int flag = 0;	//用來當作切換幾個頁面用的標記
	
	
    //@SuppressWarnings("null")
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
        
        
        /*這邊因為那個MeteorMapActivity只能抓到main，所以我直接把main配給他
         * 然後這裡就用另一個layout*/
        setContentView(R.layout.entrytopiclayout);
        
        startTime = System.currentTimeMillis();
        //設定定時要執行的方法
        handler.removeCallbacks(updateTimer);
        //設定Delay的時間
        handler.postDelayed(updateTimer, 1000);
        //selectVersionPage();
        
        
        /*------------------資料庫初始部分------------------------*/
        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//新建資料庫輔助類別物件
        /*這裡我是先去抓資料庫，看資料庫裏面有沒有資料，如果沒有，就先設為default
         * 設為default是作為短期的做法，實際上應該是要偵測沒字串，但它裡面會有太多像\n或是\r之類的
         * 這些都算成字串...會造成判斷有影響，技術上要花更多時間修改查詢
         * 
         * 2012/4/4修正成判斷是否字串為空
         * 成功建置!因為發現舊有抓default的方式，會造成之後建置的資料庫雖然值是正確的
         * 但因為名字一定不會跟default一樣，而造成他都重刷成預設值
         * */
        String result = queryData(myHelper);	//向資料庫中查詢資料
        if("".equals(result.trim())){	
        	insertAndUpdateData(myHelper);	//向資料庫中插入和更新資料，目的是初始預設值
        	result = queryData(myHelper);	//向資料庫中查詢資料
        	//showToast("result:"+result+", length:"+result.length());
        	
        }
        if("default".equals(result.trim())){
        	flagForCheckDB = true;
        }
        //showToast("result:"+result+", length:"+result.length());
        //showToast("Bruce how are u");
    }
   
    /*----------------------------------資料庫升級操作函示區------------------------------*/
    //初始資料庫
    //方法：向資料庫中的表中插入和更新資料
  	public void insertAndUpdateData(MySQLiteHelper myHelper){
  		SQLiteDatabase db = myHelper.getWritableDatabase();	//獲取資料庫物件
  		//使用execSQL方法向表中插入資料
  		db.execSQL("insert into user_info(name,age,career,major1,major2,major3) values('default',20,'default','default','default','default')");	
  		db.close();			//關閉SQLiteDatabase物件
  	}
  	//方法：從資料庫中查詢資料
  	public String queryData(MySQLiteHelper myHelper){
  		String result="";
  		SQLiteDatabase db = myHelper.getReadableDatabase();		//獲得資料庫物件
  		Cursor cursor = db.query(" user_info", null, null, null, null, null, "id asc");	//查詢表中資料
  		int nameIndex = cursor.getColumnIndex("name");	//獲取name欄的索引
  		int ageIndex = cursor.getColumnIndex("age");	//獲取age欄的索引
  		int careerIndex = cursor.getColumnIndex("career");	//獲取career欄的索引
  		int major1Index = cursor.getColumnIndex("major1");	//獲取major1欄的索引
  		int major2Index = cursor.getColumnIndex("major2");	//獲取major2欄的索引
  		int major3Index = cursor.getColumnIndex("major3");	//獲取major3欄的索引
  		for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){	//遍歷結果集，提取資料
  			result = result + cursor.getString(nameIndex)+"    ";
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
  	
  //底下是設定計時器內容
    private Runnable updateTimer = new Runnable() {
    	public void run() {
    		Long spentTime = System.currentTimeMillis() - startTime;
    		//計算目前已過秒數
    		Long seconds = (spentTime/1000) % 60;
    		if(seconds == 1){
    			selectVersionPage();
    		}
    		handler.postDelayed(this, 1000);
    	}
    };
    
    /*切換成第二個頁面*/
    public void selectVersionPage(){
    	setContentView(R.layout.showwhorulayout);
    	
    	Button b1 = (Button) findViewById(R.id.button1);
    	b1.setOnClickListener(new Button.OnClickListener()
        {
    		
			@Override
			public void onClick(View v) {
				
				/*我把按下按鈕之後的切入畫面動作
				 * 放在Process dialod function中
				 * */
//				new asyncTaskUpdateProgress().onPreExecute();
//				asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     //設這的目的是讓使用者知道現在資料庫要讀取了
//				task.execute();
				
				if(flagForCheckDB == true){	//為true則表示資料庫沒有設過
					flag = 1;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();
//					ShowMsgDialog("錯誤","偵測到您並沒有留資料，將導到設定畫面",1);
					// TODO Auto-generated method stub
				}else{
					flag = 2;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();  
//					String name = queryData(myHelper);
//					ShowMsgDialog("歡迎", name,2);
				}
				
			}
        });
    	
    	Button b2 = (Button) findViewById(R.id.button2);
    	b2.setOnClickListener(new Button.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				
//				new asyncTaskUpdateProgress().onPreExecute();
//				asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
//				task.execute();  
				
				if(flagForCheckDB == true){	//為true則表示資料庫沒有設過
					flag = 3;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();  
//					ShowMsgDialog("錯誤","偵測到您並沒有留資料，將導到設定畫面",3);
					// TODO Auto-generated method stub
				}else{
					flag = 4;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();  
//					String name = queryData(myHelper);
//					ShowMsgDialog("歡迎", name,4);
				}
			}
        });
    	
    	
    }
    /*
    public void fuctionPage(){
    	setContentView(R.layout.function);
    	
    }
    */
    
    public class asyncTaskUpdateProgress extends AsyncTask<Void, Integer, Void> {
    	 
        int progress;
        ProgressDialog progressDialog;
         
	     @Override
	     protected void onPostExecute(Void result) {
	      // TODO Auto-generated method stub
	    	 progressDialog.dismiss();
	    	 
	    	 if(flag == 1){	//為true則表示資料庫沒有設過
	    		 ShowMsgDialog("Error","Detect...No profiles. Will goto setting view",1);
					// TODO Auto-generated method stub
			}if(flag == 2){
				String name = queryData(myHelper);
				ShowMsgDialog("Welcome", name,2);
			}if(flag == 3){
				ShowMsgDialog("Error","Detect...No profiles. Will goto setting view",3);
			}if(flag == 4){
				String name = queryData(myHelper);
				ShowMsgDialog("Welcome", name,4);
			}
	    	 
	     }
	    
		@Override
	    protected void onPreExecute() {
	      // TODO Auto-generated method stub
		      progress = 0;
		      progressDialog = ProgressDialog.show(DicaVer4Activity.this, "Confirn database", "Database reading...");
		      //((Object) progressDialog).doInBackground();
	    }
	    
	     @Override
	     protected Void doInBackground(Void... arg0) {
		      // TODO Auto-generated method stub
		      while(progress<100){
			       progress++;
			       SystemClock.sleep(30); 
			       //sleep(3000);
		      }
		      return null;
	     }
    }
    
    
    /*----------------------------幫助Toast在Thread中打出訊息-------------------------- */
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
					 if(flag == 1){	//flag == 1則表示進入設定頁面
						    Intent intent = new Intent();
							intent.setClass(DicaVer4Activity.this, PropertySetting.class);
							startActivity(intent);
							DicaVer4Activity.this.finish();
					 }
					 if(flag == 2){	//flag == 2則表示進入功能頁面
						    Intent intent = new Intent();
							intent.setClass(DicaVer4Activity.this, MainFunctionActivity.class);
							startActivity(intent);
							DicaVer4Activity.this.finish();
					 }
					 if(flag == 3){	//flag == 3則表示進入求救者資料設定頁面
						    Intent intent = new Intent();
							intent.setClass(DicaVer4Activity.this, PropertySettingForRequester.class);
							startActivity(intent);
							DicaVer4Activity.this.finish();
					 }
					 if(flag == 4){	//flag == 4則表示進入求救者頁面
						    Intent intent = new Intent();
							intent.setClass(DicaVer4Activity.this, MeteorMapActivityForRequester.class);
							startActivity(intent);
							DicaVer4Activity.this.finish();
					 }   
				 }
		  };;
		  MyAlertDialog.setNeutralButton("OK",OkClick );
		  MyAlertDialog.show();
	 }
}