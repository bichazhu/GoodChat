package com.zeke.goodchat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class SessionRoomJoin extends Activity {

  private String courseID;
  private String courseName;
  private String userName;
  private String hostIP;
  private Firebase ref;
	  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";

  private TextView tv;
  private Button btn;
  private EditText et;
  
  private SenseThread st;
 

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_join_session_room);
    
    tv = (TextView)findViewById(R.id.join_session_hostIP);
    
    userName = getIntent().getExtras().getString("user_name");
    courseID = getIntent().getExtras().getString("course_id");
    courseName = getIntent().getExtras().getString("course_name");
    
    // First we get a reference to the location of the user's name data:
    ref = new Firebase(appURL + "/GlobalChat/");
    ref.child(courseID).child(courseName).child("ClassOn").addListenerForSingleValueEvent(new ValueEventListener(){
		@Override
		public void onCancelled(FirebaseError arg0) {
		}

		@Override
		public void onDataChange(DataSnapshot snap) {
			hostIP = (String) snap.getValue();
			tv.setText(hostIP);
		}
		  
	  });
    
    et = (EditText)findViewById(R.id.join_session_code);
    
    btn = (Button)findViewById(R.id.join_session_button);
    btn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
    	if(!hostIP.equals("none"))
    	{
	        // Send a hello msg to server
	        String helloMsg = "HELLO, "+userName+", "+et.getText().toString();
	        new SendThread(helloMsg,SessionRoomUtil.SEND_PORT,hostIP,SessionRoomUtil.RECEIVE_PORT).start();
    	}
      }

    });
    

  }

  @Override
  protected void onStart() {
    super.onStart();

    st = new SenseThread();
    st.start();
  }



  private class SenseThread extends Thread{
    public boolean continueSenseThread;
    private DatagramSocket receiveSocket = null;

    public SenseThread(){
      try {
        receiveSocket = new DatagramSocket(SessionRoomUtil.RECEIVE_PORT);
      } catch (SocketException e) {
        Log.d("DEBUG", "SenseThread: SocketException");
      }
    }
    @Override
    public void run() {
      Log.d("DEBUG","SenseThread: Began");
      continueSenseThread = true;
      try {
        while(continueSenseThread)
        {
          String msg = SessionRoomUtil.receiveMessage(receiveSocket);
          String []str = msg.split(", ");
          if(str[1].equals("WELCOME"))
          {
            Intent intent = new Intent(SessionRoomJoin.this,SessionRoomClient.class);
            intent.putExtra("SessionRoomName", str[2]);
            intent.putExtra("SessionRoomIP", str[0]);
            intent.putExtra("UserName", userName);
            startActivity(intent);
          }
          if(str[1].equals("DECLINE"))
          {
            createToast("Wrong code. Request is declined!");
          }
        }
        receiveSocket.close();
      } catch (IOException e) {
        if(receiveSocket!=null)
          receiveSocket.close();
        Log.d("DEBUG","SenseThread: IOException");
      }
      Log.d("DEBUG","SenseThread: Stopped");
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
        e.printStackTrace();
      }
    }
  };

  public void createToast(final String toast)
  {
      runOnUiThread(new Runnable() {
          public void run()
          {
              Toast.makeText(SessionRoomJoin.this, toast, Toast.LENGTH_SHORT).show();
          }
      });
  }

  // Do the endian conversion
  int little2big(int i) {
    return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
  }

  @Override
  protected void onPause() {
    if(st!=null)
      st.interrupt();
    super.onPause();
  }

}
