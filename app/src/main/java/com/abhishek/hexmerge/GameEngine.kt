package com.abhishek.hexmerge

import kotlin.math.max
import kotlin.random.Random

class GameEngine(val grid: HexGrid = HexGrid(3), private val random: Random = Random.Default) {
    val tiles = linkedMapOf<HexCoord, Int>(); val queue = ArrayDeque<Int>(); private val history = ArrayDeque<GameSnapshot>()
    var score: Long = 0; private set; var bestScore=0L; var level=1; var coins=100; var unlockedLevel=1
    val stars=mutableMapOf<Int,Int>(); var combo=0; private set
    val target get()=32 shl (level-1).coerceAtMost(14); val highestTile get()=tiles.values.maxOrNull()?:0; val canUndo get()=history.isNotEmpty()
    init{reset(1,false)}
    fun reset(newLevel:Int=level,clearHistory:Boolean=true){if(clearHistory)history.clear();level=newLevel.coerceAtLeast(1);tiles.clear();queue.clear();score=0;combo=0;repeat(3){queue.addLast(generateValue())};repeat(if(level<=2)3 else if(level<=5)5 else 7){spawnRandomTile()}}
    private fun generateValue():Int{val r=random.nextFloat();return when{level<=3->if(r<.75)2 else if(r<.97)4 else 8;level<=8->if(r<.65)2 else if(r<.93)4 else if(r<.985)8 else 16;else->if(r<.58)2 else if(r<.88)4 else if(r<.97)8 else 16}}
    private fun spawnRandomTile(){emptyCells().randomOrNull(random)?.let{tiles[it]=generateValue()}}
    fun emptyCells()=grid.cells.filter{it !in tiles};fun getNeighbors(c:HexCoord)=grid.getNeighbors(c);fun getValidCells()=grid.cells
    fun place(cell:HexCoord,queueIndex:Int=0):Boolean{if(cell !in grid.cells||cell in tiles||queueIndex !in 0 until queue.size)return false;history.addLast(snapshot());while(history.size>20)history.removeFirst();val v=queue.removeAt(queueIndex);queue.addLast(generateValue());tiles[cell]=v;resolveMerges(cell);spawnRandomTile();if(score>bestScore)bestScore=score;return true}
    private fun resolveMerges(origin:HexCoord){var a=origin;combo=0;while(true){val v=tiles[a]?:return;val m=grid.getNeighbors(a).filter{tiles[it]==v};if(m.isEmpty())return;val p=m.minWith(compareBy<HexCoord>({it.q},{it.r}));tiles.remove(p);tiles[a]=v*2;combo++;score+=v*2L*combo}}
    fun undo():Boolean{val s=history.removeLastOrNull()?:return false;restore(s,false);return true}
    fun removeTile(c:HexCoord):Boolean{if(c !in tiles)return false;history.addLast(snapshot());while(history.size>20)history.removeFirst();tiles.remove(c);return true}
    fun shuffleQueue(){history.addLast(snapshot());while(history.size>20)history.removeFirst();val x=queue.toMutableList().shuffled(random);queue.clear();x.forEach(queue::addLast)}
    fun bomb(c:HexCoord):Int{if(c !in tiles)return 0;history.addLast(snapshot());while(history.size>20)history.removeFirst();var n=0;(listOf(c)+grid.getNeighbors(c).take(2)).distinct().forEach{if(tiles.remove(it)!=null)n++};return n}
    fun isGameOver()=emptyCells().isEmpty();fun targetReached()=highestTile>=target
    fun completeLevel(){val s=when{score>=target*50L->3;score>=target*25L->2;else->1};stars[level]=max(stars[level]?:0,s);coins+=25+s*10;unlockedLevel=max(unlockedLevel,level+1)}
    fun snapshot()=GameSnapshot(level,score,bestScore,coins,tiles.mapKeys{"${it.key.q},${it.key.r}"},queue.toList(),unlockedLevel,stars.toMap())
    fun restore(s:GameSnapshot,clearHistory:Boolean=true){if(clearHistory)history.clear();level=s.level;score=s.score;bestScore=s.bestScore;coins=s.coins;unlockedLevel=s.unlockedLevel;stars.clear();stars.putAll(s.stars);tiles.clear();s.tiles.forEach{(k,v)->val p=k.split(',');if(p.size==2){val c=HexCoord(p[0].toIntOrNull()?:0,p[1].toIntOrNull()?:0);if(c in grid.cells&&v>0)tiles[c]=v}};queue.clear();s.queue.filter{it>0}.forEach(queue::addLast);while(queue.size<3)queue.addLast(generateValue());combo=0}
}
