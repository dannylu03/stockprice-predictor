package com.robert.stockpricepredictor;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class PriceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstance) {
        Intent intent = getIntent();
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_price);


    }
}
