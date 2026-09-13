package com.abhishek.hexmerge

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

class HomeActivity:ComponentActivity(){
 private lateinit var save:SaveManager;private lateinit var engine:GameEngine
 override fun onCreate(b:Bundle?){super.onCreate(b);save=SaveManager(this);engine=GameEngine();save.restore(engine);home()}
 private fun home(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(24,24,24,24);background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(Color.rgb(15,42,80),Color.rgb(5,17,35)))};root.addView(TextView(this).apply{text="HEX\nMERGE";textSize=42f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD},LinearLayout.LayoutParams(-1,0,1f));root.addView(TextView(this).apply{text="LEVEL ${engine.level}   •   ★ ${engine.coins}\nBEST ${engine.bestScore}";textSize=16f;gravity=Gravity.CENTER;setTextColor(Color.LTGRAY)},LinearLayout.LayoutParams(-1,80));button(root,"PLAY"){play()};button(root,"LEVELS"){levels()};button(root,"DAILY CHALLENGE"){daily()};button(root,"SHOP"){shop()};button(root,"SETTINGS"){settings()};root.addView(TextView(this).apply{text="Hexagonal number merging • offline playable";setTextColor(Color.GRAY);gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,70));setContentView(root)}
 private fun play(){startActivity(Intent(this,GameActivity::class.java))}
 private fun button(p:LinearLayout,t:String,a:()->Unit){p.addView(Button(this).apply{text=t;textSize=15f;setOnClickListener{a()}},LinearLayout.LayoutParams(-1,56).apply{setMargins(0,6,0,6)})}
 private fun levels(){val items=(1..20).map{n->"Level $n  • Target ${32 shl (n-1).coerceAtMost(14)}  ${"★".repeat(engine.stars[n]?:0)}"}.toTypedArray();AlertDialog.Builder(this).setTitle("LEVEL SELECT").setItems(items){_,i->if(i+1<=engine.unlockedLevel){engine.reset(i+1);save.save(engine);play()}else Toast.makeText(this,"Complete earlier levels to unlock",Toast.LENGTH_SHORT).show()}.setNegativeButton("CLOSE",null).show()}
 private fun daily(){AlertDialog.Builder(this).setTitle("DAILY CHALLENGE").setMessage("Target: 512\nToday's best: ${DailyChallengeManager(this).best()}\n\nThe daily seed is deterministic for the current day.").setPositiveButton("PLAY"){_,_->play()}.setNegativeButton("CLOSE",null).show()}
 private fun shop(){AlertDialog.Builder(this).setTitle("SHOP • ${engine.coins} coins").setItems(arrayOf("Hammer • 25 coins","Shuffle • 20 coins","Undo • 30 coins","Theme • 150 coins")){_,i->val c=intArrayOf(25,20,30,150)[i];if(engine.coins>=c){engine.coins-=c;save.save(engine);Toast.makeText(this,"Purchased!",Toast.LENGTH_SHORT).show()}else Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show()}}.setNegativeButton("CLOSE",null).show()}
 private fun settings(){val music=CheckBox(this).apply{text="Music";isChecked=true};val sfx=CheckBox(this).apply{text="Sound effects";isChecked=true};val h=CheckBox(this).apply{text="Haptics";isChecked=true};val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(18,18,18,18);addView(music);addView(sfx);addView(h)};AlertDialog.Builder(this).setTitle("SETTINGS").setView(box).setPositiveButton("SAVE",null).setNeutralButton("RESET PROGRESS"){_,_->AlertDialog.Builder(this).setTitle("Reset everything?").setMessage("All levels, stars and coins will be cleared.").setPositiveButton("RESET"){_,_->save.clear();engine=GameEngine();home()}.setNegativeButton("CANCEL",null).show()}.show()}
 override fun onResume(){super.onResume();if(::save.isInitialized){engine=GameEngine();save.restore(engine)}}
}