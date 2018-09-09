package com.example.medi.stoolurine;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class ReportActivity extends BaseActivity {
    ListView listView;
    ListViewAdapter listViewAdapter;
    ArrayList<ReportItem> list;
    TextView tv_report_title;
    Button bt_prev, bt_edit;
    String name, pid, pk;
    Date date;
    boolean editFlag=false;

    private RequestQueue queue;
    public static final String TAG = "ReportTAG";

    private String urlToken = "http://54.202.222.14/api-token-auth/";
    private String urlData = "http://54.202.222.14/dashboard/patients/api/patients-dashboard/";


    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_report);

        //patient info
        final Intent intent = getIntent();
        pid = MediValues.pid;
        name = MediValues.patientData.get(pid).get("name");
        pk = MediValues.patientData.get(pid).get("pk");

        queue = Volley.newRequestQueue(this);
        urlData = urlData.concat(pk);
        getPatientRecords("기록 조회 중입니다...");

        listView = findViewById(R.id.ReportList);

        TextView title_pname = findViewById(R.id.p_name);
        title_pname.setText(name + " 님");

        //오늘날짜
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        date = new Date(year, month, day);

        bt_prev = findViewById(R.id.Bnt_prev);
        bt_edit = findViewById(R.id.bt_edit);
        tv_report_title = findViewById(R.id.tv_report_date);
        list = new ArrayList<ReportItem>();
        listView = findViewById(R.id.ReportList);

        tv_report_title.setText(name + "님의 기록 입니다");

        bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediValues.patientRecord = null;
                MediValues.pkRecordTag = null;
                list.clear();

                getPatientRecords("새로고침 중입니다...");
            }
        });

        bt_prev.setText("처음으로");
        bt_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(ReportActivity.this, MenuActivity.class);
                intent2.putExtra("pid", pid);
                startActivity(intent2);
            }
        });
    }

    public void fillList() {

        for (int i =  0; i <MediValues.patientRecord.length; i++) {
            String date = MediValues.patientRecord[i].get("date");
            String time = MediValues.patientRecord[i].get("time");
            String type = MediValues.patientRecord[i].get("type");
            String amount = MediValues.patientRecord[i].get("amount");
            String record_pk = MediValues.patientRecord[i].get("record_pk");

            //소변&대변이 아니면 pass
            if(!(type.contains("Voiding") || type.contains("대변"))) {
                continue;
            }

            //대변
            if(type.contains("대변"))
                amount = "1회";

            if(type.contains("Voiding"))
                amount = String.format("%.2f", Float.parseFloat(amount));

            StringTokenizer tok_date = new StringTokenizer(date, "-");
            date = String.format("%s/%s/%s", tok_date.nextToken(), tok_date.nextToken(), tok_date.nextToken());

            StringTokenizer tok_time = new StringTokenizer(time, ":");
            time = String.format("%s시 %s분", tok_time.nextToken(), tok_time.nextToken());

            list.add(new ReportItem(record_pk, date, time, type, amount));
        }
    }

    private class ListViewAdapter extends BaseAdapter {

        private List<ReportItem> list;
        private LayoutInflater inflater;

        private ListViewAdapter(Context context, ArrayList<ReportItem> list) {
            this.list = list;
            inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;

            if (view == null) {
                view = inflater.inflate(R.layout.report_item, viewGroup, false);
                holder = new ViewHolder();
                holder.tv_time = view.findViewById(R.id.time);
                holder.tv_tag = view.findViewById(R.id.tag);
                holder.tv_val1 = view.findViewById(R.id.val1);
                holder.tv_val2 = view.findViewById(R.id.val2);
                holder.bt_del = view.findViewById(R.id.btn_delete);
                holder.bt_edit=view.findViewById(R.id.btn_edit);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tv_time.setText(list.get(i).getDate());
            holder.tv_tag.setText(list.get(i).getTag());
            holder.tv_val1.setText(list.get(i).getVal1());
            holder.tv_val2.setText(list.get(i).getVal2());

            holder.bt_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditAlertDialog editAlertDialog = new EditAlertDialog();
                    editAlertDialog.showDialog(ReportActivity.this, i);
                }
            });

            holder.bt_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeleteAlertDialog delAlert = new DeleteAlertDialog();
                    delAlert.showDialog(ReportActivity.this, i);
                }
            });
            return view;
        }
    }

    public class DeleteAlertDialog {

        public void showDialog(Activity activity, final int index) {
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
                    MediDeleteRequest delRecord = new MediDeleteRequest(list.get(index).getRecordPk(), getApplicationContext());

                    dialog.dismiss();
                    final ProgressDialog progress = new ProgressDialog(ReportActivity.this);
                    progress.setTitle("로딩중");
                    progress.setMessage("기록 삭제 중입니다...");
                    progress.setCancelable(false);
                    progress.show();

                    MediValues.patientRecord = null;
                    MediValues.pkRecordTag = null;
                    list.clear();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            getPatientRecords("해당 기록이 삭제되었습니다");
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

    public class EditAlertDialog {

        public void showDialog(Activity activity, final int index) {
            final Dialog dialog = new Dialog(activity);
            dialog.setCancelable(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.record_delete_prompt);

            TextView tv=dialog.findViewById(R.id.tv);
            tv.setText("기록을 수정하시겠습니까?\n한 번 수정하면 복구가 불가능합니다");

            Button delCancel = (Button) dialog.findViewById(R.id.delCancel);
            Button delConfirm = (Button) dialog.findViewById(R.id.delConfirm);
            delConfirm.setText("수정");

            delCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editFlag=false;
                    dialog.dismiss();
                }
            });

            delConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediDeleteRequest delRecord = new MediDeleteRequest(list.get(index).getRecordPk(), getApplicationContext());
                    MediValues.patientRecord = null;
                    MediValues.pkRecordTag = null;
                    editFlag=true;
                    dialog.dismiss();

                    int type =2;

                    if(list.get(index).getVal1().contains("Voiding"))
                        type =3;

                    Intent intent = new Intent(ReportActivity.this,TimeDateActivity.class );
                    intent.putExtra("val", type);
                    startActivity(intent);
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
    public class ViewHolder {
        TextView tv_time, tv_tag, tv_val1, tv_val2;
        Button bt_del, bt_edit;
    }

    protected void getPatientRecords(String msg) {
        MediGetRequest GETrequest = new MediGetRequest(pk, "records", this);
        final ProgressDialog progress = new ProgressDialog(this);

        progress.setTitle("로딩중");
        progress.setMessage(msg);
        progress.setCancelable(false);
        progress.show();

        // 기록 조회를 위해 5초 기다림
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progress.dismiss();
                fillList();
                listViewAdapter = new ListViewAdapter(getApplicationContext(), list);
                listView.setAdapter(listViewAdapter);
            }
        }, 2000);
    }
}
