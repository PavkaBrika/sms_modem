package com.breakneck.domain.model

import java.util.Date

data class Message(
    val cellNumber: String,
    val text: String,
    val date: String,
    val sender: Sender?
) {
    constructor(
        cellNumber: String,
        text: String,
        date: String
    ) : this(cellNumber = cellNumber, text = text, date = date, sender = null)
}