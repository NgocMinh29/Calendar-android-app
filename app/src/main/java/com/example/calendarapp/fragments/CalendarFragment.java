package com.example.calendarapp.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.activities.AddCourseActivity;
import com.example.calendarapp.activities.AddEventActivity;
import com.example.calendarapp.activities.EditCourseActivity;
import com.example.calendarapp.activities.EditEventActivity;
import com.example.calendarapp.adapters.CalendarEventAdapter;
import com.example.calendarapp.ai.AIChatActivity;
import com.example.calendarapp.ai.ConversationsActivity;
import com.example.calendarapp.models.Course;
import com.example.calendarapp.models.Event;
import com.example.calendarapp.premium.PremiumPurchaseActivity;
import com.example.calendarapp.utils.ApiHelper;
import com.example.calendarapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment implements CalendarEventAdapter.OnCalendarItemListener {
    private static final String TAG = "CalendarFragment";

    private TextView tvTitle, tvMonth;
    private TabLayout tabDays;
    private RecyclerView rvCalendarItems;
    private FloatingActionButton fabAddCalendarItem;
    private List<Object> calendarItems;
    private CalendarEventAdapter adapter;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    private String[] dayNames = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
    private Calendar currentDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        apiHelper = new ApiHelper(getContext());
        sessionManager = new SessionManager(getContext());

        tvTitle = view.findViewById(R.id.tv_title);
        tvMonth = view.findViewById(R.id.tv_month);
        tabDays = view.findViewById(R.id.tab_days);
        rvCalendarItems = view.findViewById(R.id.rv_calendar_items);
        fabAddCalendarItem = view.findViewById(R.id.fab_add_calendar_item);

        setupCalendarView();
        setupTabLayout();
        loadCalendarItems();

        fabAddCalendarItem.setOnClickListener(v -> {
            if (sessionManager.canPerformApiOperations()) {
                showAddOptionsDialog();
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm mới", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setupCalendarView() {
        // Set title
        tvTitle.setText("Thời khóa biểu");

        // Set current month
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi", "VN"));
        tvMonth.setText("Tháng " + (currentDate.get(Calendar.MONTH) + 1));

        // Setup RecyclerView for calendar items
        calendarItems = new ArrayList<>();
        adapter = new CalendarEventAdapter(getContext(), calendarItems, this);
        rvCalendarItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCalendarItems.setAdapter(adapter);
    }

    private void setupTabLayout() {
        // Xóa tất cả tab hiện tại (bao gồm cả TabItem mặc định từ XML)
        tabDays.removeAllTabs();
        // Đọc ngày bắt đầu tuần từ SharedPreferences - THÊM PHẦN NÀY
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        int startOfWeek = prefs.getInt("week_start_day", Calendar.MONDAY);
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK) - 2; // Chuyển từ Calendar.DAY_OF_WEEK sang index 0-6
        if (today < 0) today = 6; // Chủ nhật

        // Thêm các tab cho các ngày trong tuần
        for (int i = 0; i < 7; i++) {
            TabLayout.Tab tab = tabDays.newTab();

            // Tạo view tùy chỉnh cho tab
            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab_day, null);
            TextView tvDay = tabView.findViewById(R.id.tv_day);
            TextView tvDate = tabView.findViewById(R.id.tv_date);

            // Thiết lập ngày trong tuần (Thứ Hai, Thứ Ba, ...)
            tvDay.setText(dayNames[i]);

            // Tính toán ngày trong tháng
            Calendar tempCal = (Calendar) calendar.clone();
            int dayDiff = i - today;
            tempCal.add(Calendar.DAY_OF_WEEK, dayDiff);
            tvDate.setText(String.valueOf(tempCal.get(Calendar.DAY_OF_MONTH)));

            // Lưu trữ ngày thực tế vào tag của tab
            tab.setTag(tempCal.getTime());

            // Highlight tab hiện tại
            if (i == today) {
                tvDay.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvDate.setTextColor(getResources().getColor(R.color.colorPrimary));
                tabView.setBackgroundResource(R.drawable.tab_selected_background);
            }

            tab.setCustomView(tabView);
            tabDays.addTab(tab);
        }

        // Chọn tab hiện tại
        tabDays.selectTab(tabDays.getTabAt(today));

        // Xử lý sự kiện khi chọn tab
        tabDays.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                // Highlight tab được chọn
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    TextView tvDay = tabView.findViewById(R.id.tv_day);
                    TextView tvDate = tabView.findViewById(R.id.tv_date);
                    tvDay.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvDate.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tabView.setBackgroundResource(R.drawable.tab_selected_background);
                }

                // Cập nhật danh sách sự kiện và môn học cho ngày được chọn
                loadCalendarItemsForDay(dayNames[position]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Bỏ highlight tab không được chọn
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    TextView tvDay = tabView.findViewById(R.id.tv_day);
                    TextView tvDate = tabView.findViewById(R.id.tv_date);
                    tvDay.setTextColor(getResources().getColor(R.color.colorText));
                    tvDate.setTextColor(getResources().getColor(R.color.colorText));
                    tabView.setBackgroundResource(android.R.color.transparent);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần xử lý
            }
        });
    }

    private void loadCalendarItems() {
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2; // Chuyển từ Calendar.DAY_OF_WEEK sang index 0-6
        if (dayOfWeek < 0) dayOfWeek = 6; // Chủ nhật

        // Tải dữ liệu cho ngày hiện tại
        loadCalendarItemsForDay(dayNames[dayOfWeek]);
    }

    private void loadCalendarItemsForDay(String dayOfWeek) {
        Log.d(TAG, "Loading calendar items for day: " + dayOfWeek);

        if (!sessionManager.canPerformApiOperations()) {
            // Clear dữ liệu cũ
            calendarItems.clear();
            adapter.notifyDataSetChanged();

            if (sessionManager.isGuest()) {
                Toast.makeText(getContext(), "Chế độ khách - không có dữ liệu", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Clear dữ liệu cũ trước khi load mới
        calendarItems.clear();
        adapter.notifyDataSetChanged();

        // Lấy ngày tương ứng với tab được chọn
        Calendar selectedCalendar = getSelectedDateForDayOfWeek(dayOfWeek);
        Date selectedDate = selectedCalendar.getTime();

        Log.d(TAG, "Selected date: " + selectedDate);

        showProgressDialog("Đang tải dữ liệu...");

        // Biến đếm để đảm bảo cả 2 API call đều hoàn thành
        final int[] completedCalls = {0};
        final List<Object> tempItems = new ArrayList<>();

        // Load courses for the selected day
        apiHelper.getActiveCoursesForDay(dayOfWeek, selectedDate, new ApiHelper.ApiCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                Log.d(TAG, "Loaded " + courses.size() + " courses");
                synchronized (tempItems) {
                    tempItems.addAll(courses);
                    completedCalls[0]++;

                    // Nếu cả 2 API call đều hoàn thành
                    if (completedCalls[0] >= 2) {
                        updateCalendarItems(tempItems);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading courses: " + error);
                synchronized (tempItems) {
                    completedCalls[0]++;

                    // Nếu cả 2 API call đều hoàn thành (kể cả lỗi)
                    if (completedCalls[0] >= 2) {
                        updateCalendarItems(tempItems);
                    }
                }
                Toast.makeText(getContext(), "Lỗi tải môn học: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load events for the selected date
        apiHelper.getEventsForDate(selectedDate, new ApiHelper.ApiCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                Log.d(TAG, "Loaded " + events.size() + " events");
                synchronized (tempItems) {
                    tempItems.addAll(events);
                    completedCalls[0]++;

                    // Nếu cả 2 API call đều hoàn thành
                    if (completedCalls[0] >= 2) {
                        updateCalendarItems(tempItems);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading events: " + error);
                synchronized (tempItems) {
                    completedCalls[0]++;

                    // Nếu cả 2 API call đều hoàn thành (kể cả lỗi)
                    if (completedCalls[0] >= 2) {
                        updateCalendarItems(tempItems);
                    }
                }
                Toast.makeText(getContext(), "Lỗi tải sự kiện: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCalendarItems(List<Object> items) {
        hideProgressDialog();

        // Clear dữ liệu cũ và thêm dữ liệu mới
        calendarItems.clear();
        calendarItems.addAll(items);

        Log.d(TAG, "Updated calendar with " + calendarItems.size() + " items");

        // Notify adapter
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }
    }

    // Phương thức mới để lấy ngày tương ứng với thứ trong tuần
    private Calendar getSelectedDateForDayOfWeek(String dayOfWeek) {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Chuyển đổi từ tên thứ sang giá trị Calendar.DAY_OF_WEEK
        int targetDayOfWeek;
        switch (dayOfWeek) {
            case "Thứ Hai": targetDayOfWeek = Calendar.MONDAY; break;
            case "Thứ Ba": targetDayOfWeek = Calendar.TUESDAY; break;
            case "Thứ Tư": targetDayOfWeek = Calendar.WEDNESDAY; break;
            case "Thứ Năm": targetDayOfWeek = Calendar.THURSDAY; break;
            case "Thứ Sáu": targetDayOfWeek = Calendar.FRIDAY; break;
            case "Thứ Bảy": targetDayOfWeek = Calendar.SATURDAY; break;
            case "Chủ Nhật": targetDayOfWeek = Calendar.SUNDAY; break;
            default: targetDayOfWeek = currentDayOfWeek;
        }

        // Tính số ngày cần thêm/bớt để đạt được ngày mục tiêu
        int daysToAdd = targetDayOfWeek - currentDayOfWeek;
        if (daysToAdd < 0) {
            daysToAdd += 7; // Nếu ngày mục tiêu đã qua trong tuần này, lấy ngày của tuần sau
        }

        // Tạo calendar mới với ngày mục tiêu
        Calendar targetCalendar = (Calendar) calendar.clone();
        targetCalendar.add(Calendar.DAY_OF_YEAR, daysToAdd);

        return targetCalendar;
    }

    private void showAddOptionsDialog() {
        // Hiển thị dialog chọn thêm sự kiện, môn học hoặc chat AI
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm mới");

        String[] options;
        if (sessionManager.isPremium()) {
            options = new String[]{"Thêm sự kiện", "Thêm môn học", "🤖 Trò chuyện với AI", "📚 Lịch sử AI Chat"};
        } else {
            options = new String[]{"Thêm sự kiện", "Thêm môn học", "🤖 Trò chuyện với AI (Premium)"};
        }

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Thêm sự kiện
                Intent intent = new Intent(getActivity(), AddEventActivity.class);
                startActivity(intent);
            } else if (which == 1) {
                // Thêm môn học
                Intent intent = new Intent(getActivity(), AddCourseActivity.class);
                startActivity(intent);
            } else if (which == 2) {
                // Trò chuyện với AI
                if (sessionManager.isPremium()) {
                    Intent intent = new Intent(getActivity(), AIChatActivity.class);
                    startActivity(intent);
                } else {
                    showPremiumRequiredDialog();
                }
            } else if (which == 3 && sessionManager.isPremium()) {
                // Lịch sử AI Chat - CHỈ HIỂN THỊ CHO PREMIUM
                Intent intent = new Intent(getActivity(), ConversationsActivity.class);
                startActivity(intent);
            }
        });
        builder.show();
    }

    private void showPremiumRequiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("🤖 Tính năng Premium");
        builder.setMessage("Trò chuyện với AI là tính năng dành riêng cho người dùng Premium.\n\n" +
                "Với AI học tập, bạn có thể:\n" +
                "• Hỏi đáp về các môn học\n" +
                "• Nhận hướng dẫn giải bài tập\n" +
                "• Tư vấn phương pháp học tập\n" +
                "• Và nhiều tính năng khác!\n\n" +
                "Bạn có muốn nâng cấp lên Premium không?");

        builder.setPositiveButton("Nâng cấp Premium", (dialog, which) -> {
            Intent intent = new Intent(getActivity(), PremiumPurchaseActivity.class);
            startActivity(intent);
        });

        builder.setNegativeButton("Để sau", null);
        builder.show();
    }

    @Override
    public void onCourseClick(Course course) {
        if (sessionManager.canPerformApiOperations()) {
            Intent intent = new Intent(getActivity(), EditCourseActivity.class);
            intent.putExtra("COURSE", course);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để chỉnh sửa", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEventClick(Event event) {
        if (sessionManager.canPerformApiOperations()) {
            Intent intent = new Intent(getActivity(), EditEventActivity.class);
            intent.putExtra("EVENT", event);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để chỉnh sửa", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại dữ liệu khi quay lại fragment
        loadCalendarItems();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
}
