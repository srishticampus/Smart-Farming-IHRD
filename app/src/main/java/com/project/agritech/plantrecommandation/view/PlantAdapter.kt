package com.project.agritech.plantrecommandation.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.agritech.R
import com.project.agritech.plantrecommandation.model.Plant

class PlantAdapter(
    private val plantList: List<Plant>,
    private val onItemClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val plantImage: ImageView = itemView.findViewById(R.id.plantImage)
        val plantName: TextView = itemView.findViewById(R.id.plantName)

        fun bind(plant: Plant, onItemClick: (Plant) -> Unit) {
            plantName.text = plant.name

            // Load image from URL using Glide
            Glide.with(itemView.context)
                .load(plant.imageRes) // URL from API
                .placeholder(R.drawable.tomato) // Show while loading
                .error(R.drawable.fig) // If failed to load
                .into(plantImage)

            itemView.setOnClickListener { onItemClick(plant) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(plantList[position], onItemClick)
    }

    override fun getItemCount(): Int = plantList.size
}
