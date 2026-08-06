package iss.nus.edu.sg.smartmartdeliveryapp.model

data class OrderRequest(
    val trackingNo: String,
    val deliveryPersonId: Long
)
