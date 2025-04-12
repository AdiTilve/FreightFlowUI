package com.example.freightflow.utils

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.freightflow.R
import com.example.freightflow.model.Assignment
import com.example.freightflow.model.Flight

class AirportAdapter : RecyclerView.Adapter<AirportAdapter.ViewHolder>() {
    private var flight: Flight? = null
    private var assignment: Assignment? = null

    fun updateData(flight: Flight?, assignment: Assignment?) {
        this.flight = flight
        this.assignment = assignment
        notifyDataSetChanged()
    }

    fun updateFlight(newFlight: Flight) {
        this.flight = newFlight
        notifyDataSetChanged()
    }
    fun updateAssignment(newAssignment: Assignment) {
        this.assignment = newAssignment
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_airport, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        flight?.let { holder.bindFlight(it) }
        assignment?.let { holder.bindAssignment(it) }
    }

    override fun getItemCount(): Int = 1 // Since we're showing one combined item

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindFlight(flight: Flight) {
            itemView.findViewById<TextView>(R.id.HeaderTextView).text = "Flight ${flight.flightNumber}"
            itemView.findViewById<TextView>(R.id.airlineTextView).text = flight.airlineName
            itemView.findViewById<TextView>(R.id.routeTextView).text = "${flight.arrivalAirport} → ${flight.destinationAirport}"
            itemView.findViewById<TextView>(R.id.statusTextView).text = flight.state
            itemView.findViewById<TextView>(R.id.terminalTextView).text = flight.terminal
            itemView.findViewById<TextView>(R.id.landingTimeTextView).text = flight.landingTime
        }

        fun bindAssignment(assignment: Assignment) {
            itemView.findViewById<TextView>(R.id.cargoTypeTextView).text = assignment.cargoType
            itemView.findViewById<TextView>(R.id.priorityLevelTextView).text = assignment.priorityLevel
            itemView.findViewById<TextView>(R.id.assignmentTypeTextView).text = assignment.assignmentType
        }
    }
}