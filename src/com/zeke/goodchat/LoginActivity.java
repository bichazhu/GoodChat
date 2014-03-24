package com.zeke.goodchat;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;
import com.firebase.simplelogin.User;

public class LoginActivity extends Activity implements OnClickListener {

  private final String TAG = "LoginActivity";
  
  private Firebase reference;
  private SimpleLogin authClient;
  
  private EditText email, password;
  
  private Dialog progressDialog;
  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // make the layout FULLSCREEN and set contentView
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.login);
    
    // start a progress dialog until either we login or we have to login
    progressDialog = ProgressDialog.show(LoginActivity.this, "", "Loading ...", true);
    
    // get the reference from Firebase and authClient
    reference = new Firebase(appURL);
    authClient = new SimpleLogin(reference, this);
    
    // check for user authentication
    authClient.checkAuthStatus(new SimpleLoginAuthenticatedHandler() {
      
      @Override
      public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
        
        // dismiss the dialog once we get a result
        progressDialog.dismiss();
        
        if (error != null) {
          // There was an error performing the check
          showShortToast("Please check internet connection");
          Log.e(TAG, "auth error:" + error.toString());
          
        } else if (user == null) {
          // No user is logged in, let user input login info.
          
        } else {
          // There is a logged in user, start MainActivity.
          startMainActivity();
        }
      }
      
    });
    
    // find the buttons
    Button loginButton = (Button) findViewById(R.id.button_Login);
    loginButton.setOnClickListener(this);
    Button signupButton = (Button) findViewById(R.id.button_SignUp);
    signupButton.setOnClickListener(this);
    
    // login edittexts
    email = (EditText) findViewById(R.id.edittext_Login_Username);
    password = (EditText) findViewById(R.id.edittext_Login_Password);
  }

  @Override
  public void onClick(View v) {
    
    // hide keyboard.
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
    
    // input holders
    String theEmail = email.getText().toString();
    String thePassword = password.getText().toString();
    
    // safe check if there is info entered
    if (theEmail.isEmpty() || thePassword.isEmpty()) {
      showShortToast("Please enter Email and Password");
      return;
    }
    
    switch (v.getId()) {
      case R.id.button_Login:
        
        // login the user
        authClient.loginWithEmail(theEmail, thePassword, new SimpleLoginAuthenticatedHandler() {
  
          @Override
          public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
            if(error != null) {
              // There was an error logging into this account
              Log.e(TAG, "Login auth error:" + error.toString());
              showShortToast(error.toString());
            }
            else {
              // We are now logged in
              startMainActivity();
            }
          }
        });
        break;
      case R.id.button_SignUp:
      default:
        
        authClient.createUser(theEmail, thePassword, new SimpleLoginAuthenticatedHandler() {

          @Override
          public void authenticated(com.firebase.simplelogin.enums.Error error, User user) {
            if(error != null) {
              // There was an error creating this account
              Log.e(TAG, "Signup auth error:" + error.toString());
              showShortToast(error.toString());
            }
            else {
              // We are now logged in
              showShortToast("Account created successfully. Now you can login.");
            }
          }
        });
        break;
    }
  }
  
  @Override
  public void onDestroy() {
    super.onDestroy();
    
    // remove the progress dialog
    if(progressDialog != null){
      progressDialog.dismiss();
      progressDialog = null;
    }
  }
  
  /**
   ***************************************************************************
   Helper Methods below this point. 
   ***************************************************************************
   */
  
  /**
   * Helper method to show a Toast message for short time to the user.
   * @param message
   */
  private void showShortToast(String message) {
    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
  }
  
  /**
   * Save the username int SharedPreferences if needed.
   * Then start MainActivity.
   */
  private void startMainActivity() {

    // save the username to SharedPreferences if there is any data
    if(!email.getText().toString().equals("")) {
      SharedPreferences pref = getApplication().getSharedPreferences("GoodChatPreferences", 0);
      pref.edit().putString("username", getUsername()).commit();
    }
    
    // Start MainActivity
    Intent startMainActivity = new Intent(LoginActivity.this, MainActivity.class);
    startActivity(startMainActivity);
    finish();
  }
  
  /**
   * Extract the first part of the email before the '@' and use it as username.
   * @return username
   */
  private String getUsername() {
    String theEmail = email.getText().toString();
    
    String[] email = theEmail.split("@", 2);
    
    return email[0];
  }
  
}
