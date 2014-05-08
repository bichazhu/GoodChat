package com.zeke.goodchat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.zeke.goodchat.adapters.ChatListAdapter;
import com.zeke.goodchat.adapters.UserListAdapter;

/**
 * This class will create a Global chat for all to be able to communicate.
 * 
 * @author Bin
 * 
 */
public class GlobalChatActivity extends ListActivity {

	// Current User
	private String currentUser;

	// User title: creator or member
	private String userTitle;

	// Firebase
	private Firebase ref_to_course;
	private Firebase ref_to_specific_date;
	private ChatListAdapter chatListAdapter;

	// App URL
	private final String appURL = "https://intense-fire-8812.firebaseio.com";

	// Course Name and Unique ID
	private String courseName;
	private String courseID;

	// Array that stores all user names
	private ArrayList<String> users;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.globalchat);

		// Prepare the list of users
		users = new ArrayList<String>();
		
		// Set the username with the login credentials
		setupUsername();

		userTitle = getIntent().getStringExtra("title");
		courseID = getIntent().getStringExtra("course_id");
		courseName = getIntent().getStringExtra("course_name");

		// If courseUniqueId == "", we need to put a new entry in the database
		if (courseID.equals("")) {

			// Make and get a new reference to the newly created course
			ref_to_course = new Firebase(appURL + "/GlobalChat/").push().child(courseName);
			courseID = ref_to_course.getParent().getName();

			// In UserList, set the current user as creator
			ref_to_course.child("UserList").child(currentUser).setValue("creator");
			userTitle = "creator";

			// "ClassOn" is where we put host IP address. For now we put a default of 'none'.
			ref_to_course.child("ClassOn").setValue("none");

		} else {

			// We get reference to this already-created-course
			ref_to_course = new Firebase(appURL + "/GlobalChat/").child(courseID).child(courseName);

		}

		// sets the title
		if (userTitle.equals("creator")) {
			setTitle(currentUser + " (creator - " + courseName + ")");
		} else {
			setTitle(currentUser + " (member - " + courseName + ")");
		}

		// Get the User list ready
		getUserList();

		// reference to the specific class date
		ref_to_specific_date = ref_to_course.child(getDate());

		// Setup our input methods. Enter key on the keyboard or pushing the send button
		EditText inputText = (EditText) findViewById(R.id.messageInput);
		inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
				if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
					sendMessage();
				}
				return true;
			}
		});

		ImageButton sendMsg = (ImageButton) findViewById(R.id.sendButton);
		sendMsg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendMessage();
			}
		});

	}

	@Override
	public void onStart() {
		super.onStart();
		
		// Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
		final ListView listView = getListView();
		
		// Tell our list adapter that we only want 50 messages at a time
		chatListAdapter = new ChatListAdapter(ref_to_specific_date.limit(50), this, R.layout.chat_message, currentUser);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.globalchat, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// if we are not the creator, we hide some features
		if (!userTitle.equals("creator")) {

			MenuItem add_user = menu.findItem(R.id.add_user);
			MenuItem remove_course = menu.findItem(R.id.remove_course);

			if (add_user != null) {
				add_user.setVisible(false);
			}
			if (remove_course != null) {
				remove_course.setVisible(false);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
	
			case R.id.class_room:
				
				String userlist = getUserListString();
				if (userlist != null) {
	
					// start SessionMainActivity and send values we will need
					Intent startLocalSessionActivity = new Intent(GlobalChatActivity.this, SessionMainActivity.class);
					startLocalSessionActivity.putExtra("course_name", courseName);
					startLocalSessionActivity.putExtra("course_id", courseID);
					startLocalSessionActivity.putExtra("user_name", currentUser);
					startLocalSessionActivity.putExtra("title", userTitle);
					startLocalSessionActivity.putExtra("name_list", userlist);
					startActivity(startLocalSessionActivity);
				}
				return true;
	
			case R.id.find_previous:
				
				// start LectureDatesActivity and send values we will need
				Intent startLectureDatesActivity = new Intent(GlobalChatActivity.this, LectureDatesActivity.class);
				startLectureDatesActivity.putExtra("course_name", courseName);
				startLectureDatesActivity.putExtra("course_id", courseID);
				startLectureDatesActivity.putExtra("user_name", currentUser);
				startLectureDatesActivity.putExtra("title", userTitle);
				startActivity(startLectureDatesActivity);
				return true;
	
			case R.id.add_user:
				showAddUserDialog();
				return true;
	
			case R.id.remove_course:
				
				// Check if user is the creator of the course
				if (userTitle.equals("creator")) {
					showDeleteConfirmationDialog();
				} else {
					showShortToast("You did not create this course.\nCan not remove this course!");
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
		currentUser = prefs.getString("username", "NoUserName");

	}

	/**
	 * Helper method to show a Toast message for short time to the user.
	 * 
	 * @param message
	 */
	private void showShortToast(String message) {
		Toast.makeText(GlobalChatActivity.this, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Sends the message in the form of a Data object.
	 */
	private void sendMessage() {
		EditText inputText = (EditText) findViewById(R.id.messageInput);
		String input = inputText.getText().toString();

		if (!input.equals("")) {

			// Create our Data object to send
			Data chat = new Data(input, currentUser);
			ref_to_specific_date.push().setValue(chat);

			// reset the inputText and dereference the Data object
			inputText.setText("");
			chat = null;
		}
	}

	/**
	 * Gets the lecture date in format yyyy,MM,dd
	 * 
	 * @return Lecture's date
	 */
	@SuppressLint("SimpleDateFormat")
	private String getDate() {

		/**
		 * If there is no lecture_date, then create a new Lecture.
		 */
		if (getIntent().getExtras().get("lecture_date") == null) {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd");
			return sdf.format(date);

		} else {

			// here we get the date in this format 'Lecture from: yyyy-MM-dd'
			String lecture_date = getIntent().getExtras().get("lecture_date").toString();

			// since we know the date has an predetermined length, we can do this.
			String year = lecture_date.substring(14, 18);
			String month = lecture_date.substring(19, 21);
			String day = lecture_date.substring(22, 24);

			StringBuffer sb = new StringBuffer();
			sb.append(year + ",");
			sb.append(month + ",");
			sb.append(day);

			Log.v("getDate()", sb.toString());

			return sb.toString();
		}
	}

	/**
	 * Get user list from database
	 */
	private void getUserList() {
		
		if(users != null) {
			
			// clear the current list
			users.clear();
			
			Firebase baseref = new Firebase(appURL + "/GlobalChat/").child(courseID).child(courseName);
			Firebase ref = baseref.child("UserList");
			ref.addChildEventListener(new ChildEventListener() {
				@Override
				public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
					String username = snapshot.getName();
	
					if (!username.equals("")) {
						users.add(username);
					}
				}
	
				@Override
				public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
					String username = snapshot.getName();
	
					if (!username.equals("")) {
						users.add(username);
					}
				}
	
				@Override
				public void onChildRemoved(DataSnapshot snapshot) {
					String username = snapshot.getName();
	
					if (!username.equals("")) {
						users.remove(username);
					}
				}
	
				@Override
				public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
					// We do not need to implement this part.
				}
	
				@Override
				public void onCancelled(FirebaseError arg0) {
					// We do not need to implement this part.
				}
			});
		}
	}

	/**
	 * Get a name list, format like: "user1, user2, user3, ..." This string will
	 * be passed to local session and be used to take attendance.
	 * 
	 * @return list or ""
	 */
	private String getUserListString() {
		
		if(users != null) {
			if (users.size() == 0) {
				getUserList();
				return null;
			}
	
			String str_list = users.get(0);
			for (int i = 1; i < users.size(); i++) {
				str_list += ", " + users.get(i);
			}
			
			return str_list;
		}

		return "";
	}

	/**
	 * Show an AlertDialog for user to confirm deletion of course
	 */
	private void showDeleteConfirmationDialog() {

		AlertDialog.Builder alert = new AlertDialog.Builder(GlobalChatActivity.this);
		alert.setTitle("Delete Course")
			.setPositiveButton("Delete",
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, int whichButton) {
							ref_to_course.removeValue();
							finish(); // finish this activity and return to the caller
						}
					})
			.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.cancel();
						}
					});
		alert.show();

	}

	/**
	 * This method will add the new user if we are the creator of this course.
	 */
	private void showAddUserDialog() {

		// Only creator can add new users
		if (!userTitle.equals("creator")) {
			showShortToast("You did not create this course.\nCan not add user!");
			return;
		}

		// get the view ready
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(R.layout.activity_user_list, null);

		// set the adapter in the listview
		final UserListAdapter adapter = new UserListAdapter(this, ref_to_course);
		ListView lv = (ListView) view.findViewById(R.id.listview_users);
		lv.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		final EditText et_user = (EditText) view.findViewById(R.id.edittext_add_user);

		Button add_user = (Button) view.findViewById(R.id.button_add_user);
		add_user.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String user = et_user.getText().toString();
				if (user != null && !user.equals("") && !user.equals(currentUser)) {

					// Setup the user list. New users are labeled as "member"
					ref_to_course.child("UserList").child(user).setValue("member");
					adapter.notifyDataSetChanged();

					// reset the edittext
					et_user.setText("");
				}
			}
		});

		// Set the dialog features and show it.
		Dialog dialog = new Dialog(this);
		dialog.setContentView(view);
		dialog.setTitle("Add users");
		dialog.setCancelable(true);
		dialog.show();
	}
	
}
