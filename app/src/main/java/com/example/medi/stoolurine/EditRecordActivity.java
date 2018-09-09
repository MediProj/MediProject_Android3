package com.example.medi.stoolurine;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Locale;

public class EditRecordActivity extends BaseActivity{
    String pid, time, date,amount, record_pk;
    int type;
    int year, month, day, hour, minute, second;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_urine);

        Button del = findViewById(R.id.btn_delete);
        Button edit = findViewById(R.id.editConfirm);
        Button cancel = findViewById(R.id.editCancel);
        final TextView tv_time = findViewById(R.id.time);
        final TextView tv_amount = findViewById(R.id.amount);

        final Intent intent = getIntent();
        pid = MediValues.pid;
        final String name= MediValues.patientData.get(pid).get("name");

        record_pk = intent.getStringExtra("record_pk");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        amount = intent.getStringExtra("amount");
        type = intent.getIntExtra("type", 0);

        String[] dateArr = date.split("/");
        year = Integer.parseInt(dateArr[0]);
        month = Integer.parseInt(dateArr[1]);
        day = Integer.parseInt(dateArr[2]);

        hour =Integer.parseInt(time.split("시 ")[0]);
        String tmp = time.split("시 ")[1];
        minute= Integer.parseInt(tmp.split("분")[0]);
        second=0;

        MediValues.mediDate = String.format(Locale.US, "%d-%d-%d", year, month, day);
        MediValues.mediTime = String.format(Locale.US, "%d:%d:%d", hour, minute, second);

        tv_time.setText(date +" / " + time);
        tv_amount.setText(amount);


        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAlertDialog delAlert = new DeleteAlertDialog();
                delAlert.showDialog(EditRecordActivity.this);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_time = tv_time.getText().toString();
                String new_amount = tv_amount.getText().toString();

                //잘못된 입력
                if (new_time.matches("") || new_amount.matches("")) {
                    Toast.makeText(getApplicationContext(), "잘못된 형식입니다. 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                }

                else {                    //삭제 후 다시 post

                    MediDeleteRequest deleteRequest = new MediDeleteRequest(record_pk, getApplicationContext());
                    //MediPostRequest postRequest = new MediPostRequest(getApplicationContext(), pid, name, MediValues.OUTPUT, MediValues.URINE, Float.parseFloat(new_amount), null);

                    Intent intent2 = new Intent(EditRecordActivity.this, TimeDateActivity.class);
                    intent2.putExtra("val", type);
                    startActivity(intent2);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(EditRecordActivity.this, ReportActivity.class);
                intent2.putExtra("pid", pid);
                startActivity(intent2);
            }
        });
    }


    public class DeleteAlertDialog {

        public void showDialog(Activity activity) {
            final Dialog dialog = new Dialog(activity);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.record_delete_prompt);

            Button delCancel = (Button) dialog.findViewById(R.id.delCancel);
            delCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            Button delConfirm = (Button) dialog.findViewById(R.id.delConfirm);
            delConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediDeleteRequest delRecord = new MediDeleteRequest(record_pk, getApplicationContext());

                    dialog.dismiss();
                    final ProgressDialog progress = new ProgressDialog(EditRecordActivity.this);
                    progress.setTitle("로딩중");
                    progress.setMessage("기록 삭제 중입니다...");
                    progress.setCancelable(false);
                    progress.show();

                    MediValues.patientRecord = null;
                    MediValues.pkRecordTag = null;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                        }
                    }, 2000);
                }
            });

            //Here's the magic..
            //Set the dialog to not focusable (makes navigation ignore us adding the window)
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            //Show the dialog!
            dialog.show();

            //Set the dialog to immersive
            dialog.getWindow().getDecorView().setSystemUiVisibility(
                    activity.getWindow().getDecorView().getSystemUiVisibility());

            //Clear the not focusable flag from the window
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            dialog.getWindow().setLayout(1200, 600);
        }
    }

}
