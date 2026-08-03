package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderResponse
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderStatus
import iss.nus.edu.sg.smartmartdeliveryapp.adapter.OrderAdapter

class ListViewActivity :
    AppCompatActivity(),
    AdapterView.OnItemClickListener {

    private lateinit var listView: ListView

    private var records: List<OrderResponse> =
        emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_view)

        listView = findViewById(R.id.listView)
        listView.onItemClickListener = this

        loadRecords()
    }

    private fun loadRecords() {
        showLoading(true)

        records = listOf(
            OrderResponse(
                id = 1L,
                trackingNo = "TRK-2026-0001",
                deliveryPersonId = 1L,
                status = OrderStatus.PACKED
            ),
            OrderResponse(
                id = 2L,
                trackingNo = "TRK-2026-0002",
                deliveryPersonId = 1L,
                status = OrderStatus.PICKED_UP
            ),
            OrderResponse(
                id = 3L,
                trackingNo = "TRK-2026-0003",
                deliveryPersonId = 1L,
                status = OrderStatus.DELIVERED
            )
        )

        listView.adapter = OrderAdapter(
            this,
            records
        )

        showLoading(false)
    }

    private fun showLoading(show: Boolean) {
        val loadingOverlay =
            findViewById<FrameLayout>(
                R.id.loadingOverlay
            )

        loadingOverlay.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    override fun onItemClick(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ) {
        val selectedOrder =
            records[position]

        Toast.makeText(
            this,
            "Selected ${selectedOrder.trackingNo}",
            Toast.LENGTH_SHORT
        ).show()
    }
}