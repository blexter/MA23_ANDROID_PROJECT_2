package com.example.ma23_android_project_2.ui.list

import places
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.ma23_android_project_2.MainActivity
import com.example.ma23_android_project_2.R

class ListRecyclerAdapter (val activity: MainActivity,
                           val context : Context,
                           val places: List<places>,
                           private val onDeleteClickListener: OnDeleteClickListener,
    val fragment : Fragment
) : RecyclerView.Adapter<ListRecyclerAdapter.ViewHolder>() {

    val layoutInflater = LayoutInflater.from(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = places[position]
        Log.d("ListRecyclerAdapter", "Binding data for position $position: $place")

        holder.nameTextView.text = place.name
        holder.lonLatTextView.text = "${place.positionLat} \n ${place.positionLon}"
        holder.placePosition = position
        if(!activity.loggedIn) {
            holder.deleteButton.isVisible = false;
        }


    }

    override fun getItemCount() = places.size

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val nameTextView = itemView.findViewById<TextView>(R.id.nameEditText)
        val lonLatTextView = itemView.findViewById<TextView>(R.id.lonLatTextView)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.deleteButton)
        var placePosition = 0

        init {
            deleteButton.setOnClickListener{
                onDeleteClickListener.onDeleteClick(placePosition)
            }

            itemView.setOnClickListener {
                val bundle = bundleOf("position" to placePosition)
                findNavController(fragment).navigate(R.id.nav_addPlace, bundle)
            }

        }
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }
}