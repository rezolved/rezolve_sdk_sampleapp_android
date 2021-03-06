package com.rezolve.shared.sspact;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.rezolve.sdk.location.LocationHelper;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.ssp.interfaces.SspSubmitActDataInterface;
import com.rezolve.sdk.ssp.model.PageBuildingBlock;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk.ssp.model.SspActAnswer;
import com.rezolve.sdk.ssp.model.SspActSubmission;
import com.rezolve.sdk.ssp.model.SspActSubmissionResponse;
import com.rezolve.sdk.ssp.model.form.SelectionOption;
import com.rezolve.sdk.ssp.model.form.Type;
import com.rezolve.shared.BuyView;
import com.rezolve.shared.R;
import com.rezolve.shared.SspActManagerProvider;
import com.rezolve.shared.authentication.AuthenticationService;
import com.rezolve.shared.authentication.AuthenticationServiceProvider;
import com.rezolve.shared.utils.ProductUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SspActActivity extends AppCompatActivity implements SspActBlockEventListener,
        DatePickerWithDateConditionsFragment.DatePickerWithDateConditionsListener,
        BuyView.SlideToBuyListener {

    private SspAct sspAct;
    private List<BlockWrapper> blocks;

    private RecyclerView recyclerView;
    private SspActBlockAdapter adapter;
    private BuyView buyView;

    private final AuthenticationService authenticationService = AuthenticationServiceProvider.getAuthenticationService();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssp_act);

        // Bind views
        recyclerView = findViewById(R.id.page_building_recycler);
        sspAct = ProductUtils.getSspActFromArgs(getIntent().getExtras());

        displayActDetails();
        prepareBuyView(sspAct);
    }

    private void prepareBuyView(SspAct sspAct) {
        buyView = new BuyView(
                findViewById(R.id.slider_container),
                this
        );
        buyView.setVisible(!sspAct.isInformationPage());
        buyView.setEnabled(!sspAct.isInformationPage());
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
        updateAnswerForBlock(blockWrapper.block, selectionOption.getDescription(), true);
    }

    @Override
    public void onTextInputBlockChange(BlockWrapper blockWrapper, String text) {
        updateAnswerForBlock(blockWrapper.block, text, false);
    }

    private void setDatePickerFragment(BlockWrapper blockWrapper) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, DatePickerWithDateConditionsFragment.getInstance(blockWrapper.block)).commit();
    }

    @Override
    public void onDateSelected(PageBuildingBlock block, String answer) {
        updateAnswerForBlock(block, answer, true);
        onPickerClose();
    }

    private void updateAnswerForBlock(PageBuildingBlock block, String answer, boolean updateAdapter) {
        boolean allRequiredOptionsSelected = true;
        for (int i = 0; i < blocks.size(); i++) {
            BlockWrapper blockWrapper = blocks.get(i);
            if (block.getId().equals(blockWrapper.block.getId())) {
                blockWrapper.answerToDisplay = answer;
                blocks.set(i, blockWrapper);
                if (updateAdapter) {
                    adapter.notifyItemChanged(i);
                }
            }

            if (allRequiredOptionsSelected && blockWrapper.block.isRequired() && TextUtils.isEmpty(blockWrapper.answerToDisplay)) {
                allRequiredOptionsSelected = false;
            }
        }

        buyView.setEnabled(allRequiredOptionsSelected);
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

    @Override
    public void onSlideToBuySuccess() {
        submitAnswer(assembleSubmission());
    }

    private List<SspActAnswer> getAnswers() {
        List<SspActAnswer> answers = new ArrayList<>();
        for (BlockWrapper blockWrapper : blocks) {
            if (blockWrapper.block.getSspActQuestion() != null) {
                String answer = getOptionIdForAnswer(blockWrapper.block, blockWrapper.answerToDisplay);
                if (answer != null) {
                    answers.add(blockWrapper.block.getSspActQuestion().answer(answer));
                }
            }
        }
        return answers;
    }

    private String getOptionIdForAnswer(PageBuildingBlock question, String answer) {
        if (question.getType() == Type.SELECT) {
            for(SelectionOption selectionOption : question.getData().getSelectionOptions()) {
                if (selectionOption.getDescription().equals(answer)) {
                    return String.valueOf(selectionOption.getValue());
                }
            }
        } else {
            return answer;
        }
        return null;
    }

    private SspActSubmission assembleSubmission() {
        return new SspActSubmission.Builder()
                .setAnswers(getAnswers())
                .setEmail("test@example.com")
                .setFirstName("Tester")
                .setLastName("Testman")
                .setLocation(LocationHelper.getInstance(this).getLastKnownLocation().getRezolveLocation())
                .setPersonTitle("Sir")
                .setPhone("+447400258461")
                .setServiceId(sspAct.getServiceId())
                .setUserId(UUID.randomUUID().toString()) // in production application UserId should be static
                .setEntityId(authenticationService.getEntityId())
                .setUserName("TesterTestman")
                .build();
    }

    private void submitAnswer(SspActSubmission submission) {
        ((SspActManagerProvider)getApplicationContext()).getSspActManager().submitAnswer(sspAct.getId(), submission, new SspSubmitActDataInterface() {
            @Override
            public void onSubmitActDataSuccess(SspActSubmissionResponse response) {
                Toast.makeText(SspActActivity.this, getString(R.string.submission_success), Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            @Override
            public void onError(@NonNull RezolveError error) {
                Toast.makeText(SspActActivity.this, getString(R.string.failed_to_submit), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
