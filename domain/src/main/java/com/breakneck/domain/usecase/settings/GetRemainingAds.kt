package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.RemainingAdsQuantity
import com.breakneck.domain.repository.SettingsRepository

class GetRemainingAds(private val settingsRepository: SettingsRepository) {

    fun execute(): RemainingAdsQuantity {
        return settingsRepository.getRemainingAdsQuantity()
    }
}