package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.content.Intent
import android.graphics.Color
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

import iss.nus.edu.sg.smartmartdeliveryapp.api.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import iss.nus.edu.sg.smartmartdeliveryapp.adapter.OrderAdapter
import retrofit2.HttpException
import java.io.IOException

class ListViewActivity :
    AppCompatActivity(),
    AdapterView.OnItemClickListener {

    private lateinit var listView: ListView
    private lateinit var orderAdapter: OrderAdapter

    private val allRecords =
        mutableListOf<OrderResponse>()

    private val records =
        mutableListOf<OrderResponse>()

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_view)

        listView = findViewById<ListView>(R.id.listView)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        orderAdapter = OrderAdapter(
            this,
            records
        )

        listView.adapter = orderAdapter
        listView.onItemClickListener = this

        bottomNavigation.setOnItemSelectedListener {
                menuItem ->

            Log.d(
                "BOTTOM_NAV",
                "Selected item: ${menuItem.itemId}"
            )

            when (menuItem.itemId) {
                R.id.navInProgress -> {
                    loadRecords(completed = false)
                    true
                }

                R.id.navCompleted -> {
                    loadRecords(completed = true)
                    true
                }

                else -> false
            }
        }

        // Load API data after views and adapter are ready
        loadRecords(completed = false)
    }

    private fun loadRecords(completed: Boolean) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                Log.d("ORDER_API", "Request started")

                val response =
                    if (completed) {
                        RetrofitClient.orderApi
                            .getCompletedOrders(1L)
                    } else {
                        RetrofitClient.orderApi
                            .getInProgressOrders(1L)
                    }

                Log.d(
                    "ORDER_API",
                    "Received ${response.size} orders: $response"
                )
                records.clear()
                records.addAll(response)

                orderAdapter.notifyDataSetChanged()

                if (completed) {
                    updateBadge(
                        R.id.navCompleted,
                        response.size
                    )
                } else {
                    updateBadge(
                        R.id.navInProgress,
                        response.size
                    )
                }

                if (response.isEmpty()) {
                    Toast.makeText(
                        this@ListViewActivity,
                        "No assigned orders found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                val tvNoRecords =
                    findViewById<TextView>(
                        R.id.txtViewNoRecords
                    )

                tvNoRecords.visibility =
                    if (records.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
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

        val intent = Intent(
            this,
            WorkflowActivity::class.java
        ).apply {
            putExtra("ORDER_ID", selectedOrder.id)
            putExtra(
                "TRACKING_NO",
                selectedOrder.trackingNo
            )
            putExtra(
                "DELIVERY_PERSON_ID",
                selectedOrder.deliveryPersonId
            )
            putExtra(
                "ORDER_STATUS",
                selectedOrder.status.name
            )
            putExtra(
                "RECIPIENT_ADDRESS",
                "2 Clementi West Street 2 129605"
            )
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadRecords(completed = false)
        loadNavigationCounts()
    }

    private fun updateBadge(
        menuItemId: Int,
        count: Int
    ) {
        if (count > 0) {
            val badge =
                bottomNavigation.getOrCreateBadge(
                    menuItemId
                )

            badge.isVisible = true
            badge.number = count
            badge.backgroundColor =
                Color.parseColor("#6A4FB3")
            badge.badgeTextColor = Color.WHITE
        } else {
            bottomNavigation.removeBadge(menuItemId)
        }
    }

    private fun loadNavigationCounts() {
        lifecycleScope.launch {
            try {
                val inProgressOrders =
                    RetrofitClient.orderApi
                        .getInProgressOrders(1L)

                val completedOrders =
                    RetrofitClient.orderApi
                        .getCompletedOrders(1L)

                updateBadge(
                    R.id.navInProgress,
                    inProgressOrders.size
                )

                updateBadge(
                    R.id.navCompleted,
                    completedOrders.size
                )
            } catch (e: Exception) {
                Log.e(
                    "ORDER_COUNT",
                    "Unable to load order counts",
                    e
                )
            }
        }
    }
}