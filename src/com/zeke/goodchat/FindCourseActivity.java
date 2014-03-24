package com.zeke.goodchat;

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

import com.firebase.client.Firebase;
import com.zeke.goodchat.adapters.CourseListAdapter;

public class FindCourseActivity extends Activity {

  private Firebase ref;
  ListView listview;

  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.course_list);
    
    setTitle("Find Course");
    
    // First we get a reference to the location of the user's name data:
    ref = new Firebase(appURL + "/GlobalChat");

    CourseListAdapter adapter = new CourseListAdapter(this, ref);
    listview =  (ListView) findViewById(R.id.listview_courses);
    listview.setAdapter(adapter);
    
    listview.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        TextView course_name = (TextView) v.findViewById(R.id.textview_course_name);        
        createLectureAlertDialog(course_name.getText().toString());
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
                  Intent startLoginActivity = new Intent(FindCourseActivity.this, GlobalChatActivity.class);
                  startLoginActivity.putExtra("course_name", course_name);
                  startActivity(startLoginActivity);
              }
            })
        .setNeutralButton("Find", 
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                Intent startLectureDatesActivity = new Intent(FindCourseActivity.this, LectureDatesActivity.class);
                startLectureDatesActivity.putExtra("course_name", course_name);
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
  
}
