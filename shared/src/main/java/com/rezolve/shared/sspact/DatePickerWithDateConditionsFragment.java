package com.rezolve.shared.sspact;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rezolve.sdk.ssp.model.PageBuildingBlock;
import com.rezolve.shared.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DatePickerWithDateConditionsFragment extends Fragment implements View.OnClickListener, DatePicker.OnDateChangedListener {

    private final static String ARG_PAGE_BUILDING_BLOCK = "page.building.block";
    public final static String DATE_HH_MM_SUFFIX = " 00:00";

    private PageBuildingBlock pageBuildingBlock;
    private DatePickerWithDateConditionsListener listener;
    private DatePicker datePicker;

    public static DatePickerWithDateConditionsFragment getInstance(PageBuildingBlock block) {
        DatePickerWithDateConditionsFragment fragment = new DatePickerWithDateConditionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PAGE_BUILDING_BLOCK, block.entityToJson().toString());
        fragment.setArguments(bundle);
        return fragment;
    }

    interface DatePickerWithDateConditionsListener {
        void onDateSelected(PageBuildingBlock block, String answer);
        void onPickerClose();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_custom_date_picker, container, false);
        rootView.findViewById(R.id.custom_date_picker_close).setOnClickListener(this);

        try {
            pageBuildingBlock = PageBuildingBlock.jsonToEntity(new JSONObject(getArguments().getString(ARG_PAGE_BUILDING_BLOCK)));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        datePicker = rootView.findViewById(R.id.product_option_custom_date_picker);

        ((TextView)rootView.findViewById(R.id.custom_date_picker_title)).setText(pageBuildingBlock.getData().getText());

        if (pageBuildingBlock.getData().getDateConditions() != null) {
            Calendar today = Calendar.getInstance();
            Calendar start = stringDateToCalendar(pageBuildingBlock.getData().getDateConditions().getStartDateLimit());
            Calendar end = stringDateToCalendar(pageBuildingBlock.getData().getDateConditions().getEndDateLimit());

            if (end != null && today.after(end)) {
                Toast.makeText(getContext(), getString(R.string.ssp_act_event_inactive), Toast.LENGTH_SHORT);
            } else {
                rootView.findViewById(R.id.custom_date_picker_done).setOnClickListener(this);

                if (pageBuildingBlock.getData().getDateConditions().onlyShowDaysInFuture() && (start == null || today.after(start))) {
                    start = today;
                }

                if (start != null && today.before(start)) {
                    today = start;
                }

                datePicker.init(
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH),
                        this
                );

                if (start != null) {
                    datePicker.setMinDate(start.getTimeInMillis());
                }

                if (end != null) {
                    datePicker.setMaxDate(end.getTimeInMillis());
                }
            }
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getActivity() instanceof DatePickerWithDateConditionsListener) {
            listener = (DatePickerWithDateConditionsListener)getActivity();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.custom_date_picker_close) {
            listener.onPickerClose();
        } else if (id == R.id.custom_date_picker_done) {
            String result = String.format("%02d", datePicker.getDayOfMonth()).concat("/").concat(String.format("%02d", datePicker.getMonth() + 1)).concat("/").concat(String.valueOf(datePicker.getYear())).concat(DATE_HH_MM_SUFFIX);
            listener.onDateSelected(pageBuildingBlock, result);
        }
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {

    }

    private Calendar stringDateToCalendar(String value) {
        if (value != null) {
            try {
                Date date = ISO_8601_24H_FULL_FORMAT.get().parse(value);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private ThreadLocal<SimpleDateFormat> ISO_8601_24H_FULL_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Nullable
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format;
        }
    };
}
