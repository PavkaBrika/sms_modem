package com.breakneck.sms_modem.presentation.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BuildCompat
import androidx.fragment.app.Fragment
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.databinding.FragmentInfoBinding

class InfoFragment: Fragment() {

    lateinit var binding: FragmentInfoBinding

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

        return view
    }
}