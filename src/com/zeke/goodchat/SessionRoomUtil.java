package com.zeke.goodchat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SessionRoomUtil {
  //public final static int BROADCAST_PORT = 55553;
  //public final static int BROADCASTRECEIVE_PORT = 55554;
  public final static int SEND_PORT = 55555;
  public final static int RECEIVE_PORT = 55556;

  public static void sendMessage(DatagramSocket socket, String msg, String dest, int port) throws IOException {
    //Log.d("Send", "Sending data " + data);
    DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(),
        InetAddress.getByName(dest), port);
    socket.send(packet);
  }

  public static String receiveMessage(DatagramSocket socket) throws IOException {
    byte[] buf = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buf,buf.length);
    socket.receive(packet);
    return packet.getAddress().getHostAddress()+", "+new String(buf,0,packet.getLength());
  }
}
