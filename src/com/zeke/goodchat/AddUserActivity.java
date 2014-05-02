package com.zeke.goodchat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.zeke.goodchat.adapters.UserListAdapter;

/**
 * This activity is used to add new users to the course
 * Activity is invoked by a menu button of Global
 * @author xianggao
 *
 */
public class AddUserActivity extends Activity {
	
	// Database
	private Firebase ref;  
	private final String appURL = "https://intense-fire-8812.firebaseio.com";
	
	// Listview to show current user that have registered in the course, including the creator
	private ListView listview;
	// Edittext, used to input new user name
	private EditText editText;
	private Button addBtn;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_list);
		
		// Course name is formatted as "coursename @ uniquenumber"
		String path[] = getCourseName().split("@ ");
    	ref = new Firebase(appURL + "/GlobalChat/").child(path[1]).child(path[0]);

    	// Using UserListAdapter to inflate the listview
    	UserListAdapter adapter = new UserListAdapter(this, ref);
    	listview =  (ListView) findViewById(R.id.listview_users);
    	listview.setAdapter(adapter);
    	
    	editText = (EditText)findViewById(R.id.add_user_et);
    	addBtn = (Button)findViewById(R.id.add_user_btn);
    	addBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				String user = editText.getText().toString();
				if(user!=null && !user.equals(""))
					addUser(user);
			}
    		
    	});
    	
	}
	
	/**
	 * @return the course name from the intent.
	 */
	private String getCourseName() {
	  return getIntent().getExtras().get("course_name").toString();
	}
	
	/**
	 * @return the user name from the intent.
	 */
	private String getUserName() {
	  return getIntent().getExtras().get("user_name").toString();
	}
	
	/** 
	 * Add a new user as member
	 */
	private void addUser(String user) {
		// Setup the user list. New users are labeled as "member"
		if(!user.equals(getUserName()))
			ref.child("UserList").child(user).setValue("member");
	}
}
