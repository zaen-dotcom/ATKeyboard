package com.rekber.atkeyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // Deklarasi Database Helper
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- INI PEMICU DATABASE-NYA ---
        // Saat MainActivity terbuka, ini akan otomatis membuat HunterKeyboard.db
        dbHelper = DatabaseHelper(this)
        dbHelper.writableDatabase
        // -------------------------------

        val btnEnable = findViewById<Button>(R.id.btnEnableKeyboard)
        val btnSelect = findViewById<Button>(R.id.btnSelectKeyboard)
        val btnManage = findViewById<Button>(R.id.btnManagePayloads)

        btnEnable.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        btnSelect.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        btnManage.setOnClickListener {
            val intent = Intent(this, AutoTextActivity::class.java)
            startActivity(intent)
        }
    }
}