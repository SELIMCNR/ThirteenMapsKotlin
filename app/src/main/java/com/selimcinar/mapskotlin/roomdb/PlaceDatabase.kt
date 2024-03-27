package com.selimcinar.mapskotlin.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.selimcinar.mapskotlin.model.Place


@Database(entities = [Place::class], version = 1)
abstract class PlaceDatabase:RoomDatabase(){
    abstract fun  placeDao():PlaceDao
}