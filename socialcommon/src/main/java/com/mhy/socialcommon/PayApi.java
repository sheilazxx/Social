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


/**
 * 支付基类
 */
public abstract class PayApi {

    protected Activity mAct;
    //只允许一个实例化回调
    protected static OnPayListener mPayResultListener;
    private  int mPayType;

    /**
     * 设置支付类型
     * @see SocialType
     * @param authType SocialType
     */
    protected void setPayType(int authType) {
        mPayType = authType;
    }

    /**
     * 支付类型
     * @return 支付类型
     */
    protected int getPayType() {
        return mPayType;
    }

    public PayApi(Activity act,OnPayListener l){
        mAct = act;
        setOnPayListener(l);
    }

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
            mPayResultListener.onPayOk();
        }
    }

    /**
     * 返回支付失败
     */
    public static void callbackPayFail(String code, String msg){
        if(mPayResultListener != null){
            mPayResultListener.onPayFail(code, msg);
        }
    }

    /**
     * 支付回调
     */
    public interface OnPayListener {

        /**
         * 支付回调-成功支付
         */
        void onPayOk();

        /**
         * 支付回调-支付失败
         */
        void onPayFail(String code, String msg);
    }

    /**
     * 支付宝个人 捐赠
     * @param urlCode 收款码 /末尾
     */
    public void alipayMe(String urlCode){
//        urlCode="fkx19953nsokfrvlylqoy86";//00c060630igcenu4bfbud2e/fkx150444qjqymmownj8acb/fkx19568debwbeygnkf6273
        if (hasInstall("com.eg.android.AlipayGphone")){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F"+urlCode+"%3F_s%3Dweb-other"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mAct.startActivity(intent);}
    }


    /**
     * 打开weixin扫一扫界面 如需收款，请自行操作保存收款码到相册步骤
     */
    public  void startWxScan() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
//        intent.setFlags(335544320);==>
        intent.setFlags( FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP );
//        或者
//        intent.setFlags( FLAG_RECEIVER_FOREGROUND | FLAG_ACTIVITY_CLEAR_TOP );
        intent.setAction("android.intent.action.VIEW");
        if (isActivityAvailable(intent)) {
            mAct.startActivity(intent);
        } else {
            Toast.makeText(mAct, "未安装微信～", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 打开支付宝扫一扫界面
     *
     * @param context Context
     * @return 是否成功打开 Activity
     */
    public static boolean openAlipayScan(Context context) {
        try {
            Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            if (context instanceof TileService) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (context instanceof TileService) {
                    ((TileService) context).startActivityAndCollapse(intent);
                }
            } else {
                context.startActivity(intent);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 打开支付宝付款码
     *
     * @param context Context
     * @return 是否成功打开 Activity
     */
    public static boolean openAlipayBarcode(Context context) {
        try {
            Uri uri = Uri.parse("alipayqr://platformapi/startapp?saId=20000056");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            if (context instanceof TileService) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (context instanceof TileService) { ((TileService) context).startActivityAndCollapse(intent);
                }
            } else {
                context.startActivity(intent);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否安装某APP
     * @param pack 包名
     * @return true 已安装
     */
    protected boolean hasInstall(String pack) {
        PackageManager pm = mAct.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(pack, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * intent是否可达
     * @param intent intent.putE等
     * @return true ok
     */
    protected boolean isActivityAvailable(Intent intent) {
        PackageManager pm = mAct.getPackageManager();
        if (pm == null) {
            return false;
        }
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }

}
