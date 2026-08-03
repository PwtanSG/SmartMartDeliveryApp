package iss.nus.edu.sg.smartmartdeliveryapp.model

data class OrderResponse(
    val id: Long,
    val trackingNo: String,
    val deliveryPersonId: Long?,
    val status: OrderStatus
)