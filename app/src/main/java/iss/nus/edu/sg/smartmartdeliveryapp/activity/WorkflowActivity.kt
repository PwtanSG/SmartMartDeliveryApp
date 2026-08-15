package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import iss.nus.edu.sg.smartmartdeliveryapp.R
import iss.nus.edu.sg.smartmartdeliveryapp.api.RetrofitClient
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderStatus
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.io.File
import iss.nus.edu.sg.smartmartdeliveryapp.api.UploadApiClient
import iss.nus.edu.sg.smartmartdeliveryapp.model.ConfirmDeliveryRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import iss.nus.edu.sg.smartmartdeliveryapp.model.ViewPhotoRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WorkflowActivity : AppCompatActivity() {
    private lateinit var btnTakePhoto: Button
    private lateinit var trackingNo: String
    private var deliveryPhotoFile: File? = null
    private lateinit var barcodeScanner: GmsBarcodeScanner
    private var deliveryProofKey: String? = null

    private var deliveredAt: String? = ""
    private lateinit var tvDeliveredAt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_workflow)

        findViewById<ImageButton>(
                R.id.btnTopBack
            ).setOnClickListener {
            finish()
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

        val btnScan = findViewById<Button>(R.id.btnScan)

        btnScan.visibility = View.VISIBLE

        val orderId =
            intent.getLongExtra("ORDER_ID", -1L)

        trackingNo =
            intent.getStringExtra("TRACKING_NO") ?: ""
        Toast.makeText(this, "test tno:" + trackingNo, Toast.LENGTH_SHORT).show()

        deliveryProofKey = intent.getStringExtra("DELIVERY_PROOF_KEY")

        val recipent_fname = intent.getStringExtra("RECIPENT_FIRST_NAME")
        val recipent_lname = intent.getStringExtra("RECIPENT_LAST_NAME")
        var tvName = findViewById<TextView>(R.id.tvName)
        tvName.text = recipent_fname + " " + recipent_lname

        val recipientPhone =
            intent.getStringExtra(
                "RECIPIENT_PHONE"
            )?.trim().orEmpty()

        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        tvPhone.text = recipientPhone

        val recipientAddress =
            intent.getStringExtra(
                "RECIPIENT_ADDRESS"
            )?.trim().orEmpty()

        val tvAddress = findViewById<TextView>(R.id.tvAddress)
        tvAddress.text = recipientAddress

        findViewById<View>(R.id.rowPhone).setOnClickListener {
            if (recipientPhone.isBlank()) {
                Toast.makeText(
                    this,
                    "Phone number is unavailable",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }
            startActivity(
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$recipientPhone")
                )
            )
        }

        findViewById<View>(R.id.rowAddress).setOnClickListener {
            if (recipientAddress.isBlank()) {
                Toast.makeText(
                    this,
                    "Address is unavailable",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            openNavigation(recipientAddress)
        }

        btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)

        val deliveryPersonId =
            intent.getLongExtra(
                "DELIVERY_PERSON_ID",
                -1L
            )

        val btnViewProof = findViewById<Button>(R.id.btnViewProof)

        val trackNo = findViewById<EditText>(R.id.etTrackingNo)
        trackNo.setText(trackingNo)

        var trackingNoTv = findViewById<TextView>(R.id.tvTrackingNo)
        trackingNoTv.text = trackingNo

        val statusName =
            intent.getStringExtra("ORDER_STATUS")
        if (statusName == OrderStatus.PACKED.name) {
            btnScan.text = "Scan > Pick Up"
        }
        if (statusName == OrderStatus.PICKED_UP.name) {
            btnScan.text = "Scan > Delivered"
        }
        // Toast.makeText(this, "Status: ${statusName}", Toast.LENGTH_SHORT).show()
        deliveredAt = intent.getStringExtra("DELIVERED_AT")
        tvDeliveredAt = findViewById<TextView>(R.id.tvDeliveredAt)
        if (statusName == OrderStatus.DELIVERED.name) {
            tvDeliveredAt.text = formatDateTime(deliveredAt)
        } else {
            tvDeliveredAt.text = "In-progress"
        }

        when (statusName) {
            OrderStatus.PACKED.name -> {
                btnScan.text = "Scan > Pick Up"
                btnScan.isEnabled = true
                btnScan.visibility = View.VISIBLE
                btnScan.setOnClickListener {
                    // Start barcode scanner
                    trackingNo =
                        trackNo.text.toString().trim()

                    if (trackingNo.isBlank()) {
                        trackNo.error =
                            "Tracking number is required"

                        return@setOnClickListener
                    }

                    Log.d("BARCODE_SCAN", "Scan button pressed")
                    Log.d(
                            "BARCODE_SCAN",
                            "Button clicked, status=$statusName"
                        )

                    scanOrder(
                        expectedTrackingNo = trackingNo,
                        deliveryPersonId = deliveryPersonId,
                        statusName = statusName
                    )
                }
            }

            OrderStatus.PICKED_UP.name -> {
                btnScan.text = "Scan > Delivered"
                btnScan.isEnabled = true

                Log.d("BARCODE_SCAN", "Scan button pressed")
                Log.d(
                    "BARCODE_SCAN",
                    "Button clicked, status=$statusName"
                )
                btnScan.setOnClickListener {
                    // Start barcode scanner
                    trackingNo =
                        trackNo.text.toString().trim()

                    if (trackingNo.isBlank()) {
                        trackNo.error =
                            "Tracking number is required"

                        return@setOnClickListener
                    }

                    Log.d("BARCODE_SCAN", "Scan button pressed")

                    scanOrder(
                        expectedTrackingNo = trackingNo,
                        deliveryPersonId = deliveryPersonId,
                        statusName = statusName
                    )
                }

            }

            OrderStatus.DELIVERED.name -> {
                btnScan.text = "Delivered"
                btnScan.isEnabled = false
                btnScan.setBackgroundColor(Color.GRAY)

                if (!deliveryProofKey.isNullOrBlank()) {
                    btnViewProof.visibility = View.VISIBLE

                    btnViewProof.setOnClickListener {
                        openDeliveryProof()
                    }
                } else {
                    btnViewProof.visibility = View.GONE
                }
            }

            else -> {
                btnScan.visibility = View.GONE
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
                    Toast.LENGTH_LONG
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

    private fun scanOrder(
        expectedTrackingNo: String,
        deliveryPersonId: Long,
        statusName: String
    ) {
        barcodeScanner.startScan()
            .addOnSuccessListener { barcode ->
                val scannedTrackingNo =
                    barcode.rawValue?.trim()

                if (scannedTrackingNo.isNullOrBlank()) {
                    Toast.makeText(
                        this,
                        "Invalid barcode",
                        Toast.LENGTH_LONG
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

                if (statusName == OrderStatus.PACKED.name) {
                    showPickupConfirmation(expectedTrackingNo, deliveryPersonId)
                }

                if (statusName == OrderStatus.PICKED_UP.name) {
//                    val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)

                    if (statusName == OrderStatus.PICKED_UP.name) {
                        btnTakePhoto.visibility = View.VISIBLE
                        btnTakePhoto.setOnClickListener {
                            if (trackingNo != "") {
                                takeDeliveryPhoto()
                            } else {
                                Log.e("", "No Tracking No.")
                                Toast.makeText(this, "No trackingNo", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        btnTakePhoto.visibility = View.GONE
                    }
                }
            }
    }

    private fun openNavigation(
        recipientAddress: String
    ) {
        val uri = Uri.parse(
            "google.navigation:q=" +
                    Uri.encode(recipientAddress) +
                    "&mode=d"
        )

        val intent = Intent(
            Intent.ACTION_VIEW,
            uri
        ).apply {
            setPackage(
                "com.google.android.apps.maps"
            )
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val browserUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1" +
                        "&destination=" +
                        Uri.encode(recipientAddress)
            )

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    browserUri
                )
            )
        }
    }

    private fun showRecipientLocation(
        recipientAddress: String
    ) {
        val uri = Uri.parse(
            "geo:0,0?q=" +
                    Uri.encode(recipientAddress)
        )

        val intent =
            Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(
                    "com.google.android.apps.maps"
                )
            }

        startActivity(intent)
    }

    private val takePhotoLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            val file = deliveryPhotoFile

            if (success && file != null) {
                showDeliveryConfirmation(file)
            } else {
                Toast.makeText(
                    this,
                    "Photo capture cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun takeDeliveryPhoto() {
        val directory =
            File(cacheDir, "delivery_photos")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        deliveryPhotoFile =
            File.createTempFile(
                "delivery_",
                ".jpg",
                directory
            )

        val photoUri =
            FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                deliveryPhotoFile!!
            )

        takePhotoLauncher.launch(photoUri)
    }

    private fun uploadPhoto(
        photoFile: File
    ) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                // Request presigned upload URL
                Log.d(
                    "DELIVERY_PROOF",
                    "STEP 1: Request presigned URL"
                )

                val uploadDetails =
                    UploadApiClient.uploadApi
                        .createUploadUrl()

                Log.d(
                    "DELIVERY_PROOF",
                    "STEP 1 OK: ${uploadDetails.fileKey}"
                )

                val imageBody =
                    photoFile.asRequestBody(
                        "image/jpeg".toMediaType()
                    )
                Log.d(
                    "DELIVERY_PROOF",
                    "STEP 2: Upload to S3"
                )

                // Upload photo to S3
                val uploadResponse =
                    UploadApiClient.uploadApi
                        .uploadPhotoToS3(
                            uploadUrl =
                                uploadDetails.uploadUrl,
                            imageBody = imageBody
                        )


                val s3Error =
                    uploadResponse.errorBody()?.string()

                Log.d(
                    "DELIVERY_PROOF",
                    "STEP 2 response: ${uploadResponse.code()}"
                )

                if (!uploadResponse.isSuccessful) {
                    throw IOException(
                        "S3 HTTP ${uploadResponse.code()}: " +
                                uploadResponse
                                    .errorBody()
                                    ?.string()
                    )
                }

                Log.d(
                    "DELIVERY_PROOF",
                    "STEP 3: Update Spring Boot, " +
                            "trackingNo=[$trackingNo]"
                )
                // Update order through Spring Boot
                val updatedOrder =
                    RetrofitClient.orderApi
                        .confirmDeliveryProof(
                            trackingNo = trackingNo,
                            request =
                                ConfirmDeliveryRequest(
                                    fileKey =
                                        uploadDetails.fileKey
                                )
                        )
                Log.d(
                    "DELIVERY_PROOF",
                    "STEP 3 OK"
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "${updatedOrder.trackingNo} delivered successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } catch (e: HttpException) {
                val errorBody =
                    e.response()
                        ?.errorBody()
                        ?.string()

                Log.e(
                    "DELIVERY_PROOF",
                    "HTTP ${e.code()}: $errorBody",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "HTTP ${e.code()}: $errorBody",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: IOException) {
                Log.e(
                    "DELIVERY_PROOF",
                    "Network/S3 error: ${e.message}",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Upload failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e(
                    "DELIVERY_PROOF",
                    "Unexpected error: ${e.message}",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Delivery failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showPickupConfirmation(
        scannedTrackingNo: String,
        deliveryPersonId: Long
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm pickup")
            .setMessage(
                "Pick up parcel $scannedTrackingNo?"
            )
            .setPositiveButton("Confirm") { _, _ ->
                pickupOrder(
                    scannedTrackingNo_ =
                        scannedTrackingNo,
                    deliveryPersonId_ =
                        deliveryPersonId
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeliveryConfirmation(
//        scannedTrackingNo: String,
        file: File
//        deliveryPersonId: Long
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm delivery")
            .setMessage(
                "Mark parcel $trackingNo as delivered?"
            )
            .setPositiveButton("Confirm") { _, _ ->
//                deliveredOrder(
//                    scannedTrackingNo_ =
//                        scannedTrackingNo,
//                    deliveryPersonId_ =
//                        deliveryPersonId
//                )
                uploadPhoto(
                    photoFile = file,
                    // trackingNo = trackingNo
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        val loadingOverlay =
            findViewById<FrameLayout>(
                R.id.loadingOverlay
            )

        loadingOverlay.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    private fun openDeliveryProof() {
        val fileKey = deliveryProofKey

        if (fileKey.isNullOrBlank()) {
            Toast.makeText(
                this,
                "Delivery proof is unavailable",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val response =
                    UploadApiClient.uploadApi
                        .getPhotoViewUrl(
                            ViewPhotoRequest(
                                fileKey = fileKey
                            )
                        )

                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(response.viewUrl)
                    )

                startActivity(browserIntent)

            } catch (e: HttpException) {
                val errorBody =
                    e.response()
                        ?.errorBody()
                        ?.string()

                Log.e(
                    "VIEW_PROOF",
                    "HTTP ${e.code()}: $errorBody",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Unable to retrieve photo: HTTP ${e.code()}",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e(
                    "VIEW_PROOF",
                    "Unable to view proof",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Unable to view proof: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                showLoading(false)
            }
        }
    }

    private fun formatDateTime(
        value: String?
    ): String {
        if (value.isNullOrBlank()) {
            return "-"
        }

        return try {
            val dateTime =
                LocalDateTime.parse(value)

            val outputFormatter =
                DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, h:mm a"
                )

            dateTime.format(outputFormatter)

        } catch (e: Exception) {
            value
        }
    }

}