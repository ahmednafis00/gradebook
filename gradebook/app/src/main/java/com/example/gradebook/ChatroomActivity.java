package com.example.gradebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ChatroomActivity extends AppCompatActivity {
    private EditText input_msg;
    private TextView chat_conversation;
    private String user_name;
    private DatabaseReference root;
    private String temp_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        Intent thisIntent = getIntent();
        user_name = thisIntent.getStringExtra("user_name");
        String room_name = thisIntent.getStringExtra("room_name");
        Log.i("gradebook", "Chatting");
        setTitle(room_name);

        Button btn_send_msg = findViewById(R.id.btn_send);
        input_msg = findViewById(R.id.msg_input);
        chat_conversation = findViewById(R.id.textView);

        root = FirebaseDatabase.getInstance().getReference().child(room_name);
        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> map = new HashMap<>();
                temp_key = root.push().getKey();
                root.updateChildren(map);

                // appends user message
                DatabaseReference message_root = root.child(temp_key);
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("name", user_name);
                msgMap.put("msg", input_msg.getText().toString());
                message_root.updateChildren(msgMap);
            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void append_chat_conversation(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {
            String chat_msg = (String) ((DataSnapshot) i.next()).getValue();
            String chat_user_name = (String) ((DataSnapshot) i.next()).getValue();
            chat_conversation.append(chat_user_name + " : " + chat_msg + "\n");
        }
    }
}
