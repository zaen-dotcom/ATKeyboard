package com.rekber.atkeyboard

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class AddAutoTextActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_auto_text)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Database
        dbHelper = DatabaseHelper(this)

        // Hubungkan ke komponen UI
        val etJudul = findViewById<TextInputEditText>(R.id.etJudul)
        val etIsiTeks = findViewById<TextInputEditText>(R.id.etIsiTeks)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Logika saat tombol simpan diklik
        btnSave.setOnClickListener {
            val judul = etJudul.text.toString().trim()
            val isiTeks = etIsiTeks.text.toString().trim()

            // Validasi agar tidak menyimpan data kosong
            if (judul.isEmpty() || isiTeks.isEmpty()) {
                Toast.makeText(this, "Judul dan Isi Teks tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan ke SQLite
            val isInserted = dbHelper.insertAutoText(judul, isiTeks)

            if (isInserted) {
                Toast.makeText(this, "Payload berhasil disimpan!", Toast.LENGTH_SHORT).show()
                // Tutup activity ini dan kembali ke halaman sebelumnya (AutoTextActivity)
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan payload.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}