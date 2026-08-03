package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderResponse
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderStatus
import iss.nus.edu.sg.smartmartdeliveryapp.adapter.OrderAdapter
import iss.nus.edu.sg.smartmartdeliveryapp.api.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Log
import retrofit2.HttpException
import java.io.IOException

class ListViewActivity :
    AppCompatActivity(),
    AdapterView.OnItemClickListener {

    private lateinit var listView: ListView
    private lateinit var orderAdapter: OrderAdapter

    private val records =
        mutableListOf<OrderResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_view)

        listView = findViewById(R.id.listView)
        orderAdapter = OrderAdapter(
            this,
            records
        )
        listView.adapter = orderAdapter
        listView.onItemClickListener = this

        loadRecords()
    }

    private fun loadRecords() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                Log.d("ORDER_API", "Request started")

                val response =
                    RetrofitClient.orderApi
                        .getAssignedOrders(1L)

                Log.d(
                    "ORDER_API",
                    "Received ${response.size} orders: $response"
                )

                records.clear()
                records.addAll(response)

                orderAdapter.notifyDataSetChanged()

                if (response.isEmpty()) {
                    Toast.makeText(
                        this@ListViewActivity,
                        "No assigned orders found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: HttpException) {
                Log.e(
                    "ORDER_API",
                    "HTTP error ${e.code()}: " +
                            e.response()?.errorBody()?.string(),
                    e
                )

                Toast.makeText(
                    this@ListViewActivity,
                    "HTTP error ${e.code()}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                Log.e(
                    "ORDER_API",
                    "Connection error: ${e.message}",
                    e
                )

                Toast.makeText(
                    this@ListViewActivity,
                    "Cannot connect to Spring Boot",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(
                    "ORDER_API",
                    "Conversion/error: ${e.message}",
                    e
                )

                Toast.makeText(
                    this@ListViewActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
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