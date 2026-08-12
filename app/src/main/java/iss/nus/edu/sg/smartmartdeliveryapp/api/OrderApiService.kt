package iss.nus.edu.sg.smartmartdeliveryapp.api

import iss.nus.edu.sg.smartmartdeliveryapp.model.ConfirmDeliveryRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.DeviceTokenRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderRequest
import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response

interface OrderApiService {

    @GET("api/orders/assigned/{deliveryPersonId}")
    suspend fun getAssignedOrders(
        @Path("deliveryPersonId")
        deliveryPersonId: Long
    ): List<OrderResponse>

    @GET(
        "api/orders/in-progress/{deliveryPersonId}"
    )
    suspend fun getInProgressOrders(
        @Path("deliveryPersonId")
        deliveryPersonId: Long
    ): List<OrderResponse>

    @GET(
        "api/orders/completed/{deliveryPersonId}"
    )
    suspend fun getCompletedOrders(
        @Path("deliveryPersonId")
        deliveryPersonId: Long
    ): List<OrderResponse>

    @PATCH("api/orders/pickup")
    suspend fun pickupOrder(
        @Body request: OrderRequest
    ): OrderResponse

    @PATCH("api/orders/delivered")
    suspend fun deliveredOrder(
        @Body request: OrderRequest
    ): OrderResponse

    @GET(
        "api/orders/search/{trackingNo}/{deliveryPersonId}"
    )
    suspend fun searchOrder(
        @Path("trackingNo")
        trackingNo: String,

        @Path("deliveryPersonId")
        deliveryPersonId: Long
    ): OrderResponse

    @POST("api/orders/{trackingNo}/proof/confirm")
    suspend fun confirmDeliveryProof(
        @Path("trackingNo") trackingNo: String,
        @Body request: ConfirmDeliveryRequest
    ): OrderResponse

    // driver device token registration for firebase
    @POST("api/device-tokens")
    suspend fun registerDeviceToken(
        @Body request: DeviceTokenRequest
    ): Response<Unit> // means the endpoint returns no response body HTTP 204 No Content
}