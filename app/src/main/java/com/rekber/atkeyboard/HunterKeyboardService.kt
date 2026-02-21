package com.rekber.atkeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewFlipper
import android.util.TypedValue
import android.graphics.Color
import android.view.Gravity
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent

class HunterKeyboardService : InputMethodService() {

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var btnToggleAutoText: Button
    private lateinit var layoutQwerty: LinearLayout
    private lateinit var layoutAutoTextList: LinearLayout

    // Deklarasi Database Helper
    private lateinit var dbHelper: DatabaseHelper

    private var isAutoTextView = false
    private var isSymbolView = false
    private var isShifted = false // Untuk huruf besar/kecil

    // Variabel untuk fitur pintar (UX)
    private var lastSpaceTime: Long = 0
    private var isDeleting = false
    private val deleteHandler = Handler(Looper.getMainLooper())
    private val deleteRunnable = object : Runnable {
        override fun run() {
            if (isDeleting) {
                currentInputConnection?.deleteSurroundingText(1, 0)
                deleteHandler.postDelayed(this, 50) // Kecepatan hapus beruntun
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        dbHelper = DatabaseHelper(this)
    }

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.layout_keyboard, null)

        viewFlipper = keyboardView.findViewById(R.id.viewFlipper)
        btnToggleAutoText = keyboardView.findViewById(R.id.btnToggleAutoText)
        layoutQwerty = keyboardView.findViewById(R.id.layoutQwerty)
        layoutAutoTextList = keyboardView.findViewById(R.id.layoutAutoTextList)

        renderKeyboardLayout()

        btnToggleAutoText.setOnClickListener {
            if (isAutoTextView) {
                viewFlipper.displayedChild = 0
                btnToggleAutoText.text = "🚀  Auto Text Payloads"
                isAutoTextView = false
            } else {
                setupAutoTextList()
                viewFlipper.displayedChild = 1
                btnToggleAutoText.text = "⌨️ Kembali ke Keyboard"
                isAutoTextView = true
            }
        }

        return keyboardView
    }

    private fun renderKeyboardLayout() {
        layoutQwerty.removeAllViews()

        val rows = if (isSymbolView) {
            arrayOf(
                arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                arrayOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
                arrayOf("<", ">", "{", "}", "[", "]", "=", "*", "\"", "'"),
                arrayOf("ABC", "~", "`", "|", "\\", ":", ";", "!", "?", "DEL"),
                arrayOf(",", "SPACE", ".", "ENTER")
            )
        } else {
            arrayOf(
                arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                arrayOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
                arrayOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
                arrayOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "DEL"),
                arrayOf("?123", "/", "SPACE", ".", "ENTER")
            )
        }

        for (row in rows) {
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (key in row) {
                val weightVar = when (key) {
                    "SPACE" -> 4f
                    "ENTER", "DEL", "SHIFT", "?123", "ABC" -> 1.5f
                    else -> 1f
                }

                val heightInDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 46f, resources.displayMetrics
                ).toInt()

                val marginHorizontal = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics
                ).toInt()

                val marginVertical = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics
                ).toInt()

                val layoutParams = LinearLayout.LayoutParams(0, heightInDp).apply {
                    weight = weightVar
                    setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical)
                }

                val keyView: View

                // --- PEMISAHAN LOGIKA: ICON vs TEKS ---
                if (key in listOf("SHIFT", "DEL", "ENTER")) {
                    keyView = ImageButton(this).apply {
                        this.layoutParams = layoutParams
                        setBackgroundResource(R.drawable.bg_key_action)

                        when (key) {
                            "SHIFT" -> {
                                setImageResource(R.drawable.ic_keyboard_shift)
                                val iconColor = if (isShifted) Color.parseColor("#1A73E8") else Color.parseColor("#202124")
                                setColorFilter(iconColor)
                            }
                            "DEL" -> {
                                setImageResource(R.drawable.ic_keyboard_delete)
                                setColorFilter(Color.parseColor("#202124"))
                            }
                            "ENTER" -> {
                                setImageResource(R.drawable.ic_keyboard_enter)
                                setColorFilter(Color.parseColor("#202124"))
                            }
                        }
                    }
                } else {
                    keyView = Button(this).apply {
                        this.layoutParams = layoutParams
                        text = if (isShifted && key.length == 1 && key[0].isLetter()) key.uppercase() else key
                        isAllCaps = false
                        textSize = 18f
                        setTextColor(Color.parseColor("#202124"))

                        if (key in listOf("?123", "ABC", "SPACE")) {
                            setBackgroundResource(R.drawable.bg_key_action)
                        } else {
                            setBackgroundResource(R.drawable.bg_key_normal)
                        }
                    }
                }

                // --- EVENT SENTUH (Touch Listener) Pintar, Ringan, Tanpa Getar ---
                keyView.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            v.isPressed = true // Efek visual saat ditekan

                            val ic = currentInputConnection ?: return@setOnTouchListener true

                            when (key) {
                                "DEL" -> {
                                    val selectedText = ic.getSelectedText(0)
                                    if (selectedText.isNullOrEmpty()) {
                                        ic.deleteSurroundingText(1, 0)
                                        // Mulai timer hapus beruntun (Long-press)
                                        isDeleting = true
                                        deleteHandler.postDelayed(deleteRunnable, 400)
                                    } else {
                                        ic.commitText("", 1) // Hapus teks yang di-block
                                    }
                                }
                                "SPACE" -> {
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastSpaceTime < 300) {
                                        ic.deleteSurroundingText(1, 0)
                                        ic.commitText(". ", 1) // Double-tap jadi titik
                                        lastSpaceTime = 0
                                    } else {
                                        ic.commitText(" ", 1)
                                        lastSpaceTime = currentTime
                                    }
                                }
                                "ENTER" -> ic.commitText("\n", 1)
                                "SHIFT" -> {
                                    isShifted = !isShifted
                                    renderKeyboardLayout()
                                }
                                "?123" -> {
                                    isSymbolView = true
                                    renderKeyboardLayout()
                                }
                                "ABC" -> {
                                    isSymbolView = false
                                    renderKeyboardLayout()
                                }
                                else -> {
                                    val textToCommit = if (isShifted && key.length == 1 && key[0].isLetter()) key.uppercase() else key
                                    ic.commitText(textToCommit, 1)

                                    // Pintar: otomatis balik ke huruf kecil setelah ngetik 1 huruf kapital
                                    if (isShifted && textToCommit.length == 1 && textToCommit[0].isLetter()) {
                                        isShifted = false
                                        renderKeyboardLayout()
                                    }
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            v.isPressed = false
                            if (key == "DEL") {
                                // Hentikan hapus beruntun saat jari diangkat
                                isDeleting = false
                                deleteHandler.removeCallbacks(deleteRunnable)
                            }
                            true
                        }
                    }
                    false
                }
                rowLayout.addView(keyView)
            }
            layoutQwerty.addView(rowLayout)
        }
    }

    private fun setupAutoTextList() {
        layoutAutoTextList.removeAllViews()

        val autoTexts = dbHelper.getAllAutoTexts()

        if (autoTexts.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "Belum ada Auto Text.\nSilakan tambahkan lewat aplikasi Hunter Keyboard."
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, 60, 0, 0)
            }
            layoutAutoTextList.addView(tvEmpty)
            return
        }

        for (item in autoTexts) {
            val btn = Button(this).apply {
                text = item.judul
                isAllCaps = false
                textSize = 16f
                setTextColor(Color.parseColor("#202124"))
                setBackgroundResource(R.drawable.bg_key_normal)

                val marginInDp = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
                ).toInt()

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(marginInDp, marginInDp, marginInDp, marginInDp)
                }
            }

            btn.setOnClickListener {
                currentInputConnection?.commitText(item.isiTeks, 1)

                viewFlipper.displayedChild = 0
                btnToggleAutoText.text = "🚀  Auto Text Payloads"
                isAutoTextView = false
            }

            layoutAutoTextList.addView(btn)
        }
    }
}