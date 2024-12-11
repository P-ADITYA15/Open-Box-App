package com.example.openbox;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText messageInput;
    private ScrollView chatScrollView;
    private LinearLayout chatContainer;  // Changed from TextView to LinearLayout

    private DatabaseReference contactFormDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        contactFormDB = FirebaseDatabase.getInstance().getReference("contactForm");

        // Initialize views
        messageInput = findViewById(R.id.messageInput);
        chatContainer = findViewById(R.id.chatContainer);  // LinearLayout for holding messages
        chatScrollView = findViewById(R.id.chatScrollView);
        Button sendButton = findViewById(R.id.sendButton);

        // Load existing messages
        loadMessages();

        // Handle user input with the Enter key
        messageInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                handleUserInput();
                return true;
            }
            return false;
        });

        // Handle user input with the Send button
        sendButton.setOnClickListener(v -> handleUserInput());

    }

    private void handleUserInput() {
        String messageContent = messageInput.getText().toString().trim();

        if (!messageContent.isEmpty()) {
            String currTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            saveMessages(messageContent, currTime);
            messageInput.setText("");
        }
    }

    private void saveMessages(String message, String time) {
        // Push a new message to the database
        DatabaseReference newMessageRef = contactFormDB.push();
        newMessageRef.setValue(new Message(message, time));
    }

    private void loadMessages() {
        contactFormDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatContainer.removeAllViews();  // Clear the chat container

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        addMessage(message.getName(), message.getTime());
                    }
                }

                // Scroll to the bottom of the chat
                chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void addMessage(String content, String time) {
        // Create a RelativeLayout for the message and time (this will allow positioning of time at the bottom right)
        RelativeLayout messageLayout = new RelativeLayout(this);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        messageLayout.setPadding(16, 16, 16, 16);  // Padding for the message container

        // Create a TextView for the message content
        TextView messageText = new TextView(this);
        messageText.setText(content);
        messageText.setTextColor(getResources().getColor(R.color.messageTextColor));
        messageText.setTextSize(16);
        messageText.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create a TextView for the time
        TextView timeText = new TextView(this);
        timeText.setText(time);
        timeText.setTextColor(getResources().getColor(R.color.timeTextColor));  // Use a different color for the time
        timeText.setTextSize(14);

        // Set the position of the time to the bottom-right corner
        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);  // Align the time to the bottom
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_END);     // Align the time to the right
        timeText.setLayoutParams(timeParams);

        // Add the message and time TextViews to the message layout
        messageLayout.addView(messageText);
        messageLayout.addView(timeText);

        // Add the message layout to the chat container
        chatContainer.addView(messageLayout);
    }



    // Message model class
    public static class Message {
        private String name;
        private String time;

        public Message() {
            // Default constructor required for calls to DataSnapshot.getValue(Message.class)
        }

        public Message(String name, String time) {
            this.name = name;
            this.time = time;
        }

        public String getName() {
            return name;
        }

        public String getTime() {
            return time;
        }
    }
}
