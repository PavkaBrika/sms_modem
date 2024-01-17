package com.breakneck.sms_modem.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.Sender
import com.breakneck.sms_modem.R

class MessageAdapter(
    private val messagesList: MutableList<Message>,
    private val onMessageClickListener: OnMessageClickListener) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cellNumberTextView = itemView.findViewById<TextView>(R.id.cellNumberTextView)
        val dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)
        val senderImageView = itemView.findViewById<ImageView>(R.id.senderImageView)
        val textTextView = itemView.findViewById<TextView>(R.id.textTextView)
        val errorCardView = itemView.findViewById<CardView>(R.id.errorCardView)
        val errorTextView = itemView.findViewById<TextView>(R.id.errorTextView)
    }

    interface OnMessageClickListener {
        fun onErrorMessageClick(message: Message, position: Int)
    }

    fun addItem(item: Message) {
        messagesList.add(item)
        notifyItemInserted(messagesList.size - 1)
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
        val onErrorMessageClickListener = View.OnClickListener {
            holder.errorTextView.setText(R.string.loading)
            onMessageClickListener.onErrorMessageClick(message = message, position = position)
        }
        if ((message.sent == true) || (message.sent == null)) {
            holder.errorCardView.visibility = View.GONE
        } else {
            holder.errorCardView.visibility = View.VISIBLE
            holder.itemView.setOnClickListener(onErrorMessageClickListener)
        }
        holder.dateTextView.text = message.date
        holder.textTextView.text = message.text


    }
}