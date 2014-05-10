package com.zeke.goodchat.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.zeke.goodchat.R;

public class LectureDatesListAdapter extends BaseAdapter {

  private final String TAG = "LectureDatesListAdapter";
  private LayoutInflater layoutInflater;
  private ArrayList<String> courseDates = new ArrayList<String>();
  
    public LectureDatesListAdapter(Activity activity, Firebase ref) {
      
      layoutInflater = LayoutInflater.from(activity);
      
      ref.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
            String courseDate = snapshot.getName();
            if(!courseDate.equals("UserList") && !courseDate.equals("ClassOn"))
            {
	            courseDate = formatCourseDate(courseDate);
	            
	            if(!courseDate.equals("")) {
	              courseDates.add(courseDate);
	            }
	            notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
          String courseDate = snapshot.getName();
          if(!courseDate.equals("UserList") && !courseDate.equals("ClassOn"))
          {
        	  courseDate = formatCourseDate(courseDate);
	          
	          if(!courseDate.equals("")) {
	            courseDates.set(courseDates.indexOf(courseDate), courseDate);
	          }
	          
	          notifyDataSetChanged();
          }
        }

        @Override
        public void onChildRemoved(DataSnapshot snapshot) {
          String courseDate = snapshot.getName();
          if(!courseDate.equals("UserList") && !courseDate.equals("ClassOn"))
          {
        	  courseDate = formatCourseDate(courseDate);
	          if(!courseDate.equals("")) {
	            courseDates.remove(courseDates.indexOf(courseDate));
	          }
	          notifyDataSetChanged();
          }
        }

        @Override
        public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
          // We do not need to implement this part.
        }

        @Override
        public void onCancelled(FirebaseError error) {
          Log.e(TAG, "Error:" + error.getMessage());
        }
      });
    }


    @Override
    public int getCount() {
        return courseDates.size();
    }

    @Override
    public Object getItem(int i) {
        return courseDates.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
      
      view = layoutInflater.inflate(R.layout.course_item, viewGroup, false);
      
      TextView course_date = (TextView) view.findViewById(R.id.textview_course_name);
      course_date.setText(courseDates.get(index));
      
      return view;
    }

    /**
     * 
     * @param courseDate has the format of yyy,MM,dd,HH:mm
     * @return this format: Lecture from: yyyy-MM-dd at HH:mm
     * // <Modified by Xiang> Each day has one chat: format: yyyy-MM-dd
     */
    private String formatCourseDate(String courseDate) {
      
      // save check
      if(courseDate == null || courseDate.isEmpty()) {
        return "";
      }
      
      StringBuffer sb = new StringBuffer();
      
      // Splits the whole string delimited by ','
      String[] splittedDate = courseDate.split(",", -1);
      
      // since we know the format of the date, we won't get NullPointer here.
      sb.append("Lecture from: ");
      sb.append(splittedDate[0] + "-");
      sb.append(splittedDate[1] + "-");
      sb.append(splittedDate[2]);
      //sb.append(splittedDate[2] + " at ");
      //sb.append(splittedDate[3]);
      
      return sb.toString();
    }
}
