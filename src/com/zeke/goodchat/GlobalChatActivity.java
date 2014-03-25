package com.zeke.goodchat;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.zeke.goodchat.adapters.ChatListAdapter;

public class GlobalChatActivity extends ListActivity {
  
  private String username;
  private Firebase ref;
  private ChatListAdapter chatListAdapter;
  private String uniqueCourseNumber;
  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.globalchat);

    // Set the username with the login credentials
    setupUsername();
        
    // First we get a reference to the location of the user's name data:
    //ref = new Firebase(appURL + "/GlobalChat/" + getCourseName() + "/" + getDate());
    if(getIntent().getBooleanExtra("create", true))
    	ref = new Firebase(appURL + "/GlobalChat/").push().child(getCourseName());
    else
    {
    	String path[] = getCourseName().split("@ ");
    	ref = new Firebase(appURL + "/GlobalChat/").child(path[1]).child(path[0]);
    }
    // User list, the first user is the creator
    ref.child("UserList").child(username).setValue("Creator");
    setupUserList();
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

  }

  private void setupUserList() {
	  // Setup the user list: an example!!!
	  ref.child("UserList").child("user2").setValue("member");
	  ref.child("UserList").child("user3").setValue("member");
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
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH:mm");
      return sdf.format(date);
    } else {
      // here we get the date in this format 'Lecture from: yyyy-MM-dd at HH:mm'
      String lecture_date = getIntent().getExtras().get("lecture_date").toString();
      
      // since we know the date has an predetermined length, we can do this.
      String year = lecture_date.substring(14, 18);
      String month = lecture_date.substring(19, 21);
      String day = lecture_date.substring(22, 24);
      String hourAndMinutes = lecture_date.substring(28, 33);
      
      StringBuffer sb = new StringBuffer();
      sb.append(year + ",");
      sb.append(month + ",");
      sb.append(day + ",");
      sb.append(hourAndMinutes);
      
      Log.v("getDate()", sb.toString());
      
      return sb.toString();
    }
  }
}
