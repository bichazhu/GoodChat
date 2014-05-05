package com.zeke.goodchat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
  
  // Send file with tcp
  public static void sendFile(Socket socket, String filePath){
	  File file = new File(filePath);
	  byte[] mybytearray = new byte[(int) file.length()];
	  try{
		  FileInputStream fileInputStream = new FileInputStream(file);
		  BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);  
	
		  bufferedInputStream.read(mybytearray, 0, mybytearray.length); //read the file
	
		  OutputStream outputStream = socket.getOutputStream();
	
		  outputStream.write(mybytearray, 0, mybytearray.length); //write file to the output stream byte by byte
		  outputStream.flush();
		  bufferedInputStream.close();
		  outputStream.close();
	  } catch(Exception e){
		  int a = 10;
	  }
  }
  
  // Receive file with tcp
  public static void receiveFile(ServerSocket socket, String filePath){
	  try{
		  Socket clientSocket = socket.accept();
		  
		  
	      byte[] mybytearray = new byte[1000000];    //create byte array to buffer the file
	
	      InputStream inputStream = clientSocket.getInputStream();
	      FileOutputStream fileOutputStream = new FileOutputStream(filePath);
	      BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
	
	      //following lines read the input slide file byte by byte
	      int bytesRead = inputStream.read(mybytearray, 0, mybytearray.length);
	      int current = bytesRead;
	
	      
	      while (bytesRead != -1){
	          bytesRead = inputStream.read(mybytearray, current, (mybytearray.length - current));
	          if (bytesRead >= 0) {
	              current += bytesRead;
	          }
	      }
	
	      bufferedOutputStream.write(mybytearray, 0, current);
	      bufferedOutputStream.flush();
	      bufferedOutputStream.close();
	      inputStream.close();
	      clientSocket.close();
	  } catch (Exception e)
	  {
		  int a = 10;
	  }
  }
}
