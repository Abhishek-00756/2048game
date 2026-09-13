package com.abhishek.hexmerge

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.setPadding
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    private lateinit var root: FrameLayout
    private lateinit var gameView: HexGameView
    private lateinit var scoreText: TextView
    private lateinit var bestText: TextView
    private lateinit var targetText: TextView
    private lateinit var levelText: TextView
    private lateinit var coinText: TextView
    private lateinit var engine: GameEngine
    private lateinit var save: SaveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = GameEngine(); save = SaveManager(this); save.restore(engine)
        buildUi()
    }

    private fun buildUi() {
        root = FrameLayout(this).apply { setBackgroundColor(Color.rgb(7, 17, 32)) }
        setContentView(root)

        val gradient = GradientDrawable(GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.rgb(15, 42, 80), Color.rgb(5, 17, 35)))
        root.background = gradient

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(10))
        }
        root.addView(column, FrameLayout.LayoutParams(-1,-1))

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val pause = Button(this).apply { text = "Ⅱ"; textSize = 18f; setOnClickListener { pauseDialog() } }
        top.addView(pause, LinearLayout.LayoutParams(dp(52),dp(48)))
        levelText = label("LEVEL 1",18f,true); top.addView(levelText, LinearLayout.LayoutParams(0,dp(48),1f).apply { gravity = Gravity.CENTER })
        coinText = label("★ 0",17f,true); top.addView(coinText, LinearLayout.LayoutParams(dp(90),dp(48)))
        column.addView(top)

        targetText = label("TARGET 32",14f,true).apply { gravity = Gravity.CENTER }
        column.addView(targetText, LinearLayout.LayoutParams(-1,dp(28)))

        val scores = LinearLayout(this).apply { gravity = Gravity.CENTER }
        scoreText = label("SCORE 0",15f,true); bestText = label("BEST 0",15f,true)
        scores.addView(scoreText, LinearLayout.LayoutParams(0,dp(34),1f)); scores.addView(bestText, LinearLayout.LayoutParams(0,dp(34),1f))
        column.addView(scores)

        gameView = HexGameView().apply { setEngine(engine) }
        column.addView(gameView, LinearLayout.LayoutParams(-1,0,1f))

        val queueTitle = label("NEXT",12f,true); queueTitle.gravity = Gravity.CENTER
        column.addView(queueTitle, LinearLayout.LayoutParams(-1,dp(24)))
        val queueRow = LinearLayout(this).apply { gravity = Gravity.CENTER }
        repeat(3) { i ->
            val tv = label("",20f,true).apply { gravity = Gravity.CENTER; id = 1000+i }
            queueRow.addView(tv, LinearLayout.LayoutParams(dp(78),dp(58)).apply { setMargins(dp(6),0,dp(6),0) })
        }
        column.addView(queueRow)

        val controls = LinearLayout(this).apply { gravity = Gravity.CENTER }
        fun control(text:String, action:()->Unit): Button = Button(this).apply { this.text=text; textSize=12f; setOnClickListener { action() } }
        controls.addView(control("UNDO") { Toast.makeText(this,"Undo is reserved for a future update",Toast.LENGTH_SHORT).show() })
        controls.addView(control("SHUFFLE") { engine.shuffleQueue(); refresh() })
        controls.addView(control("HAMMER") { useHammer() })
        column.addView(controls, LinearLayout.LayoutParams(-1,dp(52)))
        refresh()
    }

    private fun useHammer() {
        if (engine.tiles.isEmpty()) return
        val tile = engine.tiles.keys.firstOrNull() ?: return
        engine.removeTile(tile); save.save(engine); refresh()
    }

    private fun pauseDialog() {
        AlertDialog.Builder(this).setTitle("PAUSED")
            .setItems(arrayOf("RESUME","RESTART","HOME")) { _, which ->
                when(which) { 1 -> { engine.reset(); save.save(engine); refresh() }; 2 -> homeMessage() }
            }.show()
    }

    private fun homeMessage() { Toast.makeText(this,"Progress saved. Press back or restart to return to this game.",Toast.LENGTH_LONG).show() }

    private fun refresh() {
        levelText.text = "LEVEL ${engine.level}"; targetText.text = "TARGET ${engine.target}"
        scoreText.text = "SCORE ${format(engine.score)}"; bestText.text = "BEST ${format(engine.bestScore)}"; coinText.text = "★ ${engine.coins}"
        val q = engine.queue.toList(); for(i in 0..2) findViewById<TextView>(1000+i)?.text = q.getOrNull(i)?.toString() ?: ""
        gameView.invalidate()
        if (engine.targetReached()) levelCompleteDialog()
        else if (engine.isGameOver()) gameOverDialog()
    }

    private var completionShown = false
    private fun levelCompleteDialog() {
        if (completionShown) return; completionShown = true
        engine.completeLevel(); save.save(engine)
        AlertDialog.Builder(this).setTitle("LEVEL COMPLETE!")
            .setMessage("Target ${engine.target} reached.\nScore: ${format(engine.score)}\nCoins earned!")
            .setPositiveButton("NEXT LEVEL") { _, _ -> completionShown=false; engine.reset(engine.level+1); save.save(engine); refresh() }
            .setNegativeButton("KEEP PLAYING") { _, _ -> completionShown=false }.show()
    }

    private fun gameOverDialog() {
        AlertDialog.Builder(this).setTitle("GAME OVER")
            .setMessage("Score: ${format(engine.score)}\nBest: ${format(engine.bestScore)}\nHighest tile: ${engine.highestTile}")
            .setPositiveButton("RETRY") { _, _ -> engine.reset(); save.save(engine); refresh() }
            .setNegativeButton("CLOSE",null).show()
    }

    override fun onPause() { super.onPause(); save.save(engine) }
    override fun onBackPressed() { pauseDialog() }

    private fun label(text:String,size:Float,bold:Boolean) = TextView(this).apply { this.text=text; textSize=size; setTextColor(Color.WHITE); if(bold) typeface=Typeface.DEFAULT_BOLD }
    private fun dp(v:Int) = (v * resources.displayMetrics.density).toInt()
    private fun format(v:Long) = String.format(Locale.US,"%,d",v)

    inner class HexGameView : View(this@MainActivity) {
        private var game: GameEngine? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()
        private var sizePx = 0f
        private var centerX = 0f
        private var centerY = 0f
        private var selected: Int? = null
        private var downX=0f; private var downY=0f
        private val radius = 3

        fun setEngine(e: GameEngine) { game=e }

        override fun onDraw(c: Canvas) {
            super.onDraw(c); val g=game ?: return
            centerX=width/2f; centerY=height/2f
            sizePx=min(width,height)/7.25f
            paint.style=Paint.Style.STROKE; paint.strokeWidth=dp(2).toFloat(); paint.color=Color.argb(90,120,210,255)
            for (cell in g.gridForUi()) { drawHex(c, cell, cellCenter(cell), null, false) }
            paint.style=Paint.Style.FILL
            g.tiles.forEach { (cell,value) -> drawHex(c,cell,cellCenter(cell),value,true) }
        }

        private fun drawHex(c:Canvas, cell:HexCoord, p:PointF, value:Int?, filled:Boolean) {
            polygon(p.x,p.y,sizePx*0.9f)
            if(filled) {
                paint.shader=LinearGradient(p.x-sizePx,p.y-sizePx,p.x+sizePx,p.y+sizePx,tileColor(value!!),Color.BLACK,Shader.TileMode.CLAMP)
                c.drawPath(path,paint); paint.shader=null
                paint.color=Color.WHITE; paint.textAlign=Paint.Align.CENTER; paint.typeface=Typeface.DEFAULT_BOLD
                paint.textSize=when{value>=1024->18f;value>=128->20f;else->23f}*resources.displayMetrics.density
                val fm=paint.fontMetrics; c.drawText(value.toString(),p.x,p.y-(fm.ascent+fm.descent)/2,paint)
            } else {
                paint.style=Paint.Style.FILL; paint.color=Color.argb(20,90,180,255); c.drawPath(path,paint)
                paint.style=Paint.Style.STROKE; paint.color=Color.argb(100,100,210,255); paint.strokeWidth=dp(2).toFloat(); c.drawPath(path,paint)
            }
        }

        private fun polygon(cx:Float,cy:Float,r:Float){ path.reset(); for(i in 0..5){ val a=Math.toRadians((60*i-30).toDouble()); val x=cx+(r*cos(a)).toFloat(); val y=cy+(r*sin(a)).toFloat(); if(i==0)path.moveTo(x,y) else path.lineTo(x,y)}; path.close() }
        private fun cellCenter(h:HexCoord):PointF { val x=centerX+sizePx*1.5f*h.q; val y=centerY+sizePx*sqrt3()*(h.r+h.q/2f); return PointF(x,y) }
        private fun sqrt3()=1.7320508f

        private fun nearestCell(x:Float,y:Float):HexCoord? {
            val candidates=game?.getValidCellsUi()?:return null
            return candidates.minByOrNull { val p=cellCenter(it); val dx=p.x-x; val dy=p.y-y; dx*dx+dy*dy }
        }

        override fun onTouchEvent(e:MotionEvent):Boolean {
            val g=game?:return true
            when(e.actionMasked){
                MotionEvent.ACTION_DOWN -> { downX=e.x; downY=e.y; selected=nearestCell(e.x,e.y); return true }
                MotionEvent.ACTION_UP -> {
                    val cell=nearestCell(e.x,e.y)
                    if(cell!=null && g.place(cell)){ performClick(); save.save(g); refresh(); }
                    selected=null; invalidate(); return true
                }
            }; return true
        }

        override fun performClick():Boolean { super.performClick(); return true }
        private fun tileColor(v:Int):Int=when(v){2->Color.rgb(55,150,220);4->Color.rgb(70,190,125);8->Color.rgb(235,115,75);16->Color.rgb(245,145,70);32->Color.rgb(225,65,110);64->Color.rgb(145,85,220);128->Color.rgb(105,65,185);256->Color.rgb(225,175,55);512->Color.rgb(65,200,190);1024->Color.rgb(245,90,180);else->Color.rgb(240,210,90)}
    }

    private fun GameEngine.gridForUi() = run { getValidCellsUi() }
    private fun GameEngine.getValidCellsUi() = HexGrid(3).cells
}
