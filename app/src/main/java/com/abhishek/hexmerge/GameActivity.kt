package com.abhishek.hexmerge

import android.app.AlertDialog
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.ComponentActivity
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class GameActivity:ComponentActivity(){
 private lateinit var engine:GameEngine;private lateinit var save:SaveManager;private lateinit var board:BoardView;private lateinit var hud:TextView;private lateinit var queue:TextView
 override fun onCreate(b:Bundle?){super.onCreate(b);engine=GameEngine();save=SaveManager(this);save.restore(engine);build();if(!getPreferences(0).getBoolean("tutorial",false)){tutorial();getPreferences(0).edit().putBoolean("tutorial",true).apply()}}
 private fun build(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(12,10,12,8);background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(Color.rgb(14,42,80),Color.rgb(4,15,30)))};setContentView(root)
  val top=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL};top.addView(Button(this@GameActivity).apply{text="Ⅱ";setOnClickListener{pause()}},LinearLayout.LayoutParams(55,50));hud=TextView(this).apply{textColor();textSize=16f;gravity=Gravity.CENTER};top.addView(hud,LinearLayout.LayoutParams(0,50,1f));top.addView(TextView(this).apply{text="★ ${engine.coins}";textColor();textSize=16f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(90,50));root.addView(top)
  root.addView(TextView(this).apply{text="TARGET ${engine.target}";textColor();textSize=14f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,32));board=BoardView();root.addView(board,LinearLayout.LayoutParams(-1,0,1f));root.addView(TextView(this).apply{text="NEXT TILE  •  ${engine.queue.joinToString("   ")}";textColor();textSize=13f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,52));
  val actions=LinearLayout(this).apply{gravity=Gravity.CENTER};listOf("UNDO","SHUFFLE","HAMMER","BOMB").forEach{t->actions.addView(Button(this@GameActivity).apply{text=t;textSize=10f;setOnClickListener{act(t)}},LinearLayout.LayoutParams(0,52,1f))};root.addView(actions);refresh()
 }
 private fun TextView.textColor(){setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD}
 private fun act(t:String){when(t){"UNDO"->engine.undo();"SHUFFLE"->engine.shuffleQueue();"HAMMER"->{if(engine.coins>=25)engine.tiles.keys.firstOrNull()?.let{engine.removeTile(it);engine.coins-=25}else toast("Hammer costs 25 coins")};"BOMB"->{if(engine.coins>=40)engine.tiles.keys.firstOrNull()?.let{if(engine.bomb(it)>0)engine.coins-=40}else toast("Bomb costs 40 coins")}};save.save(engine);refresh()}
 private fun refresh(){hud.text="LEVEL ${engine.level}   •   SCORE ${fmt(engine.score)}   •   BEST ${fmt(engine.bestScore)}";board.invalidate();if(engine.targetReached())complete();else if(engine.isGameOver())over()}
 private fun complete(){AlertDialog.Builder(this).setTitle("LEVEL COMPLETE!").setMessage("Target ${engine.target}\nScore ${fmt(engine.score)}\nStars ${"★".repeat(when{engine.score>=engine.target*50L->3;engine.score>=engine.target*25L->2;else->1})}").setPositiveButton("NEXT"){_,_->engine.completeLevel();engine.reset(engine.level+1);save.save(engine);refresh()}.setNegativeButton("KEEP PLAYING",null).show()}
 private fun over(){AlertDialog.Builder(this).setTitle("GAME OVER").setMessage("Score ${fmt(engine.score)}\nHighest ${engine.highestTile}").setPositiveButton("RETRY"){_,_->engine.reset();save.save(engine);refresh()}.setNegativeButton("HOME"){_,_->finish()}.show()}
 private fun pause(){AlertDialog.Builder(this).setTitle("PAUSED").setItems(arrayOf("RESUME","RESTART","HOME")){_,w->when(w){1->{engine.reset();save.save(engine);refresh()};2->finish()}}.show()}
 private fun tutorial(){AlertDialog.Builder(this).setTitle("HOW TO PLAY").setMessage("Place the current tile on an empty hex. Adjacent equal numbers merge and double. Reach the target to finish the level.\n\nTip: plan around the six neighbors and use boosters when the board gets crowded.").setPositiveButton("PLAY",null).show()}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show();private fun fmt(v:Long)=String.format(Locale.US,"%,d",v);private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 override fun onPause(){super.onPause();save.save(engine)}
 inner class BoardView:View(this@GameActivity){private val p=Paint(1);private val path=Path();private var r=0f;private var cx=0f;private var cy=0f
  override fun onDraw(c:Canvas){cx=width/2f;cy=height/2f;r=min(width,height)/8f;engine.grid.cells.forEach{hex(c,it,center(it),0)};engine.tiles.forEach{(h,v)->hex(c,h,center(h),v)}}
  private fun center(h:HexCoord)=PointF(cx+r*1.5f*h.q,cy+r*1.7320508f*(h.r+h.q/2f))
  private fun hex(c:Canvas,h:HexCoord,o:PointF,v:Int){path.reset();for(i in 0..5){val a=Math.toRadians((60*i-30).toDouble());val x=o.x+(r*.88f*cos(a)).toFloat();val y=o.y+(r*.88f*sin(a)).toFloat();if(i==0)path.moveTo(x,y)else path.lineTo(x,y)};path.close();if(v==0){p.style=Paint.Style.FILL;p.color=Color.argb(25,100,200,255);c.drawPath(path,p);p.style=Paint.Style.STROKE;p.strokeWidth=dp(1).toFloat();p.color=Color.argb(110,100,210,255);c.drawPath(path,p)}else{p.style=Paint.Style.FILL;p.color=tile(v);c.drawPath(path,p);p.style=Paint.Style.STROKE;p.color=Color.argb(120,255,255,255);p.strokeWidth=dp(1).toFloat();c.drawPath(path,p);p.style=Paint.Style.FILL;p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.DEFAULT_BOLD;p.textSize=(if(v>=128)18f else 22f)*resources.displayMetrics.density;val f=p.fontMetrics;c.drawText(v.toString(),o.x,o.y-(f.ascent+f.descent)/2,p)}}
  private fun tile(v:Int)=when(v){2->Color.rgb(55,150,220);4->Color.rgb(70,190,125);8->Color.rgb(235,115,75);16->Color.rgb(245,145,70);32->Color.rgb(225,65,110);64->Color.rgb(145,85,220);128->Color.rgb(105,65,185);256->Color.rgb(225,175,55);512->Color.rgb(65,200,190);1024->Color.rgb(245,90,180);else->Color.rgb(240,210,90)}
  override fun onTouchEvent(e:MotionEvent):Boolean{if(e.actionMasked==MotionEvent.ACTION_UP){val h=engine.grid.cells.minByOrNull{val o=center(it);val dx=o.x-e.x;val dy=o.y-e.y;dx*dx+dy*dy};if(h!=null&&engine.place(h)){save.save(engine);refresh()}else toast("Place on an empty hex")};return true}
 }
}