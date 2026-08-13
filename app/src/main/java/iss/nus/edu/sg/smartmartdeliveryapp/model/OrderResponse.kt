package iss.nus.edu.sg.smartmartdeliveryapp.model

data class OrderResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val shippingAddress : String,
    val phoneNumber: String,
    val trackingNo: String,
    val deliveryPersonId: Long?,
    val status: OrderStatus,
    val deliveryProofKey: String?,
    val deliveredAt: String? = null
)
