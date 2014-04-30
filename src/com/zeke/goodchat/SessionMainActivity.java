package com.zeke.goodchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SessionMainActivity extends Activity {


  private Button createBtn;
  private Button joinBtn;
  private String title;
  private String courseName;
  private String userName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    title = getIntent().getExtras().getString("title");
    courseName = getIntent().getExtras().getString("course_name");
    userName = getIntent().getExtras().getString("username");

    createBtn = (Button)findViewById(R.id.create_btn);
    createBtn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
    	if(title.equals("creator"))
    	{
	        Intent intent = new Intent(SessionMainActivity.this,SessionRoomCreate.class);
	        intent.putExtra("course_name", courseName);
	        intent.putExtra("user_name", userName);
	        startActivity(intent);
    	}
    	else
    		makeToast("You can't host a class!");
      }

    });
    
    joinBtn = (Button)findViewById(R.id.join_btn);
    joinBtn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
        Intent intent = new Intent(SessionMainActivity.this,SessionRoomJoin.class);
        intent.putExtra("course_name", courseName);
        intent.putExtra("user_name", userName);
        startActivity(intent);
      }

    });
    
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }
  
  private void makeToast(String msg)
  {
	  Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }


}
