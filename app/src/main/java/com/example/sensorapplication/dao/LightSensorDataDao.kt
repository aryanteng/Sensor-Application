package com.example.sensorapplication.dao

import com.example.sensorapplication.models.LightSensorData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LightSensorDataDao {
    @Query("SELECT * FROM light_sensor_data")
    fun getAll(): List<LightSensorData>

    @Insert
    fun insert(data: LightSensorData)

    @Delete
    fun delete(data: LightSensorData)
}
