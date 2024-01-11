package com.breakneck.sms_modem.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.Sender
import com.breakneck.sms_modem.R

class MessageAdapter(private val messagesList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cellNumberTextView = itemView.findViewById<TextView>(R.id.cellNumberTextView)
        val dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)
        val senderImageView = itemView.findViewById<ImageView>(R.id.senderImageView)
        val textTextView = itemView.findViewById<TextView>(R.id.textTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]
        holder.cellNumberTextView.text = message.cellNumber
        holder.senderImageView.setImageResource(
            when (message.sender) {
                Sender.Phone -> R.drawable.baseline_phone_24
                Sender.Server -> R.drawable.baseline_server_24
                null -> R.drawable.baseline_settings_24
            }
        )
        //TODO ADD NEW FIELD DATE TO MESSAGE AND SHOW IT IN THIS PLACE
        holder.dateTextView.text = message.date
        holder.textTextView.text = message.text
    }
}