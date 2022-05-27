package xyz.akopartem.subtracker;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;


public class TestFirebaseInstanceIdService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("TOKEN", s);
        send(s);
    }
    public interface TokenSend {
        @POST("token")
        Call<Response> send(@Body HashMap<String, String> token);
    }

    public void send(String s) {
        new Thread(() -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://192.168.3.21:8080/")
                    .build();
            TokenSend ts = retrofit.create(TokenSend.class);
            try {
                HashMap<String, String> ss = new HashMap<>();
                ss.put("token", s);
                ts.send(ss).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}