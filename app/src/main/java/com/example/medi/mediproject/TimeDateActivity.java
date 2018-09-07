package com.example.medi.mediproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeDateActivity extends BaseActivity {
    Button bt_prev, bt_next, bt_pick;
    TextView tv, res;
    DatePicker pickDate;
    TimePicker pickTime;

    String name =null;
    String pid,str_pk;
    int page_id=0;
    int year, month, day, hour, minute, second;

    boolean dateSet = false;
    boolean dateCorrect = false;

    public void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_time_date);

        tv=findViewById(R.id.tv);

        bt_next = findViewById(R.id.Bnt_next);
        bt_prev = findViewById(R.id.Bnt_prev);

        bt_pick = findViewById(R.id.btnPicker);

        pickDate = findViewById(R.id.picD);
        pickTime = findViewById(R.id.picT);

        res = findViewById(R.id.pickResult);

        //title 표시
        final Intent intent = getIntent();
        page_id= intent.getIntExtra("val",0);
        pid = intent.getStringExtra("pid");
        name = MediValues.patientData.get(pid).get("name");
        str_pk = MediValues.patientData.get(pid).get("pk");

        TextView title_pname = findViewById(R.id.p_name);
        title_pname.setText(name+" 님");

        if(page_id==0)
            tv.setText("대변을 보신 날짜/시간을 선택하고 다음 버튼을 눌러주세요");

        else if(page_id==1)
            tv.setText("소변을 보신 날짜/시간을 선택하고 다음 버튼을 눌러주세요");

        else if(page_id==2)
            tv.setText("날짜/시간을 선택하고 다음 버튼을 눌러주세요");

        res.setText(R.string.promptPick);

        // Select Time button
        bt_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDateTime();
            }
        });

        //prev(Menu page)
        bt_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(TimeDateActivity.this, MenuActivity.class);
                intent2.putExtra("val",page_id);
                intent2.putExtra("pid",pid);
                startActivity(intent2);
            }
        });

        //Next
        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!dateSet) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(TimeDateActivity.this);
                    LayoutInflater inf = TimeDateActivity.this.getLayoutInflater();

                    alertBuilder.setView(inf.inflate(R.layout.date_time_pick_alert, null));
                    alertBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                    alert.getWindow().setLayout(700, 250);
                }
                else if(!dateCorrect) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(TimeDateActivity.this);
                    LayoutInflater inf = TimeDateActivity.this.getLayoutInflater();

                    alertBuilder.setView(inf.inflate(R.layout.date_time_wrong_alert, null));
                    alertBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                    alert.getWindow().setLayout(900, 250);
                }
                //다음 페이지로 이동
                else {
                    if (page_id==0) {
                        Intent intent2 = new Intent(TimeDateActivity.this, CheckStoolActivity.class);
                        intent2.putExtra("pid", pid);
                        startActivity(intent2);
                    }
                    else if(page_id==1) {
                        Intent intent2 = new Intent(TimeDateActivity.this, CheckUrineActivity.class);
                        intent2.putExtra("pid", pid);
                        startActivity(intent2);
                    }



                }
            }
        });

    }

    public void updateDateTime() {
        year = pickDate.getYear();
        month = pickDate.getMonth() + 1; // Jan is 0
        day = pickDate.getDayOfMonth();

        hour = pickTime.getCurrentHour();
        minute = pickTime.getCurrentMinute();
        second = 0;

        res.setText("선택하신 날짜와 시간: " + String.format(Locale.KOREA, "%d년 %d월 %d일 %d시 %d분", year, month, day, hour, minute));
        MediValues.mediDate = String.format(Locale.US, "%d-%d-%d", year, month, day);
        MediValues.mediTime = String.format(Locale.US, "%d:%d:%d", hour, minute, second);

        dateSet = true;

        Calendar tmpCal = Calendar.getInstance();
        tmpCal.set(pickDate.getYear(), pickDate.getMonth(), pickDate.getDayOfMonth(),
                pickTime.getCurrentHour(), pickTime.getCurrentMinute(), 0);

        long setTime = tmpCal.getTimeInMillis();

        Calendar curCal = Calendar.getInstance();
        curCal.setTimeZone(TimeZone.getTimeZone("KST"));
        long curTime = curCal.getTimeInMillis();

        dateCorrect = curTime >= setTime;
    }
}
