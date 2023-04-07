package com.example.sensorapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "light_sensor_data")
data class LightSensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val illuminance: Float
)