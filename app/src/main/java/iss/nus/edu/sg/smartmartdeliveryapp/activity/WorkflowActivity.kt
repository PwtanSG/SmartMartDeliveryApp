package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.api.RetrofitClient
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderResponse
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderStatus
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

private lateinit var barcodeScanner: GmsBarcodeScanner
class WorkflowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workflow)

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

        val cancel_btn = findViewById<Button>(R.id.btnCancel)
        cancel_btn.setBackgroundColor(Color.GRAY)
        cancel_btn.setOnClickListener {
            startActivity(Intent(this, ListViewActivity::class.java))
        }
        val btnScan = findViewById<Button>(R.id.btnScan)
//        btnScan.setOnClickListener {
//            scanOrder()
//        }
//        btnScan.setOnClickListener()
        btnScan.visibility = View.VISIBLE

        val btnButton = findViewById<Button>(R.id.button)
        btnButton.visibility = View.GONE

        val orderId =
            intent.getLongExtra("ORDER_ID", -1L)

        val trackingNo =
            intent.getStringExtra("TRACKING_NO") ?: ""

        val deliveryPersonId =
            intent.getLongExtra(
                "DELIVERY_PERSON_ID",
                -1L
            )

        val trackNo = findViewById<EditText>(R.id.etTrackingNo)
        trackNo.setText(trackingNo)

        val statusName =
            intent.getStringExtra("ORDER_STATUS")

        btnScan.setOnClickListener {
            scanOrder(trackingNo, deliveryPersonId)
        }

        val btn = findViewById<Button>(R.id.button)
        when (statusName) {
            OrderStatus.PACKED.name -> {
                btn.text = "PICK UP"
                btn.isEnabled = true
                btn.setOnClickListener {
                    // Start barcode scanner
                    val trackingNo =
                        trackNo.text.toString().trim()

                    if (trackingNo.isBlank()) {
                        trackNo.error =
                            "Tracking number is required"

                        return@setOnClickListener
                    }

                    AlertDialog.Builder(this)
                        .setTitle("Confirm pickup")
                        .setMessage(
                            "Pick up parcel ${trackNo.text.toString()}?"
                        )
                        .setPositiveButton("Confirm") { _, _ ->
                            pickupOrder(
                                scannedTrackingNo_ =
                                    trackNo.text.toString(),
                                deliveryPersonId_ =
                                    deliveryPersonId
                            )
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }

            OrderStatus.PICKED_UP.name -> {
                btn.text = "Mark as DELIVERED"
                btn.isEnabled = true
                btn.setOnClickListener {
                    // Take delivery photo
                        val trackingNo =
                            trackNo.text.toString().trim()
//                        scanOrder(trackingNo, deliveryPersonId)

                        AlertDialog.Builder(this)
                            .setTitle("Confirm pickup")
                            .setMessage(
                                "Delivered parcel ${trackNo.text.toString()}?"
                            )
                            .setPositiveButton("Confirm") { _, _ ->
                                deliveredOrder(
                                    scannedTrackingNo_ =
                                        trackNo.text.toString(),
                                    deliveryPersonId_ =
                                        deliveryPersonId
                                )
                            }
                            .setNegativeButton("Cancel", null)
                            .show()

                }
            }

            OrderStatus.DELIVERED.name -> {
                btn.text = "Delivered"
                btn.isEnabled = false
                btn.setBackgroundColor(Color.GRAY)
            }

            else -> {
                btn.visibility = View.GONE
            }
        }
    }

    private fun pickupOrder(
        scannedTrackingNo_: String,
        deliveryPersonId_: Long
    ) {
        lifecycleScope.launch {
            try {
                val updatedOrder =
                    RetrofitClient.orderApi.pickupOrder(
                        OrderRequest(
                            trackingNo = scannedTrackingNo_,
                            deliveryPersonId = deliveryPersonId_
                        )
                    )

                Toast.makeText(
                    this@WorkflowActivity,
                    "${updatedOrder.trackingNo} picked up",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            } catch (e: HttpException) {
                val errorMessage =
                    e.response()
                        ?.errorBody()
                        ?.string()

                Log.e(
                    "PICKUP_API",
                    "HTTP ${e.code()}: $errorMessage",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    errorMessage ?: "Pickup failed",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this@WorkflowActivity,
                    "Cannot connect to Spring Boot",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(
                    "PICKUP_API",
                    "Pickup failed",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Pickup failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deliveredOrder(
        scannedTrackingNo_: String,
        deliveryPersonId_: Long
    ) {
        lifecycleScope.launch {
            try {
                val updatedOrder =
                    RetrofitClient.orderApi.deliveredOrder(
                        OrderRequest(
                            trackingNo = scannedTrackingNo_,
                            deliveryPersonId = deliveryPersonId_
                        )
                    )

                Toast.makeText(
                    this@WorkflowActivity,
                    "${updatedOrder.trackingNo} delivered",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            } catch (e: HttpException) {
                val errorMessage =
                    e.response()
                        ?.errorBody()
                        ?.string()

                Log.e(
                    "DELIVERED_API",
                    "HTTP ${e.code()}: $errorMessage",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    errorMessage ?: "delivery api failed",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    this@WorkflowActivity,
                    "Cannot connect to Spring Boot",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(
                    "DELIVERED_API",
                    "Delivered api failed",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "delivered failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun scanOrder1(order: OrderResponse) {
        barcodeScanner.startScan()
            .addOnSuccessListener { barcode ->
                val scannedTrackingNo =
                    barcode.rawValue?.trim()

                if (scannedTrackingNo.isNullOrBlank()) {
                    Toast.makeText(
                        this,
                        "Barcode has no value",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }

                if (scannedTrackingNo == order.trackingNo) {
//                    pickupOrder(order)
                    Toast.makeText(
                        this,
                        "Wrong parcel. Expected ${order.trackingNo}, " +
                                "but scanned $scannedTrackingNo",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Wrong parcel. Expected ${order.trackingNo}, " +
                                "but scanned $scannedTrackingNo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnCanceledListener {
                Toast.makeText(
                    this,
                    "Scanning cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "BARCODE_SCANNER",
                    "Scanning failed",
                    exception
                )

                Toast.makeText(
                    this,
                    "Unable to scan barcode",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun scanOrder(
        expectedTrackingNo: String,
        deliveryPersonId: Long
    ) {
        barcodeScanner.startScan()
            .addOnSuccessListener { barcode ->
                val scannedTrackingNo =
                    barcode.rawValue?.trim()

                if (scannedTrackingNo.isNullOrBlank()) {
                    Toast.makeText(
                        this,
                        "Invalid barcode",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnSuccessListener
                }

                if (scannedTrackingNo != expectedTrackingNo) {
                    Toast.makeText(
                        this,
                        "Wrong parcel scanned",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }


                Toast.makeText(
                        this,
                        "scanned ${scannedTrackingNo}",
                        Toast.LENGTH_LONG
                    ).show()

                val btnScan = findViewById<Button>(R.id.btnScan)
                btnScan.visibility = View.GONE

                val btnButton = findViewById<Button>(R.id.button)
                btnButton.visibility = View.VISIBLE

//                confirmPickUp(
//                    scannedTrackingNo,
//                    deliveryPersonId
//                )
            }
    }
}