package com.example.workshiftapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.workshiftapp.R;
import com.example.workshiftapp.adapters.MessageAdapter;
import com.example.workshiftapp.models.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatScreen extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private Button buttonSend;

    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    // Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference groupChatRef;

    // Example: Hard-coded username
    private String username = "AnonymousUser";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatScreen newInstance(String param1, String param2) {
        ChatScreen fragment = new ChatScreen();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_screen, container, false);

        // Initialize UI elements from the inflated 'view'
        recyclerView = view.findViewById(R.id.recyclerViewMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, requireContext());
        recyclerView.setAdapter(messageAdapter);

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        // All messages will go under "group_chat"
        groupChatRef = firebaseDatabase.getReference("Root").child("group_chat");

        // Load existing messages and listen for new ones
        loadMessages();

        // Handle Send button
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return view;
    }

    private void loadMessages() {
        groupChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                // Scroll to bottom after loading messages
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String msg = editTextMessage.getText().toString().trim();
        if (msg.isEmpty()) {
            return; // Don't send empty
        }

        Message messageObject = new Message(username, msg, System.currentTimeMillis());
        groupChatRef.push().setValue(messageObject);

        editTextMessage.setText("");
    }
}

