package com.nightonke.saver.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.nightonke.saver.R;
import com.nightonke.saver.activity.UtcCoinApplication;
import com.nightonke.saver.model.RecordManager;
import com.nightonke.saver.model.SettingManager;
import com.nightonke.saver.util.UtcCoinUtil;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by 伟平 on 2015/10/27.
 */

public class EditRemarkFragment extends Fragment {

    private int fragmentPosition;
    private int tagId = -1;

    public MaterialEditText editView;

    private View mView;

    Activity activity;

    static public EditRemarkFragment newInstance(int position, int type) {
        EditRemarkFragment fragment = new EditRemarkFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("type", type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.edit_remark_fragment, container, false);
        editView = (MaterialEditText)mView.findViewById(R.id.remark);

        if (getArguments().getInt("type") == UtcCoinFragmentManager.MAIN_ACTIVITY_FRAGMENT) {
            UtcCoinFragmentManager.mainActivityEditRemarkFragment = this;
        } else if (getArguments().getInt("type") == UtcCoinFragmentManager.EDIT_RECORD_ACTIVITY_FRAGMENT) {
            UtcCoinFragmentManager.editRecordActivityEditRemarkFragment = this;
        }

        boolean shouldChange
                = SettingManager.getInstance().getIsMonthLimit()
                && SettingManager.getInstance().getIsColorRemind()
                && RecordManager.getCurrentMonthExpense()
                >= SettingManager.getInstance().getMonthWarning();

        setEditColor(shouldChange);

        if (getArguments().getInt("type") == UtcCoinFragmentManager.EDIT_RECORD_ACTIVITY_FRAGMENT
                && UtcCoinUtil.editRecordPosition != -1) {
            UtcCoinFragmentManager.editRecordActivityEditRemarkFragment
                    .setRemark(RecordManager.SELECTED_RECORDS.get(UtcCoinUtil.editRecordPosition).getRemark());
            UtcCoinFragmentManager.editRecordActivityEditRemarkFragment.setLastSelection();
        }

        return mView;
    }

    public interface OnTagItemSelectedListener {
        void onTagItemPicked(int position);
    }

    public void updateTags() {

    }

    public int getTagId() {
        return tagId;
    }

    public void setTag(int p) {
        tagId = RecordManager.TAGS.get(p).getId();
    }

    public String getNumberText() {
        return editView.getText().toString();
    }

    public void setNumberText(String string) {
        editView.setText(string);
    }

    public String getHelpText() {
        return editView.getHelperText();
    }

    public void setHelpText(String string) {
        editView.setHelperText(string);
    }

    public void setLastSelection() {
        editView.setSelection(editView.getText().length());
    }

    public void editRequestFocus() {
        editView.requestFocus();
        InputMethodManager keyboard = (InputMethodManager)
                UtcCoinApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(editView, InputMethodManager.SHOW_IMPLICIT);
    }

    public void setEditColor(boolean shouldChange) {
        if (shouldChange) {
            editView.setTextColor(SettingManager.getInstance().getRemindColor());
            editView.setPrimaryColor(SettingManager.getInstance().getRemindColor());
            editView.setHelperTextColor(SettingManager.getInstance().getRemindColor());
        } else {
            editView.setTextColor(UtcCoinUtil.getInstance().MY_BLUE);
            editView.setPrimaryColor(UtcCoinUtil.getInstance().MY_BLUE);
            editView.setHelperTextColor(UtcCoinUtil.getInstance().MY_BLUE);
        }
    }

    public String getRemark() {
        return editView.getText().toString();
    }

    public void setRemark(String string) {
        editView.setText(string);
    }

}
