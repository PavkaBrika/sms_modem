package com.breakneck.domain.model

sealed class Sender {
    data object Server: Sender()
    data object Phone: Sender()
}