package com.breakneck.sms_modem.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks
import com.breakneck.domain.PART_ADS_QUANTITY
import com.breakneck.domain.TOTAL_ADS_QUANTITY
import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.databinding.FragmentMainBinding
import com.breakneck.sms_modem.viewmodel.MainActivityViewModel
import com.breakneck.sms_modem.viewmodel.MainFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    val TAG = "MainFragment"

    private val mainActivityVM by activityViewModel<MainActivityViewModel>()
    private val vm by viewModel<MainFragmentViewModel>()

    interface ActivityInterface {
        fun serviceAction(intent: ServiceIntent)

        fun showSettingsBottomSheetDialog()

        fun updateServiceRemainingTimer()

        fun getDeviceIpAddress(): String

        fun checkPermissions(): Boolean
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

        Appodeal.muteVideosIfCallsMuted(true)
        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
            override fun onRewardedVideoClicked() {
                Log.e("Appodeal", "onRewardedVideoClicked")
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
                vm.isRewardAdLoading()
                Log.e("Appodeal", "onRewardedVideoClosed")
            }

            override fun onRewardedVideoExpired() {
                vm.isRewardAdLoading()
                Log.e("Appodeal", "onRewardedVideoExpired")
            }

            override fun onRewardedVideoFailedToLoad() {
                vm.isRewardAdLoading()
                Log.e("Appodeal", "onRewardedVideoFailedToLoad")
            }

            override fun onRewardedVideoFinished(amount: Double, currency: String) {
                Log.e("Appodeal", "onRewardedVideoFinished")
//                val quantity = mainActivityVM.remainingAds.value
//                if ((quantity!!.value % PART_ADS_QUANTITY == 1) && (quantity.value != TOTAL_ADS_QUANTITY) && (quantity.value != 0)) {
//                    mainActivityVM.saveServiceRemainingTime()
//                    if (mainActivityVM.networkServiceBoundState.value is ServiceBoundState.Bounded)
//                        activityInterface.updateServiceRemainingTimer()
//                }
//                mainActivityVM.onAdView()
                vm.isRewardAdLoading()
            }

            override fun onRewardedVideoLoaded(isPrecache: Boolean) {
                vm.isRewardAdLoadSuccess()
                Log.e("Appodeal", "onRewardedVideoLoaded")
            }

            override fun onRewardedVideoShowFailed() {
                vm.isRewardAdLoading()
                Log.e("Appodeal", "onRewardedVideoShowFailed")
            }

            override fun onRewardedVideoShown() {
                Log.e("Appodeal", "onRewardedVideoShown")
            }
        })

//        vm.isRewardAdLoaded.observe(viewLifecycleOwner) { isLoaded ->
//            if (isLoaded) {
//                binding.watchAdButton.setText(R.string.ad)
//                binding.watchAdButton.isEnabled = true
//            }
//            else {
//                binding.watchAdButton.setText(R.string.loading)
//                binding.watchAdButton.isEnabled = false
//            }
//        }

        mainActivityVM.serverIpAddress.observe(viewLifecycleOwner) { address ->
            binding.ipAddressTextView.text = address.value
        }

        mainActivityVM.port.observe(viewLifecycleOwner) { port ->
            binding.portTextView.text = getString(R.string.colon_port, port.value)
        }

        binding.activateServiceButton.setOnClickListener {
            if (activityInterface.checkPermissions()) {
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
        }

        binding.settingsButton.setOnClickListener {
            activityInterface.showSettingsBottomSheetDialog()
        }

        binding.watchAdButton.setOnClickListener {
            val quantity = mainActivityVM.remainingAds.value
            if ((quantity!!.value % PART_ADS_QUANTITY == 1) && (quantity.value != TOTAL_ADS_QUANTITY) && (quantity.value != 0)) {
                mainActivityVM.saveServiceRemainingTime()
                if (mainActivityVM.networkServiceBoundState.value is ServiceBoundState.Bounded)
                    activityInterface.updateServiceRemainingTimer()
            }
            mainActivityVM.onAdView()
//            if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
//                Appodeal.show(requireActivity(), Appodeal.REWARDED_VIDEO)
//            }
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
            binding.serviceTimeRemainingTextView.text = String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
            )
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
            if (quantity.value <= 0)
                binding.watchAdButton.isEnabled = false
            else
                binding.watchAdButton.isEnabled = true
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