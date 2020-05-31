package com.mhy.socialcommon;

import android.app.Activity;
import android.content.Intent;

/**
 * 分享平台公共组件模块
 */
public abstract class ShareApi {

    protected Activity mActivity;

    private static int mShareType;
    private String mInfo;
    protected static OnShareListener mShareListener;

    public ShareApi(Activity act,int t, OnShareListener l) {
        mActivity = act;
        setOnShareListener(l);
        setShareType(t);
    }
    public void setInfo(String orInfo) {
        this.mInfo = orInfo;
    }

    public String getInfo() {
        return mInfo;
    }
    protected void setShareType(int shareType){
        mShareType = shareType;
    }
    protected int getShareType() {
        return mShareType;
    }
    public abstract void doShare(ShareEntity content);


    /**
     * 应用分享成功回调
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    }

    /**
     * 设置分享回调
     * @param l l
     */
    public void setOnShareListener(OnShareListener l){
        mShareListener = l;
    }

    /**
     * 返回分享成功
     */
    public static void callbackShareOk(){
        if(mShareListener != null){
            mShareListener.onShareOk(mShareType);
        }
    }

    /**
     * 返回分享失败
     * @param msg 错误详情
     */
    public static void callbackShareFail(String msg){
        if(mShareListener != null){
            mShareListener.onShareFail(mShareType, msg);
        }
    }

    /**
     * 分享回调
     */
    public interface OnShareListener {

        /**
         * 分享回调-成功分享
         */
        void onShareOk(int type);

        /**
         * 分享回调-支付分享
         */
        void onShareFail(int type, String msg);
    }
}
