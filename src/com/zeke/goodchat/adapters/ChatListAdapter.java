package com.zeke.goodchat.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.firebase.client.Query;
import com.zeke.goodchat.Data;
import com.zeke.goodchat.R;
import com.zeke.goodchat.R.id;

/**
 * User: greg
 * Date: 6/21/13
 * Time: 2:39 PM
 */

/**
 * This class is an example of how to use FirebaseListAdapter. It uses the <code>Chat</code> class to encapsulate the
 * data for each individual chat message
 */
public class ChatListAdapter extends FirebaseListAdapter<Data> {

    // The username for this client. We use this to indicate which messages originated from this user
    private String username;

    public ChatListAdapter(Query ref, Activity activity, int layout, String username) {
        super(ref, Data.class, layout, activity);
        this.username = username;
    }

    /**
     * Bind an instance of the <code>Data</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Data</code> instance that represents the current data to bind.
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param data An instance representing the current state of a chat message
     */
    @Override
    protected void populateView(View view, Data data) {
        // Map a Data object to an entry in our listview
        String author = data.getAuthor();
        TextView authorText = (TextView)view.findViewById(R.id.author);
        authorText.setText(author + ": ");
        // If the message was sent by this user, color it differently
        if (author.equals(username)) {
            authorText.setTextColor(Color.RED);
        } else {
            authorText.setTextColor(Color.BLUE);
        }
        ((TextView)view.findViewById(R.id.message)).setText(data.getMessage());
    }
}
