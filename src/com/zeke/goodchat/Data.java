package com.zeke.goodchat;

/**
 * This class represents the data that the user will be sending.
 * 
 * @author Bin
 * 
 */
@SuppressWarnings("unused")
public class Data {

    private String message;
    private String author;

    private Data() { 
      // Default Constructor
    }

    Data(String message, String author) {
        this.message = message;
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }
}
