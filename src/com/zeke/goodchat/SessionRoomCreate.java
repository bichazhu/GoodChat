package com.zeke.goodchat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

public class SessionRoomCreate extends Activity {

  private TextView IPTv;
  private TextView courseNameTv;
  private TextView userNameTv;
  private Button btn;
  private EditText et;

  private String myIP;
  private String courseID;
  private String courseName;
  private String userName;
  private Firebase ref;
  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";
  private final String ID_SEPARATOR = MainActivity.ID_SEPARATOR;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_session_room);
    
    // Get information from previous activity
    courseID = getIntent().getExtras().getString("course_id");
    courseName = getIntent().getExtras().getString("course_name");
    userName = getIntent().getExtras().getString("user_name");
    
    // First we get a reference to the location of the user's name data:
    ref = new Firebase(appURL + "/GlobalChat/");
    
    // Show the host IP on layout
    IPTv = (TextView)findViewById(R.id.create_view_ip_textview);

    try {
      myIP = getIPAddress();
      IPTv.setText("My IP: "+myIP+", Subnet: "+getSubnetMask());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    
    // Show course name
    courseNameTv = (TextView)findViewById(R.id.create_view_name_textview);
    courseNameTv.setText(courseName + ID_SEPARATOR + courseID);
    
    // Show user name
    userNameTv = (TextView)findViewById(R.id.create_view_nick_textview);
    userNameTv.setText(userName);
    
    // Edit text is used to input unique code for the session room
    et = (EditText)findViewById(R.id.create_session_code);
    
    // Create a new session room
    btn = (Button)findViewById(R.id.create_view_btn);
    btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				if (!et.getText().toString().isEmpty()) {
					// Once a class is held, the "ClassOn" entry is updated by the host ip 
					// Other users can join the local network with this ip
					ref.child(courseID).child(courseName).child("ClassOn").setValue(myIP);

					Intent intent = new Intent(SessionRoomCreate.this, SessionRoomHost.class);
					intent.putExtra("course_id", courseID);
					intent.putExtra("course_name", courseName);
					intent.putExtra("UserName", userName);
					intent.putExtra("MyIPAddress", myIP);
					intent.putExtra("name_list", getIntent().getExtras().getString("name_list"));
					intent.putExtra("code", et.getText().toString());
					startActivity(intent);
				} else {
					Toast.makeText(getApplicationContext(), "Please enter a code", Toast.LENGTH_SHORT).show();
				}
			}

    });
  }

  // Get IP of current device
  public String getIPAddress() throws UnknownHostException {
    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifi.getDhcpInfo();
    int address = dhcp.ipAddress;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++)
      quads[k] = (byte) (address >> (k * 8));
    return InetAddress.getByAddress(quads).getHostAddress();
  }

  // Get subnet mask of current device
  public String getSubnetMask() throws UnknownHostException {
    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifi.getDhcpInfo();
    int address = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++)
      quads[k] = (byte) (address >> (k * 8));
    return InetAddress.getByAddress(quads).getHostAddress();
  }


}
