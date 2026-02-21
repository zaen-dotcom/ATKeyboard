package com.rekber.atkeyboard

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "HunterKeyboard.db"
        private const val DATABASE_VERSION = 1

        // Nama Tabel dan Kolom
        const val TABLE_NAME = "auto_texts"
        const val COLUMN_ID = "id"
        const val COLUMN_JUDUL = "judul"
        const val COLUMN_ISI_TEKS = "isi_teks"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Query untuk membuat tabel saat aplikasi pertama kali diinstal/dibuka
        val createTableQuery = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_JUDUL TEXT, "
                + "$COLUMN_ISI_TEKS TEXT)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Fungsi untuk menyimpan payload baru
    fun insertAutoText(judul: String, isiTeks: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_JUDUL, judul)
        values.put(COLUMN_ISI_TEKS, isiTeks)

        // insert() mengembalikan -1 jika gagal
        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    fun getAllAutoTexts(): List<AutoText> {
        val list = mutableListOf<AutoText>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val judul = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_JUDUL))
                val isiTeks = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ISI_TEKS))
                list.add(AutoText(id, judul, isiTeks))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // Fungsi untuk menghapus data berdasarkan ID
    fun deleteAutoText(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}