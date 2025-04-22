package com.example.calendarapp.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "calendar.db";
    private static final int DATABASE_VERSION = 1;

    // Common column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOTIFICATION = "notification";
    private static final String COLUMN_REMINDER_MINUTES = "reminder_minutes";

    // Event table
    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_EVENT_TITLE = "title";
    private static final String COLUMN_EVENT_NOTE = "note";
    private static final String COLUMN_EVENT_DATE = "date";
    private static final String COLUMN_EVENT_TIME = "time";
    private static final String COLUMN_EVENT_LOCATION = "location";

    // Course table
    private static final String TABLE_COURSES = "courses";
    private static final String COLUMN_COURSE_NAME = "name";
    private static final String COLUMN_COURSE_ROOM = "room";
    private static final String COLUMN_COURSE_DAY = "day_of_week";
    private static final String COLUMN_COURSE_START_TIME = "start_time";
    private static final String COLUMN_COURSE_END_TIME = "end_time";
    private static final String COLUMN_COURSE_START_DATE = "start_date";
    private static final String COLUMN_COURSE_END_DATE = "end_date";
    private static final String COLUMN_COURSE_FREQUENCY = "week_frequency";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create events table
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EVENT_TITLE + " TEXT,"
                + COLUMN_EVENT_NOTE + " TEXT,"
                + COLUMN_EVENT_DATE + " TEXT,"
                + COLUMN_EVENT_TIME + " TEXT,"
                + COLUMN_EVENT_LOCATION + " TEXT,"
                + COLUMN_NOTIFICATION + " INTEGER,"
                + COLUMN_REMINDER_MINUTES + " INTEGER"
                + ")";
        db.execSQL(CREATE_EVENTS_TABLE);

        // Create courses table
        String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_COURSE_NAME + " TEXT,"
                + COLUMN_COURSE_ROOM + " TEXT,"
                + COLUMN_COURSE_DAY + " TEXT,"
                + COLUMN_COURSE_START_TIME + " TEXT,"
                + COLUMN_COURSE_END_TIME + " TEXT,"
                + COLUMN_COURSE_START_DATE + " TEXT,"
                + COLUMN_COURSE_END_DATE + " TEXT,"
                + COLUMN_COURSE_FREQUENCY + " INTEGER,"
                + COLUMN_NOTIFICATION + " INTEGER,"
                + COLUMN_REMINDER_MINUTES + " INTEGER"
                + ")";
        db.execSQL(CREATE_COURSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);

        // Create tables again
        onCreate(db);
    }

    // Event CRUD operations

    public long addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_TITLE, event.getTitle());
        values.put(COLUMN_EVENT_NOTE, event.getNote());
        values.put(COLUMN_EVENT_DATE, dateFormat.format(event.getDate()));
        values.put(COLUMN_EVENT_TIME, event.getTime());
        values.put(COLUMN_EVENT_LOCATION, event.getLocation());
        values.put(COLUMN_NOTIFICATION, event.isNotification() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, event.getReminderMinutes());

        // Insert row
        long id = db.insert(TABLE_EVENTS, null, values);
        db.close();

        return id;
    }

    public Event getEvent(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_EVENTS,
                new String[] {
                        COLUMN_ID,
                        COLUMN_EVENT_TITLE,
                        COLUMN_EVENT_NOTE,
                        COLUMN_EVENT_DATE,
                        COLUMN_EVENT_TIME,
                        COLUMN_EVENT_LOCATION,
                        COLUMN_NOTIFICATION,
                        COLUMN_REMINDER_MINUTES
                },
                COLUMN_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null, null
        );

        if (cursor != null)
            cursor.moveToFirst();

        Event event = null;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                event = new Event(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        dateFormat.parse(cursor.getString(3)),
                        cursor.getString(4),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7),
                        cursor.getString(5)
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cursor.close();
        }

        db.close();
        return event;
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + COLUMN_EVENT_DATE + " ASC, " + COLUMN_EVENT_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    Event event = new Event(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            dateFormat.parse(cursor.getString(3)),
                            cursor.getString(4),
                            cursor.getInt(6) == 1,
                            cursor.getInt(7),
                            cursor.getString(5)
                    );
                    eventList.add(event);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return eventList;
    }

    public List<Event> getEventsForDate(Date date) {
        List<Event> eventList = new ArrayList<>();

        String dateString = dateFormat.format(date);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_EVENTS,
                new String[] {
                        COLUMN_ID,
                        COLUMN_EVENT_TITLE,
                        COLUMN_EVENT_NOTE,
                        COLUMN_EVENT_DATE,
                        COLUMN_EVENT_TIME,
                        COLUMN_EVENT_LOCATION,
                        COLUMN_NOTIFICATION,
                        COLUMN_REMINDER_MINUTES
                },
                COLUMN_EVENT_DATE + "=?",
                new String[] { dateString },
                null, null,
                COLUMN_EVENT_TIME + " ASC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                try {
                    Event event = new Event(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            dateFormat.parse(cursor.getString(3)),
                            cursor.getString(4),
                            cursor.getInt(6) == 1,
                            cursor.getInt(7),
                            cursor.getString(5)
                    );
                    eventList.add(event);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return eventList;
    }

    public int updateEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_TITLE, event.getTitle());
        values.put(COLUMN_EVENT_NOTE, event.getNote());
        values.put(COLUMN_EVENT_DATE, dateFormat.format(event.getDate()));
        values.put(COLUMN_EVENT_TIME, event.getTime());
        values.put(COLUMN_EVENT_LOCATION, event.getLocation());
        values.put(COLUMN_NOTIFICATION, event.isNotification() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, event.getReminderMinutes());

        // Update row
        int result = db.update(
                TABLE_EVENTS,
                values,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(event.getId()) }
        );

        db.close();
        return result;
    }

    public void deleteEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                TABLE_EVENTS,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(event.getId()) }
        );
        db.close();
    }

    // Course CRUD operations

    public long addCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_NAME, course.getName());
        values.put(COLUMN_COURSE_ROOM, course.getRoom());
        values.put(COLUMN_COURSE_DAY, course.getDayOfWeek());
        values.put(COLUMN_COURSE_START_TIME, course.getStartTime());
        values.put(COLUMN_COURSE_END_TIME, course.getEndTime());
        values.put(COLUMN_COURSE_START_DATE, dateFormat.format(course.getStartDate()));
        values.put(COLUMN_COURSE_END_DATE, dateFormat.format(course.getEndDate()));
        values.put(COLUMN_COURSE_FREQUENCY, course.getWeekFrequency());
        values.put(COLUMN_NOTIFICATION, course.isNotification() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, course.getReminderMinutes());

        // Insert row
        long id = db.insert(TABLE_COURSES, null, values);
        db.close();

        return id;
    }

    public Course getCourse(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_COURSES,
                new String[] {
                        COLUMN_ID,
                        COLUMN_COURSE_NAME,
                        COLUMN_COURSE_ROOM,
                        COLUMN_COURSE_DAY,
                        COLUMN_COURSE_START_TIME,
                        COLUMN_COURSE_END_TIME,
                        COLUMN_COURSE_START_DATE,
                        COLUMN_COURSE_END_DATE,
                        COLUMN_COURSE_FREQUENCY,
                        COLUMN_NOTIFICATION,
                        COLUMN_REMINDER_MINUTES
                },
                COLUMN_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null, null
        );

        if (cursor != null)
            cursor.moveToFirst();

        Course course = null;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                course = new Course(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        dateFormat.parse(cursor.getString(6)),
                        dateFormat.parse(cursor.getString(7)),
                        cursor.getInt(8),
                        cursor.getInt(9) == 1,
                        cursor.getInt(10)
                );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cursor.close();
        }

        db.close();
        return course;
    }

    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_COURSES + " ORDER BY " + COLUMN_COURSE_DAY + " ASC, " + COLUMN_COURSE_START_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    Course course = new Course(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            dateFormat.parse(cursor.getString(6)),
                            dateFormat.parse(cursor.getString(7)),
                            cursor.getInt(8),
                            cursor.getInt(9) == 1,
                            cursor.getInt(10)
                    );
                    courseList.add(course);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return courseList;
    }

    public List<Course> getCoursesForDay(String dayOfWeek) {
        List<Course> courseList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_COURSES,
                new String[] {
                        COLUMN_ID,
                        COLUMN_COURSE_NAME,
                        COLUMN_COURSE_ROOM,
                        COLUMN_COURSE_DAY,
                        COLUMN_COURSE_START_TIME,
                        COLUMN_COURSE_END_TIME,
                        COLUMN_COURSE_START_DATE,
                        COLUMN_COURSE_END_DATE,
                        COLUMN_COURSE_FREQUENCY,
                        COLUMN_NOTIFICATION,
                        COLUMN_REMINDER_MINUTES
                },
                COLUMN_COURSE_DAY + "=?",
                new String[] { dayOfWeek },
                null, null,
                COLUMN_COURSE_START_TIME + " ASC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                try {
                    Course course = new Course(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            dateFormat.parse(cursor.getString(6)),
                            dateFormat.parse(cursor.getString(7)),
                            cursor.getInt(8),
                            cursor.getInt(9) == 1,
                            cursor.getInt(10)
                    );
                    courseList.add(course);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return courseList;
    }

    public int updateCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_NAME, course.getName());
        values.put(COLUMN_COURSE_ROOM, course.getRoom());
        values.put(COLUMN_COURSE_DAY, course.getDayOfWeek());
        values.put(COLUMN_COURSE_START_TIME, course.getStartTime());
        values.put(COLUMN_COURSE_END_TIME, course.getEndTime());
        values.put(COLUMN_COURSE_START_DATE, dateFormat.format(course.getStartDate()));
        values.put(COLUMN_COURSE_END_DATE, dateFormat.format(course.getEndDate()));
        values.put(COLUMN_COURSE_FREQUENCY, course.getWeekFrequency());
        values.put(COLUMN_NOTIFICATION, course.isNotification() ? 1 : 0);
        values.put(COLUMN_REMINDER_MINUTES, course.getReminderMinutes());

        // Update row
        int result = db.update(
                TABLE_COURSES,
                values,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(course.getId()) }
        );

        db.close();
        return result;
    }

    public void deleteCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                TABLE_COURSES,
                COLUMN_ID + " = ?",
                new String[] { String.valueOf(course.getId()) }
        );
        db.close();
    }
}
