package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.RemainingAdsQuantity
import com.breakneck.domain.repository.SettingsRepository

class SaveRemainingAds(private val settingsRepository: SettingsRepository) {

    fun execute(quantity: RemainingAdsQuantity) {
        settingsRepository.saveRemainingAdsQuantity(quantity = quantity)
    }
}