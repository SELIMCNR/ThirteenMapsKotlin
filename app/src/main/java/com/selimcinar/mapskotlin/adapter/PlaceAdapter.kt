package com.selimcinar.mapskotlin.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.Placeholder
import androidx.recyclerview.widget.RecyclerView
import com.selimcinar.mapskotlin.databinding.RecyclerRowMapsBinding
import com.selimcinar.mapskotlin.model.Place
import com.selimcinar.mapskotlin.view.MapsActivity

class PlaceAdapter(val placeList: List<Place>) : RecyclerView.Adapter<PlaceAdapter.Placeholder>() {
    class Placeholder(val recyclerRowMapsBinding: RecyclerRowMapsBinding):RecyclerView.ViewHolder(recyclerRowMapsBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Placeholder {
       val recyclerRowMapsBinding=RecyclerRowMapsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return  Placeholder(recyclerRowMapsBinding)
    }

    override fun getItemCount(): Int {
        return  placeList.size
    }

    override fun onBindViewHolder(holder: Placeholder, position: Int) {
        holder.recyclerRowMapsBinding.recyclerTextMaps.text = placeList.get(position).name
        holder.itemView.setOnClickListener {
            val  intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace",placeList.get(position))
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }


}