package com.breakneck.sms_modem.presentation.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.Sender
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.util.FromTimestampToDateString
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.adapter.MessageAdapter
import com.breakneck.sms_modem.databinding.FragmentMessagesBinding
import com.breakneck.sms_modem.viewmodel.MessageFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale

class MessagesFragment : Fragment() {

    lateinit var binding: FragmentMessagesBinding

    private val vm by activityViewModel<MessageFragmentViewModel>()
    /**
     * UNCOMMENT FOR LANGUAGE TEST
     */
//    val saveMessage: SaveSentMessage by inject()
    /**
     * UNCOMMENT FOR LANGUAGE TEST
     */

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * UNCOMMENT FOR LANGUAGE TEST
     */
//    private fun getCurrentLocale(context: Context): Locale {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            context.resources.configuration.locales.get(0);
//        } else {
//            context.resources.configuration.locale;
//        }
//    }
    /**
     * UNCOMMENT FOR LANGUAGE TEST
     */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * UNCOMMENT FOR LANGUAGE TEST
         */
//        lifecycleScope.launch(Dispatchers.IO) {
//            val date = FromTimestampToDateString().execute(
//                System.currentTimeMillis(),
//                getCurrentLocale(requireContext())
//            )
//            for (i in 0..20) {
//                val message = Message(
//                    cellNumber = "+123456789$i",
//                    text = if (i % 2 == 0){
//                        getString(R.string.your_code_is_20, i.toString())
//                    } else {
//                        getString(R.string.thanks_for_code)
//                    },
//                    date = date,
//                    sender = if (i % 2 == 0){
//                        Sender.Server
//                    } else {
//                        Sender.Phone
//                    },
//
//                )
//                saveMessage.execute(message = message)
//            }
//        }
        /**
         * UNCOMMENT FOR LANGUAGE TEST
         */

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