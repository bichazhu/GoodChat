package com.zeke.goodchat.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.zeke.goodchat.R;

/**
 * This adapter will retrieve information from our UserList.
 * @author Bin
 *
 */
public class UserListAdapter extends BaseAdapter {

	private final String TAG = "UserListAdapter";
	private LayoutInflater layoutInflater;
	private ArrayList<String> users;
	
	private Firebase ref_to_userlist;
	private Firebase ref_to_user;
	private boolean isAddingUser;

	/**
	 * Helper class to hold the view of our results.
	 */
	static class ViewHolder {
		TextView textview_username;
		Button button_remove_user;
	}

	public UserListAdapter(Activity activity, Firebase ref_to_course, boolean isAddingUser) {

		// Set our adapter values
		layoutInflater = LayoutInflater.from(activity);
		ref_to_userlist = ref_to_course.child("UserList");
		this.isAddingUser = isAddingUser;

		// Store here the users we retrieve for this adapter
		users = new ArrayList<String>();
		
		// Set this listener to update our 'users' array
		ref_to_userlist.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
				String username = snapshot.getName();

				if (!username.equals("")) {
					users.add(username);
				}
				notifyDataSetChanged();
			}

			@Override
			public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
				String username = snapshot.getName();

				if (!username.equals("")) {
					users.add(username);
				}
				notifyDataSetChanged();
			}

			@Override
			public void onChildRemoved(DataSnapshot snapshot) {
				String username = snapshot.getName();

				if (!username.equals("")) {
					users.remove(username);
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
	public View getView(final int index, View convertView, ViewGroup viewGroup) {

		View view = convertView;
		
		// reuse views using a holder. Which is a bit faster than regular findViewByid every time
		if (view == null) {
			
			// inflate the view
			view = layoutInflater.inflate(R.layout.user_item, viewGroup, false);
			
			// configure view holder
			ViewHolder viewHolder = new ViewHolder();
			
			// find the features
			viewHolder.textview_username = (TextView) view.findViewById(R.id.textview_course_name);		
			viewHolder.button_remove_user = (Button) view.findViewById(R.id.button_remove_user);
			    
			view.setTag(viewHolder);
		}
		
	    // fill data on this holder
	    ViewHolder holder = (ViewHolder) view.getTag();
        
		// set username
	    holder.textview_username.setText(users.get(index));

    	// Hide the 'remove button' if we are adding users
	    if(isAddingUser) {
	    	holder.button_remove_user.setVisibility(View.GONE);
	    	
	    } else {
	    	
	    	// set button feature to remove from database
		    holder.button_remove_user.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {	
					ref_to_user = ref_to_userlist.child(users.get(index));
					ref_to_user.removeValue();
				}
			});
	    }
	    
		return view;
	}
}
