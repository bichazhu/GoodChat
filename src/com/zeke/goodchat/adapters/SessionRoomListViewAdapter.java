package com.zeke.goodchat.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zeke.goodchat.R;
import com.zeke.goodchat.SessionRoomInfo;

public class SessionRoomListViewAdapter extends ArrayAdapter<SessionRoomInfo> {
  private final Context context;
  private final ArrayList<SessionRoomInfo> values;

  public SessionRoomListViewAdapter(Context context, ArrayList<SessionRoomInfo> values) {
    super(context, R.layout.sessionroom_rowview, values);
    this.context = context;
    this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.sessionroom_rowview, parent, false);
    TextView textViewName = (TextView) rowView.findViewById(R.id.sessionroomname);
    TextView textViewIP = (TextView) rowView.findViewById(R.id.sessionroomip);
    textViewName.setText(values.get(position).name);
    textViewIP.setText(values.get(position).addr);

    return rowView;
  }
}
