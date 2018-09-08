package com.example.medi.stoolurine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StoolActivity extends BaseActivity {

    Button bt_prev, bt_next;
    TextView tv1, tv2;
    String pid,name,time;
    float amount;
    int user_pk, stool_num=0;

    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_stool);

        bt_next = findViewById(R.id.Bnt_next);
        bt_prev = findViewById(R.id.Bnt_prev);
        tv1 = findViewById(R.id.tv1);
        tv2=findViewById(R.id.tv2);

        final Intent intent = getIntent();
        pid=intent.getStringExtra("pid");
        String stool_type = intent.getStringExtra("type");

        name = MediValues.patientData.get(pid).get("name");
        TextView title_pname = findViewById(R.id.p_name);
        title_pname.setText(name+" 님");

        tv2.setText(stool_type + " 대변 1회를 기록합니다");

        //next bnt 텍스트 수전
        bt_next.setText("등록");

        bt_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(StoolActivity.this, CheckStoolActivity.class);
                intent2.putExtra("val", 0);
                intent2.putExtra("pid",pid);
                startActivity(intent2);
            }
        });

        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(StoolActivity.this, ReportActivity.class);
                MediPostRequest postRequest = new MediPostRequest(view.getContext(), pid, name,MediValues.OUTPUT, MediValues.STOOL, 1.0f, time );
                Toast.makeText(getApplicationContext(),"성공적으로 등록되었습니다", Toast.LENGTH_LONG).show();
                intent2.putExtra("pid",pid);
                startActivity(intent2);
            }
        });

    }

}
