package process.finalver.dica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class SetActivity extends Activity {
	
	Handler handler = new Handler();
	
	public  void  onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.setview2);  
        
        Button b1 = (Button) findViewById(R.id.setbutton1);
        b1.setOnClickListener(new Button.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					Intent intent = new Intent();
					intent.setClass(SetActivity.this, PropertySetting.class);
					startActivity(intent);
					SetActivity.this.finish();
					
				}catch(Exception e){
					showToast("Error:" + e);
				}
				
			}
        	
        });
    }
	
	public void showToast(final String msg) {
		  handler.post(new Runnable() {
		   public void run() {
		    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
		   }
		  });
	 }
}
