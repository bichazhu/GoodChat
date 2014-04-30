package com.zeke.goodchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.zeke.goodchat.adapters.LectureDatesListAdapter;

public class LectureDatesActivity extends Activity {

  private Firebase ref;
  private ListView listview;

  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.course_list);
    
    setTitle(getCourseName());
    
    // First we get a reference to the location of the user's name data:
    //ref = new Firebase(appURL + "/GlobalChat/" + getCourseName());
    String path[] = getCourseName().split("@ ");
    ref = new Firebase(appURL + "/GlobalChat/");
    ref = ref.child(path[1]).child(path[0]);

    LectureDatesListAdapter adapter = new LectureDatesListAdapter(this, ref);
    listview =  (ListView) findViewById(R.id.listview_courses);
    listview.setAdapter(adapter);
    
    listview.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        TextView lecture_date = (TextView) v.findViewById(R.id.textview_course_name);
        
        Intent startGlobalChatActivity = new Intent(LectureDatesActivity.this, GlobalChatActivity.class);
        startGlobalChatActivity.putExtra("course_name", getCourseName());
        startGlobalChatActivity.putExtra("lecture_date", lecture_date.getText().toString());
        startGlobalChatActivity.putExtra("create", false);
        startActivity(startGlobalChatActivity);
      }
    });
    
  }
  
  /**
   * @return the course name from the intent.
   */
  private String getCourseName() {
    return getIntent().getExtras().get("course_name").toString();
  }
}
