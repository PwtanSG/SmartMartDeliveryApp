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
import android.util.Log.e
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import iss.nus.edu.sg.smartmartdeliveryapp.adapter.OrderAdapter
import retrofit2.HttpException
import java.io.IOException
import kotlin.text.clear

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

    private lateinit var barcodeScanner: GmsBarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_view)

        val scannerOptions =
            GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_QR_CODE
                )
                .enableAutoZoom()
                .build()

        barcodeScanner =
            GmsBarcodeScanning.getClient(
                this,
                scannerOptions
            )

        listView = findViewById<ListView>(R.id.listView)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        orderAdapter = OrderAdapter(
            this,
            records
        )

        listView.adapter = orderAdapter
        listView.onItemClickListener = this

        bottomNavigation.setOnItemReselectedListener {
                menuItem ->

            if (menuItem.itemId == R.id.navSearch) {
                records.clear()
                orderAdapter.notifyDataSetChanged()
                val tvNoRecords =
                    findViewById<TextView>(
                        R.id.txtViewNoRecords
                    )
                tvNoRecords.visibility = View.VISIBLE
                tvNoRecords.text = "Click Search"
                scanOrder()
            }
        }

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

                R.id.navSearch -> {
                    records.clear()
                    orderAdapter.notifyDataSetChanged()
                    val tvNoRecords =
                        findViewById<TextView>(
                            R.id.txtViewNoRecords
                        )
                    tvNoRecords.visibility = View.VISIBLE
                    tvNoRecords.text = "Click Search"
                    scanOrder()
//                    searchOrder("TRK-2026-0001", 1L)
                    true
                }
                else -> false
            }
        }

        // Load API data after views and adapter are ready
//        loadRecords(completed = false)
    }


    // after onCreate
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

                if (records.isEmpty()) {
                    tvNoRecords.text =
                        if (completed) {
                            "No completed orders"
                        } else if (completed == false) {
                                "No in-progress orders"
                        } else {
                                "Click search"
                        }


                    tvNoRecords.visibility = View.VISIBLE
                } else {
                    tvNoRecords.visibility = View.GONE
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

//    override fun onResume() {
//        super.onResume()
//        loadRecords(completed = false)
//        loadNavigationCounts()
//    }

    override fun onResume() {
        super.onResume()

        when (bottomNavigation.selectedItemId) {
            R.id.navInProgress -> {
                loadRecords(completed = false)
            }

            R.id.navCompleted -> {
                loadRecords(completed = true)
                val tvNoRecords = findViewById<TextView>(R.id.txtViewNoRecords)
                tvNoRecords.visibility = View.GONE
            }

            R.id.navSearch -> {
                // Do not reload records.
                // scanOrder/searchOrder will update the ListView.
            }
        }

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
    private fun searchOrder(
        trackingNo: String,
        deliveryPersonId: Long
    ) {
        showLoading(true)
        Log.e(
            "SEARCH_ORDER",
            "seach order : ${trackingNo} ${deliveryPersonId}"
        )
        lifecycleScope.launch {
            try {
                val order =
                    RetrofitClient.orderApi.searchOrder(
                        trackingNo,
                        deliveryPersonId
                    )

                records.clear()
                records.add(order)

                orderAdapter.notifyDataSetChanged()

                when (bottomNavigation.selectedItemId) {

                        R.id.navInProgress -> {
                            val tvtxtViewNoRecords = findViewById<TextView>(R.id.txtViewNoRecords)
                            tvtxtViewNoRecords.visibility = View.VISIBLE
                            tvtxtViewNoRecords.text = "Record found : " + records.size
                        }
                        R.id.navCompleted -> {
                            val tvtxtViewNoRecords = findViewById<TextView>(R.id.txtViewNoRecords)
                                tvtxtViewNoRecords.visibility = View.VISIBLE
                                tvtxtViewNoRecords.text = "Record found : " + records.size
                        }

                        R.id.navSearch -> {
                            // Do not reload records.
                            // scanOrder/searchOrder will update the ListView.
                            val tvtxtViewNoRecords = findViewById<TextView>(R.id.txtViewNoRecords)
                            if (records.size > 0) {
                                tvtxtViewNoRecords.visibility = View.VISIBLE
                                tvtxtViewNoRecords.text = "Record found : " + trackingNo
                            } else {
                                tvtxtViewNoRecords.visibility = View.VISIBLE
                                tvtxtViewNoRecords.text = "Record found : " + records.size
                            }

                        }
                        else -> {
                            val tvtxtViewNoRecords = findViewById<TextView>(R.id.txtViewNoRecords)
                            tvtxtViewNoRecords.visibility = View.GONE
                            tvtxtViewNoRecords.text = ""
                        }
                    }

                } catch (e: HttpException) {
                if (e.code() == 404) {
                    Toast.makeText(
                        this@ListViewActivity,
                        trackingNo + deliveryPersonId + "Order not found or not assigned to you : " + e.code() + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@ListViewActivity,
                        "Search failed: HTTP ${e.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                Toast.makeText(
                    this@ListViewActivity,
                    "Cannot connect to Spring Boot",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(
                    "ORDER_SEARCH",
                    "Search failed",
                    e
                )

                Toast.makeText(
                    this@ListViewActivity,
                    "Search failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun scanOrder() {
        barcodeScanner.startScan()
            .addOnSuccessListener { barcode ->

                val trackingNo =
                    barcode.rawValue?.trim()

                Log.e(
                    "SCAN_ORDER",
                    "track no : ${trackingNo}"
                )

                if (trackingNo.isNullOrBlank()) {
                    Toast.makeText(
                        this,
                        "Invalid barcode",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }
                Toast.makeText(
                    this,
                    "Searching Tracking No : ${trackingNo}",
                    Toast.LENGTH_LONG
                ).show()
                searchOrder(trackingNo, 1L)
            }
            .addOnCanceledListener {
                Toast.makeText(
                    this,
                    "Scanning cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Scanning failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun scanOrder1() {
        barcodeScanner.startScan()
            .addOnSuccessListener { barcode ->

                val trackingNo =
                    barcode.rawValue?.trim()

                if (trackingNo.isNullOrBlank()) {
                    return@addOnSuccessListener
                }

                searchOrder("TRK-2026-0001", 1L)
            }
    }

    private fun handleScannedOrder(
        trackingNo: String,
        deliveryPersonId_: Long
    ) {
        searchOrder(
            trackingNo = trackingNo,
            deliveryPersonId = 1L
        )
    }

    }

