package com.example.dimitris.blinkdetector;

import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private XYPlot plot;

    private TextView mNumTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plotting();

        if (savedInstanceState == null) {
            Log.e(TAG,"fragment creation");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothFragment fragment = new BluetoothFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        mNumTextView = (TextView) findViewById(R.id.numTextView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void plotting (){
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.graph);

        // create a couple arrays of y-values to plot:
        final Number[] domainLabels = {1, 2, 3, 6, 7, 8, 9, 10, 13, 14};
        Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");

        plot.addSeries(series1, new LineAndPointFormatter(Color.GREEN, Color.GREEN, null, null));
    }

}
