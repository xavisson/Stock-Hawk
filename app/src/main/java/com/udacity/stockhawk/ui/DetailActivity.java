package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.HistoryItem;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.udacity.stockhawk.utils.StockUtils.formatDate;
import static com.udacity.stockhawk.utils.StockUtils.formatHistory;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int GRAPH_STOCK_LOADER = 10;
    private static final String STATE_SYMBOL = "stateSymbol";
    private static final String STATE_HISTORY = "stateHistory";

    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;

    private String symbol = "";
    private String history;
    private float rawAbsoluteChange = 0;
    private float rawPercentageChange = 0;

    private List<HistoryItem> historyItemList = new ArrayList<>();

    @BindView(R.id.symbol)
    TextView graphTitle;
    @BindView(R.id.price)
    TextView priceTextView;
    @BindView(R.id.change)
    TextView changeTextView;
    @BindView(R.id.stock_chart)
    LineChart stockChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {

            symbol = savedInstanceState.getString(STATE_SYMBOL);
            history = savedInstanceState.getString(STATE_HISTORY);

            historyItemList = formatHistory(history);
            drawChart();

        } else {

            if (getIntent().hasExtra("symbol"))
                symbol = getIntent().getStringExtra("symbol");

            getSupportLoaderManager().initLoader(GRAPH_STOCK_LOADER, null, this);
        }


        graphTitle.setText(symbol);

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = Contract.Quote.COLUMN_SYMBOL + "=?";
        String [] selectionArgs = new String[] { symbol };

        return new CursorLoader(
                this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst())
        {
            float price = data.getFloat(Contract.Quote.POSITION_PRICE);
            priceTextView.setText(dollarFormat.format(price));

            rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            rawPercentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                changeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                changeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            updateChange();
            history = data.getString(Contract.Quote.POSITION_HISTORY);
            historyItemList = formatHistory(history);
            drawChart();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void drawChart() {

        List<Entry> entries = new ArrayList<>();
        final String [] formattedDates = new String[historyItemList.size()];

        int i = 0;
        for (HistoryItem item : historyItemList) {
            formattedDates[i] = formatDate(Long.parseLong(item.getDate()));
            entries.add(new Entry(i, item.getPrice()));
            i++;
        }

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis)
            {
                return formattedDates[(int) value];
            }
        };

        XAxis xAxis = stockChart.getXAxis();
        xAxis.setValueFormatter(formatter);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);

        YAxis yAxis = stockChart.getAxis(YAxis.AxisDependency.LEFT);
        yAxis.setTextSize(12f);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(true);

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.graph_legend));
        dataSet.setColor(Color.WHITE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleColorHole(Color.WHITE);
        dataSet.setCircleSize(3f);

        Legend legend = stockChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);

        stockChart.setDescription(null);

        LineData lineData = new LineData(dataSet);
        stockChart.setAutoScaleMinMaxEnabled(true);
        stockChart.setData(lineData);
        stockChart.invalidate(); // refresh
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    private void updateChange() {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            changeTextView.setText(dollarFormatWithPlus.format(rawAbsoluteChange));
        } else {
            changeTextView.setText(percentageFormat.format(rawPercentageChange));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            updateChange();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(STATE_SYMBOL, symbol);
        outState.putString(STATE_HISTORY, history);

        super.onSaveInstanceState(outState);
    }
}
