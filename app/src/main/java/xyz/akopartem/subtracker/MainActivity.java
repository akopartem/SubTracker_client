package xyz.akopartem.subtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    protected static Activity activity;
    static String token;
    static SubAdapter sa;
    static RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Произошла ошибка!")
                                .setMessage("К сожалению произошла непредвиденная ошибка, просим перезапустить или переустановить приложение. В случае дальнейших проблем предлагаю связаться со мной, tg:akopartem")
                                .setPositiveButton("OK", (DialogInterface.OnClickListener) (dialog, id) -> {
                                    finishAffinity();
                                    dialog.cancel();
                                })
                                .setOnDismissListener((dialog) -> {
                                    finishAffinity();
                                    dialog.cancel();
                                }).show();
                    }
                    MainActivity.token = task.getResult();
                });
        if (!Checker.isInternetAvailable(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Отсутствует подключение к интернету!")
                    .setMessage("Для работы с приложением требуется подключение к интернету. Подключитесь, пожалуйста к сети и попробуйте снова.")
                    .setPositiveButton("OK", (DialogInterface.OnClickListener) (dialog, id) -> {
                        finishAffinity();
                        dialog.cancel();
                    })
                    .setOnDismissListener((dialog) -> {
                        finishAffinity();
                        dialog.cancel();
                    }).show();
        }

        DBManager db = DBManager.getInstance(this);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        if (!(db.getAllResults().size() == 0)) {
            sa = new SubAdapter(this, db.getAllResults());
            recyclerView.setAdapter(sa);
        } else {
            findViewById(R.id.addNewTW).setVisibility(View.VISIBLE);
        }
        FloatingActionButton b = findViewById(R.id.btn);
        b.setOnClickListener(e -> {
            Intent intent = new Intent(MainActivity.this, AddSub.class);
            startActivity(intent);
        });
    }

}
