package com.example.calendarapp.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "calendar_app.db";
    private static final int DATABASE_VERSION = 1;

    // Bảng Event
    private static final String TABLE_EVENTS = "events";
    private static final String EVENT_ID = "id";
    private static final String EVENT_TITLE = "title";
    private static final String EVENT_NOTE = "note";
    private static final String EVENT_DATE = "date";
    private static final String EVENT_TIME = "time";
    private static final String EVENT_NOTIFICATION = "notification";
    private static final String EVENT_REMINDER_MINUTES = "reminder_minutes";
    private static final String EVENT_LOCATION = "location";

    // Bảng Course
    private static final String TABLE_COURSES = "courses";
    private static final String COURSE_ID = "id";
    private static final String COURSE_NAME = "name";
    private static final String COURSE_ROOM = "room";
    private static final String COURSE_DAY_OF_WEEK = "day_of_week";
    private static final String COURSE_START_TIME = "start_time";
    private static final String COURSE_END_TIME = "end_time";
    private static final String COURSE_START_DATE = "start_date";
    private static final String COURSE_END_DATE = "end_date";
    private static final String COURSE_WEEK_FREQUENCY = "week_frequency";
    private static final String COURSE_NOTIFICATION = "notification";
    private static final String COURSE_REMINDER_MINUTES = "reminder_minutes";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Event
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + EVENT_TITLE + " TEXT,"
                + EVENT_NOTE + " TEXT,"
                + EVENT_DATE + " TEXT,"
                + EVENT_TIME + " TEXT,"
                + EVENT_NOTIFICATION + " INTEGER,"
                + EVENT_REMINDER_MINUTES + " INTEGER,"
                + EVENT_LOCATION + " TEXT"
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);

        // Tạo bảng Course
        String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
                + COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COURSE_NAME + " TEXT,"
                + COURSE_ROOM + " TEXT,"
                + COURSE_DAY_OF_WEEK + " TEXT,"
                + COURSE_START_TIME + " TEXT,"
                + COURSE_END_TIME + " TEXT,"
                + COURSE_START_DATE + " TEXT,"
                + COURSE_END_DATE + " TEXT,"
                + COURSE_WEEK_FREQUENCY + " INTEGER,"
                + COURSE_NOTIFICATION + " INTEGER,"
                + COURSE_REMINDER_MINUTES + " INTEGER"
                + ")";
        db.execSQL(CREATE_COURSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);

        // Tạo lại bảng mới
        onCreate(db);
    }

    // Các phương thức CRUD cho Event

    public long addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EVENT_TITLE, event.getTitle());
        values.put(EVENT_NOTE, event.getNote());
        values.put(EVENT_DATE, dateFormat.format(event.getDate()));
        values.put(EVENT_TIME, event.getTime());
        values.put(EVENT_NOTIFICATION, event.isNotification() ? 1 : 0);
        values.put(EVENT_REMINDER_MINUTES, event.getReminderMinutes());
        values.put(EVENT_LOCATION, event.getLocation());

        // Thêm vào database
        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();

        return id;
    }

    public Event getEvent(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS, null, EVENT_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Event event = cursorToEvent(cursor);
        cursor.close();

        return event;
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + EVENT_DATE + " ASC, " + EVENT_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Event event = cursorToEvent(cursor);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return eventList;
    }

    public List<Event> getEventsForDate(Date date) {
        List<Event> eventList = new ArrayList<>();

        String dateString = dateFormat.format(date);
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " WHERE " + EVENT_DATE + "=? ORDER BY " + EVENT_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{dateString});

        if (cursor.moveToFirst()) {
            do {
                Event event = cursorToEvent(cursor);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return eventList;
    }

    public int updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EVENT_TITLE, event.getTitle());
        values.put(EVENT_NOTE, event.getNote());
        values.put(EVENT_DATE, dateFormat.format(event.getDate()));
        values.put(EVENT_TIME, event.getTime());
        values.put(EVENT_NOTIFICATION, event.isNotification() ? 1 : 0);
        values.put(EVENT_REMINDER_MINUTES, event.getReminderMinutes());
        values.put(EVENT_LOCATION, event.getLocation());

        // Cập nhật
        return db.update(TABLE_EVENTS, values, EVENT_ID + "=?",
                new String[]{String.valueOf(event.getId())});
    }

    public void deleteEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, EVENT_ID + "=?",
                new String[]{String.valueOf(event.getId())});
        db.close();
    }

    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();

        event.setId(cursor.getLong(cursor.getColumnIndex(EVENT_ID)));
        event.setTitle(cursor.getString(cursor.getColumnIndex(EVENT_TITLE)));
        event.setNote(cursor.getString(cursor.getColumnIndex(EVENT_NOTE)));

        String dateString = cursor.getString(cursor.getColumnIndex(EVENT_DATE));
        try {
            event.setDate(dateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
            event.setDate(new Date()); // Mặc định là ngày hiện tại nếu có lỗi
        }

        event.setTime(cursor.getString(cursor.getColumnIndex(EVENT_TIME)));
        event.setNotification(cursor.getInt(cursor.getColumnIndex(EVENT_NOTIFICATION)) == 1);
        event.setReminderMinutes(cursor.getInt(cursor.getColumnIndex(EVENT_REMINDER_MINUTES)));
        event.setLocation(cursor.getString(cursor.getColumnIndex(EVENT_LOCATION)));

        return event;
    }

    // Các phương thức CRUD cho Course

    public long addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COURSE_NAME, course.getName());
        values.put(COURSE_ROOM, course.getRoom());
        values.put(COURSE_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COURSE_START_TIME, course.getStartTime());
        values.put(COURSE_END_TIME, course.getEndTime());
        values.put(COURSE_START_DATE, dateFormat.format(course.getStartDate()));
        values.put(COURSE_END_DATE, dateFormat.format(course.getEndDate()));
        values.put(COURSE_WEEK_FREQUENCY, course.getWeekFrequency());
        values.put(COURSE_NOTIFICATION, course.isNotification() ? 1 : 0);
        values.put(COURSE_REMINDER_MINUTES, course.getReminderMinutes());

        // Thêm vào database
        long id = db.insert(TABLE_COURSES, null, values);
        db.close();

        return id;
    }

    public Course getCourse(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_COURSES, null, COURSE_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Course course = cursorToCourse(cursor);
        cursor.close();

        return course;
    }

    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_COURSES + " ORDER BY " + COURSE_DAY_OF_WEEK + " ASC, " + COURSE_START_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = cursorToCourse(cursor);
                courseList.add(course);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return courseList;
    }

    public List<Course> getCoursesForDay(String dayOfWeek) {
        List<Course> courseList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_COURSES + " WHERE " + COURSE_DAY_OF_WEEK + "=? ORDER BY " + COURSE_START_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{dayOfWeek});

        if (cursor.moveToFirst()) {
            do {
                Course course = cursorToCourse(cursor);

                // Kiểm tra xem khóa học có trong khoảng thời gian hiện tại không
                Date currentDate = new Date();
                if (currentDate.after(course.getStartDate()) && currentDate.before(course.getEndDate())) {
                    courseList.add(course);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return courseList;
    }

    public List<Course> getActiveCoursesForDay(String dayOfWeek) {
        List<Course> courseList = new ArrayList<>();
        Date currentDate = new Date();

        String selectQuery = "SELECT * FROM " + TABLE_COURSES + " WHERE " + COURSE_DAY_OF_WEEK + "=? ORDER BY " + COURSE_START_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{dayOfWeek});

        if (cursor.moveToFirst()) {
            do {
                Course course = cursorToCourse(cursor);

                // Kiểm tra xem khóa học có trong khoảng thời gian hiện tại không
                if (isDateInRange(currentDate, course.getStartDate(), course.getEndDate())) {
                    courseList.add(course);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return courseList;
    }

    private boolean isDateInRange(Date date, Date startDate, Date endDate) {
        return (date.equals(startDate) || date.after(startDate)) &&
                (date.equals(endDate) || date.before(endDate));
    }

    public int updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COURSE_NAME, course.getName());
        values.put(COURSE_ROOM, course.getRoom());
        values.put(COURSE_DAY_OF_WEEK, course.getDayOfWeek());
        values.put(COURSE_START_TIME, course.getStartTime());
        values.put(COURSE_END_TIME, course.getEndTime());
        values.put(COURSE_START_DATE, dateFormat.format(course.getStartDate()));
        values.put(COURSE_END_DATE, dateFormat.format(course.getEndDate()));
        values.put(COURSE_WEEK_FREQUENCY, course.getWeekFrequency());
        values.put(COURSE_NOTIFICATION, course.isNotification() ? 1 : 0);
        values.put(COURSE_REMINDER_MINUTES, course.getReminderMinutes());

        // Cập nhật
        return db.update(TABLE_COURSES, values, COURSE_ID + "=?",
                new String[]{String.valueOf(course.getId())});
    }

    public void deleteCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COURSES, COURSE_ID + "=?",
                new String[]{String.valueOf(course.getId())});
        db.close();
    }

    private Course cursorToCourse(Cursor cursor) {
        Course course = new Course();

        course.setId(cursor.getLong(cursor.getColumnIndex(COURSE_ID)));
        course.setName(cursor.getString(cursor.getColumnIndex(COURSE_NAME)));
        course.setRoom(cursor.getString(cursor.getColumnIndex(COURSE_ROOM)));
        course.setDayOfWeek(cursor.getString(cursor.getColumnIndex(COURSE_DAY_OF_WEEK)));
        course.setStartTime(cursor.getString(cursor.getColumnIndex(COURSE_START_TIME)));
        course.setEndTime(cursor.getString(cursor.getColumnIndex(COURSE_END_TIME)));

        String startDateString = cursor.getString(cursor.getColumnIndex(COURSE_START_DATE));
        String endDateString = cursor.getString(cursor.getColumnIndex(COURSE_END_DATE));

        try {
            course.setStartDate(dateFormat.parse(startDateString));
            course.setEndDate(dateFormat.parse(endDateString));
        } catch (ParseException e) {
            e.printStackTrace();
            course.setStartDate(new Date());
            course.setEndDate(new Date());
        }

        course.setWeekFrequency(cursor.getInt(cursor.getColumnIndex(COURSE_WEEK_FREQUENCY)));
        course.setNotification(cursor.getInt(cursor.getColumnIndex(COURSE_NOTIFICATION)) == 1);
        course.setReminderMinutes(cursor.getInt(cursor.getColumnIndex(COURSE_REMINDER_MINUTES)));

        return course;
    }
}
