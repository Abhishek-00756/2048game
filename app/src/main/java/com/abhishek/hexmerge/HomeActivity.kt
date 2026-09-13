package com.abhishek.hexmerge

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class HomeActivity : ComponentActivity() {
    private lateinit var save: SaveManager
    private lateinit var engine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        save = SaveManager(this)
        engine = GameEngine()
        save.restore(engine)
        showHome()
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(15, 42, 80), Color.rgb(5, 17, 35))
            )
        }

        root.addView(
            TextView(this).apply {
                text = "HEX\nMERGE"
                textSize = 42f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
            },
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        root.addView(
            TextView(this).apply {
                text = "LEVEL ${engine.level}   •   COINS ${engine.coins}\nBEST ${engine.bestScore}"
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(Color.LTGRAY)
            },
            LinearLayout.LayoutParams(-1, 80)
        )

        addMenuButton(root, "PLAY") { play() }
        addMenuButton(root, "LEVELS") { showLevels() }
        addMenuButton(root, "DAILY CHALLENGE") { showDailyChallenge() }
        addMenuButton(root, "SHOP") { showShop() }
        addMenuButton(root, "SETTINGS") { showSettings() }

        root.addView(
            TextView(this).apply {
                text = "Hexagonal number merging • offline playable"
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(-1, 70)
        )

        setContentView(root)
    }

    private fun play() {
        startActivity(Intent(this, GameActivity::class.java))
    }

    private fun addMenuButton(parent: LinearLayout, label: String, action: () -> Unit) {
        parent.addView(
            Button(this).apply {
                text = label
                textSize = 15f
                setOnClickListener { action() }
            },
            LinearLayout.LayoutParams(-1, 56).apply {
                setMargins(0, 6, 0, 6)
            }
        )
    }

    private fun showLevels() {
        val items = (1..20).map { number ->
            val target = 32 shl (number - 1).coerceAtMost(14)
            val stars = "★".repeat(engine.stars[number] ?: 0)
            "Level $number  • Target $target  $stars"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("LEVEL SELECT")
            .setItems(items) { _, index ->
                val selectedLevel = index + 1
                if (selectedLevel <= engine.unlockedLevel) {
                    engine.reset(selectedLevel)
                    save.save(engine)
                    play()
                } else {
                    Toast.makeText(
                        this,
                        "Complete earlier levels to unlock",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showDailyChallenge() {
        val daily = DailyChallengeManager(this)
        AlertDialog.Builder(this)
            .setTitle("DAILY CHALLENGE")
            .setMessage(
                "Target: 512\nToday's best: ${daily.best()}\n\n" +
                    "The daily seed is deterministic for the current day."
            )
            .setPositiveButton("PLAY") { _, _ -> play() }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showShop() {
        val costs = intArrayOf(25, 20, 30, 150)
        val items = arrayOf(
            "Hammer • 25 coins",
            "Shuffle • 20 coins",
            "Undo • 30 coins",
            "Theme • 150 coins"
        )

        AlertDialog.Builder(this)
            .setTitle("SHOP • ${engine.coins} coins")
            .setItems(items) { _, index ->
                val cost = costs[index]
                if (engine.coins >= cost) {
                    engine.coins -= cost
                    save.save(engine)
                    Toast.makeText(this, "Purchased!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Not enough coins", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun showSettings() {
        val music = CheckBox(this).apply {
            text = "Music"
            isChecked = true
        }
        val sfx = CheckBox(this).apply {
            text = "Sound effects"
            isChecked = true
        }
        val haptics = CheckBox(this).apply {
            text = "Haptics"
            isChecked = true
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            addView(music)
            addView(sfx)
            addView(haptics)
        }

        AlertDialog.Builder(this)
            .setTitle("SETTINGS")
            .setView(box)
            .setPositiveButton("SAVE", null)
            .setNeutralButton("RESET PROGRESS") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Reset everything?")
                    .setMessage("All levels, stars and coins will be cleared.")
                    .setPositiveButton("RESET") { _, _ ->
                        save.clear()
                        engine = GameEngine()
                        showHome()
                    }
                    .setNegativeButton("CANCEL", null)
                    .show()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (::save.isInitialized) {
            engine = GameEngine()
            save.restore(engine)
        }
    }
}
