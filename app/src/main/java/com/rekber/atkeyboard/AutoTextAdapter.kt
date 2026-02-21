package com.rekber.atkeyboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AutoTextAdapter(
    private var dataList: List<AutoText>,
    private val onDeleteClick: (AutoText) -> Unit // Fungsi callback saat tombol hapus diklik
) : RecyclerView.Adapter<AutoTextAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul: TextView = view.findViewById(R.id.tvJudul)
        val tvIsiTeks: TextView = view.findViewById(R.id.tvIsiTeks)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_auto_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvJudul.text = item.judul
        holder.tvIsiTeks.text = item.isiTeks

        // Saat tombol hapus diklik
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = dataList.size

    // Fungsi untuk memperbarui data di list
    fun updateData(newList: List<AutoText>) {
        dataList = newList
        notifyDataSetChanged()
    }
}