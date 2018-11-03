package com.nightonke.saver.util;

import android.graphics.Color;

import com.github.johnpersano.supertoasts.SuperToast;
import com.nightonke.saver.activity.UtcCoinApplication;

/**
 * Created by Weiping on 2015/11/30.
 */
public class UtcCoinToast {
    private static UtcCoinToast ourInstance = new UtcCoinToast();

    public static UtcCoinToast getInstance() {
        return ourInstance;
    }

    private UtcCoinToast() {
    }

    public void showToast(int text, int color) {
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(UtcCoinApplication.getAppContext());
        superToast.setAnimations(UtcCoinUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(UtcCoinApplication.getAppContext().getResources().getString(text));
        superToast.setBackground(color);
        superToast.getTextView().setTypeface(UtcCoinUtil.typefaceLatoLight);
        superToast.show();
    }

    public void showToast(String text, int color) {
        SuperToast.cancelAllSuperToasts();
        SuperToast superToast = new SuperToast(UtcCoinApplication.getAppContext());
        superToast.setAnimations(UtcCoinUtil.TOAST_ANIMATION);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setTextColor(Color.parseColor("#ffffff"));
        superToast.setTextSize(SuperToast.TextSize.SMALL);
        superToast.setText(text);
        superToast.setBackground(color);
        superToast.getTextView().setTypeface(UtcCoinUtil.typefaceLatoLight);
        superToast.show();
    }
}
