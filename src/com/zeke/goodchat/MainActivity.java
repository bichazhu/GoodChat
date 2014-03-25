package com.zeke.goodchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.simplelogin.SimpleLogin;

/**
 * This class contains the main view for the user to interact with the app.
 * 
 * @author Bin
 */
public class MainActivity extends Activity implements OnClickListener {

  private String username;
  private Firebase ref;
  private SimpleLogin authClient;
  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Set the username with the login credentials
    setupUsername();

    // First we get a reference to the location of the user's name data:
    ref = new Firebase(appURL);
    authClient = new SimpleLogin(ref, this);
    
//    authClient.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
//      @Override
//      public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
//        if (error != null) {
//          // Oh no! There was an error performing the check
//          showShortToast("Oh no! There was an error performing the check");
//        } else if (user == null) {
//          // No user is logged in
//          showShortToast("No user is logged in");
//        } else {
//          // There is a logged in user
//          showShortToast("There is a logged in user");
//        }
//      }
//    });

    // find the buttons and set them.
    Button createCourse = (Button) findViewById(R.id.button_create_course);
    createCourse.setOnClickListener(this);
    Button findCourse = (Button) findViewById(R.id.button_find_course);
    findCourse.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.button_create_course:
        createNewCourseAlertDialog();
        break;
        
      case R.id.button_find_course:
      default:
        Intent startFindCourseActivity = new Intent(MainActivity.this, FindCourseActivity.class);
        startActivity(startFindCourseActivity);
        break;
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
      case R.id.logout:
        // Logout from Application
        authClient.logout();
        
        Intent startLoginActivity = new Intent(MainActivity.this, LoginActivity.class);
        finish();
        startActivity(startLoginActivity);
        
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
    username = prefs.getString("username", "NoUserName");
    
    // sets the title in the actionbar to be the username
    setTitle(username);
  }

  /**
   * Helper method to show a Toast message for short time to the user.
   * @param message
   */
  private void showShortToast(String message) {
    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
  }
  
  /**
   * Creates an Alert Dialog for user to input the course name and proceed.
   */
  private void createNewCourseAlertDialog() {
    LayoutInflater factory = LayoutInflater.from(MainActivity.this);

    // get the layout view and edit text for the course name
    final View textEntryView = factory.inflate(R.layout.create_new_course, null);
    final EditText edittext_new_course_name = (EditText) textEntryView.findViewById(R.id.edittext_new_course_name);
    
    // get the alert dialog ready for user
    final AlertDialog.Builder alert = new AlertDialog.Builder( MainActivity.this);
    alert.setTitle("Create new Course Chat")
        .setView(textEntryView)
        .setPositiveButton("Create",
            new DialogInterface.OnClickListener() {
              public void onClick( final DialogInterface dialog, int whichButton) {
                String course_name = edittext_new_course_name.getText().toString();
                
                if(!course_name.isEmpty()) {
                  Intent startGlobalChatActivity = new Intent(MainActivity.this, GlobalChatActivity.class);
                  startGlobalChatActivity.putExtra("course_name", course_name);
                  startGlobalChatActivity.putExtra("create", true);
                  startActivity(startGlobalChatActivity);
                } else {
                  showShortToast("Please enter a course name");
                }
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
