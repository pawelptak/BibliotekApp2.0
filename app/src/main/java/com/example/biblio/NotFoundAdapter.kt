package com.example.bibliotekapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class NotFoundAdapter: RecyclerView.Adapter<MyViewHolder2>(){
    override fun onBindViewHolder(holder: MyViewHolder2, position: Int) {


    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder2 {
        val layoutInflater = LayoutInflater.from(parent.context)
        val wiersz = layoutInflater.inflate(R.layout.not_found, parent, false) //dostarczam xml
        return MyViewHolder2(wiersz)
    }



}

class MyViewHolder2(val view: View): RecyclerView.ViewHolder(view)