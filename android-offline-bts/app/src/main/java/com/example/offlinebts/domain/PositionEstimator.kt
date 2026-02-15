package com.example.offlinebts.domain

import com.example.offlinebts.data.cell.LocalCellDatabase
import com.example.offlinebts.data.telephony.SeenCell
import kotlin.math.pow

data class EstimatedPosition(
    val lat: Double,
    val lon: Double,
    val confidence: Double,
    val usedCellIds: List<Long>
)

class PositionEstimator(
    private val db: LocalCellDatabase
) {

    fun estimateFromCells(cells: List<SeenCell>): EstimatedPosition? {
        val candidates = cells.filter { it.rsrpDbm != null }
        val towers = db.findByCellIds(candidates.map { it.cellId }.toSet())
        if (towers.size < 2) return null

        val weighted = towers.mapNotNull { tower ->
            val signal = candidates.firstOrNull { it.cellId == tower.cellId }?.rsrpDbm ?: return@mapNotNull null
            val weight = dbmToWeight(signal)
            Triple(tower.lat, tower.lon, weight)
        }

        val sum = weighted.sumOf { it.third }
        if (sum == 0.0) return null

        val lat = weighted.sumOf { it.first * it.third } / sum
        val lon = weighted.sumOf { it.second * it.third } / sum
        val confidence = (weighted.size / 4.0).coerceIn(0.1, 1.0)

        return EstimatedPosition(lat, lon, confidence, towers.map { it.cellId })
    }

    private fun dbmToWeight(rsrpDbm: Int): Double {
        val clamped = rsrpDbm.coerceIn(-130, -70)
        return 10.0.pow((clamped + 130) / 20.0)
    }
}
