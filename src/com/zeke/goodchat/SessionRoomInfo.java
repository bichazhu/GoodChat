package com.zeke.goodchat;

public class SessionRoomInfo {
  public String name;
  public String addr;
  @Override
  public String toString() {
    return addr+": "+name;
  }
}