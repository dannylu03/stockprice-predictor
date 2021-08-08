package com.robert.stockpricepredictor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // store important UI elements globally
    private AutoCompleteTextView autocompleteStockName;
    private TextView logText;

    private ArrayList<String> stockNames = new ArrayList<>();

    private String stockNameSelected;

    private String finnhubApiToken = "c46vqk2ad3iagvmhh27g";
    private final String URL_FINNHUB_SYMBOL_LOOKUP_BASE = "https://finnhub.io/api/v1/stock/symbol?exchange=US";
    private final String URL_FINNHUB_TOKEN_ARG = "&token=";
    private String URL_FINNHUB;

    private String jsonData;

    private class Stock {
        String currency;
        String description;
        String displaySymbol;
        String symbol;
        String type;
        String mic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Gson gson = new Gson();

        // get important UI elements, store them globally
        autocompleteStockName = findViewById(R.id.autocompleteStockName);
        logText = findViewById(R.id.loggingTextView);

        // Setting up the autocomplete field for the stock names input box
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, stockNames);
        autocompleteStockName.setThreshold(1);
        autocompleteStockName.setCompletionHint("Enter a stock name");
        autocompleteStockName.setHint("Enter a stock name");

        Thread thread = new Thread(() -> {
            URL_FINNHUB = URL_FINNHUB_SYMBOL_LOOKUP_BASE + URL_FINNHUB_TOKEN_ARG + finnhubApiToken;
            try {
                jsonData = readUrl(URL_FINNHUB);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Stock[] exchangeStocks = gson.fromJson(jsonData, Stock[].class);
            for (Stock stock : exchangeStocks) {
                stockNames.add(stock.symbol);
                Log.d(null, stock.symbol);
            }
        });
        thread.start();
        try {
            thread.join();
            adapter.notifyDataSetChanged();
        } catch (InterruptedException e) {
            Log.d(null, e.toString());
        }

        autocompleteStockName.setAdapter(adapter);
    }

    private String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public void submitStockInfo(View view) {

        stockNameSelected = autocompleteStockName.getText().toString();

        if(stockNames.contains(stockNameSelected)) {
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody formbody = new FormBody.Builder().add("stock",stockNameSelected)
                    .build();
            Request request = new Request.Builder().url("").post(formbody).build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    TextView textView = findViewById((R.id.textViewPrice));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                textView.setText((response.body().string()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                }
            });

        }
        else {
            logText.setText("Error: Not a valid ticker symbol!");
        }
    }

    public void clearInfo(View view) {
        autocompleteStockName.setText("");
        logText.setText("");
    }
}