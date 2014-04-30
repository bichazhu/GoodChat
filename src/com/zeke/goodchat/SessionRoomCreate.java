package com.zeke.goodchat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class SessionRoomCreate extends Activity {

  private TextView IPTv;
  private TextView courseNameTv;
  private TextView userNameTv;
  private Button btn;

  private String myIP;
  private String courseName;
  private String userName;
  private Firebase ref;
  
  private final String appURL = "https://intense-fire-8812.firebaseio.com";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_session_room);
    
    courseName = getIntent().getExtras().getString("course_name");
    userName = getIntent().getExtras().getString("user_name");
    
    // First we get a reference to the location of the user's name data:
    ref = new Firebase(appURL + "/GlobalChat/");
    
    IPTv = (TextView)findViewById(R.id.create_view_ip_textview);

    try {
      myIP = getIPAddress();
      IPTv.setText("My IP: "+myIP+", Subnet: "+getSubnetMask());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    
    courseNameTv = (TextView)findViewById(R.id.create_view_name_textview);
    courseNameTv.setText(courseName);
    
    userNameTv = (TextView)findViewById(R.id.create_view_nick_textview);
    userNameTv.setText(userName);
    
    btn = (Button)findViewById(R.id.create_view_btn);
    btn.setOnClickListener(new OnClickListener(){

      @Override
      public void onClick(View view) {
    	String path[] = courseName.split("@ ");
    	ref.child(path[1]).child(path[0]).child("ClassOn").setValue(myIP);
        
        Intent intent = new Intent(SessionRoomCreate.this,SessionRoomHost.class);
        intent.putExtra("SessionRoomName", courseName);
        intent.putExtra("UserName", userName);
        intent.putExtra("MyIPAddress", myIP);
        startActivity(intent);
      }

    });
  }

  public String getIPAddress() throws UnknownHostException {
    WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifi.getDhcpInfo();
    int address = dhcp.ipAddress;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++)
      quads[k] = (byte) (address >> (k * 8));
    return InetAddress.getByAddress(quads).getHostAddress();
  }

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
