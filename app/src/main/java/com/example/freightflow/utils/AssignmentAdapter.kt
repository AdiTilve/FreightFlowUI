package com.example.freightflow.utils

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.freightflow.utils.AirportActivity
import com.example.freightflow.R
import com.example.freightflow.model.Assignment

class AssignmentAdapter(private val assignments: MutableList<Assignment>) :
    RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder>() {

    var onStartAssignmentClicked: ((Assignment) -> Unit)? = null

    fun updateAssignment(updatedAssignment: Assignment) {
        val position = assignments.indexOfFirst { it.assignmentNumber == updatedAssignment.assignmentNumber }
        if (position != -1) {
            assignments[position] = updatedAssignment
            notifyItemChanged(position)
        } else {
            assignments.add(updatedAssignment)
            notifyItemInserted(assignments.size - 1)
        }
    }

    fun clearAssignments() {
        assignments.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assignment, parent, false)
        return AssignmentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val currentAssignment = assignments[position]

        // Set header with label and value
        holder.assignmentHeaderTextView.text = "Assignment Number: ${currentAssignment.assignmentNumber}"

        // Set other values
        holder.flightNumberTextView.text = currentAssignment.flightNumber
        holder.cargoTypeTextView.text = currentAssignment.cargoType
        holder.priorityLevelTextView.text = currentAssignment.priorityLevel
        holder.assignmentTypeTextView.text = currentAssignment.assignmentType
        holder.statusTextView.text = currentAssignment.assignmentStatus
        holder.flightStatusTextView.text = currentAssignment.flightStatus

        // Configure button state based on business logic
        val canStartAssignment = currentAssignment.flightStatus == "LANDED" &&
                currentAssignment.assignmentStatus == "PENDING"

        holder.startAssignmentButton.isEnabled = canStartAssignment
        holder.startAssignmentButton.alpha = if (canStartAssignment) 1f else 0.5f

        holder.startAssignmentButton.setOnClickListener {
            if (canStartAssignment) {
                // Use holder.itemView.context instead of just itemView.context
                val intent = Intent(holder.itemView.context, AirportActivity::class.java).apply {
                    putExtra("FLIGHT_NUMBER", currentAssignment.flightNumber)
                    putExtra("ASSIGNMENT_NUMBER", currentAssignment.assignmentNumber)
                }
                holder.itemView.context.startActivity(intent)

                // Optional: Keep your callback if needed
                onStartAssignmentClicked?.invoke(currentAssignment)
            }
        }


        // Set visual feedback based on status
        setStatusVisuals(holder, currentAssignment)
    }

    private fun setStatusVisuals(holder: AssignmentViewHolder, assignment: Assignment) {
        // Set color for assignment status text
        when (assignment.assignmentStatus) {
            "PENDING" -> holder.statusTextView.setTextColor(Color.parseColor("#FFA500")) // Orange
            "IN_PROGRESS" -> holder.statusTextView.setTextColor(Color.BLUE)
            "COMPLETED" -> holder.statusTextView.setTextColor(Color.GREEN)
            "CANCELLED" -> holder.statusTextView.setTextColor(Color.RED)
            else -> holder.statusTextView.setTextColor(Color.BLACK)
        }

        // Set color for flight status text
        when (assignment.flightStatus) {
            "LANDED" -> holder.flightStatusTextView.setTextColor(Color.GREEN)
            "DELAYED" -> holder.flightStatusTextView.setTextColor(Color.parseColor("#FFA500")) // Orange
            "CANCELLED" -> holder.flightStatusTextView.setTextColor(Color.RED)
            "SCHEDULED" -> holder.flightStatusTextView.setTextColor(Color.BLUE)
            else -> holder.flightStatusTextView.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount(): Int = assignments.size

    inner class AssignmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Header
        val assignmentHeaderTextView: TextView = itemView.findViewById(R.id.assignmentHeaderTextView)

        // Details
        val flightNumberTextView: TextView = itemView.findViewById(R.id.flightNumberTextView)
        val cargoTypeTextView: TextView = itemView.findViewById(R.id.cargoTypeTextView)
        val priorityLevelTextView: TextView = itemView.findViewById(R.id.priorityLevelTextView)
        val assignmentTypeTextView: TextView = itemView.findViewById(R.id.assignmentTypeTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val flightStatusTextView: TextView = itemView.findViewById(R.id.flightStatusTextView)

        // Action button
        val startAssignmentButton: Button = itemView.findViewById(R.id.startAssignmentButton)
    }
}