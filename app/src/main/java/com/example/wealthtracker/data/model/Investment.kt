package com.example.wealthtracker.data.model

import java.util.UUID

data class Investment(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var type: InvestmentType = InvestmentType.STOCK,
    var units: Double = 0.0,
    var purchasePrice: Double = 0.0,
    var purchaseDate: Long = System.currentTimeMillis(),
    var currentNav: Double? = null,
    var fundCode: String? = null,
    var lastUpdated: Long = System.currentTimeMillis()
)

enum class InvestmentType {
    STOCK, MUTUAL_FUND, SIP
}
