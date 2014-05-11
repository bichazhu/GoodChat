package com.zeke.goodchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SessionMainActivity extends Activity {
	
  private Button createBtn;
  private Button joinBtn;
  private String title;
  private String courseID;
  private String courseName;
  private String userName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // Get information from previous activity
    title = getIntent().getExtras().getString("title");
    courseID = getIntent().getExtras().getString("course_id");
    courseName = getIntent().getExtras().getString("course_name");
    userName = getIntent().getExtras().getString("user_name");

	// Create button is used to create a new session
    createBtn = (Button)findViewById(R.id.create_btn);
    createBtn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
    	// Only creator can hold a class
		if (title.equals("creator")) {
	        Intent intent = new Intent(SessionMainActivity.this,SessionRoomCreate.class);
	        intent.putExtra("course_id", courseID);
	        intent.putExtra("course_name", courseName);
	        intent.putExtra("user_name", userName);
	        intent.putExtra("name_list", getIntent().getExtras().getString("name_list"));
	        startActivity(intent);
    	} else {
    		showShortToast("You can't host a class!");
    	}
      }

    });
    
    // Join button is used to join an exsisting session
    joinBtn = (Button)findViewById(R.id.join_btn);
    joinBtn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
        Intent intent = new Intent(SessionMainActivity.this,SessionRoomJoin.class);
        intent.putExtra("course_id", courseID);
        intent.putExtra("course_name", courseName);
        intent.putExtra("user_name", userName);
        startActivity(intent);
      }

    });
    
    
  }
  
  /**
   * Helper method to show a Toast message for short time to the user.
   * @param message
   */
  private void showShortToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }


}
