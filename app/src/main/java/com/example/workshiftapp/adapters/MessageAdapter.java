package com.example.workshiftapp.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.workshiftapp.R;
import com.example.workshiftapp.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private Context context;

    public MessageAdapter(List<Message> messageList, Context context) {
        this.messageList = messageList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_card, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message currentMessage = messageList.get(position);

        // Set username
        holder.textViewUsername.setText(currentMessage.getUsername());

        // Set message body
        holder.textViewMessageBody.setText(currentMessage.getMessage());

        // Format timestamp
        holder.textViewTimestamp.setText(currentMessage.getTimestamp());

        // Load user photo using Glide
        Glide.with(holder.itemView.getContext())
                .load(currentMessage.getUserPhoto()) // URL or URI of the user image
                .placeholder(R.drawable.ic_user_placeholder) // Placeholder while loading
                .circleCrop() // Crop image into a circle
                .into(holder.imageViewUserPicture);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername, textViewMessageBody, textViewTimestamp;
        ImageView imageViewUserPicture; // Added reference for the ImageView

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewUsername);
            textViewMessageBody = itemView.findViewById(R.id.textViewMessageBody);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            imageViewUserPicture = itemView.findViewById(R.id.imageViewUserPicture); // Bind the ImageView
        }
    }
}
