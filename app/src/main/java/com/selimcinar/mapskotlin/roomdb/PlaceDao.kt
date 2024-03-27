package com.selimcinar.mapskotlin.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.selimcinar.mapskotlin.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable


@Dao
interface PlaceDao {
    @Query("Select * from place")
    fun  getAll(): Flowable< List<Place>>
/*
    @Query("Select * from place Where id = :id")
    fun  getAll(id:String): List<Place>
*/
    @Insert
    fun insert(place: Place) : Completable

    @Delete
    fun  delete(place: Place) :Completable


}