package com.breakneck.sms_modem.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.MessageFullListVisibilityState
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.adapter.MessageAdapter
import com.breakneck.sms_modem.databinding.FragmentMainBinding
import com.breakneck.sms_modem.viewmodel.MainActivityViewModel
import com.breakneck.sms_modem.viewmodel.MainFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class MainFragment: Fragment() {

    private lateinit var binding: FragmentMainBinding

    val TAG = "MainFragment"

    private val mainActivityVM by activityViewModel<MainActivityViewModel>()
    private val vm by viewModel<MainFragmentViewModel>()

    interface ActivityInterface {
        fun serviceAction(intent: ServiceIntent)

        fun showSettingsBottomSheetDialog()

        fun updateServiceRemainingTimer()

        fun getDeviceIpAddress(): String
    }

    lateinit var activityInterface: ActivityInterface
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityInterface = context as ActivityInterface
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        mainActivityVM.serverIpAddress.observe(viewLifecycleOwner) { address ->
            binding.ipAddressTextView.text = address.value
        }

        mainActivityVM.port.observe(viewLifecycleOwner) { port ->
            binding.portTextView.text = getString(R.string.colon_port, port.value)
        }

        binding.activateServiceButton.setOnClickListener {
            try {
                if (mainActivityVM.serviceRemainingTime.value!! > 0) {
                    activityInterface.serviceAction(mainActivityVM.networkServiceIntent.value!!)
                    mainActivityVM.changeServiceIntent()
                    mainActivityVM.setServiceStateLoading()
                } else {
                    mainActivityVM.setServiceError(getString(R.string.unable_to_start_service_please_watch_ads))
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
                //TODO change hardcode strings to string file
                Toast.makeText(requireActivity(), "Error", Toast.LENGTH_SHORT).show()
            }
        }

        binding.settingsButton.setOnClickListener {
            activityInterface.showSettingsBottomSheetDialog()
        }

        binding.watchAdButton.setOnClickListener {
            mainActivityVM.onAdView()
        }

        mainActivityVM.networkServiceIntent.observe(viewLifecycleOwner) { intent ->
            when (intent) {
                ServiceIntent.Disable -> {
                    binding.activateServiceButton.text = getString(R.string.disable)
                }

                ServiceIntent.Enable -> {
                    binding.activateServiceButton.text = getString(R.string.enable)
                }
            }
        }

        mainActivityVM.networkServiceState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ServiceState.Enabled -> {
                    binding.stateTextView.text = getString(R.string.enabled)
                    binding.settingsButton.apply {
                        isEnabled = false
                        setStrokeColorResource(R.color.enabled_button)
                    }
                    binding.activateServiceButton.apply {
                        isEnabled = true
                        setRippleColorResource(R.color.black)
                        setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.enabled_button
                            )
                        )
                    }
                    binding.stateCardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.enabled_card
                        )
                    )
                }

                ServiceState.Disabled -> {
                    binding.stateTextView.text = getString(R.string.disabled)
                    binding.settingsButton.apply {
                        isEnabled = true
                        setStrokeColorResource(R.color.disabled_button)
                        setRippleColorResource(R.color.disabled_button)
                    }
                    binding.activateServiceButton.apply {
                        isEnabled = true
                        setBackgroundColor(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.disabled_button
                            )
                        )
                        setRippleColorResource(R.color.black)
                    }
                    binding.stateCardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.disabled_card
                        )
                    )
                    try {
                        mainActivityVM.setDeviceIpAddress(address = IpAddress(value = activityInterface.getDeviceIpAddress()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        vm.changeNetworkState()
                    }

                }

                ServiceState.Loading -> {
                    //TODO do something animation on loading
                    binding.stateTextView.text = getString(R.string.loading)
                    binding.settingsButton.isEnabled = false
                    binding.activateServiceButton.isEnabled = false
                }
            }
        }

        mainActivityVM.serviceRemainingTime.observe(viewLifecycleOwner) { time ->
            val millis = time * 1000
            binding.serviceTimeRemainingTextView.text = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            );
        }

        mainActivityVM.serviceError.observe(viewLifecycleOwner) { error ->
            if (!error.equals("")) {
                binding.errorCardView.visibility = View.VISIBLE
                binding.errorTextView.text = error
            } else {
                binding.errorCardView.visibility = View.GONE
            }
        }

        mainActivityVM.messageDestinationUrl.observe(viewLifecycleOwner) { url ->
            if (url.value.isEmpty()) {
                binding.messageDestinationTextView.visibility = View.GONE
            } else {
                binding.messageDestinationTextView.visibility = View.VISIBLE
                binding.messageDestinationTextView.text = url.value
            }
        }

        mainActivityVM.remainingAds.observe(viewLifecycleOwner) { quantity ->
            binding.adsToViewRemainingTextView.text = quantity.value.toString()
            if ((quantity.value % 5 == 0) && (quantity.value != 15)) {
                mainActivityVM.saveServiceRemainingTime()
                if (mainActivityVM.networkServiceBoundState.value is ServiceBoundState.Bounded)
                    activityInterface.updateServiceRemainingTimer()
            }
        }

//        vm.networkState.observe(this) { state ->
//            when (state) {
//                NetworkState.Available -> {
//
//                }
//                NetworkState.Unavailable -> {
//                    binding.stateTextView.text = getString(R.string.network_connection_unavailable)
//                    binding.settingsButton.apply {
//                        isEnabled = true
//                        setStrokeColorResource(R.color.disabled_button)
//                        setRippleColorResource(R.color.disabled_button)
//                    }
//                    binding.activateServiceButton.apply {
//                        isEnabled = false
//                        setBackgroundColor(
//                            ContextCompat.getColor(
//                                this@MainActivity,
//                                R.color.disabled_button
//                            )
//                        )
//                        setRippleColorResource(R.color.black)
//                    }
//                    binding.stateCardView.setCardBackgroundColor(
//                        ContextCompat.getColor(
//                            this,
//                            R.color.disabled_card
//                        )
//                    )
//                }
//            }
//        }

        return view
    }

}