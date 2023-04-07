package com.example.sensorapplication.DAOs

import com.example.sensorapplication.LightSensorData
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
