package com.zeke.goodchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.zeke.goodchat.adapters.LectureDatesListAdapter;

public class LectureDatesActivity extends Activity {

  private Firebase ref;
  private ListView listview;
  private String courseName;
  private String courseID;
  private String userTitle;

  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.list);
    
    courseID = getIntent().getStringExtra("course_id");
    courseName = getIntent().getStringExtra("course_name");
    userTitle = getIntent().getStringExtra("title");
    setTitle(courseName);
    
    // First we get a reference to the location of the user's name data:
    ref = new Firebase(appURL + "/GlobalChat/");
    ref = ref.child(courseID).child(courseName);

    LectureDatesListAdapter adapter = new LectureDatesListAdapter(this, ref);
    listview =  (ListView) findViewById(R.id.listview_list);
    listview.setAdapter(adapter);
    
    listview.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        TextView lecture_date = (TextView) v.findViewById(R.id.textview_course_name);
        
        Intent startGlobalChatActivity = new Intent(LectureDatesActivity.this, GlobalChatActivity.class);
        startGlobalChatActivity.putExtra("course_id", courseID);
        startGlobalChatActivity.putExtra("course_name", courseName);
        startGlobalChatActivity.putExtra("lecture_date", lecture_date.getText().toString());
        startGlobalChatActivity.putExtra("create", false);
        startGlobalChatActivity.putExtra("title", userTitle);
        startActivity(startGlobalChatActivity);
      }
    });
    
  }
  
}
