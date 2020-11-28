package com.mhy.wxlibrary.pay;

import android.app.Activity;
import android.text.TextUtils;

import com.mhy.socialcommon.PayApi;
import com.mhy.socialcommon.PayContent;
import com.mhy.socialcommon.SocialType;
import com.mhy.wxlibrary.WxSocial;
import com.mhy.wxlibrary.bean.WxPayContent;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.JumpToOfflinePay;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 微信支付
 */
public class WxPay extends PayApi {

    private IWXAPI msgApi;

    /**
     * 支付
     *
     * @param act act
     * @param l   回调
     */
    public WxPay(Activity act, OnPayListener l) {
        super(act, l);
        mAct = act;
        setOnPayListener(l);
        mPayType=SocialType.WEIXIN_Pay;
    }

    /**
     * 之前先和后台交互好订单
     * 这一套后台已经帮你处理好金额之类的了
     *
     * @param payInfo 支付payInfo
     */

    @Override
    public void doPay(PayContent payInfo) {
        if (payInfo == null) {
            callbackPayFail("payInfo为空");
            return;
        }
        final WxPayContent content;
        if (payInfo.getPayType() != SocialType.WEIXIN_Pay) {
            callbackPayFail( "类型参数错误");
            return;
        }else {
            content= (WxPayContent) payInfo;
        }

        if (TextUtils.isEmpty(content.getAppid())&&TextUtils.isEmpty(WxSocial.getWeixinId())) {
            callbackPayFail( "appid为空");
            return;
        }

//       appid 商户号 密钥
//        WxSocial.setWeixinPay(content.appid, content.partnerid, content.prepayid);
        msgApi = WXAPIFactory.createWXAPI(mAct, TextUtils.isEmpty(content.getAppid())? WxSocial.getWeixinId():content.getAppid());

        if (!msgApi.isWXAppInstalled()) {
//            Toast.makeText(mAct, "微信未安装", Toast.LENGTH_SHORT).show();
            callbackPayFail( "微信未安装");
            return;
        }
        new AsyncTaskEx<Void, Void, WxPayContent>() {

            @Override
            protected WxPayContent doInBackground(Void... params) {
                return content;
            }

            @Override
            protected void onPostExecute(WxPayContent result) {
                PayReq req = new PayReq();
                req.appId = result.getAppid();
                req.partnerId = result.getPartnerid();
                req.prepayId = result.getPrepayid();
                req.packageValue = result.getPackageValue();
                req.nonceStr = result.getNoncestr();
                req.timeStamp = result.getTimestamp();
                req.sign = result.getSign();
//        msgApi.registerApp(WxSocial.getWeixinId());
                msgApi.registerApp(TextUtils.isEmpty(result.getAppid())? WxSocial.getWeixinId() : result.getAppid());
                if (TextUtils.isEmpty(WxSocial.getWeixinId())) {
                    callbackPayFail( "appid空");
                    return;
                }
                msgApi.sendReq(req);
            }
        }.execute();
    }

    /**
     * 离线支付
     */
    public void offPay() {
        int wxSdkVersion = msgApi.getWXAppSupportAPI();
        if (wxSdkVersion >= Build.OFFLINE_PAY_SDK_INT) {
            msgApi.sendReq(new JumpToOfflinePay.Req());
        } else {
            callbackPayFail( "不支持离线支付");
//            Toast.makeText(mAct, "不支持离线支付", Toast.LENGTH_LONG).show();
        }
    }
}
