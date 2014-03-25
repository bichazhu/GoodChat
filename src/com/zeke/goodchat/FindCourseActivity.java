package com.zeke.goodchat;

import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

  private Firebase ref;
  ListView listview;
  private String username;

  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.course_list);
    
    setTitle("Find Course");
    username = getIntent().getStringExtra("username");
    
    // First we get a reference to the location of the user's name data:
    ref = new Firebase(appURL + "/GlobalChat/");

    CourseListAdapter adapter = new CourseListAdapter(this, ref);
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
   * Creates an Alert Dialog for user to create/find lecture and proceed.
   */
  private void createLectureAlertDialog(final String course_name) {
    
    // get the alert dialog ready for user
    final AlertDialog.Builder alert = new AlertDialog.Builder( FindCourseActivity.this);
    alert.setMessage("Create or Find Lecture?")
        .setPositiveButton("Create",
            new DialogInterface.OnClickListener() {
              public void onClick( final DialogInterface dialog, int whichButton) {
                  Intent GlobalChatActivity = new Intent(FindCourseActivity.this, GlobalChatActivity.class);
                  GlobalChatActivity.putExtra("course_name", course_name);
                  GlobalChatActivity.putExtra("username", username);
                  GlobalChatActivity.putExtra("create", false);
                  startActivity(GlobalChatActivity);
              }
            })
        .setNeutralButton("Find", 
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                Intent startLectureDatesActivity = new Intent(FindCourseActivity.this, LectureDatesActivity.class);
                startLectureDatesActivity.putExtra("course_name", course_name);
                startLectureDatesActivity.putExtra("username", username);
                startActivity(startLectureDatesActivity);
              }
            })
        .setNegativeButton("Cancel",
            new DialogInterface.OnClickListener() {
              public void onClick( DialogInterface dialog, int whichButton) {
                dialog.cancel();
              }
            });
    alert.show();
  }
  
  private void createToast(String msg){
	  Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }
  
  private void checkAccessibility(String courseName){
	  String path[] = courseName.split("@ ");
	  ref.child(path[1]).child(path[0]).child("UserList").child(username).addListenerForSingleValueEvent(new ValueEventListener(){
		@Override
		public void onCancelled(FirebaseError arg0) {
		}

		@Override
		public void onDataChange(DataSnapshot snap) {
			String title = (String) snap.getValue();
			if(title==null)
			{
				createToast("You are not registered in this course!");
			}
			else
			{
				String courseName = snap.getRef().getParent().getParent().getName() + 
									"@ " + snap.getRef().getParent().getParent().getParent().getName();
				createLectureAlertDialog(courseName);
			}
		}
		  
	  });
  }
}
