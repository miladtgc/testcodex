package com.example.offlinebts.data.cell

data class CellTower(
    val radio: RadioType,
    val cellId: Long,
    val enodeb: Int?,
    val lat: Double,
    val lon: Double
)

enum class RadioType { GSM, WCDMA, LTE }

class LocalCellDatabase(private val towers: List<CellTower>) {

    fun findByCellId(cellId: Long): CellTower? = towers.firstOrNull { it.cellId == cellId }

    fun findByCellIds(cellIds: Set<Long>): List<CellTower> = towers.filter { it.cellId in cellIds }

    companion object {
        fun seedNeighborhoodCells(): LocalCellDatabase {
            return LocalCellDatabase(
                listOf(
                    CellTower(RadioType.LTE, cellId = 424001, enodeb = 1656, lat = 35.7000, lon = 51.4000),
                    CellTower(RadioType.LTE, cellId = 424002, enodeb = 1656, lat = 35.7010, lon = 51.3980),
                    CellTower(RadioType.WCDMA, cellId = 31721, enodeb = null, lat = 35.6990, lon = 51.4020),
                    CellTower(RadioType.GSM, cellId = 9101, enodeb = null, lat = 35.7022, lon = 51.4012)
                )
            )
        }
    }
}
