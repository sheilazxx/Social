package com.mhy.wxlibrary.auth;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.mhy.socialcommon.AuthApi;
import com.mhy.socialcommon.SocialType;
import com.mhy.wxlibrary.WxSocial;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelbiz.SubscribeMiniProgramMsg;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;


/**
 * 微信登陆
 */
public class WxAuth extends AuthApi {


    /*
     * 执行登陆操作
     * @param act activity
     * @param l 回调监听
     */
    public WxAuth(Activity act, OnAuthListener l) {
        super(act, l);
        setAuthType(SocialType.WEIXIN_Auth);
    }

    /*基本信息验证*/
    private boolean baseVerify(OnAuthListener callback) {
        if (TextUtils.isEmpty(WxSocial.getWeixinId())) {
            if (callback != null) {
                callback.onError(1, "appid为空");
            }
            return true;
        }
        return false;
    }

    IWXAPI api;

    /**
     * 登陆认证
     */
    public void doAuth() {
        if (baseVerify(mOnAuthListener)) {
            return;
        }
        api = WXAPIFactory.createWXAPI(mActivity, WxSocial.getWeixinId(), true);
        api.registerApp(WxSocial.getWeixinId());

        if (!api.isWXAppInstalled()) {
            Toast.makeText(mActivity, "微信未安装", Toast.LENGTH_SHORT).show();
            return;
        }
        // send oauth request
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat";//String.valueOf(System.currentTimeMillis());

        api.sendReq(req);
    }
    /**
     * 打开微信小程序
     * @param miniAppId  小程序原始ID "gh_d43f693ca31f"
     * @param path 类似http的url方法来传递参数 "page/index?key1=xxx&key2=yyy";
     * @param type WXMiniProgramTypeRelease 正式版
     *             WXMiniProgramTypeTest 开发版
     *             WXMiniProgramTypePreview 体验版
     *             {@link WXLaunchMiniProgram.Req}
     *
     */
    public void doOpenMiniApp(String miniAppId,String path,int type){
        IWXAPI api = WXAPIFactory.createWXAPI(mActivity, WxSocial.getWeixinId(), true);
        api.registerApp(WxSocial.getWeixinId());

        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        req.userName = miniAppId; // 填小程序原始id
        req.path = path;                  //拉起小程序页面的可带参路径，不填默认拉起小程序首页
        req.miniprogramType =type /*WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE*/;// 可选打开 开发版，体验版和正式版
        api.sendReq(req);
    }
    /**
     * 订阅小程序消息
     * @param miniAppId 小程序 appid
     */
    public void subMiniAppMsg(String miniAppId) {
        if (api.getWXAppSupportAPI() >= Build.SUBSCRIBE_MINI_PROGRAM_MSG_SUPPORTED_SDK_INT) {
            SubscribeMiniProgramMsg.Req req = new SubscribeMiniProgramMsg.Req();
            req.miniProgramAppId = miniAppId;

            boolean ret = api.sendReq(req);
            String message = String.format("sendReq ret : %s", ret);
//        Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
            setCompleteCallBack(message);

        } else {
            setErrorCallBack("不支持");
        }

    }

    /**
     * 订阅消息
     *
     * @param scene 1000
     * options.scene是1036，这个场景id表示app分享。
     * options.scene是1069，这个场景id表示从app打开。
     * @param templateId Jo-ywGDy0K9zGY87D2Cs2D51ExMoA1xSor7UxfIiLG8
     * @param reserved abcAbc
     */
    public void subAppMsg(int scene, String templateId, String reserved) {
        if (api.getWXAppSupportAPI() >= Build.SUBSCRIBE_MESSAGE_SUPPORTED_SDK_INT) {
            SubscribeMessage.Req req = new SubscribeMessage.Req();
            req.scene = scene;
            req.templateID = templateId;
            req.reserved = reserved;

            boolean ret = api.sendReq(req);
//            Toast.makeText(mActivity, "sendReq result = " + ret, Toast.LENGTH_SHORT).show();
            setCompleteCallBack("sendReq result = " + ret);

        } else {
            setErrorCallBack("不支持");
        }

    }

    /**启动微信*/
    public boolean launch() {
        return api.openWXApp();
    }

    /**注册*/
    public void register() {
        api.registerApp(WxSocial.getWeixinId());
    }

    /** 反注册*/
    public void unRegister() {
        api.unregisterApp();
    }

}
