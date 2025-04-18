import com.example.freightflow.model.Assignment
import com.example.freightflow.model.Flight
import com.example.freightflow.model.TrafficSignal
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RouteApiService {
    @GET("/api/signals/{signalId}")
    fun getSignalById(@Path("signalId") signalId: String): Call<TrafficSignal>

    @GET("api/assignments")
    fun getAssignments(): Call<Map<String, Assignment>>  // Expecting a map of assignments

    // Fetch a specific assignment by ID
    @GET("api/assignments/{assignmentNumber}")
    fun getAssignment(@Path("assignmentNumber") assignmentId: String): Call<Assignment>

    @GET("flights/{flightNumber}")
    fun getFlight(@Path("flightNumber") flightNumber: String): Call<Flight>

}

interface UserApiService {
    @POST("user/login")
    fun login(@Query("username") username: String, @Query("password") password: String): Call<String>
}
