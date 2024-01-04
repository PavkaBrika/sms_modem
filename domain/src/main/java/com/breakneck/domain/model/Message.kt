package com.breakneck.domain.model

data class Message(
    val cellNumber: String,
    val text: String,
    val sender: Sender?
) {
    constructor(
        cellNumber: String,
        text: String
    ) : this(cellNumber = cellNumber, text = text, sender = null)
}