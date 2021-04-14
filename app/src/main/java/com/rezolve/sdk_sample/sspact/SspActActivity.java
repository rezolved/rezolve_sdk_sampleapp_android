package com.rezolve.sdk_sample.sspact;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.rezolve.sdk.ssp.model.PageBuildingBlock;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk.ssp.model.SspActAnswerDate;
import com.rezolve.sdk.ssp.model.form.SelectionOption;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.utils.ProductUtils;

import java.util.ArrayList;
import java.util.List;

public class SspActActivity extends AppCompatActivity implements SspActBlockEventListener,
        DatePickerWithDateConditionsFragment.DatePickerWithDateConditionsListener {

    private SspAct sspAct;
    private List<BlockWrapper> blocks;

    private RecyclerView recyclerView;
    private SspActBlockAdapter adapter;

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
        adapter = new SspActBlockAdapter();
        adapter.submitList(blocks);
        adapter.setEventListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDateBlockSelected(BlockWrapper blockWrapper, int position) {
        setDatePickerFragment(blockWrapper);
    }

    @Override
    public void onSelectBlockOptionSelected(BlockWrapper blockWrapper, SelectionOption selectionOption) {

    }

    @Override
    public void onTextInputBlockChange(BlockWrapper blockWrapper, String text) {

    }

    private void setDatePickerFragment(BlockWrapper blockWrapper) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, DatePickerWithDateConditionsFragment.getInstance(blockWrapper.block)).commit();
    }

    @Override
    public void onDateSelected(SspActAnswerDate answer) {
        for (int i = 0; i < blocks.size(); i++) {
            BlockWrapper blockWrapper = blocks.get(i);
            if (answer.getQuestionId().equals(blockWrapper.block.getId())) {
                blockWrapper.answerToDisplay = answer.getAnswer();
                blocks.set(i, blockWrapper);
                adapter.notifyItemChanged(i);
                break;
            }
        }
        onPickerClose();
    }

    @Override
    public void onPickerClose() {
        removeChildFragment();
    }

    public void removeChildFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment != null) {
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commit();
    }
}
