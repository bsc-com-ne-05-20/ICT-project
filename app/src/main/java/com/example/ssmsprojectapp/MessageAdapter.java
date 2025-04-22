package com.example.ssmsprojectapp;

// MessageAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.isSentByMe()) {
            // Sent message
            holder.sentLayout.setVisibility(View.VISIBLE);
            holder.receivedLayout.setVisibility(View.GONE);

            holder.sentMessageText.setText(message.getContent());
            holder.sentMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            // Set status icon (sent/delivered/read)
        } else {
            // Received message
            holder.sentLayout.setVisibility(View.GONE);
            holder.receivedLayout.setVisibility(View.VISIBLE);

            holder.receivedSenderName.setText(message.getSenderName());
            holder.receivedMessageText.setText(message.getContent());
            holder.receivedMessageTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout receivedLayout, sentLayout;
        TextView receivedSenderName, receivedMessageText, receivedMessageTime;
        TextView sentMessageText, sentMessageTime;
        ImageView sentMessageStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            receivedLayout = itemView.findViewById(R.id.received_message_layout);
            sentLayout = itemView.findViewById(R.id.sent_message_layout);

            receivedSenderName = itemView.findViewById(R.id.received_sender_name);
            receivedMessageText = itemView.findViewById(R.id.received_message_text);
            receivedMessageTime = itemView.findViewById(R.id.received_message_time);

            sentMessageText = itemView.findViewById(R.id.sent_message_text);
            sentMessageTime = itemView.findViewById(R.id.sent_message_time);
            sentMessageStatus = itemView.findViewById(R.id.sent_message_status);
        }
    }
}
