package iss.nus.edu.sg.smartmartdeliveryapp.adapter
import android.app.Activity
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

class OrderAdapter(private val context: Context,
                    private val records: List<OrderResponse>

) : ArrayAdapter<Any?> (
    context, R.layout.job_row
){
    init {
        addAll(*arrayOfNulls<Any>(records.size))
    }

    override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
        var _view = view

        if (_view == null) {
            val inflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            _view = inflater.inflate(R.layout.job_row, parent, false)
        }

        val record = records[pos]
        // set the text for TextView
        val btnStatus = _view.findViewById<Button>(R.id.btnStatus)
        val textViewTrackingNo = _view.findViewById<TextView>(R.id.tvTrackingNo)
        textViewTrackingNo.text = record.trackingNo


        when (record.status) {
            OrderStatus.PACKED -> {
                btnStatus.text = "Packed"
                btnStatus.isEnabled = true
                btnStatus.setBackgroundColor(Color.parseColor("#198754"))
            }

            OrderStatus.PICKED_UP -> {
                btnStatus.text = "Picked Up"
                btnStatus.isEnabled = true
                btnStatus.setBackgroundColor(Color.parseColor("#FFBF00"))
            }

            OrderStatus.DELIVERED -> {
                btnStatus.text = "Delivered"
                btnStatus.isEnabled = false
                btnStatus.setBackgroundColor(Color.LTGRAY)
            }

            else -> {
                btnStatus.text = record.status.name
                btnStatus.isEnabled = false
            }
        }

        btnStatus.setOnClickListener {
            Toast.makeText(
                context,
                "Btn Pressed ${record.trackingNo} ${record.status}",
                Toast.LENGTH_SHORT
            ).show()
        }
        return _view
    }
}
