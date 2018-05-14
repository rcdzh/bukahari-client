package com.estimote.notification;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MyApplication application = (MyApplication) getApplication();

        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(this,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d("app", "requirements fulfilled");
                                application.enableBeaconNotifications();
                                return null;
                            }
                        },
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                Log.e("app", "requirements missing: " + requirements);
                                return null;
                            }
                        },
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "requirements error: " + throwable);
                                return null;
                            }
                        });

        generateBarChart(null);
    }

    public void recordDatetime(View view) {
        EditText inDate = findViewById(R.id.in_date);
        String dateStr = inDate.getText().toString();
        EditText inTime = findViewById(R.id.in_time);
        String timeStr = inTime.getText().toString() + ":00";

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            Date date = df.parse(dateStr + " " + timeStr);
            MyRemoteDBHandler req = new MyRemoteDBHandler(
//                "http://bukahari.test/api/attendance",
                "http://bukahari.000webhostapp.com/api/attendance",
                "nashr3",
                date
            );
            req.execute("POST");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void generateBarChart(View view) {
        Long defaultFrom = new GregorianCalendar(2018, 0, 1).getTime().getTime() / 1000;
        Long defaultTo = new GregorianCalendar(2018, 0, 7).getTime().getTime() / 1000;
        MyRemoteDBHandler req = new MyRemoteDBHandler(
            "http://bukahari.test/api/attendance?username=nashr3&from="
                + defaultFrom
                + "&to="
                + defaultTo
//            "http://bukahari.000webhostapp.com/api/attendance"
        );
        req.execute("GET");

        BarChart chart = findViewById(R.id.chart);
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        JSONArray array = new JSONArray();
        try {
            JSONObject item = new JSONObject();
            item.put("date", "2018-01-01");
            item.put("checked_in_at", "11:00");
            item.put("checked_out_at", "21:00");
            array.put(item);

            item = new JSONObject();
            item.put("date", "2018-01-02");
            item.put("checked_in_at", "10:31");
            item.put("checked_out_at", "21:34");
            array.put(item);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<BarEntry> entries = new ArrayList<BarEntry>();
        for (int i = 0; i < array.length(); i++) {
            entries.add(new BarEntry(i, (i+1)*12));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Label Blabla");
        BarData lineData = new BarData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }
}
