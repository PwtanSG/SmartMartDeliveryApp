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
        tvRecipientName.text = "Alice Tay"
        tvRecipientPhone.text = "91231234"
        tvRecipientAddress.text =
            "123 Orchard Rd #12-12 S123321"

//        when (order.status) {
//            OrderStatus.PACKED -> {
//                btnStatus.text = "Packed"
//                btnStatus.isEnabled = true
//                btnStatus.setBackgroundColor(Color.parseColor("#198754"))
//            }
//
//            OrderStatus.PICKED_UP -> {
//                btnStatus.text = "Picked Up"
//                btnStatus.isEnabled = true
//                btnStatus.setBackgroundColor(Color.parseColor("#FFBF00"))
//            }
//
//            OrderStatus.DELIVERED -> {
//                btnStatus.text = "Delivered"
//                btnStatus.isEnabled = false
//                btnStatus.setBackgroundColor(Color.GRAY)
//            }
//
//            else -> {
//                btnStatus.text = order.status.name
//                btnStatus.isEnabled = false
//                btnStatus.setBackgroundColor(Color.LTGRAY)
//            }
//        }

        when (order.status) {
            OrderStatus.PACKED -> {
                btnStatus.text = "Packed"
                btnStatus.setTextColor(Color.WHITE)
                btnStatus.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#6A4FB3")
                    )
                btnStatus.isEnabled = true
                btnStatus.isClickable = true
            }

            OrderStatus.PICKED_UP -> {
                btnStatus.text = "Picked Up"
                btnStatus.setTextColor(Color.WHITE)
                btnStatus.backgroundTintList =
                    ColorStateList.valueOf(
                        Color.parseColor("#EF6C00")
                    )
                btnStatus.isEnabled = true
                btnStatus.isClickable = true
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

        btnStatus.setOnClickListener {
            Toast.makeText(
                context,
                "Btn Pressed ${order.trackingNo} ${order.status}",
                Toast.LENGTH_SHORT
            ).show()
        }
        return rowView
    }
}