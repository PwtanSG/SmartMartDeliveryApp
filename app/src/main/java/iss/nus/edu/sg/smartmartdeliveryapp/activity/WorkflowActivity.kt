package iss.nus.edu.sg.smartmartdeliveryapp.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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

private lateinit var barcodeScanner: GmsBarcodeScanner

private lateinit var trackingNo: String
private var deliveryPhotoFile: File? = null
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

        val btnMap = findViewById<Button>(R.id.btnMap)
        btnMap.setOnClickListener {
            val address =
                intent.getStringExtra(
                    "RECIPIENT_ADDRESS"
                ) ?: ""

            if (address.isBlank()) {
                Toast.makeText(
                    this,
                    "Recipient address is missing",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            openNavigation(address)
        }

//        btnMap.setOnClickListener {
//            openNavigation(
//                "116 Lorong 2 Toa Payoh S310116"
//            )
//        }

//        btnMap.setOnClickListener {
//            showRecipientLocation(
//                "2 Clementi West Street 2, Singapore 129605"
//            )
//        }
        val back_btn = findViewById<Button>(R.id.btnBack)
        back_btn.setBackgroundColor(Color.GRAY)
        back_btn.setOnClickListener {
            startActivity(Intent(this, ListViewActivity::class.java))
        }
        val btnScan = findViewById<Button>(R.id.btnScan)

        btnScan.visibility = View.VISIBLE

        val btnButton = findViewById<Button>(R.id.button)
        btnButton.visibility = View.GONE

        val orderId =
            intent.getLongExtra("ORDER_ID", -1L)

        trackingNo =
            intent.getStringExtra("TRACKING_NO") ?: ""

        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)

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

        btnTakePhoto.visibility = View.VISIBLE
        btnTakePhoto.setOnClickListener {
            if (trackingNo != "") {
                takeDeliveryPhoto()
            } else {
                Log.e("", "No Tracking No.")
                Toast.makeText(this, "No trackingNo", Toast.LENGTH_SHORT).show()
            }
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

//                    AlertDialog.Builder(this)
                    MaterialAlertDialogBuilder(this)
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

//                        AlertDialog.Builder(this)
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Confirm delivered")
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
                val trackingNo = null
                uploadPhoto(
                    photoFile = file,
                    // trackingNo = trackingNo
                )
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

    private fun uploadPhoto1(
        photoFile: File
    ) {
//        showLoading(true)

        lifecycleScope.launch {
            try {
                // POST to API Gateway
                val uploadDetails =
                    UploadApiClient.uploadApi
                        .createUploadUrl()

                val imageBody =
                    photoFile.asRequestBody(
                        "image/jpeg".toMediaType()
                    )

                // PUT directly to S3 using Retrofit
                val uploadResponse =
                    UploadApiClient.uploadApi
                        .uploadPhotoToS3(
                            uploadUrl =
                                uploadDetails.uploadUrl,

                            imageBody =
                                imageBody
                        )

                if (!uploadResponse.isSuccessful) {
                    val errorBody =
                        uploadResponse
                            .errorBody()
                            ?.string()

                    Log.e(
                        "S3_UPLOAD",
                        "HTTP ${uploadResponse.code()}: " +
                                errorBody
                    )

                    throw IOException(
                        "S3 upload failed: " +
                                uploadResponse.code()
                    )
                }

                Log.d(
                    "S3_UPLOAD",
                    "Uploaded: ${uploadDetails.fileKey}"
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Delivery photo uploaded",
                    Toast.LENGTH_SHORT
                ).show()

//                confirmDelivery(
//                    uploadDetails.fileKey
//                )
            } catch (e: Exception) {
                Log.e(
                    "S3_UPLOAD",
                    "Upload failed: " +
                            "${e.javaClass.simpleName}: ${e.message}",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Upload failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                //showLoading(false)
            }
        }
    }

    private fun uploadPhoto2(
        photoFile: File,
    ) {
        lifecycleScope.launch {
            try {
                Log.d(
                    "S3_UPLOAD",
                    "File exists=${photoFile.exists()}, " +
                            "size=${photoFile.length()}"
                )

                Log.d(
                    "S3_UPLOAD",
                    "Requesting presigned URL"
                )

                val uploadDetails =
                    UploadApiClient.uploadApi
                        .createUploadUrl()

                Log.d(
                    "S3_UPLOAD",
                    "Received file key: " +
                            uploadDetails.fileKey
                )

                val imageBody =
                    photoFile.asRequestBody(
                        "image/jpeg".toMediaType()
                    )

                Log.d(
                    "S3_UPLOAD",
                    "Starting S3 PUT"
                )

                val uploadResponse =
                    UploadApiClient.uploadApi
                        .uploadPhotoToS3(
                            uploadUrl =
                                uploadDetails.uploadUrl,
                            imageBody = imageBody
                        )


                val errorText =
                    uploadResponse
                        .errorBody()
                        ?.string()

                Log.d(
                    "S3_UPLOAD",
                    "S3 response=${uploadResponse.code()}"
                )

                if (!uploadResponse.isSuccessful) {
                    Log.e(
                        "S3_UPLOAD",
                        "S3 error: $errorText"
                    )

                    throw IOException(
                        "S3 HTTP ${uploadResponse.code()}"
                    )
                }

                // S3 upload succeeded.
                // Save the permanent S3 file key and mark the order delivered.
                val updatedOrder =
                    RetrofitClient.orderApi.confirmDeliveryProof(
                        trackingNo = trackingNo,
                        request = ConfirmDeliveryRequest(
                            fileKey = uploadDetails.fileKey
                        )
                    )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Photo uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: HttpException) {
                val errorText =
                    e.response()
                        ?.errorBody()
                        ?.string()

                Log.e(
                    "S3_UPLOAD",
                    "API Gateway HTTP ${e.code()}: $errorText",
                    e
                )
            } catch (e: Exception) {
                Log.e(
                    "S3_UPLOAD",
                    "Upload failed: " +
                            "${e.javaClass.simpleName}: ${e.message}",
                    e
                )
            }
        }
    }

    private fun uploadPhoto9(
        photoFile: File
    ) {
        lifecycleScope.launch {
            try {
                val uploadDetails =
                    UploadApiClient.uploadApi
                        .createUploadUrl()

                val imageBody =
                    photoFile.asRequestBody(
                        "image/jpeg".toMediaType()
                    )

                val uploadResponse =
                    UploadApiClient.uploadApi
                        .uploadPhotoToS3(
                            uploadUrl =
                                uploadDetails.uploadUrl,
                            imageBody = imageBody
                        )

                if (!uploadResponse.isSuccessful) {
                    throw IOException(
                        "S3 upload failed: HTTP " +
                                uploadResponse.code()
                    )
                }

                // Uses the WorkflowActivity trackingNo property
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

                Toast.makeText(
                    this@WorkflowActivity,
                    "${updatedOrder.trackingNo} delivered successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } catch (e: Exception) {
                Log.e(
                    "DELIVERY_PROOF",
                    "Delivery failed",
                    e
                )

                Toast.makeText(
                    this@WorkflowActivity,
                    "Delivery failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun uploadPhoto(
        photoFile: File
    ) {
        lifecycleScope.launch {
            try {
                // Request presigned upload URL
                val uploadDetails =
                    UploadApiClient.uploadApi
                        .createUploadUrl()

                val imageBody =
                    photoFile.asRequestBody(
                        "image/jpeg".toMediaType()
                    )

                // Upload photo to S3
                val uploadResponse =
                    UploadApiClient.uploadApi
                        .uploadPhotoToS3(
                            uploadUrl =
                                uploadDetails.uploadUrl,
                            imageBody = imageBody
                        )

                if (!uploadResponse.isSuccessful) {
                    throw IOException(
                        "S3 HTTP ${uploadResponse.code()}: " +
                                uploadResponse
                                    .errorBody()
                                    ?.string()
                    )
                }

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
            }
        }
    }

}