package com.breakneck.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.breakneck.data.entity.MessageData

@Database(entities = [MessageData::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun messageDao(): MessageDao

}