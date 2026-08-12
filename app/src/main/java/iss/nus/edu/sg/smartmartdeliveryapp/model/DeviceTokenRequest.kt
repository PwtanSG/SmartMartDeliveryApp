package iss.nus.edu.sg.smartmartdeliveryapp.model

data class DeviceTokenRequest(
    val deliveryPersonId: Long,
    val fcmToken: String
)
