package com.abhishek.hexmerge

import kotlin.math.max
import kotlin.random.Random

class GameEngine(
    val grid: HexGrid = HexGrid(3),
    private val random: Random = Random.Default
) {
    val tiles = linkedMapOf<HexCoord, Int>()
    val queue = ArrayDeque<Int>()
    var score: Long = 0
        private set
    var bestScore: Long = 0
    var level: Int = 1
    var coins: Int = 0
    var unlockedLevel: Int = 1
    val stars = mutableMapOf<Int, Int>()

    val target: Int get() = 32 shl (level - 1).coerceAtMost(14)
    val highestTile: Int get() = tiles.values.maxOrNull() ?: 0

    init { reset(level) }

    fun reset(newLevel: Int = level) {
        level = newLevel.coerceAtLeast(1)
        tiles.clear()
        score = 0
        queue.clear()
        repeat(3) { queue.addLast(generateValue()) }
        val initial = when { level <= 2 -> 3; level <= 5 -> 5; else -> 7 }
        repeat(initial) { spawnRandomTile() }
    }

    private fun generateValue(): Int {
        val roll = random.nextFloat()
        return when {
            level <= 3 -> if (roll < .75f) 2 else if (roll < .97f) 4 else 8
            level <= 8 -> if (roll < .65f) 2 else if (roll < .93f) 4 else if (roll < .985f) 8 else 16
            else -> if (roll < .58f) 2 else if (roll < .88f) 4 else if (roll < .97f) 8 else 16
        }
    }

    private fun spawnRandomTile() {
        val empty = emptyCells()
        if (empty.isNotEmpty()) tiles[empty.random(random)] = generateValue()
    }

    fun emptyCells(): List<HexCoord> = grid.cells.filter { it !in tiles }
    fun getNeighbors(cell: HexCoord): List<HexCoord> = grid.getNeighbors(cell)
    fun getValidCells(): List<HexCoord> = grid.cells

    fun place(cell: HexCoord): Boolean {
        if (cell !in grid.cells || cell in tiles || queue.isEmpty()) return false
        tiles[cell] = queue.removeFirst()
        queue.addLast(generateValue())
        resolveMerges(cell)
        spawnRandomTile()
        if (score > bestScore) bestScore = score
        return true
    }

    private fun resolveMerges(origin: HexCoord) {
        var anchor = origin
        var combo = 0
        while (true) {
            val value = tiles[anchor] ?: return
            val matching = grid.getNeighbors(anchor).filter { tiles[it] == value }
            if (matching.isEmpty()) return
            val partner = matching.minWith(compareBy<HexCoord>({ it.q }, { it.r }))
            tiles.remove(partner)
            tiles[anchor] = value * 2
            score += value * 2L * (1 + combo)
            combo++
        }
    }

    fun removeTile(cell: HexCoord): Boolean = tiles.remove(cell) != null

    fun shuffleQueue() {
        val values = queue.toMutableList().shuffled(random)
        queue.clear(); values.forEach(queue::addLast)
    }

    fun isGameOver(): Boolean = emptyCells().isEmpty()
    fun targetReached(): Boolean = highestTile >= target

    fun completeLevel() {
        val star = when {
            score >= target * 50L -> 3
            score >= target * 25L -> 2
            else -> 1
        }
        stars[level] = max(stars[level] ?: 0, star)
        coins += 25 + star * 10
        unlockedLevel = max(unlockedLevel, level + 1)
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        level, score, bestScore, coins,
        tiles.mapKeys { "${it.key.q},${it.key.r}" }, queue.toList(), unlockedLevel, stars.toMap()
    )

    fun restore(s: GameSnapshot) {
        level = s.level.coerceAtLeast(1)
        score = s.score.coerceAtLeast(0)
        bestScore = max(s.bestScore, score)
        coins = s.coins.coerceAtLeast(0)
        unlockedLevel = max(1, s.unlockedLevel)
        stars.clear(); stars.putAll(s.stars.filterKeys { it >= 1 }.filterValues { it in 1..3 })
        tiles.clear()
        s.tiles.forEach { (key, value) ->
            val p = key.split(',')
            if (p.size == 2) {
                val cell = HexCoord(p[0].toIntOrNull() ?: Int.MIN_VALUE, p[1].toIntOrNull() ?: Int.MIN_VALUE)
                if (cell in grid.cells && value > 0) tiles[cell] = value
            }
        }
        queue.clear(); s.queue.filter { it > 0 }.take(5).forEach(queue::addLast)
        while (queue.size < 3) queue.addLast(generateValue())
    }
}
