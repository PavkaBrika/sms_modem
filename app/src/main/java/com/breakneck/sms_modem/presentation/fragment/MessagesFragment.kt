package com.breakneck.sms_modem.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.breakneck.domain.model.Message
import com.breakneck.domain.usecase.message.SendMessageToServer
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.sms_modem.adapter.MessageAdapter
import com.breakneck.sms_modem.databinding.FragmentMessagesBinding
import com.breakneck.sms_modem.viewmodel.MessageFragmentViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.component.inject

class MessagesFragment : Fragment() {

    lateinit var binding: FragmentMessagesBinding

    private val vm by activityViewModel<MessageFragmentViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val view = binding.root

        vm.messageList.observe(viewLifecycleOwner) { list ->
            binding.messagesRecyclerView.apply {
                if (list.isEmpty()) {
                    binding.noMessagesHintTextView.visibility = View.VISIBLE
                    binding.messagesLinearLayout.visibility = View.GONE
                } else {
                    binding.noMessagesHintTextView.visibility = View.GONE
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