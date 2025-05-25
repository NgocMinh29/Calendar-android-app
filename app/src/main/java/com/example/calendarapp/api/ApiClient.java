package com.example.calendarapp.api;

import android.os.Build;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            String BASE_URL;

            // Tự động phát hiện máy ảo
            if (isEmulator()) {
                BASE_URL = "http://10.0.2.2:8080/schedule_api/";
                Log.d("ApiClient", "Đang chạy trên máy ảo - dùng 10.0.2.2");
            } else {
                String ip = getHostIpAddress();
                if (ip != null) {
                    BASE_URL = "http://" + ip + ":8080/schedule_api/";
                    Log.d("ApiClient", "Máy thật - IP: " + ip);
                } else {
                    // fallback nếu không lấy được IP
                    BASE_URL = "http://10.0.140.127:8080/schedule_api/";
                    Log.w("ApiClient", "Không lấy được IP - dùng IP mặc định");
                }
            }

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    private static boolean isEmulator() {
        return Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("Emulator")
                || Build.BRAND.contains("generic")
                || Build.DEVICE.contains("generic");
    }

    private static String getHostIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.isUp() || intf.isLoopback() || intf.getName().contains("vm")) continue;

                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    String ip = addr.getHostAddress();
                    if (!addr.isLoopbackAddress() && ip.indexOf(':') < 0 && ip.startsWith("10.")) {
                        return ip;  // Ưu tiên IP trong mạng 10.x.x.x (Wi-Fi của bạn)
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
