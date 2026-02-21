package com.rekber.atkeyboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AutoTextActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvAutoText: RecyclerView
    private lateinit var adapter: AutoTextAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auto_text)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)
        rvAutoText = findViewById(R.id.rvAutoText)

        // 1. Setup RecyclerView
        rvAutoText.layoutManager = LinearLayoutManager(this)
        adapter = AutoTextAdapter(emptyList()) { autoText ->
            // Menampilkan dialog konfirmasi sebelum menghapus
            showDeleteConfirmation(autoText)
        }
        rvAutoText.adapter = adapter

        // 2. Setup Floating Action Button (FAB)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddAutoTextActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi ini dipanggil setiap kali halaman ini muncul di layar
    override fun onResume() {
        super.onResume()
        refreshList()
    }

    // Mengambil data terbaru dari DB dan memasukkannya ke Adapter
    private fun refreshList() {
        val dataList = dbHelper.getAllAutoTexts()
        adapter.updateData(dataList)
    }

    // Konfirmasi penghapusan agar lebih profesional dan aman
    private fun showDeleteConfirmation(autoText: AutoText) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Payload?")
            .setMessage("Apakah kamu yakin ingin menghapus '${autoText.judul}'?")
            .setPositiveButton("Hapus") { _, _ ->
                val isDeleted = dbHelper.deleteAutoText(autoText.id)
                if (isDeleted) {
                    Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    refreshList() // Muat ulang list setelah dihapus
                } else {
                    Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}