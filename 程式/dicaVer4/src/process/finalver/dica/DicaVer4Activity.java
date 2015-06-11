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
	
	/*----------------------------�p�ɾ�����---------------------------*/
	private Long startTime;
	private Handler handler = new Handler();
	public ProgressDialog myDialog;
	
	/*----------------------------��Ʈw����----------------------------*/
	MySQLiteHelper myHelper;	//��Ʈw���U���O���󪺤ޥ�
	boolean flagForCheckDB = false;
	
	/*----------------------------����������----------------------------
	 * flag������ShowMsgDialog()�̭���if�P�_��
	 * �Ω����UasyncTaskUpdateProgress������諸alertDialog
	 * */
	int flag = 0;	//�Ψӷ�@�����X�ӭ����Ϊ��аO
	
	
    //@SuppressWarnings("null")
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
        
        
        /*�o��]������MeteorMapActivity�u����main�A�ҥH�ڪ�����main�t���L
         * �M��o�̴N�Υt�@��layout*/
        setContentView(R.layout.entrytopiclayout);
        
        startTime = System.currentTimeMillis();
        //�]�w�w�ɭn���檺��k
        handler.removeCallbacks(updateTimer);
        //�]�wDelay���ɶ�
        handler.postDelayed(updateTimer, 1000);
        //selectVersionPage();
        
        
        /*------------------��Ʈw��l����------------------------*/
        myHelper = new MySQLiteHelper(this, "user_db", null, 1);	//�s�ظ�Ʈw���U���O����
        /*�o�̧ڬO���h���Ʈw�A�ݸ�Ʈw�ح����S����ơA�p�G�S���A�N���]��default
         * �]��default�O�@���u�������k�A��ڤW���ӬO�n�����S�r��A�����̭��|���Ӧh��\n�άO\r������
         * �o�ǳ��⦨�r��...�|�y���P�_���v�T�A�޳N�W�n���h�ɶ��ק�d��
         * 
         * 2012/4/4�ץ����P�_�O�_�r�ꬰ��
         * ���\�ظm!�]���o�{�¦���default���覡�A�|�y������ظm����Ʈw���M�ȬO���T��
         * ���]���W�r�@�w���|��default�@�ˡA�ӳy���L�����ꦨ�w�]��
         * */
        String result = queryData(myHelper);	//�V��Ʈw���d�߸��
        if("".equals(result.trim())){	
        	insertAndUpdateData(myHelper);	//�V��Ʈw�����J�M��s��ơA�ت��O��l�w�]��
        	result = queryData(myHelper);	//�V��Ʈw���d�߸��
        	//showToast("result:"+result+", length:"+result.length());
        	
        }
        if("default".equals(result.trim())){
        	flagForCheckDB = true;
        }
        //showToast("result:"+result+", length:"+result.length());
        //showToast("Bruce how are u");
    }
   
    /*----------------------------------��Ʈw�ɯžާ@��ܰ�------------------------------*/
    //��l��Ʈw
    //��k�G�V��Ʈw���������J�M��s���
  	public void insertAndUpdateData(MySQLiteHelper myHelper){
  		SQLiteDatabase db = myHelper.getWritableDatabase();	//�����Ʈw����
  		//�ϥ�execSQL��k�V�����J���
  		db.execSQL("insert into user_info(name,age,career,major1,major2,major3) values('default',20,'default','default','default','default')");	
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
  			result = result + cursor.getString(nameIndex)+"    ";
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
  	
  //���U�O�]�w�p�ɾ����e
    private Runnable updateTimer = new Runnable() {
    	public void run() {
    		Long spentTime = System.currentTimeMillis() - startTime;
    		//�p��ثe�w�L���
    		Long seconds = (spentTime/1000) % 60;
    		if(seconds == 1){
    			selectVersionPage();
    		}
    		handler.postDelayed(this, 1000);
    	}
    };
    
    /*�������ĤG�ӭ���*/
    public void selectVersionPage(){
    	setContentView(R.layout.showwhorulayout);
    	
    	Button b1 = (Button) findViewById(R.id.button1);
    	b1.setOnClickListener(new Button.OnClickListener()
        {
    		
			@Override
			public void onClick(View v) {
				
				/*�ڧ���U���s���᪺���J�e���ʧ@
				 * ��bProcess dialod function��
				 * */
//				new asyncTaskUpdateProgress().onPreExecute();
//				asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     //�]�o���ت��O���ϥΪ̪��D�{�b��Ʈw�nŪ���F
//				task.execute();
				
				if(flagForCheckDB == true){	//��true�h��ܸ�Ʈw�S���]�L
					flag = 1;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();
//					ShowMsgDialog("���~","������z�èS���d��ơA�N�ɨ�]�w�e��",1);
					// TODO Auto-generated method stub
				}else{
					flag = 2;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();  
//					String name = queryData(myHelper);
//					ShowMsgDialog("�w��", name,2);
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
				
				if(flagForCheckDB == true){	//��true�h��ܸ�Ʈw�S���]�L
					flag = 3;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();  
//					ShowMsgDialog("���~","������z�èS���d��ơA�N�ɨ�]�w�e��",3);
					// TODO Auto-generated method stub
				}else{
					flag = 4;
					asyncTaskUpdateProgress task = new asyncTaskUpdateProgress();     
					task.execute();  
//					String name = queryData(myHelper);
//					ShowMsgDialog("�w��", name,4);
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
	    	 
	    	 if(flag == 1){	//��true�h��ܸ�Ʈw�S���]�L
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
    
    
    /*----------------------------���UToast�bThread�����X�T��-------------------------- */
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
					 if(flag == 1){	//flag == 1�h��ܶi�J�]�w����
						    Intent intent = new Intent();
							intent.setClass(DicaVer4Activity.this, PropertySetting.class);
							startActivity(intent);
							DicaVer4Activity.this.finish();
					 }
					 if(flag == 2){	//flag == 2�h��ܶi�J�\�୶��
						    Intent intent = new Intent();
							intent.setClass(DicaVer4Activity.this, MainFunctionActivity.class);
							startActivity(intent);
							DicaVer4Activity.this.finish();
					 }
					 if(flag == 3){	//flag == 3�h��ܶi�J�D�Ϫ̸�Ƴ]�w����
						    Intent intent = new Intent();
							intent.setClass(DicaVer4Activity.this, PropertySettingForRequester.class);
							startActivity(intent);
							DicaVer4Activity.this.finish();
					 }
					 if(flag == 4){	//flag == 4�h��ܶi�J�D�Ϫ̭���
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