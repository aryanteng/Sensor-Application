package com.example.sensorapplication.DAOs

import com.example.sensorapplication.GeomagneticRotationVectorSensorData
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
