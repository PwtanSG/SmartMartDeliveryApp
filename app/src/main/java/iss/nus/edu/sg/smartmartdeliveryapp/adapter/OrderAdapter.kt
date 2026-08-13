package iss.nus.edu.sg.smartmartdeliveryapp.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderResponse
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderStatus
import android.content.res.ColorStateList

class OrderAdapter(
    context: Context,
    private val records: MutableList<OrderResponse>
) : ArrayAdapter<OrderResponse>(
    context,
    R.layout.job_row,
    records
) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val rowView = convertView
            ?: LayoutInflater.from(context).inflate(
                R.layout.job_row,
                parent,
                false
            )

        val order = getItem(position)
            ?: return rowView

        val tvTrackingNo =
            rowView.findViewById<TextView>(
                R.id.tvTrackingNo
            )

        val tvRecipientName =
            rowView.findViewById<TextView>(
                R.id.tvRecipentName
            )

        val tvRecipientPhone =
            rowView.findViewById<TextView>(
                R.id.tvRecipentPhone
            )

        val tvRecipientAddress =
            rowView.findViewById<TextView>(
                R.id.tvRecipentAddress
            )

        val btnStatus =
            rowView.findViewById<Button>(
                R.id.btnStatus
            )

        tvTrackingNo.text = order.trackingNo

        // Temporary values until they come from the API
        tvRecipientName.text = order.firstName + " " + order.lastName
        tvRecipientPhone.text = order.phoneNumber
        tvRecipientAddress.text = order.shippingAddress

        when (order.status) {
            OrderStatus.PACKED -> {
                btnStatus.text = "Packed"
                btnStatus.setTextColor(Color.WHITE)
                btnStatus.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#6A4FB3")
                    )
                btnStatus.isEnabled = true
                btnStatus.isClickable = false
            }

            OrderStatus.PICKED_UP -> {
                btnStatus.text = "Picked Up"
                btnStatus.setTextColor(Color.WHITE)
                btnStatus.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#F0A331")
                    )
                btnStatus.isEnabled = true
                btnStatus.isClickable = false
            }

            OrderStatus.DELIVERED -> {
                btnStatus.text = "Delivered"
                btnStatus.setTextColor(Color.WHITE)
                btnStatus.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#6B6670")
                    )

                // Keep enabled to prevent Android fading the colour
                btnStatus.isEnabled = false
                btnStatus.isClickable = false
                btnStatus.isFocusable = false
            }

            else -> Unit
        }
        return rowView
    }
}