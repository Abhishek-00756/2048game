package com.abhishek.hexmerge

class HexGrid(private val radius: Int = 3) {
    val cells: List<HexCoord> = buildList {
        for (q in -radius..radius) {
            val rMin = maxOf(-radius, -q - radius)
            val rMax = minOf(radius, -q + radius)
            for (r in rMin..rMax) add(HexCoord(q, r))
        }
    }
    private val set = cells.toHashSet()

    fun getCell(q: Int, r: Int): HexCoord? = HexCoord(q, r).takeIf(set::contains)
    fun getNeighbors(cell: HexCoord): List<HexCoord> = cell.neighbors().filter(set::contains)
    fun areAdjacent(a: HexCoord, b: HexCoord): Boolean = HexCoord(b.q - a.q, b.r - a.r) in HexCoord.DIRECTIONS
    fun getValidCells(): List<HexCoord> = cells
}
