package process.finalver.dica;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class ToolsActivity extends Activity {
	public  void  onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.toolview);  
        
        
        /*---------------------------------------------------------------------------------
         * ���U�ΨӳB�z��������ϰ�
         * --------------------------------------------------------------------------------*/
        //���U��TabView���϶�
        ImageButton tabBtn = (ImageButton)findViewById(R.id.imageButton1);
        tabBtn.setOnClickListener(new ImageButton.OnClickListener()
        {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(ToolsActivity.this, CompassActivity.class);
				startActivity(intent);
				ToolsActivity.this.finish();
			}
        	
        });
    } 
}
