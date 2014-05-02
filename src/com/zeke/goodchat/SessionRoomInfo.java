package com.zeke.goodchat;

public class SessionRoomInfo {
  String name;
  String addr;
  @Override
  public String toString() {
    return addr+": "+name;
  }
}