package com.qburst.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import loggers
import com.qburst.logger.*;

public class QLogDemoActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	/* Setup logging for the application */
    	QLog.setupLogging(getApplication());
    	
    	super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.main);
        
        final Button btnRegularLog = (Button) findViewById(R.id.btnRegularLog);
        final Button btnCrashLog = (Button) findViewById(R.id.btnCrashLog);
        final EditText edittext = (EditText) findViewById(R.id.txtRegularLogText);
        
        btnRegularLog.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v){
				
				Toast toast = Toast.makeText(getApplicationContext(),
						"Logging " + edittext.getText() + "....", 1000);
				toast.show();
				/*
				 * This will show up in Android system log (check Logcat view)
				 * as well as send a message to the QLog server at Qburst.
				 */
				QLog.d("DEMOAPP", edittext.getText().toString()); 
				
			}
		});
        
        btnCrashLog.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {

				/* Try and generate a crash
				 * findViewById(13323) will not resolve to any allocated instance of an EditText
				 * Any usage of the handle crashEdit to access instance members will cause a crash
				 */
				EditText crashEdit= (EditText) findViewById(13323); 

				Toast toast = Toast.makeText(getApplicationContext(),
						"Logging " + crashEdit.getText() + "....", 1000);
				toast.show();
				
			}
		});
        
    }
}