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
import android.widget.ImageView;
import android.widget.TextView;

import com.nightonke.saver.R;
import com.nightonke.saver.activity.UtcCoinApplication;
import com.nightonke.saver.model.RecordManager;
import com.nightonke.saver.model.SettingManager;
import com.nightonke.saver.util.UtcCoinUtil;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by 伟平 on 2015/10/27.
 */

public class EditRecordFragment extends Fragment {

    private int fragmentPosition;
    private int tagId = -1;

    public MaterialEditText editView;
    public MaterialEditText remarkEditView;

    public ImageView tagImage;
    public TextView tagName;

    private View mView;

    Activity activity;

    static public EditRecordFragment newInstance(int position) {
        EditRecordFragment fragment = new EditRecordFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.edit_money_fragment, container, false);

        fragmentPosition = getArguments().getInt("position");
        remarkEditView = (MaterialEditText) mView.findViewById(R.id.remark);
        editView = (MaterialEditText) mView.findViewById(R.id.money);
        tagImage = (ImageView) mView.findViewById(R.id.tag_image);
        tagName = (TextView) mView.findViewById(R.id.tag_name);
        tagName.setTypeface(UtcCoinUtil.typefaceLatoLight);

        if (fragmentPosition == 0) {
            editView.setTypeface(UtcCoinUtil.typefaceLatoHairline);
            editView.setText("" + (int) RecordManager.RECORDS.get(UtcCoinUtil.editRecordPosition).getMoney());
            editView.requestFocus();
            editView.setHelperText(UtcCoinUtil.FLOATINGLABELS[editView.getText().toString().length()]);

            tagId = RecordManager.RECORDS.get(UtcCoinUtil.editRecordPosition).getTag();
            tagName.setText(UtcCoinUtil.GetTagName(tagId));
            tagImage.setImageResource(UtcCoinUtil.GetTagIcon(tagId));

            remarkEditView.setVisibility(View.GONE);
        } else {
            remarkEditView.setTypeface(UtcCoinUtil.GetTypeface());

            remarkEditView.setText(RecordManager.RECORDS.get(UtcCoinUtil.editRecordPosition).getRemark());
            int pos = remarkEditView.getText().length();
            remarkEditView.setSelection(pos);

            editView.setVisibility(View.GONE);
        }

        boolean shouldChange
                = SettingManager.getInstance().getIsMonthLimit()
                && SettingManager.getInstance().getIsColorRemind()
                && RecordManager.getCurrentMonthExpense()
                >= SettingManager.getInstance().getMonthWarning();

        setEditColor(shouldChange);

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
        tagName.setText(UtcCoinUtil.GetTagName(RecordManager.TAGS.get(p).getId()));
        tagImage.setImageResource(UtcCoinUtil.GetTagIcon(RecordManager.TAGS.get(p).getId()));
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

    public void editRequestFocus() {
        if (fragmentPosition == 0) {
            editView.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                    UtcCoinApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        } else {
            remarkEditView.requestFocus();
            InputMethodManager keyboard = (InputMethodManager)
                    UtcCoinApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(remarkEditView, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    public void setEditColor(boolean shouldChange) {
        if (shouldChange) {
            editView.setTextColor(SettingManager.getInstance().getRemindColor());
            editView.setPrimaryColor(SettingManager.getInstance().getRemindColor());
            editView.setHelperTextColor(SettingManager.getInstance().getRemindColor());
            remarkEditView.setTextColor(SettingManager.getInstance().getRemindColor());
            remarkEditView.setPrimaryColor(SettingManager.getInstance().getRemindColor());
            remarkEditView.setHelperTextColor(SettingManager.getInstance().getRemindColor());
        } else {
            editView.setTextColor(UtcCoinUtil.MY_BLUE);
            editView.setPrimaryColor(UtcCoinUtil.MY_BLUE);
            editView.setHelperTextColor(UtcCoinUtil.MY_BLUE);
            remarkEditView.setTextColor(UtcCoinUtil.MY_BLUE);
            remarkEditView.setPrimaryColor(UtcCoinUtil.MY_BLUE);
            remarkEditView.setHelperTextColor(UtcCoinUtil.MY_BLUE);
        }
    }

    public void setTagName(String name) {
        tagName.setText(name);
    }

    public void setTagImage(int resource) {
        tagImage.setImageResource(resource);
    }

    public String getRemark() {
        return remarkEditView.getText().toString();
    }

    public void setRemark(String string) {
        remarkEditView.setText(string);
    }

}
