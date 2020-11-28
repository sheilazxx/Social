package com.mhy.socialcommon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;


/**
 * 支付基类
 */
public abstract class PayApi {

    protected Activity mAct;
    //只允许一个实例化回调
    protected static OnPayListener mPayResultListener;

    protected static SocialType mPayType;


    public PayApi(Activity act,OnPayListener l){
        mAct = act;
        setOnPayListener(l);
    }
//    public PayApi(Activity act){
//        mAct = act;
//        setOnPayListener(null);
//    }

    /**
     * 调用支付sdk
     * @param payInfo 支付sdk
     */
    public abstract void doPay(PayContent payInfo);


    /**
     * 设置支付回调
     * @param l
     */
    protected void setOnPayListener(OnPayListener l){
        mPayResultListener = l;
    };

    /**
     * 返回支付成功
     */
    public static void callbackPayOk(){
        if(mPayResultListener != null){
            mPayResultListener.onPayOk(mPayType);
        }
    }

    /**
     * 返回支付失败
     */
    public static void callbackPayFail(String msg){
        if(mPayResultListener != null){
            mPayResultListener.onPayFail(mPayType, msg);
        }
    }

    /**
     * 支付回调
     */
    public interface OnPayListener {

        /**
         * 支付回调-成功支付
         */
        void onPayOk( SocialType type);

        /**
         * 支付回调-支付失败
         */
        void onPayFail( SocialType type, String msg);

    }


}
