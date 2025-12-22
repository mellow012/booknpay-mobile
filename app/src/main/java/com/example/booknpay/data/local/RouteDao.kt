package com.example.booknpay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.booknpay.data.model.Route
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<Route>): List<Long>

    @Query("SELECT * FROM routes")
    fun getAllRoutes(): Flow<List<Route>>
}
