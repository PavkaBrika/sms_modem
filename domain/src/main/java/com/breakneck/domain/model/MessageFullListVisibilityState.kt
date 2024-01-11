package com.breakneck.domain.model

sealed class MessageFullListVisibilityState {
    data object Visible: MessageFullListVisibilityState()
    data object Gone: MessageFullListVisibilityState()
}