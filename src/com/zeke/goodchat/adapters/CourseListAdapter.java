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
import com.zeke.goodchat.MainActivity;
import com.zeke.goodchat.R;

public class CourseListAdapter extends BaseAdapter {

	private final String TAG = "CourseListAdapter";
	private LayoutInflater layoutInflater;
	private ArrayList<String> courses = new ArrayList<String>();
	private final String ID_SEPARATOR = MainActivity.ID_SEPARATOR;

	public CourseListAdapter(Activity activity, Firebase ref) {

		layoutInflater = LayoutInflater.from(activity);

		ref.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
				String courseName = snapshot.getChildren().iterator().next().getName() + ID_SEPARATOR + snapshot.getName();
				courses.add(courseName);

				notifyDataSetChanged();
			}

			@Override
			public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
				String courseName = snapshot.getChildren().iterator().next().getName() + ID_SEPARATOR + snapshot.getName();
				courses.set(courses.indexOf(courseName), courseName);

				notifyDataSetChanged();
			}

			@Override
			public void onChildRemoved(DataSnapshot snapshot) {
				String courseName = snapshot.getChildren().iterator().next().getName()+ ID_SEPARATOR + snapshot.getName();
				courses.remove(courses.indexOf(courseName));
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
		return courses.size();
	}

	@Override
	public Object getItem(int i) {
		return courses.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {

		view = layoutInflater.inflate(R.layout.course, viewGroup, false);

		TextView course_name = (TextView) view.findViewById(R.id.textview_course_name);
		course_name.setText(courses.get(index));

		return view;
	}

}
