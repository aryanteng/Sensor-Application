package com.example.sensorapplication.dao

import com.example.sensorapplication.models.ProximitySensorData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProximitySensorDataDao {
    @Query("SELECT * FROM proximity_sensor_data")
    fun getAll(): List<ProximitySensorData>

    @Insert
    fun insert(data: ProximitySensorData)

    @Delete
    fun delete(data: ProximitySensorData)
}
