package process.finalver.dica;

/*�o�̥i�H���ӥΪ��{���X:
 * �����r��O�_����
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
		
		/*------------�����ŧi��----------*/
		private Spinner ageSpinner;
		private Spinner majorSpinner1;
		private Spinner majorSpinner2;
		private Spinner majorSpinner3;
		private Spinner careerSpinner;
		TextView TextView05;	// �Ψ����BUG�T��
		
		/*-------------�@��}�C�x�s�ŧi��-----------------*/
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
		private static final String[] majorItem = {  "�L", "�Y���y��", "�ݵĬy��","�}���y��","�j�L�y��" };
		private static final String[] majorItem2 = {"�L","�Y������", "�ⳡ����","�j�L����" };
		private static final String[] majorItem3 = { "�L","���b�ЫΤU","�x�b�s�}","�L�ˤ��ಾ��","���䦳�ܦh���`��" };
		private static final String[] career = { "�k","�k" };
		private static final boolean chooseArray[] = new boolean[majorItem.length];
		
		
		/*----------------��L����ŧi��----------------------*/
		Handler handler = new Handler();
		public String strBug = "";
		
		/*----------------------------��Ʈw����----------------------------*/
		MySQLiteHelper myHelper;	//��Ʈw���U���O���󪺤ޥ�
		boolean flagForCheckDB = false;
		SQLiteDatabase db;
  		Cursor cursor;
  		String result ="";
		
  		/*----------------------------�����s�u����-------------------------*/
  		Socket clientSocket;	// �Ȥ��socket
  		String tmp;				// �Ȧs��r�T��
  		String userName = "";	//�]�w�ǰe�����ɧA��IP�W��
  		Thread t;	//��l����� 
		
		
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
		        super.onCreate(savedInstanceState);
		        
		        /* ----------------------------�������l��---------------------------*/
				final Thread updateDB = new Thread(updateDatabase);	//Ū���
		        //updateDB.start();
				
		        /*--------------------------------------------------------------
		         * ���U�o��O�������A�C�A�b�[�J���e�Х�import window
		         * �o��n�bsetContentView�e���[�A���M�|���
		         *-------------------------------------------------------------- */
		        requestWindowFeature(Window.FEATURE_NO_TITLE);
		        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		        
		        setContentView(R.layout.propertysettinglayout);
		        
		        /*--------------------------------�s����-------------------------------------------------------*/
		        TextView05 =  (TextView) findViewById(R.id.textView5);
		        
		        
		        /*--------------------------- �������l��------------------------------------------------*/
		     	t = new Thread(readData);	//Ū���
		        t.start();
		        
		        /*------------------------------��Ʈw��l����------------------------------------*/
		        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//�s�ظ�Ʈw���U���O����
		        /*�o�̧ڬO���h���Ʈw�A�ݸ�Ʈw�ح����S����ơA�p�G�S���A�N���]��default
		         * �]��default�O�@���u�������k�A��ڤW���ӬO�n�����S�r��A�����̭��|���Ӧh��\n�άO\r������
		         * �o�ǳ��⦨�r��...�|�y���P�_���v�T�A�޳N�W�n���h�ɶ��ק�d��
		         * */
		        result = queryData(myHelper);	//�V��Ʈw���d�߸��
		        if("".equals(result.trim())){	
		        	insertAndUpdateData(myHelper);	//�V��Ʈw�����J�M��s��ơA�ت��O��l�w�]��
		        	result = queryData(myHelper);	//�V��Ʈw���d�߸��
		        	//showToast("result:"+result+", length:"+result.length());
		        	flagForCheckDB = true;
		        }
		        
		        /*--------------Spinner�� for age-----------------------*/
		        allAges = new ArrayList<Integer>();
		        for (int i = 13; i <80; i++)
		        {
		        	allAges.add(i+1);
		        }
		        /* new ArrayAdapter����ñNallAges�ǤJ */
		        adapterForAge = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, allAges);
		        adapterForAge.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		        ageSpinner = (Spinner) findViewById(R.id.spinner1);
		        
		        ageSpinner.setAdapter(adapterForAge);
		        /* �NageSpinner�[�JOnItemSelectedListener */
		        ageSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* �N�ҿ�ageSpinner���ȱa�JmyTextView�� */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//�᭱�@�y�O���oSpinner����
			        	  
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		       
		        
		      /*---------------------------Spinners�� for major----------------------------------------*/  
		        /* ��l�]�wchooseArray
		         *  chooseArray���ت��N�O�b��ʺA�վ�s�ʪ�array
		         * */
		        for(int i=0;i<majorItem.length;i++){
		        	chooseArray[i] = false;		//�]��false�N�O�٨S�Q���i�H��ܥX��
		        }
		          
		        
			  /*-------------Spinner1-------------*/
		      /*�b���p�ʮɤd�U�`�N�ASpinner1���u���v�j��Spinner2�̦�����
		       * �ҥH�d�U�`�N��Spinner1�i�H��A�hSpinner2&3�N�����
		       * �SSpinner1���F�U���w�g��L�����ήɡA�h�n�b���꺡�I�A�N�OSpinner2/3�۰��ন"�L"
		       * */
		      majors1  = new ArrayList<String>();
		      for (int i = 0; i < majorItem.length; i++)	//��}�C�Ȧ�ArraList
		      {
		    	  majors1.add(majorItem[i]);
		      }
		    //��ArrayList�নAdapter
		      adapterForMajors1  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors1);
		      adapterForMajors1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      majorSpinner1 = (Spinner) findViewById(R.id.spinner2); 
		      majorSpinner1.setAdapter(adapterForMajors1);
		        /* �NmajorSpinner1�[�JOnItemSelectedListener */
		      majorSpinner1.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
				        	  /* �N�ҿ�majorSpinner1���ȱa�JmyTextView�� */
				        	  //EditText02.setText(arg0.getSelectedItem().toString());		//�᭱�@�y�O���oSpinner����
			        	  	//updateCitySpinner(1);
			        	  //showToast(majorSpinner1.getSelectedItem().toString());
			        	
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		      /*-------------Spinner2-------------*/
		      majors2  = new ArrayList<String>();
		      for (int i = 0; i < majorItem2.length; i++)	//��}�C�Ȧ�ArraList
		      {
		    	  majors2.add(majorItem2[i]);
		      }
		      //��ArrayList�নAdapter
		      adapterForMajors2  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors2);
		      adapterForMajors2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      majorSpinner2 = (Spinner) findViewById(R.id.spinner3); 
		      majorSpinner2.setAdapter(adapterForMajors2);
		        /* �NmajorSpinner1�[�JOnItemSelectedListener */
		      majorSpinner2.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* �N�ҿ�majorSpinner2���ȱa�JmyTextView�� */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//�᭱�@�y�O���oSpinner����
			        	  //showToast(majorSpinner2.getSelectedItem().toString());
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		      /*-------------Spinner3-------------*/
		      majors3  = new ArrayList<String>();
		      for (int i = 0; i < majorItem3.length; i++)		//��}�C�Ȧ�ArraList
		      {
		    	  majors3.add(majorItem3[i]);
		      }
		      //��ArrayList�নAdapter
		      adapterForMajors3  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors3);
		      adapterForMajors3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      majorSpinner3 = (Spinner) findViewById(R.id.spinner4); 
		      majorSpinner3.setAdapter(adapterForMajors3);
		        /* �NmajorSpinner1�[�JOnItemSelectedListener */
		      majorSpinner3.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* �N�ҿ�majorSpinner3���ȱa�JmyTextView�� */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//�᭱�@�y�O���oSpinner����
			        	  //showToast(majorSpinner3.getSelectedItem().toString());
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        });     
		      /*End----------------------Spinners�� for major----------------------------------------*/
		      
		      /*---------------------------Spinners�� for career----------------------------------------*/  
		      careerNames  = new ArrayList<String>();
		      for (int i = 0; i < career.length; i++)		//��}�C�Ȧ�ArraList
		      {
		    	  careerNames.add(career[i]);
		      }
		      //��ArrayList�নAdapter
		      adapterForCareerNames = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, careerNames);
		      adapterForCareerNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		      careerSpinner = (Spinner) findViewById(R.id.spinner5); 
		      careerSpinner.setAdapter(adapterForCareerNames);
		        /* �NmajorSpinner1�[�JOnItemSelectedListener */
		      careerSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){    
			          public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
			        	  /* �N�ҿ�majorSpinner3���ȱa�JmyTextView�� */
			        	  //EditText02.setText(arg0.getSelectedItem().toString());		//�᭱�@�y�O���oSpinner����
			        	 // showToast(careerSpinner.getSelectedItem().toString());
			          }
			          
			          public void onNothingSelected(AdapterView<?> arg0)
			          {
	
			          }
		        }); 
		      
		      /*-----------------------�]�w���s��-----------------------------*/
		      Button b1 = (Button) findViewById(R.id.button1);
		    	b1.setOnClickListener(new Button.OnClickListener()
		        {
					@Override
					public void onClick(View v) {
							/*if(flagForCheckDB == true){
								String result = queryData(myHelper);	//�V��Ʈw���d�߸��
								showToast(result);
							}*/
							
							DeleteTb();
							insertAndUpdateData(myHelper);	//�V��Ʈw�����J�M��s��ơA�ت��O��l�w�]��
							String result = queryData(myHelper);	//�V��Ʈw���d�߸��
							//showToast(""+result);
							
							TextView editText1=(EditText) findViewById(R.id.editText1);
							if(!"".equals(editText1.getText().toString().trim())){	//��W�r�����Ů��
				  				if(!"�L".equals(careerSpinner.getSelectedItem().toString().trim())){
				  					if(!"�L".equals(majorSpinner1.getSelectedItem().toString().trim()) || 
				  							!"�L".equals(majorSpinner2.getSelectedItem().toString().trim()) ||
				  							!"�L".equals(majorSpinner3.getSelectedItem().toString().trim())){
				  						flagForCheckDB =true;
				  						showToast("��ƶ�Ťw����");
					  				}
				  				}
				  			}
							
							/*���U�h�P�_�C�Ӯ�l�O�_���ŦX�ڪ��̧C�n�D*/
							if("".equals(editText1.getText().toString().trim())){	//��W�r�����Ů��
								showToast("No names");
				  			}
							if("�L".equals(careerSpinner.getSelectedItem().toString().trim())){
			  						showToast("�п��¾�~");
			  				}
							if("�L".equals(majorSpinner1.getSelectedItem().toString().trim()) && 
									"�L".equals(majorSpinner2.getSelectedItem().toString().trim()) &&
									"�L".equals(majorSpinner3.getSelectedItem().toString().trim())){
		  						showToast("�Цܤֿ�ܤ@���M�~��O");
			  				}
							
							if(flagForCheckDB == true){
								try{
									
						        	// �p�G�w�s���h
									if(clientSocket.isConnected()){
										
										BufferedWriter bw;
										
										try {
											// ���o������X��y
											bw = new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
											
											// �g�J�T���A���player n:says...
											bw.write(userName + ":" +"3334567"+result+ "\n");
											
											// �ߧY�o�e
											bw.flush();
										} catch (IOException e) {
											
										}
										// �N��r����M��
										//EditText02.setText("");
									}
						        	
									showToast("�W�Ǹ�Ʈw���\!!");
									
					        	}catch(NullPointerException e){
					        		//showToast("Exception: " + e);
					        		showToast("���~�s�u!�нT�{�s�u���A!");
					        	}catch(Exception e){
					        		showToast("�o�ͥ������~: " + e);
					        		//showToast("���~�s�u!�нT�{�s�u���A!");
					        	}
								
								/*���U����VIEW*/
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
	    /*----------------------------------------------�X��onCreate-------------------------------------------------------------------------------*/   
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    /*---------------------------------------------------------------------------------------------------------------------------------------------*/   
	    
	  
	    
	  
	  /*------------------------�ήɧP�_��Ʈw�O�_�w�g���]���̧C�n�D---------------------------------*/
		private Runnable updateDatabase = new Runnable() {
			public void run() {
				 //String result = queryData(myHelper);	//�V��Ʈw���d�߸��	
				String result="";
		  		SQLiteDatabase db2 = myHelper.getReadableDatabase();		//��o��Ʈw����
		  		Cursor cursor2 = db2.query(" user_info", null, null, null, null, null, "id asc");	//�d�ߪ����
		  		int nameIndex = cursor2.getColumnIndex("name");	//���name�檺����
		  		int ageIndex = cursor2.getColumnIndex("age");	//���age�檺����
		  		int careerIndex = cursor2.getColumnIndex("career");	//���career�檺����
		  		int major1Index = cursor2.getColumnIndex("major1");	//���major1�檺����
		  		int major2Index = cursor2.getColumnIndex("major2");	//���major2�檺����
		  		int major3Index = cursor2.getColumnIndex("major3");	//���major3�檺����
		  		for(cursor2.moveToFirst();!(cursor2.isAfterLast());cursor2.moveToNext()){	//�M�����G���A�������
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
		  		cursor.close();		//�������G��
		  		db2.close();			//������Ʈw����
			}
		};
		
		/*-----------------------------------------------��Ʈw�ɯžާ@��ܰ�------------------------------------------*/
	    //��l��Ʈw
	    //��k�G�V��Ʈw���������J�M��s���
	  	public void insertAndUpdateData(MySQLiteHelper myHelper){
	  		SQLiteDatabase db = myHelper.getWritableDatabase();	//�����Ʈw����
	  		//�ϥ�execSQL��k�V�����J���
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
	  		db.close();			//����SQLiteDatabase����
	  	}
	  	//��k�G�q��Ʈw���d�߸��
	  	public String queryData(MySQLiteHelper myHelper){
	  		String result="";
	  		SQLiteDatabase db = myHelper.getReadableDatabase();		//��o��Ʈw����
	  		Cursor cursor = db.query(" user_info", null, null, null, null, null, "id asc");	//�d�ߪ����
	  		int nameIndex = cursor.getColumnIndex("name");	//���name�檺����
	  		int ageIndex = cursor.getColumnIndex("age");	//���age�檺����
	  		int careerIndex = cursor.getColumnIndex("career");	//���career�檺����
	  		int major1Index = cursor.getColumnIndex("major1");	//���major1�檺����
	  		int major2Index = cursor.getColumnIndex("major2");	//���major2�檺����
	  		int major3Index = cursor.getColumnIndex("major3");	//���major3�檺����
	  		for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){	//�M�����G���A�������
	  			result = result + cursor.getString(nameIndex)+",";
	  			result = result + cursor.getInt(ageIndex)+",";
	  			result = result + cursor.getString(careerIndex)+",";
	  			result = result + cursor.getString(major1Index)+",";
	  			result = result + cursor.getString(major2Index)+",";
	  			result = result + cursor.getString(major3Index);
	  		}
	  		//showToast("The:"+cursor.getString(nameIndex)+".");
	  		cursor.close();		//�������G��
	  		db.close();			//������Ʈw����
	  		return result;
	  	}
	  	//@Override
		public void DeleteTb() {
			SQLiteDatabase db = myHelper.getWritableDatabase();	//�����Ʈw����
			db.delete(" user_info", "1", null);		//�R��user_info�����Ҧ����
			super.onDestroy();
			
			int flag = -1;
	        db = myHelper.getWritableDatabase();
	        String sql = "delete from user_info where id = 1";
	        try {
	            db.execSQL(sql);
	        } catch (SQLException e) {
	            flag = 0;
	            showToast("���ѡI");
	        }
	        db.close();
	        if (flag == -1){
	            showToast("���\�I");
	        }
		}
	    /*End-------------------------------��Ʈw�ާ@--------------------------------------*/
		
	    //���U�������A�O�ݩ���spinner�s�ʪ�����
	    /*�����������A
	     * �D�ػݨD:
	     * Spinner1���T�������Spinner2&3����
	     * �������~Spinner1���쪺�T���p�G��Spinner2/3�ۦP�A�h��Spinner2/3���T���אּ"�L"
	     * */
	   /*private void updateCitySpinner(int spinnerNumber){

        	if(spinnerNumber == 1){	//��ܳo�O�bSpinner1�W���U�h��
        		for(int i = 1; i < majorItem2.length; i++){
        			if(majorSpinner1.getSelectedItem().toString().equals(majorSpinner2.getSelectedItem().toString())){
        				//���sSpinner2
			        	  majors2.clear();
			        	  int temp=0;
			        	  for (int j = 0; j< majorItem.length; j++)	//��}�C�Ȧ�ArraList
					      {
			        			  majorItem2[j] = null;
			        			  
			        	 }
			        	  for (int j = 0; j< majorItem.length; j++)	//��}�C�Ȧ�ArraList
					      {
			        		  if(i != j){
			        			  majorItem2[temp] = majorItem[j];
			        			  ++temp;
			        			  strBug += "majorItem2["+temp+"]"+majorItem2[temp];
			        			  TextView05.append(strBug + "\n");
			        		  }
					      }
			        	  for (int k = 0; k < temp; k++)	//��}�C�Ȧ�ArraList
					      {
					    	  majors2.add(majorItem2[k]);
					    	  //strBug += "majors2["+k+"]"+majorItem2[temp];
		        			  //TextView05.append(strBug + "\n");
					      }
			        	  //��ArrayList�নAdapter
					      adapterForMajors2  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors2);
					      adapterForMajors2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					      majorSpinner2.setAdapter(adapterForMajors2);
        			}
        			if(majorSpinner1.getSelectedItem().toString().equals(majorSpinner3.getSelectedItem().toString())){
        				//���sSpinner3
			        	  majors3.clear();
			        	  int temp=0;
			        	  for (int j = 0; j< majorItem.length; j++)	//��}�C�Ȧ�ArraList
					      {
			        			  majorItem3[j] = null;
			        	 }
			        	  for (int j = 0; j< majorItem.length; j++)	//��}�C�Ȧ�ArraList
					      {
			        		  if(i != j){
			        			  majorItem3[temp] = majorItem[j];
			        			  temp++;
			        			  strBug += "majorItem3["+temp+"]"+majorItem3[temp];
			        			  TextView05.append(strBug + "\n");
			        		  }
					      }
			        	  for (int k = 0; k < temp; k++)	//��}�C�Ȧ�ArraList
					      {
					    	  majors3.add(majorItem3[k]);
					    	  //strBug += "majors3["+k+"]"+majorItem3[temp];
		        			  //TextView05.append(strBug + "\n");
					      }
			        	  //��ArrayList�নAdapter
					      adapterForMajors3  = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, majors3);
					      adapterForMajors3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					      majorSpinner3.setAdapter(adapterForMajors3);
        			}
        		}

        	}
        	if(spinnerNumber == 2){	//��ܳo�O�bSpinner2�W���U�h��
        		
        	}
        	if(spinnerNumber == 3){	//��ܳo�O�bSpinner3�W���U�h��
	
        	}
        } */
	   
	   /*���U�Ψ����UToast�bThread�����X�T��
		 * */
		 public void showToast(final String msg) {
			  handler.post(new Runnable() {
			   public void run() {
			    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			   }
			  });
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
						int serverPort = 8078;
						//int serverPort = 8080;
						clientSocket = new Socket(serverIp, serverPort);
						
						// ���o������J��y
						BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
						
						// ��s�u��
						while (clientSocket.isConnected()) {
							showToast("Prop: ���\�s�u!");
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
								}else if(tmp.contains("2234567")){
									//��J�\��
									
								}else if(tmp.contains("3334567")){
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

								
						}
						

					} catch (IOException e) {
						
					}
				}
			};
		 
		 
    }