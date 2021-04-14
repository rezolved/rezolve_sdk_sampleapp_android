package com.rezolve.sdk_sample.sspact;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.rezolve.sdk.ssp.model.PageBuildingBlock;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.utils.ProductUtils;

import java.util.ArrayList;
import java.util.List;

public class SspActActivity extends AppCompatActivity {

    private SspAct sspAct;
    private List<BlockWrapper> blocks;

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
        blocks = new ArrayList<>();
        for (PageBuildingBlock block : sspAct.getPageBuildingBlocks()) {
            blocks.add(new BlockWrapper(block));
        }
        SspActBlockAdapter adapter = new SspActBlockAdapter();
        adapter.submitList(blocks);
        recyclerView.setAdapter(adapter);
    }
}
