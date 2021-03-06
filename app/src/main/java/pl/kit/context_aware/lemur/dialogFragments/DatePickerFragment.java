package pl.kit.context_aware.lemur.dialogFragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

import pl.kit.context_aware.lemur.R;

/**
 * Created by Tomek on 2017-04-27.
 */

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private int day;
    private int month;
    private int year;
    private int position; //position on the list in EditScript activity

    /**
     * Setters and getters
     */
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public int getMonth() {
        return month;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Inference used to communication between this dialog and activity in which it was called
     */
    public interface NoticeDialogDPFListener {
        public void onDialogDPFPositiveClick(DialogFragment dialog);
    }
    private NoticeDialogDPFListener mListener; //Object of inner inference used to communicate between dialog and activity

    /**
     * Method needed for communication between activity and dialog
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogDPFListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogDPFListener");
        }
    }

    /**
     * Method building dialog fragment
     * @param savedInstanceState
     * @return created fragment
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DatePickerDialog dpf = new DatePickerDialog(getActivity(), this, year, month-1, day);
        dpf.setTitle(this.getResources().getString(R.string.es_SetDate));
        return dpf;
    }

    /**
     * Method called when date selected
     * @param view current view
     * @param year selected year
     * @param month selected month
     * @param day selected day
     */
    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.year = year;
        this.month = month+1;
        this.day = day;
        mListener.onDialogDPFPositiveClick(DatePickerFragment.this);
    }
}