package com.example.videoframes

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.videoframes.RecyclerAdapter.MyViewHolder
import java.util.*

class RecyclerAdapter(var all: ArrayList<Bitmap>, var context: Context) : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.image_container, parent, false)
        return MyViewHolder(v, context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.displayer.setImageBitmap(all[position])
    }

    override fun getItemCount(): Int {
        return all.size
    }

    inner class MyViewHolder(itemView: View, context: Context?) : ViewHolder(itemView) {
        var displayer: ImageView

        init {
            displayer = itemView.findViewById(R.id.imgDisplayer)
        }
    }

}