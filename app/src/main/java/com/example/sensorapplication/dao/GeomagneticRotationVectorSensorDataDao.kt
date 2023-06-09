package com.example.sensorapplication.dao

import com.example.sensorapplication.models.GeomagneticRotationVectorSensorData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GeomagneticRotationVectorSensorDataDao {
    @Query("SELECT * FROM geomagnetic_rotation_vector_sensor_data")
    fun getAll(): List<GeomagneticRotationVectorSensorData>

    @Insert
    fun insert(data: GeomagneticRotationVectorSensorData)

    @Delete
    fun delete(data: GeomagneticRotationVectorSensorData)
}
