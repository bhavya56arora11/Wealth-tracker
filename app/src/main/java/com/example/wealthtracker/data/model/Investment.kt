package com.example.wealthtracker.data.model

import java.util.UUID

data class Investment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: InvestmentType,
    val units: Double,
    val purchasePrice: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val currentNav: Double? = null,
    val fundCode: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class InvestmentType {
    STOCK, MUTUAL_FUND, SIP
}
