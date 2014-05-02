package com.zeke.goodchat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.zeke.goodchat.adapters.ChatListAdapter;

public class GlobalChatActivity extends ListActivity {
  
  // User name
  private String username;
  // User title: creator or member
  private String title;
  // Firebase
  private Firebase ref;
  private ChatListAdapter chatListAdapter;
  // Unique number for the course
  private String uniqueCourseNumber;
  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  // Array that stores all user names
  private ArrayList<String> users = new ArrayList<String>();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.globalchat);

    // Set the username with the login credentials
    setupUsername();
        
    title = getIntent().getStringExtra("title");
    // <Modified by Xiang> Set up the firebase based on the status
    // If the global chat is opened by "create", we need to put a new entry in the database
    if(getIntent().getBooleanExtra("create", false))
    {
    	ref = new Firebase(appURL + "/GlobalChat/").push().child(getCourseName());
    	// User list, set the current user as creator
    	ref.child("UserList").child(username).setValue("creator");
    	title = "creator";
    	// "ClassOn" is where we put host IP address
    	ref.child("ClassOn").setValue("none");
    }
    else
    {
    	String path[] = getCourseName().split("@ ");
    	ref = new Firebase(appURL + "/GlobalChat/").child(path[1]).child(path[0]);
    }
    
    ref = ref.child(getDate());
    uniqueCourseNumber = ref.getParent().getParent().getName();
    
    // Setup our input methods. Enter key on the keyboard or pushing the send button
    EditText inputText = (EditText)findViewById(R.id.messageInput);
    inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                sendMessage();
            }
            return true;
        }
    });

    findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            sendMessage();
        }
    });
    
    getUserList();

  }

@Override
  public void onStart() {
      super.onStart();
      // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
      final ListView listView = getListView();
      // Tell our list adapter that we only want 50 messages at a time
      chatListAdapter = new ChatListAdapter(ref.limit(50), this, R.layout.chat_message, username);
      listView.setAdapter(chatListAdapter);
      chatListAdapter.registerDataSetObserver(new DataSetObserver() {
          @Override
          public void onChanged() {
              super.onChanged();
              listView.setSelection(chatListAdapter.getCount() - 1);
          }
      });
  }
  
  @Override
  public void onStop() {
      super.onStop();
      chatListAdapter.cleanup();
  }
  
  
  /**
   ***************************************************************************
   Helper Methods below this point. 
   ***************************************************************************
   */
  
  /**
   * Gets the username of the current logged in user from SharedPreferences.
   * We get 'NoUserName' if there is no username found. (This is an error).
   * And display it on the Actionbar.
   */
  private void setupUsername() {
    
    // get GoodChatPreferences from SharedPreferences
    SharedPreferences prefs = getApplication().getSharedPreferences("GoodChatPreferences", 0);
    username = prefs.getString("username", "NoUserName");
    
    // sets the title in the actionbar to be the username
    setTitle(username);
  }

  /**
   * Sends the message in the form of a Data object.
   */
  private void sendMessage() {
    EditText inputText = (EditText) findViewById(R.id.messageInput);
    String input = inputText.getText().toString();
    if (!input.equals("")) {
      // Create our Data object to send
      Data chat = new Data(input, username);
      ref.push().setValue(chat);
      inputText.setText("");
    }
  }
  
  /**
   * @return the course name from the intent.
   */
  private String getCourseName() {
    return getIntent().getExtras().get("course_name").toString();
  }

  /**
   * Gets the lecture date in format yyyy,MM,dd,HH:mm
   * @return Lecture's date
   */
  @SuppressLint("SimpleDateFormat")
  private String getDate() { 
    
    /**
     * If there is no lecture_date, then create a new Lecture.
     */
    if(getIntent().getExtras().get("lecture_date") == null) {
      Date date = new Date();
      // <Modified by Xiang> Each day has one chat
      //SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH:mm");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd");
      return sdf.format(date);
    } else {
      // here we get the date in this format 'Lecture from: yyyy-MM-dd at HH:mm'
      String lecture_date = getIntent().getExtras().get("lecture_date").toString();
      
      // <Modified by Xiang> Each day has one chat
      // since we know the date has an predetermined length, we can do this.
      String year = lecture_date.substring(14, 18);
      String month = lecture_date.substring(19, 21);
      String day = lecture_date.substring(22, 24);
      //String hourAndMinutes = lecture_date.substring(28, 33);
      
      StringBuffer sb = new StringBuffer();
      sb.append(year + ",");
      sb.append(month + ",");
      //sb.append(day + ",");
      //sb.append(hourAndMinutes);
      sb.append(day);
      
      Log.v("getDate()", sb.toString());
      
      return sb.toString();
    }
  }
  
  /**
   * Menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.globalchat, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
      case R.id.class_room:
    	String userlist = getUserListString();
    	if(userlist!=null)
    	{
	    	Intent startLocalSessionActivity = new Intent(GlobalChatActivity.this, SessionMainActivity.class);
	    	startLocalSessionActivity.putExtra("course_name", getCourseName());
	      	startLocalSessionActivity.putExtra("user_name", username);
	      	startLocalSessionActivity.putExtra("title",title);
	      	startLocalSessionActivity.putExtra("name_list",userlist);
	        startActivity(startLocalSessionActivity);
    	}
        return true;
      case R.id.find_previous:
    	Intent startLectureDatesActivity = new Intent(GlobalChatActivity.this, LectureDatesActivity.class);
        startLectureDatesActivity.putExtra("course_name", getCourseName());
        startLectureDatesActivity.putExtra("user_name", username);
        startActivity(startLectureDatesActivity);
        return true;
      case R.id.add_user:
    	// Only creator can add new users
    	if(title.equals("creator"))
    	{
    	  Intent startAddUserActivity = new Intent(GlobalChatActivity.this, AddUserActivity.class);
    	  startAddUserActivity.putExtra("course_name", getCourseName());
    	  startAddUserActivity.putExtra("user_name", username);
	      startActivity(startAddUserActivity);
    	}
    	else
    	{
    		Toast.makeText(this, "You can not add user!", Toast.LENGTH_LONG).show();
    	}
	    return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  // Get user list from database
  private void getUserList()
  {
	  users.clear();
	  String path[] = getCourseName().split("@ ");
	  Firebase baseref = new Firebase(appURL + "/GlobalChat/").child(path[1]).child(path[0]);
	  Firebase ref=baseref.child("UserList");
	  ref.addChildEventListener(new ChildEventListener() {
		  @Override
		  public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
			  String username = snapshot.getName();

			  if(!username.equals("")) {
				  users.add(username);
			  }
		  }

		  @Override
		  public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
			  String username = snapshot.getName();

			  if(!username.equals("")) {
				  users.add(username);
			  }
		  }

		  @Override
		  public void onChildRemoved(DataSnapshot snapshot) {
			  String username = snapshot.getName();

			  if(!username.equals("")) {
				  users.add(username);
			  }
		  }

		  @Override
		  public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
			  // We do not need to implement this part.
		  }

		@Override
		public void onCancelled(FirebaseError arg0) {
			// TODO Auto-generated method stub
			
		}
	  });
  }
  
  // Get a name list, format like: "user1, user2, user3, ..."
  // This string will be passed to local session and be used to take attendance.
  private String getUserListString(){
	  if(users.size()==0)
	  {
		  getUserList();
		  return null;
	  }
	  
	  String str = users.get(0);
	  for(int i=1;i<users.size();i++)
	  {
		  str += ", "+users.get(i);
	  }
	  
	  return str;
  }
  
}
