package com.zeke.goodchat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SessionRoomClient extends Activity {

  private String sessionRoomName;
  private String myName;
  private String sessionRoomIP;

  private ReceiveThread rt;

  private TextView tv;
  private String textViewContent;
  private EditText et;
  private Button sendBtn;
  
  private Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_session_room_client);
    
    mHandler = new Handler();

    sessionRoomIP = getIntent().getStringExtra("SessionRoomIP");
    sessionRoomName = getIntent().getStringExtra("SessionRoomName");
    setTitle(sessionRoomName);
    myName = getIntent().getStringExtra("UserName");

    tv = (TextView)findViewById(R.id.session_client_textview);
    tv.setText("************************\n"+"Welcome to "+sessionRoomName+"!\n"+"************************\n");
    et = (EditText)findViewById(R.id.session_client_edittext);
    sendBtn = (Button)findViewById(R.id.session_client_sendbtn);
    sendBtn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
        //tv.setText(tv.getText()+myName+": "+et.getText().toString()+"\n");
        // Send msg to server
        String msg = "MESSAGE, "+myName+", "+et.getText().toString();
        new SendThread(msg,SessionRoomUtil.SEND_PORT,sessionRoomIP,SessionRoomUtil.RECEIVE_PORT).start();
        et.setText("");
        // Hide input method
        InputMethodManager inputManager = (InputMethodManager)
            getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
               InputMethodManager.HIDE_NOT_ALWAYS);
      }

    });

  }

  @Override
  protected void onStart() {
    super.onStart();

    rt = new ReceiveThread();
    rt.start();
  }

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
      continueReceiveThread = true;
      try {
        while(continueReceiveThread)
        {
          String msg = SessionRoomUtil.receiveMessage(receiveSocket);
          String []str = msg.split(", ");
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

          }
          // poll request
          if(str[1].equals("POLL"))
          {
            textViewContent = tv.getText()+"Received a poll request!\n";
            tv.post(new Runnable(){
              @Override
              public void run() {
                tv.setText(textViewContent);
              }
            });
            mHandler.post(new Runnable(){
				@Override
				public void run() {
					createPollDialog();
				}
            });
            
          }
        }
        receiveSocket.close();
      } catch (IOException e) {
        if(receiveSocket!=null)
          receiveSocket.close();
      }
    }

    @Override
    public void interrupt() {
      if(receiveSocket!=null)
        receiveSocket.close();
      super.interrupt();
    }
  };

  @Override
  protected void onPause() {
    if(rt!=null)
      rt.interrupt();

    // Send END msg to server
    String endMsg = "END, "+myName;
    new SendThread(endMsg,SessionRoomUtil.SEND_PORT,sessionRoomIP,SessionRoomUtil.RECEIVE_PORT).start();

    super.onPause();
  }


  /**
   * Creates an Alert Dialog for poll function
   */
  private void createPollDialog() {
    // get the alert dialog ready for user
    final AlertDialog.Builder alert = new AlertDialog.Builder(SessionRoomClient.this);
    alert.setMessage("Make Your Choice")
    .setPositiveButton("C", 
    		new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int which) {
    		textViewContent = tv.getText()+"You chosed C!\n";
    		tv.setText(textViewContent);
    		// Send selection to server
    		String msg = "POLL, C";
            new SendThread(msg,SessionRoomUtil.SEND_PORT,sessionRoomIP,SessionRoomUtil.RECEIVE_PORT).start();
    	}
    })
    .setNeutralButton("B", 
    		new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog, int which) {
    		textViewContent = tv.getText()+"You chosed B!\n";
    		tv.setText(textViewContent);
    		// Send selection to server
    		String msg = "POLL, B";
            new SendThread(msg,SessionRoomUtil.SEND_PORT,sessionRoomIP,SessionRoomUtil.RECEIVE_PORT).start();
    	}
    })
    .setNegativeButton("A",
    		new DialogInterface.OnClickListener() {
    	public void onClick( DialogInterface dialog, int whichButton) {
    		textViewContent = tv.getText()+"You chosed A!\n";
    		tv.setText(textViewContent);
    		// Send selection to server
    		String msg = "POLL, A";
            new SendThread(msg,SessionRoomUtil.SEND_PORT,sessionRoomIP,SessionRoomUtil.RECEIVE_PORT).start();
    	}
    });

    alert.show();
  }


}
