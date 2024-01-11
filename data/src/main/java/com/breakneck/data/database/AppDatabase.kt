package com.breakneck.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.breakneck.data.entity.MessageData

@Database(entities = [MessageData::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun messageDao(): MessageDao

}