package com.breakneck.domain.model

import java.io.Serializable

data class Message(
    val id: Long?,
    val cellNumber: String,
    val text: String,
    val date: String,
    val sender: Sender?,
    var sent: Boolean?
): Serializable {
    constructor(
        cellNumber: String,
        text: String,
        date: String
    ) : this(id = null, cellNumber = cellNumber, text = text, date = date, sender = null, sent = null)

    constructor(
        cellNumber: String,
        text: String,
        date: String,
        sender: Sender?
    ) : this(id = null, cellNumber = cellNumber, text = text, date = date, sender = sender, sent = null)
}