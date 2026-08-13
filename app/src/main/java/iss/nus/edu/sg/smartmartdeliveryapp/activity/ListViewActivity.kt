package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
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
import iss.nus.edu.sg.smartmartdeliveryapp.api.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Log
import android.view.Gravity
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import iss.nus.edu.sg.smartmartdeliveryapp.adapter.OrderAdapter
import iss.nus.edu.sg.smartmartdeliveryapp.view.DonutChartView
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import android.Manifest
import com.google.firebase.messaging.FirebaseMessaging
import iss.nus.edu.sg.smartmartdeliveryapp.model.DeviceTokenRequest

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

    private lateinit var dashboardContainer: FrameLayout

    private lateinit var tvInProgressCount: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var donutChart: DonutChartView

    private lateinit var tvNoRecord: TextView

    private lateinit var tvCurrentDateTime: TextView

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    "Notifications are disabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list_view)

//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    Log.e(
//                        "FCM_TOKEN",
//                        "Failed to obtain FCM token",
//                        task.exception
//                    )
//                    return@addOnCompleteListener
//                }
//
//                val token = task.result
//
//                Log.d("FCM_TOKEN", token)
//
//                // Later: send this token together with the
//                // logged-in deliveryPersonId to Spring Boot.
//            }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(
                    "FCM_TOKEN",
                    "Current token: [$token]"
                )
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.orderApi.registerDeviceToken(
                            DeviceTokenRequest(
                                deliveryPersonId = 1L,
                                fcmToken = token
                            )
                        )
                        Log.d("FCM_TOKEN", "Token registered")
                        Log.d(
                            "FCM_REGISTER",
                            "HTTP ${response.code()}, " +
                                    "success=${response.isSuccessful}"
                        )
                    } catch (e: Exception) {
                        Log.e("FCM_TOKEN", "Registration failed", e)
                        Log.e(
                            "FCM_REGISTER",
                            "Registration failed",
                            e
                        )
                    }
                }
            }

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        dashboardContainer =
            findViewById(R.id.dashboardContainer)
        // default to first menu item
        dashboardContainer.visibility = View.VISIBLE
        loadDashboard()

        tvInProgressCount =
            findViewById(R.id.tvInProgressCount)

        tvCompletedCount =
            findViewById(R.id.tvCompletedCount)

        donutChart =
            findViewById(R.id.donutChart)

        tvNoRecord =
            findViewById<TextView>(R.id.txtViewNoRecords)

        tvCurrentDateTime =
            findViewById(R.id.tvCurrentDateTime)

        val topRightAccount =
            findViewById<FrameLayout>(R.id.btnAccount)

        topRightAccount.setOnClickListener { anchorView ->
//            val popupMenu =
//                PopupMenu(this, anchorView)

            val popupMenu =
                PopupMenu(
                    this,
                    anchorView,
                    Gravity.END
                )

            popupMenu.menuInflater.inflate(
                R.menu.profile_popup_menu,
                popupMenu.menu
            )

            // Display the logout icon beside the text
            popupMenu.setForceShowIcon(true)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.actionLogout -> {
                        showLogoutConfirmation()
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }

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
                tvNoRecord.visibility = View.VISIBLE
                tvNoRecord.text = "Click Search"
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
                    showOrderList()
                    loadRecords(completed = false)
                    true
                }

                R.id.navCompleted -> {
                    showOrderList()
                    loadRecords(completed = true)
                    true
                }

                R.id.navSearch -> {
                    showOrderList()
                    records.clear()
                    orderAdapter.notifyDataSetChanged()
                    tvNoRecord.visibility = View.VISIBLE
                    tvNoRecord.text = "Click Search"
                    scanOrder()
//                    searchOrder("TRK-2026-0001", 1L)
                    true
                }
                R.id.navDashboard -> {
                    showDashboard()
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
            putExtra("RECIPENT_FIRST_NAME", selectedOrder.firstName)
            putExtra("RECIPENT_LAST_NAME", selectedOrder.lastName)
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
                "RECIPIENT_PHONE",
                "+6591233211"
            )
            putExtra(
                "ORDER_STATUS",
                selectedOrder.status.name
            )
            putExtra(
                "RECIPIENT_ADDRESS",
                selectedOrder.shippingAddress
            )
            putExtra(
                "DELIVERY_PROOF_KEY",
                selectedOrder.deliveryProofKey
            )
            putExtra(
                "DELIVERED_AT",
                selectedOrder.deliveredAt
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
                    val tvtxtViewNoRecords = findViewById<TextView>(R.id.txtViewNoRecords)
                    tvtxtViewNoRecords.visibility = View.VISIBLE
                    tvtxtViewNoRecords.text = "${trackingNo} : \nRecord not found or not assigned to you"
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

    private fun handleScannedOrder(
        trackingNo: String,
        deliveryPersonId_: Long
    ) {
        searchOrder(
            trackingNo = trackingNo,
            deliveryPersonId = 1L
        )
    }

    private fun showOrderList() {
        dashboardContainer.visibility = View.GONE
        tvNoRecord.visibility = View.GONE
        listView.visibility = View.VISIBLE
    }



    private fun showDashboard() {
        listView.visibility = View.GONE
        tvNoRecord.visibility = View.GONE
        dashboardContainer.visibility = View.VISIBLE

        loadDashboard()
    }

    private fun loadDashboard() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val counts = coroutineScope {
                    val inProgressRequest = async {
                        RetrofitClient.orderApi
                            .getInProgressOrders(1L)
                    }

                    val completedRequest = async {
                        RetrofitClient.orderApi
                            .getCompletedOrders(1L)
                    }

                    Pair(
                        inProgressRequest.await().size,
                        completedRequest.await().size
                    )
                }

                val inProgressCount = counts.first
                val completedCount = counts.second

                tvInProgressCount.text =
                    inProgressCount.toString()

                tvCompletedCount.text =
                    completedCount.toString()

                donutChart.setData(
                    inProgress = inProgressCount,
                    completed = completedCount
                )
                displayCurrentDateTime()
            } catch (e: Exception) {
                Log.e(
                    "DASHBOARD_API",
                    "Failed to load dashboard",
                    e
                )

                Toast.makeText(
                    this@ListViewActivity,
                    "Unable to load dashboard",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun displayCurrentDateTime() {
        val formatter =
            java.text.SimpleDateFormat(
                "d MMM yyyy, h:mm a",
                java.util.Locale.getDefault()
            )

        tvCurrentDateTime.text =
            "As of ${formatter.format(java.util.Date())}"
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.outline_logout_24)
            .setTitle("Log out?")
            .setMessage("Are you sure you want to log out?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Log out") { _, _ ->
                logout()
            }
            .show()
    }

    private fun logout() {
        // Clear saved login token or delivery person details here.
        // TokenManager.clearToken()

        val intent =
            Intent(this, LoginActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)
    }
    }

