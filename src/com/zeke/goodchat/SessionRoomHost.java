package com.zeke.goodchat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.firebase.client.Firebase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SessionRoomHost extends Activity {

  //private BeaconThread bt;
  private ReceiveThread rt;

  private String sessionRoomName;
  private String myName;
  private String myIP;
  private String code;

  private TextView tv;
  private String textViewContent;
  private EditText et;
  private Button sendBtn;
  
  private Firebase ref;
  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";

  private int []pollStat = new int[3];
  private ArrayList<String> userList = new ArrayList<String>(); 
  // Hashtable used to take attendance
  private Hashtable<String, Boolean> userAtt = new Hashtable<String, Boolean>(); 
  
  private class UserInfo{
    String name;
    String addr;

    public UserInfo(String n, String a){
      name = n;
      addr = a;
    }
    @Override
    public String toString() {
      return addr+": "+name;
    }
  };

  volatile ArrayList<UserInfo> users = new ArrayList<UserInfo>();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_session_room_host);
    
    // Initial user list
    getUserList(getIntent().getExtras().getString("name_list"));
    
    ref = new Firebase(appURL + "/GlobalChat/");

    tv = (TextView)findViewById(R.id.session_host_textview);
    et = (EditText)findViewById(R.id.session_host_edittext);
    sendBtn = (Button)findViewById(R.id.session_host_sendbtn);
    sendBtn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
        tv.setText(tv.getText()+myName+": "+et.getText().toString()+"\n");
        for(int i=1;i<users.size();i++)
        {
          UserInfo user = users.get(i);
          String dest = user.addr;
          if(dest.equals(myIP))
            continue;
          String msg = "MESSAGE, "+myName+", "+et.getText().toString();
          new SendThread(msg,SessionRoomUtil.SEND_PORT,dest,SessionRoomUtil.RECEIVE_PORT).start();
        }
        et.setText("");
        // Hide input method
        InputMethodManager inputManager = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
               InputMethodManager.HIDE_NOT_ALWAYS);
      }

    });

    sessionRoomName = getIntent().getStringExtra("SessionRoomName");
    myName = getIntent().getStringExtra("UserName");
    myIP = getIntent().getStringExtra("MyIPAddress");
    code = getIntent().getStringExtra("code");
    
    users.add(new UserInfo(myName,myIP));

    setTitle(sessionRoomName);
  }

  @Override
  protected void onStart() {
    super.onStart();

    //bt = new BeaconThread(sessionRoomName);
    //bt.start();

    rt = new ReceiveThread();
    rt.start();
  }

  private class ReceiveThread extends Thread{
    public boolean continueReceiveThread;
    private DatagramSocket receiveSocket;

    public ReceiveThread()
    {
      try {
        receiveSocket = new DatagramSocket(SessionRoomUtil.RECEIVE_PORT);
      } catch (SocketException e) {
        Log.d("DEBUG", "ReceiveThread: SocketException");
      }
    }
    @Override
    public void run() {
      Log.d("DEBUG", "ReceiveThread: Began");
      continueReceiveThread = true;
      try {
        while(continueReceiveThread)
        {
          String msg = SessionRoomUtil.receiveMessage(receiveSocket);
          String []str = msg.split(", ");

          Log.d("DEBUG", "Received a message");
          // Respond "welcome" msg to hello
          if(str[1].equals("HELLO"))
          {
            Log.d("DEBUG", "Received a HELLO message");
            // If code matches, allow user to join
            if(str[3].equals(code))
            {
	            textViewContent = tv.getText()+str[2]+": Enters the room.\n";
	            tv.post(new Runnable(){
	              @Override
	              public void run() {
	                tv.setText(textViewContent);
	              }
	            });
	            String welcomeMsg = "WELCOME, "+sessionRoomName;
	            new SendThread(welcomeMsg,SessionRoomUtil.SEND_PORT,str[0],SessionRoomUtil.RECEIVE_PORT).start();
	
	            UserInfo tmp = new UserInfo(str[2],str[0]);
	            if(!users.contains(tmp))
	            {
	              users.add(tmp);
	            }
            }
            else
            {
            	// Doesn't match, reply a decline message
            	String welcomeMsg = "DECLINE, "+sessionRoomName;
	            new SendThread(welcomeMsg,SessionRoomUtil.SEND_PORT,str[0],SessionRoomUtil.RECEIVE_PORT).start();
            }
          }
          // Respond "beacon" msg to discovery
          if(str[1].equals("DISCOVERY"))
          {
            Log.d("DEBUG", "Received a DISCOVERY message");
            String welcomeMsg = "BEACON, "+sessionRoomName;
            new SendThread(welcomeMsg,SessionRoomUtil.SEND_PORT,str[0],SessionRoomUtil.RECEIVE_PORT).start();
          }
          // "message" msg
          if(str[1].equals("MESSAGE"))
          {
            Log.d("DEBUG", "Received a MESSAGE message");
            textViewContent = tv.getText()+str[2]+": "+str[3]+"\n";
            tv.post(new Runnable(){
              @Override
              public void run() {
                tv.setText(textViewContent);
              }
            });

            for(int i=1;i<users.size();i++)
            {
              UserInfo user = users.get(i);
              String dest = user.addr;
              if(dest.equals(myIP))
                continue;
              String forwardMsg = "MESSAGE, "+str[2]+", "+str[3];
              new SendThread(forwardMsg,SessionRoomUtil.SEND_PORT,dest,SessionRoomUtil.RECEIVE_PORT).start();
            }

          }
          // "poll" msg
          if(str[1].equals("POLL"))
          {
        	if(str[2].equals("A"))
        		pollStat[0] ++;
        	if(str[2].equals("B"))
        		pollStat[1] ++;
        	if(str[2].equals("C"))
        		pollStat[2] ++;
        	
            textViewContent = tv.getText()+"A:"+pollStat[0]+", B:"+pollStat[1]+", C:"+pollStat[2]+"\n";
            tv.post(new Runnable(){
              @Override
              public void run() {
                tv.setText(textViewContent);
              }
            });

          }
          // "end" msg
          if(str[1].equals("END"))
          {
            Log.d("DEBUG", "Received a END message");
            textViewContent = tv.getText()+str[2]+": Leaves the room.\n";
            tv.post(new Runnable(){
              @Override
              public void run() {
                tv.setText(textViewContent);
              }
            });
            UserInfo tmp = new UserInfo(str[2],str[0]);
            if(!users.contains(tmp))
            {
              users.remove(tmp);
            }
          }
        }
        receiveSocket.close();
      } catch (IOException e) {
        if(receiveSocket!=null)
          receiveSocket.close();
        Log.d("DEBUG", "HOST: IOException");
      }
    }

    @Override
    public void interrupt() {
      if(receiveSocket!=null)
        receiveSocket.close();
      super.interrupt();
    }
  };

  private class SendThread extends Thread{
    private final int sendPort;
    private final String destIP;
    private final int receivePort;
    private final String msg;

    public SendThread(String m, int sendP, String dest, int receiveP){
      sendPort = sendP;
      destIP = dest;
      receivePort = receiveP;
      msg = m;
    }
    @Override
    public void run() {
      try {
        DatagramSocket socket = new DatagramSocket(sendPort);
        SessionRoomUtil.sendMessage(socket, msg, destIP, receivePort);
        socket.close();
      } catch (IOException e) {
        Log.d("DEBUG", "HOSTSEND: IOException");
      }
    }
  };


  @Override
  protected void onPause() {
    super.onPause();
    /*
    if(bt.continueBeacon)
      bt.continueBeacon = false;
    */
    if(rt!=null)
      rt.interrupt();
    
    String path[] = sessionRoomName.split("@ ");
	ref.child(path[1]).child(path[0]).child("ClassOn").setValue("none");
  }
  
  /**
   * Menu
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.sessionhost, menu);
    return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
    case R.id.poll:
    	// Send a poll request to all users:
    	textViewContent = tv.getText()+"A poll is raised!\n";
    	tv.post(new Runnable(){
    		@Override
    		public void run() {
    			tv.setText(textViewContent);
    		}
    	});
    	pollStat[0] = 0;
    	pollStat[1] = 0;
    	pollStat[2] = 0;
    	for(int i=1;i<users.size();i++)
    	{
    		UserInfo user = users.get(i);
    		String dest = user.addr;
    		if(dest.equals(myIP))
    			continue;
    		String forwardMsg = "POLL, "+myName;
    		new SendThread(forwardMsg,SessionRoomUtil.SEND_PORT,dest,SessionRoomUtil.RECEIVE_PORT).start();
    	}
    	
    	return true;
    case R.id.attendance:
    	
    	takeAttendance();
    	
    	return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  // Get user list based on the format of user string
  void getUserList(String userString){
	  userList.clear();
	  String []users = userString.split(", ");
	  for(int i=0;i<users.length;i++)
	  {
		  userList.add(users[i]);
		  userAtt.put(users[i], false);
	  }
  }
  
  // Take attendance
  void takeAttendance()
  {
	  for(int i=0;i<users.size();i++)
	  {
		  userAtt.put(users.get(i).name, true);
	  }
	  
	  String names = "";
	  for(int i=0;i<userList.size();i++)
	  {
		  if(!userAtt.get(userList.get(i)))
			  names = names+userList.get(i)+"\n";
		  
	  }
	  textViewContent = tv.getText()+"Following students are not here:\n"+names+"\n";
	  tv.post(new Runnable(){
		  @Override
		  public void run() {
			  tv.setText(textViewContent);
		  }
	  });
  }

}
