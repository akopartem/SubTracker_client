package xyz.akopartem.subtracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class SubAdapter extends RecyclerView.Adapter<SubAdapter.ViewHolder> {

    private final LayoutInflater lf;
    private final List<Sub> list;

    SubAdapter(Context context, List<Sub> list) {
        this.list = list;
        lf = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = lf.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(SubAdapter.ViewHolder holder, int position) {
        Sub s = list.get(position);
        holder.nameView.setText(s.name);
        if (Period.between(LocalDate.now(), s.date).getDays() >= 30) {
            DBManager.dbManager.exec("UPDATE subs SET date = " + LocalDate.now() + " WHERE name = " + s.name + " AND price = " + s.price + " AND date = " + s.date.toString() + "LIMIT 1;");
            list.get(position).date = LocalDate.now();
        }
        holder.dateView.setText(s.date.minusDays(1).toString());
        holder.priceView.setText(s.price + "₽");
        holder.btn.setOnClickListener(e -> {
            if (!Checker.isInternetAvailable(MainActivity.activity)) {
                new AlertDialog.Builder(MainActivity.activity)
                        .setTitle("Отсутствует подключение к интернету!")
                        .setMessage("Для работы с приложением требуется подключение к интернету. Подключитесь, пожалуйста к сети и попробуйте снова.")
                        .setPositiveButton("OK", (DialogInterface.OnClickListener) (dialog, id) -> {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            dialog.cancel();
                        })
                        .setOnDismissListener((dialog) -> {
                            android.os.Process.killProcess(android.os.Process.myPid());
                            dialog.cancel();
                        }).show();
                return;
            }
            e.setEnabled(false);
            list.remove(position);
            ((RecyclerView) (MainActivity.activity.findViewById(R.id.rv))).removeViewAt(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());
            new Thread(() -> {
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl("http://192.168.3.21:8080/")
                        .build();
                SubRem ts = retrofit.create(SubRem.class);

                try {
                    HashMap<String, String> ss = new HashMap<>();
                    ss.put("name", holder.nameView.getText().toString());
                    ss.put("price", s.price + "");
                    ss.put("lastDate", s.date.toString());
                    ss.put("token", MainActivity.token);
                    ts.send(ss).execute();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }).start();
            DBManager.dbManager.exec("DELETE FROM subs WHERE rowid = (SELECT rowid FROM subs WHERE name = '" + s.name + "' AND price = " + s.price + " AND date = '" + s.date.toString() + "' LIMIT 1);");
            if (getItemCount() == 0) {
                MainActivity.activity.findViewById(R.id.addNewTW).setVisibility(View.VISIBLE);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameView = null;
        TextView priceView = null;
        TextView dateView = null;
        Button btn = null;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.findViewById(R.id.ll).setBackgroundResource(R.drawable.krugliye_ugli);
            nameView = itemView.findViewById(R.id.nameO);
            priceView = itemView.findViewById(R.id.priceO);
            dateView = itemView.findViewById(R.id.next);
            btn = itemView.findViewById(R.id.btnn);
        }
    }

    public List<Sub> getList() {
        return list;
    }

    public interface SubRem {
        @POST("removeSub")
        Call<Response> send(@Body HashMap<String, String> hashMap);
    }
}
