package com.breakneck.sms_modem.presentation.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.os.BuildCompat
import androidx.fragment.app.Fragment
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.databinding.FragmentInfoBinding
import com.breakneck.sms_modem.viewmodel.InfoFragmentViewModel
import com.breakneck.sms_modem.viewmodel.MainFragmentViewModel
import com.breakneck.sms_modem.viewmodel.MessageFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class InfoFragment: Fragment() {

    lateinit var binding: FragmentInfoBinding

    private val vm by activityViewModel<InfoFragmentViewModel>()
    private val messagesFragmentViewModel by activityViewModel<MessageFragmentViewModel>()

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
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        val view = binding.root

        val version = requireActivity().packageManager.getPackageInfo("com.breakneck.sms_modem", 0).versionName

        binding.writeEmailLayout.setOnClickListener {
            Intent(Intent.ACTION_SENDTO)
                .apply {
                    data = Uri.parse("mailto:pavlikbrichkin@yandex.ru")
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject, version))
                }
                .let {
                    startActivity(it)
                }
        }

        binding.appVersionTextView.text = getString(R.string.app_version, version)

        binding.reminderNotificationTimeLayout.setOnClickListener {
            showRemindNotificationDialog()
        }

        binding.deleteMessagesHistoryLayout.setOnClickListener {
            showDeleteMessagesHistoryDialog()
        }

        return view
    }

    private fun showRemindNotificationDialog() {
        val dialog = BottomSheetDialog(requireActivity())
        dialog.setContentView(R.layout.dialog_notification_settings)

        val onClickListener = object: View.OnClickListener {
            override fun onClick(view: View?) {
                when (view!!.id) {
                    //TODO CHANGE TO HOURS
                    R.id.hours3Layout -> vm.saveRemindNotificationTime(3000)
                    R.id.hours6Layout -> vm.saveRemindNotificationTime(6000)
                    R.id.hours9Layout -> vm.saveRemindNotificationTime(9000)
                    R.id.hours12Layout -> vm.saveRemindNotificationTime(12000)
                }
                dialog.dismiss()
            }
        }

        dialog.findViewById<LinearLayout>(R.id.hours3Layout)!!.setOnClickListener(onClickListener)
        dialog.findViewById<LinearLayout>(R.id.hours6Layout)!!.setOnClickListener(onClickListener)
        dialog.findViewById<LinearLayout>(R.id.hours9Layout)!!.setOnClickListener(onClickListener)
        dialog.findViewById<LinearLayout>(R.id.hours12Layout)!!.setOnClickListener(onClickListener)
        dialog.show()
    }

    private fun showDeleteMessagesHistoryDialog() {
        val dialog = BottomSheetDialog(requireActivity())
        dialog.setContentView(R.layout.dialog_delete_messages_history)

        dialog.findViewById<Button>(R.id.confirmButton)!!.setOnClickListener {
            vm.deleteAllMessages()
            messagesFragmentViewModel.changeIsMessageDeleted()
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelButton)!!.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}