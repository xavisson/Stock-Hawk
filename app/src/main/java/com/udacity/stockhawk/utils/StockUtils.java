package com.udacity.stockhawk.utils;

import com.udacity.stockhawk.data.HistoryItem;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by javidelpalacio on 18/5/17.
 */

public class StockUtils {

    public static List<HistoryItem> formatHistory(String history)
    {
        List<HistoryItem> list = new ArrayList<>();

        String[] splitedItems = history.split("\n");

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);

        for (String line : splitedItems)
        {
            String[] splitedKeyValue = line.split(",");
            String date = splitedKeyValue[0];
            float price = Float.parseFloat(formatter.format(Float.parseFloat(splitedKeyValue[1])));
            list.add(new HistoryItem(date, price));
        }

        // Sorting dates chronologically
        Collections.sort(list, new Comparator<HistoryItem>()
        {
            @Override
            public int compare(HistoryItem date2, HistoryItem date1)
            {
                return  date2.getDate().compareTo(date1.getDate());
            }
        });
        return list;
    }

    public static String formatDate(long timestamp)
    {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        return dateFormat.format(calendar.getTime());
    }
}
