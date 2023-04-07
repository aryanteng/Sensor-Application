package com.example.sensorapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geomagnetic_rotation_vector_sensor_data")
data class GeomagneticRotationVectorSensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val cos: Float,
    val headingAccuracy: Float
)