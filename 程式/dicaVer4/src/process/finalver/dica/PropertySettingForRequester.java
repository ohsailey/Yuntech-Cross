package process.finalver.dica;

/*這裡可以拿來用的程式碼:
 * 偵測字串是否為空
 * ScrollView
 * 
 * 
 * */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PropertySettingForRequester extends Activity {
		
		/*------------介面宣告區----------*/
		private Spinner ageSpinner;
		private Spinner majorSpinner1;
		private Spinner majorSpinner2;
		private Spinner majorSpinner3;
		private Spinner careerSpinner;
		TextView TextView05;	// 用來顯示BUG訊息
		
		/*-------------一般陣列儲存宣告區-----------------*/
		public List<Integer> allAges;    
		public ArrayAdapter<Integer> adapterForAge;
		public List<String> majors1;   
		public ArrayAdapter<String>adapterForMajors1;
		private List<String> majors2;   
		private ArrayAdapter<String>adapterForMajors2;
		private List<String> majors3;   
		private ArrayAdapter<String>adapterForMajors3;
		private List<String> careerNames;   
		private ArrayAdapter<String>adapterForCareerNames;
		private static final String[] majorItem = {  "無", "頭部流血", "胸腔流血","腳部流血","大腿流血" };
		private static final String[] majorItem2 = {"無","頭部穿刺傷", "手部穿刺傷","大腿穿刺傷" };
		private static final String[] majorItem3 = { "無","壓在房屋下","困在山洞","腿傷不能移動","身邊有很多受害者" };
		private static final String[] career = { "男","女" };
		private static final boolean chooseArray[] = new boolean[majorItem.length];
		
		
		/*----------------其他物件宣告區----------------------*/
		Handler handler = new Handler();
		public String strBug = "";
		
		/*----------------------------資料庫部分----------------------------*/
		MySQLiteHelper myHelper;	//資料庫輔助類別物件的引用
		boolean flagForCheckDB = false;
		SQLiteDatabase db;
  		Cursor cursor;
  		String result ="";
		
  		/*----------------------------網路連線部分-------------------------*/
  		Socket clientSocket;	// 客戶端socket
  		String tmp;				// 暫存文字訊息
  		String userName = "";	//設定傳送網路時你的IP名稱
  		Thread t;	//初始執行緒 
		
		
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        
		        /* ----------------------------執行緒初始區---------------------------*/
				final Thread updateDB = new Thread(updateDatabase);	//讀資料
		        //updateDB.start();
				
		        /*--------------------------------------------------------------
		         * 底下這行是移除狀態列，在加入之前請先import window
		         * 這行要在setContentView前面加，不然會當機
		         *-------------------------------------------------------------- */
		        requestWindowFeature(Window.FEATURE_NO_TITLE);
		        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		        
		        setContentView(R.layout.propertysettinglayout);
		        
		        /*--------------------------------連結區-------------------------------------------------------*/
		        TextView05 =  (TextView) findViewById(R.id.textView5);
		        
		        
		        /*--------------------------- 執行緒初始區------------------------------------------------*/
		     	t = new Thread(readData);	//讀資料
		        t.start();
		        
		        /*------------------------------資料庫初始部分------------------------------------*/
		        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//新建資料庫輔助類別物件
		        /*這裡我是先去抓資料庫，看資料庫裏面有沒有資料，如果沒有，就先設為default
		         * 設為default是作為短期的做法，實際上應該是要偵測沒字串，但它裡面會有太多像\n或是\r之類的
		         * 這些都算成字串...會造成判斷有影響，技術上要花更多時間修改查詢
		         * */
		        result = queryData(myHelper);	//向資料庫中查詢資料
		        if("".equals(result.trim())){	
		        	insertAndUpdateData(myHelper);	//向資料庫中插入和更新資料，目的是初始預設值
		        	result = queryData(myHelper);	//向資料庫中查詢資料
		        	//showToast("result:"+result+", length:"+result.length());
		        	flagForCheckDB = true;
		        }
		        
		        /*--------------Spinner區 for age-----------------------*/
		        allAges = new ArrayList<Integer>();
		        for (int i = 13; i <80; i++)
		        {
		        	allAges.add(i+1);
		        }
		        /* new ArrayAdapter物件並將allAges傳入 */
		        adapterForAge = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, allAges);
		        adapterForAge.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        ageSpinner = (Spinner) findViewById(R.id.spinner1);
		        
		        ageSpinner.setAdapter(adapterForAge);
		        /* 將ageSpinner加入OnItemSelectedListener */
		        ageSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* 將所選ageSpinner的值帶入myTextView中 */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//後面一句是取得Spinner的值
			        	  
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		       
		        
		      /*---------------------------Spinners區 for major----------------------------------------*/  
		        /* 初始設定chooseArray
		         *  chooseArray的目的就是在於動態調整連動的array
		         * */
		        for(int i=0;i<majorItem.length;i++){
		        	chooseArray[i] = false;		//設成false就是還沒被選到可以顯示出來
		        }
		          
		        
			  /*-------------Spinner1-------------*/
		      /*在做聯動時千萬注意，Spinner1的優先權大於Spinner2依此類推
		       * 所以千萬注意當Spinner1可以選，則Spinner2&3就不能選
		       * 又Spinner1選到了下面已經選過的情形時，則要在做圓滿點，就是Spinner2/3自動轉成"無"
		       * */
		      majors1  = new ArrayList<String>();
		      for (int i = 0; i < majorItem.length; i++)	//放陣列值至ArraList
		      {
		    	  majors1.add(majorItem[i]);
		      }
		    //把ArrayList轉成Adapter
		      adapterForMajors1  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors1);
		      adapterForMajors1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      majorSpinner1 = (Spinner) findViewById(R.id.spinner2); 
		      majorSpinner1.setAdapter(adapterForMajors1);
		        /* 將majorSpinner1加入OnItemSelectedListener */
		      majorSpinner1.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
				        	  /* 將所選majorSpinner1的值帶入myTextView中 */
				        	  //EditText02.setText(arg0.getSelectedItem().toString());		//後面一句是取得Spinner的值
			        	  	//updateCitySpinner(1);
			        	  //showToast(majorSpinner1.getSelectedItem().toString());
			        	
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		      /*-------------Spinner2-------------*/
		      majors2  = new ArrayList<String>();
		      for (int i = 0; i < majorItem2.length; i++)	//放陣列值至ArraList
		      {
		    	  majors2.add(majorItem2[i]);
		      }
		      //把ArrayList轉成Adapter
		      adapterForMajors2  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors2);
		      adapterForMajors2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      majorSpinner2 = (Spinner) findViewById(R.id.spinner3); 
		      majorSpinner2.setAdapter(adapterForMajors2);
		        /* 將majorSpinner1加入OnItemSelectedListener */
		      majorSpinner2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* 將所選majorSpinner2的值帶入myTextView中 */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//後面一句是取得Spinner的值
			        	  //showToast(majorSpinner2.getSelectedItem().toString());
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		      /*-------------Spinner3-------------*/
		      majors3  = new ArrayList<String>();
		      for (int i = 0; i < majorItem3.length; i++)		//放陣列值至ArraList
		      {
		    	  majors3.add(majorItem3[i]);
		      }
		      //把ArrayList轉成Adapter
		      adapterForMajors3  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors3);
		      adapterForMajors3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      majorSpinner3 = (Spinner) findViewById(R.id.spinner4); 
		      majorSpinner3.setAdapter(adapterForMajors3);
		        /* 將majorSpinner1加入OnItemSelectedListener */
		      majorSpinner3.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* 將所選majorSpinner3的值帶入myTextView中 */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//後面一句是取得Spinner的值
			        	  //showToast(majorSpinner3.getSelectedItem().toString());
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		      /*End----------------------Spinners區 for major----------------------------------------*/
		      
		      /*---------------------------Spinners區 for career----------------------------------------*/  
		      careerNames  = new ArrayList<String>();
		      for (int i = 0; i < career.length; i++)		//放陣列值至ArraList
		      {
		    	  careerNames.add(career[i]);
		      }
		      //把ArrayList轉成Adapter
		      adapterForCareerNames = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, careerNames);
		      adapterForCareerNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      careerSpinner = (Spinner) findViewById(R.id.spinner5); 
		      careerSpinner.setAdapter(adapterForCareerNames);
		        /* 將majorSpinner1加入OnItemSelectedListener */
		      careerSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* 將所選majorSpinner3的值帶入myTextView中 */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//後面一句是取得Spinner的值
			        	 // showToast(careerSpinner.getSelectedItem().toString());
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        }); 
		      
		      /*-----------------------設定按鈕區-----------------------------*/
		      Button b1 = (Button) findViewById(R.id.button1);
		    	b1.setOnClickListener(new Button.OnClickListener()
		        {
					@Override
					public void onClick(View v) {
							/*if(flagForCheckDB == true){
								String result = queryData(myHelper);	//向資料庫中查詢資料
								showToast(result);
							}*/
							
							DeleteTb();
							insertAndUpdateData(myHelper);	//向資料庫中插入和更新資料，目的是初始預設值
							String result = queryData(myHelper);	//向資料庫中查詢資料
							//showToast(""+result);
							
							TextView editText1=(EditText) findViewById(R.id.editText1);
							if(!"".equals(editText1.getText().toString().trim())){	//當名字不為空格時
				  				if(!"無".equals(careerSpinner.getSelectedItem().toString().trim())){
				  					if(!"無".equals(majorSpinner1.getSelectedItem().toString().trim()) || 
				  							!"無".equals(majorSpinner2.getSelectedItem().toString().trim()) ||
				  							!"無".equals(majorSpinner3.getSelectedItem().toString().trim())){
				  						flagForCheckDB =true;
				  						showToast("資料填空已完成");
					  				}
				  				}
				  			}
							
							/*底下去判斷每個格子是否有符合我的最低要求*/
							if("".equals(editText1.getText().toString().trim())){	//當名字不為空格時
								showToast("No names");
				  			}
							if("無".equals(careerSpinner.getSelectedItem().toString().trim())){
			  						showToast("請選擇職業");
			  				}
							if("無".equals(majorSpinner1.getSelectedItem().toString().trim()) && 
									"無".equals(majorSpinner2.getSelectedItem().toString().trim()) &&
									"無".equals(majorSpinner3.getSelectedItem().toString().trim())){
		  						showToast("請至少選擇一項專業能力");
			  				}
							
							if(flagForCheckDB == true){
								try{
									
						        	// 如果已連接則
									if(clientSocket.isConnected()){
										
										BufferedWriter bw;
										
										try {
											// 取得網路輸出串流
											bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
											
											// 寫入訊息，顯示player n:says...
											bw.write(userName + ":" +"3334567"+result+ "\n");
											
											// 立即發送
											bw.flush();
										} catch (IOException e) {
											
										}
										// 將文字方塊清空
										//EditText02.setText("");
									}
						        	
									showToast("上傳資料庫成功!!");
									
					        	}catch(NullPointerException e){
					        		//showToast("Exception: " + e);
					        		showToast("錯誤連線!請確認連線狀態!");
					        	}catch(Exception e){
					        		showToast("發生未知錯誤: " + e);
					        		//showToast("錯誤連線!請確認連線狀態!");
					        	}
								
								/*底下切換VIEW*/
								Intent intent = new Intent();
								intent.setClass(PropertySettingForRequester.this, MeteorMapActivityForRequester.class);
								//intent.setClass(PropertySetting.this, MeteorMapActivity.class);
								startActivity(intent);
								PropertySettingForRequester.this.finish();
							}
							
						}
		        });
		      
	    }
	    
	    
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    /*----------------------------------------------出來onCreate-------------------------------------------------------------------------------*/   
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    
	  
	    
	  
	  /*------------------------及時判斷資料庫是否已經有設成最低要求---------------------------------*/
		private Runnable updateDatabase = new Runnable() {
			public void run() {
				 //String result = queryData(myHelper);	//向資料庫中查詢資料	
				String result="";
		  		SQLiteDatabase db2 = myHelper.getReadableDatabase();		//獲得資料庫物件
		  		Cursor cursor2 = db2.query(" user_info", null, null, null, null, null, "id asc");	//查詢表中資料
		  		int nameIndex = cursor2.getColumnIndex("name");	//獲取name欄的索引
		  		int ageIndex = cursor2.getColumnIndex("age");	//獲取age欄的索引
		  		int careerIndex = cursor2.getColumnIndex("career");	//獲取career欄的索引
		  		int major1Index = cursor2.getColumnIndex("major1");	//獲取major1欄的索引
		  		int major2Index = cursor2.getColumnIndex("major2");	//獲取major2欄的索引
		  		int major3Index = cursor2.getColumnIndex("major3");	//獲取major3欄的索引
		  		for(cursor2.moveToFirst();!(cursor2.isAfterLast());cursor2.moveToNext()){	//遍歷結果集，提取資料
//		  			if(cursor2.getString(nameIndex) == null){
//		  				
//		  			}
//		  			showToast("WTFFFFFF");
//		  			result = result + cursor2.getString(nameIndex)+"    ";
//		  			result = result + cursor2.getInt(ageIndex)+"    ";
//		  			result = result + cursor2.getString(careerIndex)+"    ";
//		  			result = result + cursor2.getString(major1Index)+"    ";
//		  			result = result + cursor2.getString(major2Index)+"    ";
//		  			result = result + cursor2.getString(major3Index)+"     \n";
		  		}
		  		cursor.close();		//關閉結果集
		  		db2.close();			//關閉資料庫物件
			}
		};
		
		/*-----------------------------------------------資料庫升級操作函示區------------------------------------------*/
	    //初始資料庫
	    //方法：向資料庫中的表中插入和更新資料
	  	public void insertAndUpdateData(MySQLiteHelper myHelper){
	  		SQLiteDatabase db = myHelper.getWritableDatabase();	//獲取資料庫物件
	  		//使用execSQL方法向表中插入資料
	  		String tempExeSql = "insert into user_info(name,age,career,major1,major2,major3) values('{0}',{1},'{2}','{3}','{4}','{5}')";
	  		TextView editText1=(EditText) findViewById(R.id.editText1);
			String exeSql = MessageFormat.format(tempExeSql, editText1.getText(), 
					ageSpinner.getSelectedItem().toString(),
					careerSpinner.getSelectedItem().toString(),
					majorSpinner1.getSelectedItem().toString(),
					majorSpinner2.getSelectedItem().toString(),
					majorSpinner3.getSelectedItem().toString());
			String exeSql2 = "insert into user_info(name,age,career,major1,major2,major3) values('"+editText1.getText()+
					"','"+ageSpinner.getSelectedItem().toString()+"','"+careerSpinner.getSelectedItem().toString()+"','"+majorSpinner1.getSelectedItem().toString()+
					"','"+majorSpinner2.getSelectedItem().toString()+"','"+majorSpinner3.getSelectedItem().toString()+"')";
			//showToast(exeSql2);
	  		db.execSQL(exeSql2);	
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
	  			result = result + cursor.getString(nameIndex)+",";
	  			result = result + cursor.getInt(ageIndex)+",";
	  			result = result + cursor.getString(careerIndex)+",";
	  			result = result + cursor.getString(major1Index)+",";
	  			result = result + cursor.getString(major2Index)+",";
	  			result = result + cursor.getString(major3Index);
	  		}
	  		//showToast("The:"+cursor.getString(nameIndex)+".");
	  		cursor.close();		//關閉結果集
	  		db.close();			//關閉資料庫物件
	  		return result;
	  	}
	  	//@Override
		public void DeleteTb() {
			SQLiteDatabase db = myHelper.getWritableDatabase();	//獲取資料庫物件
			db.delete(" user_info", "1", null);		//刪除user_info表中的所有資料
			super.onDestroy();
			
			int flag = -1;
	        db = myHelper.getWritableDatabase();
	        String sql = "delete from user_info where id = 1";
	        try {
	            db.execSQL(sql);
	        } catch (SQLException e) {
	            flag = 0;
	            showToast("失敗！");
	        }
	        db.close();
	        if (flag == -1){
	            showToast("成功！");
	        }
		}
	    /*End-------------------------------資料庫操作--------------------------------------*/
		
	    //底下未完成，是屬於兩個spinner連動的部分
	    /*未完成部分，
	     * 題目需求:
	     * Spinner1的訊息不能跟Spinner2&3重複
	     * 除此之外Spinner1按到的訊息如果跟Spinner2/3相同，則把Spinner2/3的訊息改為"無"
	     * */
	   /*private void updateCitySpinner(int spinnerNumber){

        	if(spinnerNumber == 1){	//表示這是在Spinner1上按下去的
        		for(int i = 1; i < majorItem2.length; i++){
        			if(majorSpinner1.getSelectedItem().toString().equals(majorSpinner2.getSelectedItem().toString())){
        				//重製Spinner2
			        	  majors2.clear();
			        	  int temp=0;
			        	  for (int j = 0; j< majorItem.length; j++)	//放陣列值至ArraList
					      {
			        			  majorItem2[j] = null;
			        			  
			        	 }
			        	  for (int j = 0; j< majorItem.length; j++)	//放陣列值至ArraList
					      {
			        		  if(i != j){
			        			  majorItem2[temp] = majorItem[j];
			        			  ++temp;
			        			  strBug += "majorItem2["+temp+"]"+majorItem2[temp];
			        			  TextView05.append(strBug + "\n");
			        		  }
					      }
			        	  for (int k = 0; k < temp; k++)	//放陣列值至ArraList
					      {
					    	  majors2.add(majorItem2[k]);
					    	  //strBug += "majors2["+k+"]"+majorItem2[temp];
		        			  //TextView05.append(strBug + "\n");
					      }
			        	  //把ArrayList轉成Adapter
					      adapterForMajors2  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors2);
					      adapterForMajors2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					      majorSpinner2.setAdapter(adapterForMajors2);
        			}
        			if(majorSpinner1.getSelectedItem().toString().equals(majorSpinner3.getSelectedItem().toString())){
        				//重製Spinner3
			        	  majors3.clear();
			        	  int temp=0;
			        	  for (int j = 0; j< majorItem.length; j++)	//放陣列值至ArraList
					      {
			        			  majorItem3[j] = null;
			        	 }
			        	  for (int j = 0; j< majorItem.length; j++)	//放陣列值至ArraList
					      {
			        		  if(i != j){
			        			  majorItem3[temp] = majorItem[j];
			        			  temp++;
			        			  strBug += "majorItem3["+temp+"]"+majorItem3[temp];
			        			  TextView05.append(strBug + "\n");
			        		  }
					      }
			        	  for (int k = 0; k < temp; k++)	//放陣列值至ArraList
					      {
					    	  majors3.add(majorItem3[k]);
					    	  //strBug += "majors3["+k+"]"+majorItem3[temp];
		        			  //TextView05.append(strBug + "\n");
					      }
			        	  //把ArrayList轉成Adapter
					      adapterForMajors3  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors3);
					      adapterForMajors3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					      majorSpinner3.setAdapter(adapterForMajors3);
        			}
        		}

        	}
        	if(spinnerNumber == 2){	//表示這是在Spinner2上按下去的
        		
        	}
        	if(spinnerNumber == 3){	//表示這是在Spinner3上按下去的
	
        	}
        } */
	   
	   /*底下用來幫助Toast在Thread中打出訊息
		 * */
		 public void showToast(final String msg) {
			  handler.post(new Runnable() {
			   public void run() {
			    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			   }
			  });
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
						int serverPort = 8078;
						//int serverPort = 8080;
						clientSocket = new Socket(serverIp, serverPort);
						
						// 取得網路輸入串流
						BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
						
						// 當連線後
						while (clientSocket.isConnected()) {
							showToast("Prop: 成功連線!");
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
								}else if(tmp.contains("2234567")){
									//填入功能
									
								}else if(tmp.contains("3334567")){
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

								
						}
						

					} catch (IOException e) {
						
					}
				}
			};
		 
		 
    }