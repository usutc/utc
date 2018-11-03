package com.nightonke.saver.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.nightonke.saver.R;
import com.nightonke.saver.activity.UtcCoinApplication;
import com.nightonke.saver.fragment.UtcCoinFragmentManager;
import com.nightonke.saver.util.UtcCoinUtil;

/**
 * Created by Weiping on 2016/1/23.
 */
public class CustomTitleSliderView extends BaseSliderView {
    private static Typeface font = null;
    private String content;
    private int type;
    private TextView title;

    public CustomTitleSliderView(String content, int type) {
        super(UtcCoinApplication.getAppContext());
        this.content = content;
        this.type = type;
        if (type == UtcCoinFragmentManager.NUMBER_SLIDER) {
            UtcCoinFragmentManager.numberCustomTitleSliderView = this;
        } else if (type == UtcCoinFragmentManager.EXPENSE_SLIDER) {
            UtcCoinFragmentManager.expenseCustomTitleSliderView = this;
        }
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.custom_title_slider_view,null);

        LinearLayout description = (LinearLayout)v.findViewById(R.id.description_layout);
        description.setVisibility(View.GONE);

        title = (TextView)v.findViewById(R.id.title);
        title.setText(content);
        title.setTypeface(UtcCoinUtil.typefaceLatoLight);

        ImageView target = (ImageView)v.findViewById(R.id.daimajia_slider_image);
        bindEventAndShow(v, target);
        return v;
    }

    public void setTitle(String string) {
        title.setText(string);
    }
}
