package com.zeke.goodchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.zeke.goodchat.adapters.CourseListAdapter;

public class FindCourseActivity extends Activity {

  private Firebase ref_global_chat;
  ListView listview;
  private String username;

  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  private final String ID_SEPARATOR = MainActivity.ID_SEPARATOR;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.course_list);
    
    setTitle("Find Course");
    username = getIntent().getStringExtra("username");
    
    // First we get a reference to the location of the user's name data:
    ref_global_chat = new Firebase(appURL + "/GlobalChat/");

    // set the adapter to the listview showing all available courses
    CourseListAdapter adapter = new CourseListAdapter(this, ref_global_chat);
    listview =  (ListView) findViewById(R.id.listview_courses);
    listview.setAdapter(adapter);
    
    listview.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        TextView course_name = (TextView) v.findViewById(R.id.textview_course_name);        
        checkAccessibility(course_name.getText().toString());
      }
    });
    
  }
  
  /**
   ***************************************************************************
   Helper Methods below this point. 
   ***************************************************************************
   */
  
  
  /**
   * Helper method to show a Toast message for short time to the user.
   * @param message
   */
  private void showShortToast(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }
  
  /**
   * Open GlobalChatActivity
   * @param coursename
   * @param courseID
   * @param title 
   */
  private void openGlobalChat(String coursename, String courseID, String title){
	  Intent GlobalChatActivity = new Intent(FindCourseActivity.this, GlobalChatActivity.class);
	  GlobalChatActivity.putExtra("course_id", courseID);
      GlobalChatActivity.putExtra("course_name", coursename);
      GlobalChatActivity.putExtra("user_name", username);
      GlobalChatActivity.putExtra("title", title);
      startActivity(GlobalChatActivity);
  }
  
  /**
   * Check accessibility of current user to the course
   * Only users in the UserList can access a certain course
   * @param courseName
   */
  private void checkAccessibility(String courseName){
	  String path[] = courseName.split(ID_SEPARATOR);
	  ref_global_chat.child(path[1]).child(path[0]).child("UserList").child(username).addListenerForSingleValueEvent(new ValueEventListener(){
		@Override
		public void onCancelled(FirebaseError arg0) {
		}

		@Override
		public void onDataChange(DataSnapshot snap) {
			String title = (String) snap.getValue();
			if(title == null) {
				showShortToast("You are not registered in this course!");
				
			} else {
				String courseName = snap.getRef().getParent().getParent().getName();
				String courseID = snap.getRef().getParent().getParent().getParent().getName();
				openGlobalChat(courseName, courseID, title);
			}
		}
		  
	  });
  }
  
}
