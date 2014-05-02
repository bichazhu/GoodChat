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

public class UserListAdapter extends BaseAdapter {

  private final String TAG = "UserListAdapter";
  private LayoutInflater layoutInflater;
  private ArrayList<String> users = new ArrayList<String>();

  public UserListAdapter(Activity activity, Firebase baseref) {

	  layoutInflater = LayoutInflater.from(activity);
	  Firebase ref=baseref.child("UserList");
	  ref.addChildEventListener(new ChildEventListener() {
		  @Override
		  public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
			  String username = snapshot.getName();

			  if(!username.equals("")) {
				  users.add(username);
			  }
			  notifyDataSetChanged();
		  }

		  @Override
		  public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
			  String username = snapshot.getName();

			  if(!username.equals("")) {
				  users.add(username);
			  }
			  notifyDataSetChanged();
		  }

		  @Override
		  public void onChildRemoved(DataSnapshot snapshot) {
			  String username = snapshot.getName();

			  if(!username.equals("")) {
				  users.add(username);
			  }
			  notifyDataSetChanged();
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
	  return users.size();
  }

  @Override
  public Object getItem(int i) {
	  return users.get(i);
  }

  @Override
  public long getItemId(int i) {
	  return i;
  }

  @Override
  public View getView(int index, View view, ViewGroup viewGroup) {

	  view = layoutInflater.inflate(R.layout.course, viewGroup, false);

	  TextView user = (TextView) view.findViewById(R.id.textview_course_name);
	  user.setText(users.get(index));

	  return view;
  }
}
