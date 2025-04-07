package com.project.smartfarming.api

import com.project.smartfarming.editprofile.model.EditProfileResponse
import com.project.smartfarming.login.model.ForgotPasswordResponse
import com.project.smartfarming.login.model.LoginResponse
import com.project.smartfarming.plantrecommandation.model.PlantDetailResponse
import com.project.smartfarming.plantrecommandation.model.PlantResponse
import com.project.smartfarming.profile.model.ProfileResponse
import com.project.smartfarming.resetpassword.model.ResetPasswordResponse
import com.project.smartfarming.signup.model.SignupResponse
import com.project.smartfarming.waterusage.model.DateResponse
import com.project.smartfarming.waterusage.model.ViewAnalysisResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface APIInterface {

    @FormUrlEncoded
    @POST("login.php")
    suspend fun userLogin(
        @FieldMap params: HashMap<String?, String>
    ): Response<LoginResponse>

    @Multipart
    @POST("registration.php")
    suspend fun registerUser(
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("address") address: RequestBody,
        @Part("password") password: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<SignupResponse>

    @FormUrlEncoded
    @POST("change_password.php")
    suspend fun resetPassword(
        @FieldMap params: HashMap<String?, String>

    ): Response<ResetPasswordResponse>

    @FormUrlEncoded
    @POST("reset_password.php")
    suspend fun forgotPassword(
        @FieldMap params: HashMap<String?, String>
    ): Response<ForgotPasswordResponse>


    @GET("view_profile.php")
    suspend fun getUserProfile(
        @Query("userid") userId: String
    ): Response<ProfileResponse>

    @Multipart
    @POST("profile_update.php")
    suspend fun updateProfile(
        @Part("id") id: RequestBody,
        @Part("name") name: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part profileImage: MultipartBody.Part?
    ): Response<EditProfileResponse>

    @GET("date_list.php")
    suspend fun getWaterUsageDates(): Response<DateResponse>

    @GET("view_analysis_date.php")
    suspend fun getWaterUsageAnalysis(
        @Query("date") date: String
    ): Response<ViewAnalysisResponse>

    @GET("plant_suggestion.php")
    fun getPlantSuggestions(): Call<PlantResponse>

//    @GET("plant_detail.php")
//    fun getPlantDetails(@Query("plant_id") plantId: Int): Call<PlantDetailResponse>
}