package iss.nus.edu.sg.smartmartdeliveryapp.api

import iss.nus.edu.sg.smartmartdeliveryapp.model.OrderResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface OrderApiService {

    @GET("api/orders/assigned/{deliveryPersonId}")
    suspend fun getAssignedOrders(
        @Path("deliveryPersonId")
        deliveryPersonId: Long
    ): List<OrderResponse>
}