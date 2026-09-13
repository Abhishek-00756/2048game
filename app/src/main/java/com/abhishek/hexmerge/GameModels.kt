package com.abhishek.hexmerge

data class HexCoord(val q: Int, val r: Int) {
    companion object {
        val DIRECTIONS = listOf(
            HexCoord(1, 0), HexCoord(1, -1), HexCoord(0, -1),
            HexCoord(-1, 0), HexCoord(-1, 1), HexCoord(0, 1)
        )
    }

    fun neighbors(): List<HexCoord> = DIRECTIONS.map { HexCoord(q + it.q, r + it.r) }
}

data class TileData(var value: Int, val cell: HexCoord)

data class GameSnapshot(
    val level: Int,
    val score: Long,
    val bestScore: Long,
    val coins: Int,
    val tiles: Map<String, Int>,
    val queue: List<Int>,
    val unlockedLevel: Int,
    val stars: Map<Int, Int>
)
