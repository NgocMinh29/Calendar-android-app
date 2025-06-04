package com.example.calendarapp.api;

import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.ChatConversation;
import com.example.calendarapp.models.ChatMessage;
import com.example.calendarapp.models.Course;
import com.example.calendarapp.models.Event;
import com.example.calendarapp.models.LoginRequest;
import com.example.calendarapp.models.PremiumStatus;
import com.example.calendarapp.models.PurchaseRequest;
import com.example.calendarapp.models.RegisterRequest;
import com.example.calendarapp.models.SocialLoginRequest;
import com.example.calendarapp.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    // Authentication
    @POST("login.php")
    Call<ApiResponse<User>> login(@Body LoginRequest loginRequest);

    @POST("register.php")
    Call<ApiResponse<User>> register(@Body RegisterRequest registerRequest);

    @POST("social_login.php")
    Call<ApiResponse<User>> socialLogin(@Body SocialLoginRequest socialLoginRequest);

    // Premium APIs
    @GET("premium_status.php")
    Call<ApiResponse<PremiumStatus>> getPremiumStatus(@Query("user_email") String userEmail);

    @POST("purchase_premium.php")
    Call<ApiResponse<PremiumStatus>> purchasePremium(@Body PurchaseRequest purchaseRequest);

    @POST("verify_purchase.php")
    Call<ApiResponse<PremiumStatus>> verifyPurchase(@Body PurchaseRequest purchaseRequest);

    // Events - sử dụng @Body để gửi JSON
    @GET("events.php")
    Call<ApiResponse<List<Event>>> getAllEventsByEmail(@Query("user_email") String userEmail);

    @GET("events.php")
    Call<ApiResponse<List<Event>>> getEventsForDateByEmail(@Query("user_email") String userEmail, @Query("date") String date);

    @POST("events.php")
    Call<ApiResponse<Event>> createEventWithEmail(@Body Event event);

    @PUT("events.php")
    Call<ApiResponse<Event>> updateEventWithEmail(@Body Event event);

    @DELETE("events.php")
    Call<ApiResponse<String>> deleteEventWithEmail(@Query("id") long eventId, @Query("user_email") String userEmail);

    // Courses - sử dụng @Body để gửi JSON
    @GET("courses.php")
    Call<ApiResponse<List<Course>>> getAllCoursesByEmail(@Query("user_email") String userEmail);

    @GET("courses.php")
    Call<ApiResponse<List<Course>>> getActiveCoursesForDayByEmail(@Query("user_email") String userEmail, @Query("day_of_week") String dayOfWeek, @Query("current_date") String currentDate);

    @POST("courses.php")
    Call<ApiResponse<Course>> createCourseWithEmail(@Body Course course);

    @PUT("courses.php")
    Call<ApiResponse<Course>> updateCourseWithEmail(@Body Course course);

    @DELETE("courses.php")
    Call<ApiResponse<String>> deleteCourseWithEmail(@Query("id") long courseId, @Query("user_email") String userEmail);

    // AI Chat Messages
    @GET("ai_chat_messages.php")
    Call<ApiResponse<List<ChatConversation>>> getConversations(@Query("user_email") String userEmail);

    @GET("ai_chat_messages.php")
    Call<ApiResponse<List<ChatMessage>>> getChatMessages(@Query("user_email") String userEmail, @Query("conversation_id") String conversationId);

    @POST("ai_chat_messages.php")
    Call<ApiResponse<ChatMessage>> saveChatMessage(@Body ChatMessage chatMessage);

    // THÊM MỚI: Phương thức POST để xóa lịch sử chat
    @POST("ai_chat_messages.php")
    @Headers("X-HTTP-Method-Override: DELETE")
    Call<ApiResponse<String>> deleteChatHistoryWithBody(@Body Map<String, Object> requestBody);

    // Giữ lại phương thức cũ để tương thích
    @DELETE("ai_chat_messages.php")
    Call<ApiResponse<String>> deleteChatHistory(@Query("user_email") String userEmail, @Query("conversation_id") String conversationId);
}
