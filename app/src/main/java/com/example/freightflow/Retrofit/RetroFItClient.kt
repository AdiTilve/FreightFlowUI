import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"  // Replace with your Spring Boot server URL

    // Create Retrofit instance
    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // Base URL of your Spring Boot server
            .addConverterFactory(GsonConverterFactory.create())  // Gson converter for JSON parsing
            .build()
    }
}