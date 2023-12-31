package com.breakneck.data.entity

sealed class SenderData {
    data object Server: SenderData()
    data object Phone: SenderData()
}