package com.example.calendarapp.utils;

import android.content.Context;
import android.util.Log;

import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.Course;
import com.example.calendarapp.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiHelper {
    private static final String TAG = "ApiHelper";
    private ApiService apiService;
    private SessionManager sessionManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public ApiHelper(Context context) {
        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(context);
    }

    private String getCurrentUserEmail() {
        return sessionManager.getUserEmail();
    }

    // Event methods
    public void getAllEvents(ApiCallback<List<Event>> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        Call<ApiResponse<List<Event>>> call = apiService.getAllEventsByEmail(userEmail);
        call.enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get events");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                Log.e(TAG, "Error getting events", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getEventsForDate(Date date, ApiCallback<List<Event>> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        String dateString = dateFormat.format(date);
        Call<ApiResponse<List<Event>>> call = apiService.getEventsForDateByEmail(userEmail, dateString);
        call.enqueue(new Callback<ApiResponse<List<Event>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Event>>> call, Response<ApiResponse<List<Event>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Event>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get events for date");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Event>>> call, Throwable t) {
                Log.e(TAG, "Error getting events for date", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createEvent(Event event, ApiCallback<Event> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        // Set user_email vào event object trước khi gửi
        event.setUserEmail(userEmail);

        Log.d(TAG, "Creating event: " + event.getTitle() + " for user: " + userEmail);

        Call<ApiResponse<Event>> call = apiService.createEventWithEmail(event);
        call.enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to create event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                Log.e(TAG, "Error creating event", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateEvent(Event event, ApiCallback<Event> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        // Set user_email vào event object trước khi gửi
        event.setUserEmail(userEmail);

        Call<ApiResponse<Event>> call = apiService.updateEventWithEmail(event);
        call.enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Event> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to update event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                Log.e(TAG, "Error updating event", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteEvent(long eventId, ApiCallback<String> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        Call<ApiResponse<String>> call = apiService.deleteEventWithEmail(eventId, userEmail);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getMessage());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to delete event");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error deleting event", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Course methods
    public void getAllCourses(ApiCallback<List<Course>> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        Call<ApiResponse<List<Course>>> call = apiService.getAllCoursesByEmail(userEmail);
        call.enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Course>>> call, Response<ApiResponse<List<Course>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Course>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get courses");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Course>>> call, Throwable t) {
                Log.e(TAG, "Error getting courses", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getActiveCoursesForDay(String dayOfWeek, Date currentDate, ApiCallback<List<Course>> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        String dateString = dateFormat.format(currentDate);
        Call<ApiResponse<List<Course>>> call = apiService.getActiveCoursesForDayByEmail(userEmail, dayOfWeek, dateString);
        call.enqueue(new Callback<ApiResponse<List<Course>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Course>>> call, Response<ApiResponse<List<Course>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Course>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to get courses for day");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Course>>> call, Throwable t) {
                Log.e(TAG, "Error getting courses for day", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createCourse(Course course, ApiCallback<Course> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        // Set user_email vào course object trước khi gửi
        course.setUserEmail(userEmail);

        Call<ApiResponse<Course>> call = apiService.createCourseWithEmail(course);
        call.enqueue(new Callback<ApiResponse<Course>>() {
            @Override
            public void onResponse(Call<ApiResponse<Course>> call, Response<ApiResponse<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Course> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to create course");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Course>> call, Throwable t) {
                Log.e(TAG, "Error creating course", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateCourse(Course course, ApiCallback<Course> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        // Set user_email vào course object trước khi gửi
        course.setUserEmail(userEmail);

        Call<ApiResponse<Course>> call = apiService.updateCourseWithEmail(course);
        call.enqueue(new Callback<ApiResponse<Course>>() {
            @Override
            public void onResponse(Call<ApiResponse<Course>> call, Response<ApiResponse<Course>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Course> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to update course");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Course>> call, Throwable t) {
                Log.e(TAG, "Error updating course", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteCourse(long courseId, ApiCallback<String> callback) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        Call<ApiResponse<String>> call = apiService.deleteCourseWithEmail(courseId, userEmail);
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        callback.onSuccess(apiResponse.getMessage());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Failed to delete course");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error deleting course", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
