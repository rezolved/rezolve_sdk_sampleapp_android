package com.rezolve.sdk_sample.sspact;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.utils.CheckoutUtils;
import com.rezolve.sdk_sample.utils.ProductUtils;

public class SspActActivity extends AppCompatActivity {

    private SspAct sspAct;

    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssp_act);

        // Bind views
        recyclerView = findViewById(R.id.page_building_recycler);
        sspAct = ProductUtils.getSspActFromArgs(getIntent().getExtras());

        displayActDetails();
    }

    private void displayActDetails() {

    }
}
