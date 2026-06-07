package com.slottracker

data class SpinData(
    val id: Int,
    val timestamp: Long,
    val type: String,      // "loss", "win", "bonus"
    val bet: Int,
    val win: Int,
    val balanceAfter: Int,
    val multiplier: Double
)
