package xyz.akopartem.subtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AddSub extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        DBManager db = DBManager.dbManager;
        setContentView(R.layout.activity_add_sub);
        Button btn = findViewById(R.id.button);
        EditText name = ((TextInputLayout) findViewById(R.id.name)).getEditText();
        EditText price = ((TextInputLayout) findViewById(R.id.price)).getEditText();
        EditText date = ((TextInputLayout) findViewById(R.id.datepick)).getEditText();
        btn.setOnClickListener(e -> {
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
            int sum = Integer.parseInt(price.getText().toString());
            price.setText(sum + "");

            if (name.getText().length() > 10) {
                ((TextInputLayout) findViewById(R.id.name)).setError("не больше 10 символов!");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.name)).setError(null);
            }

            if (price.length() == 0) {
                ((TextInputLayout) findViewById(R.id.price)).setError("введите цену");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.price)).setError(null);
            }
            if (Integer.parseInt(price.getText().toString())<= 0) {
                ((TextInputLayout) findViewById(R.id.price)).setError("введите корректную цену");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.price)).setError(null);
            }
            if (!Pattern.matches("(\\d{2}\\.\\d{2}\\.\\d{4})", date.getText())) {
                ((TextInputLayout) findViewById(R.id.datepick)).setError("введите дату в формате dd/mm/yyyy");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.datepick)).setError(null);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.MM.yyyy");
            if (LocalDate.now().compareTo(LocalDate.parse(date.getText().toString(), formatter)) < 0) {
                ((TextInputLayout) findViewById(R.id.datepick)).setError("вы из будущего?");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.datepick)).setError(null);
            }

            if (LocalDate.now().minusDays(32).compareTo(LocalDate.parse(date.getText().toString(), formatter)) >= 0) {
                ((TextInputLayout) findViewById(R.id.datepick)).setError("дата списания была месяц назад или больше");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.datepick)).setError(null);
            }

            if (name.getText().toString().trim().isEmpty()) {
                ((TextInputLayout) findViewById(R.id.name)).setError("введите название");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.name)).setError(null);
            }

            if (price.getText().toString().replace("\\s", "").isEmpty()) {
                ((TextInputLayout) findViewById(R.id.datepick)).setError("введите цену");
                return;
            } else {
                ((TextInputLayout) findViewById(R.id.datepick)).setError(null);
            }
            new Thread(() -> {
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl("http://192.168.3.21:8080/")
                        .build();
                SubSend ts = retrofit.create(AddSub.SubSend.class);
                try {
                    HashMap<String, String> ss = new HashMap<>();
                    ss.put("name", name.getText().toString());
                    ss.put("price", price.getText().toString());
                    ss.put("lastDate", LocalDate.parse(date.getText().toString(), formatter).toString());
                    ss.put("token", MainActivity.token);
                    ts.add(ss).execute();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }).start();
            db.addResult(name.getText().toString(), Integer.parseInt(price.getText().toString()), LocalDate.parse(date.getText(), formatter));
            MainActivity.recyclerView.setAdapter(new SubAdapter(MainActivity.activity, DBManager.dbManager.getAllResults()));
            MainActivity.activity.findViewById(R.id.addNewTW).setVisibility(View.INVISIBLE);
            finish();
        });

        assert name != null;

        date.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private final String ddmmyyyy = "________";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int mon = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        mon = mon < 1 ? 1 : Math.min(mon, 12);
                        cal.set(Calendar.MONTH, mon - 1);
                        year = (year < 1900) ? 1900 : Math.min(year, LocalDate.now().getYear());
                        cal.set(Calendar.YEAR, year);
                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format("%02d%02d%02d", day, mon, year);
                    }

                    clean = String.format("%s.%s.%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    date.setText(current);
                    date.setSelection(Math.min(sel, current.length()));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public interface SubSend {
        @POST("addSub")
        Call<Response> add(@Body HashMap<String, String> subscription);
    }
}