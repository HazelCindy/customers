package com.grace.customer.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.grace.customer.R;

public class Utils {
    public static MaterialDialog configureDialog(Context context, String title, String content, String positive, MaterialDialog.SingleButtonCallback callback){
        if(callback == null){
            callback = new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            };
        }
        MaterialDialog dialog = new  MaterialDialog.Builder(context)
                .title(title)
                .widgetColorRes(R.color.colorPrimary)
                .content(content)
                .positiveText(positive)
                .negativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .autoDismiss(true)
                .onPositive(callback)
                .build();
        return dialog;
    }
    public static MaterialDialog configureProgessDialog(Context context,String title, String message){
        MaterialDialog dialog=new MaterialDialog.Builder(context)
                .title(title)
                .cancelable(false)
                .titleGravity(GravityEnum.CENTER)
                .widgetColorRes(R.color.colorPrimary)
                .customView(R.layout.progress_dialog, true)
                .build();
        View view=dialog.getCustomView();
        TextView messageText=(TextView)view.findViewById(R.id.message);
        messageText.setText(message);
        return dialog;
    }
}