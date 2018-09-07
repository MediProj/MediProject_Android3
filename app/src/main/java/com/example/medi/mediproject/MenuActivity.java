package com.example.medi.mediproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medi.mediproject.Login.MainActivity;

public class MenuActivity extends BaseActivity {
    TextView tv,tv_stool, tv_urine,tv_report;
    TextView bt_prev;
    String name,pid,pk;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final Intent intent=getIntent();

        //Patient Info (name/number)
        pid= intent.getStringExtra("pid");
        tv =findViewById(R.id.tv);
        tv_stool=findViewById(R.id.stool);
        tv_urine=findViewById(R.id.urine);
        tv_report =findViewById(R.id.report);
        bt_prev=findViewById(R.id.Bnt_logout);


        name= MediValues.patientData.get(pid).get("name");
        TextView title_pname = findViewById(R.id.p_name);
        title_pname.setText(name+" 님");
        //인사
        tv.setText(name + "("+pid +")" +"님 무엇을 기록하시겠습니까?\n이름,환자번호가 잘못된 경우 '나가기 버튼을 눌러주세요");

        //Stool page
        tv_stool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(MenuActivity.this,TimeDateActivity.class);
                intent2.putExtra("val",0);
                intent2.putExtra("pid", pid);
                startActivity(intent2);
            }
        });

        //Urine
        tv_urine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(MenuActivity.this,TimeDateActivity.class);
                intent2.putExtra("val",1);
                intent2.putExtra("pid", pid);
                startActivity(intent2);

            }
        });


        //Report
        tv_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(MenuActivity.this,ReportActivity.class);
                intent2.putExtra("pid", pid);
                startActivity(intent2);

            }
        });

        //Previous => logout
        bt_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(MenuActivity.this,MainActivity.class);
                Toast.makeText(getApplicationContext(),"로그아웃", Toast.LENGTH_SHORT).show();
                startActivity(intent2);
            }
        });

    }

}
