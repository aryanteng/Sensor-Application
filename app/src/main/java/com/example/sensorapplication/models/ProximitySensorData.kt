package com.example.sensorapplication.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proximity_sensor_data")
data class ProximitySensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val distance: Float
)