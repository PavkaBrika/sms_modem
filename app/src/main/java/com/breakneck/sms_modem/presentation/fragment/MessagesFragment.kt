package com.breakneck.sms_modem.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.breakneck.domain.model.Message
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.adapter.MessageAdapter
import com.breakneck.sms_modem.databinding.FragmentMessagesBinding
import com.breakneck.sms_modem.viewmodel.MessageFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class MessagesFragment : Fragment() {

    lateinit var binding: FragmentMessagesBinding

    private val vm by activityViewModel<MessageFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val view = binding.root

        if (vm.isMessagesDeleted.value == true)
            vm.getAllMessages()

        vm.messageList.observe(viewLifecycleOwner) { list ->
            binding.messagesRecyclerView.apply {
                if (list.isEmpty()) {
                    binding.messagesHintTextView.visibility = View.VISIBLE
                    binding.messagesHintTextView.setText(R.string.no_messages_yet)
                    binding.messagesLinearLayout.visibility = View.GONE
                } else {
                    binding.messagesHintTextView.visibility = View.GONE
                    binding.messagesLinearLayout.visibility = View.VISIBLE
                    val onMessageClickListener = object : MessageAdapter.OnMessageClickListener {
                        override fun onErrorMessageClick(message: Message, position: Int) {
                            vm.sendMessageToServer(message)
                        }
                    }
                    adapter = MessageAdapter(
                        messagesList = list.toMutableList(),
                        onMessageClickListener = onMessageClickListener
                    )
                    addItemDecoration(
                        DividerItemDecoration(
                            view.context,
                            DividerItemDecoration.VERTICAL
                        )
                    )
                }
            }
        }

        return view
    }
}