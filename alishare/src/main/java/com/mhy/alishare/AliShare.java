package com.mhy.alishare;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;

import com.alipay.share.sdk.openapi.APAPIFactory;
import com.alipay.share.sdk.openapi.APImageObject;
import com.alipay.share.sdk.openapi.APMediaMessage;
import com.alipay.share.sdk.openapi.APTextObject;
import com.alipay.share.sdk.openapi.APWebPageObject;
import com.alipay.share.sdk.openapi.BaseReq;
import com.alipay.share.sdk.openapi.BaseResp;
import com.alipay.share.sdk.openapi.IAPAPIEventHandler;
import com.alipay.share.sdk.openapi.IAPApi;
import com.alipay.share.sdk.openapi.SendMessageToZFB;
import com.mhy.socialcommon.ShareApi;
import com.mhy.socialcommon.ShareEntity;
import com.mhy.socialcommon.SocialType;

import java.io.File;

/**
 * Created By Mahongyin
 * Date    2020/11/27 12:41
 */
public class AliShare extends ShareApi implements IAPAPIEventHandler {
    private IAPApi api;

    public static String getAppId() {
        return APP_ID;
    }

    public static void setAppId(String appId) {
        APP_ID = appId;
    }

    private static String APP_ID/* = "2015060900117932"*/;

    public AliShare(Activity act, OnShareListener l) {
        super(act, l);
        mShareType = SocialType.ALIPAY_Share;
        if (TextUtils.isEmpty(getAppId())) {
            callbackShareFail("APP_ID为空,请先全局setAppId()");
        }
        api = APAPIFactory.createZFBApi(act.getApplicationContext(), APP_ID, false);
        Intent intent = act.getIntent();
        api.handleIntent(intent, this);

    }

    /**
     * 注意newintent
     *
     * @Override protected void onNewIntent(Intent intent) {
     * super.onNewIntent(intent);
     * setIntent(intent);
     * api.handleIntent(intent, this);
     * }
     */
    public void openZFB() {
        api.openZFBApp();
    }

    /**
     * 是否支持分享
     */
    private boolean isSupportShare() {
        return api.isZFBSupportAPI();
    }

    /**
     * 当前支付宝的版本号为
     */
    private int versionZFB() {
        return api.getZFBVersionCode();
    }

    @Override
    public void doShare(ShareEntity content) {
        if (content == null) return;
        String type = content.params.getString(AliShareEntity.TYPE);
        String imageurl;
        String imagepath;String tageturl;
        int image;
        String title;
        switch (type) {
            case AliShareEntity.IMG_RES:
                image = content.params.getInt(AliShareEntity.IMG_RES);
                sendByteImage(image);
                break;
            case AliShareEntity.IMG_PATH:
                imagepath = content.params.getString(AliShareEntity.IMG_PATH);
                sendLocalImage(imagepath);
                break;
            case AliShareEntity.IMG_URL:
                imageurl = content.params.getString(AliShareEntity.IMG_URL);
                sendOnlineImage(imageurl);
                break;
            case AliShareEntity.TYPE_WEB:
                tageturl = content.params.getString(AliShareEntity.WEB_URL);
                title = content.params.getString(AliShareEntity.TITLE);
                imageurl = content.params.getString(AliShareEntity.IMG_URL);
                APWebPageObject webPageObject = new APWebPageObject();
                webPageObject.webpageUrl = tageturl;
                APMediaMessage webMessage = new APMediaMessage();
                webMessage.title = title;
                String description = content.params.getString(AliShareEntity.SUMMARY);
                webMessage.description = description;
                webMessage.mediaObject = webPageObject;
                webMessage.thumbUrl = imageurl;
                SendMessageToZFB.Req webReq = new SendMessageToZFB.Req();
                webReq.message = webMessage;
                webReq.transaction = buildTransaction("webpage");
                //在支付宝版本会合并分享渠道的情况下,不需要传递分享场景参数
                if (!isAlipayIgnoreChannel()) {
                    webReq.scene = SendMessageToZFB.Req.ZFBSceneTimeLine;
//            webReq.scene = SendMessageToZFB.Req.ZFBSceneSession;
                }
                api.sendReq(webReq);
                break;
            case AliShareEntity.TYPE_TEXT:
               String summary = content.params.getString(AliShareEntity.SUMMARY);
                //初始化一个APTextObject对象
                APTextObject textObject = new APTextObject();
                textObject.text = summary;
                //用APTextObject对象初始化一个APMediaMessage对象
                APMediaMessage mediaMessage = new APMediaMessage();
                mediaMessage.mediaObject = textObject;
                //构造一个Req
                SendMessageToZFB.Req req = new SendMessageToZFB.Req();
                req.message = mediaMessage;
                //调用api接口发送消息到支付宝
                api.sendReq(req);
                break;
        }

    }

    private void sendByteImage(@DrawableRes int id) {
        Bitmap bmp = BitmapFactory.decodeResource(mActivity.getResources(), id);
        APImageObject imageObject = new APImageObject(bmp);
        APMediaMessage mediaMessage = new APMediaMessage();
        mediaMessage.mediaObject = imageObject;
        SendMessageToZFB.Req req = new SendMessageToZFB.Req();
        req.message = mediaMessage;
        req.transaction = buildTransaction("image");
        bmp.recycle();
        api.sendReq(req);

    }

    private void sendOnlineImage(String imgurl) {

        APImageObject imageObject = new APImageObject();
        imageObject.imageUrl = imgurl;
        APMediaMessage mediaMessage = new APMediaMessage();
        mediaMessage.mediaObject = imageObject;
        SendMessageToZFB.Req req = new SendMessageToZFB.Req();
        req.message = mediaMessage;
        req.transaction = buildTransaction("image");
        api.sendReq(req);


    }

    private void sendLocalImage(String path) {

        File file = new File(path);
        if (!file.exists()) {
            callbackShareFail("选择的文件不存在");
            return;
        }
        APImageObject imageObject = new APImageObject();
        imageObject.imagePath = path;
        APMediaMessage mediaMessage = new APMediaMessage();
        mediaMessage.mediaObject = imageObject;
        SendMessageToZFB.Req req = new SendMessageToZFB.Req();
        req.message = mediaMessage;
        req.transaction = buildTransaction("image");
        api.sendReq(req);

    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    private boolean isAlipayIgnoreChannel() {
        return api.getZFBVersionCode() >= 101;
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {

        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK://发送成功
                callbackShareOk();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://发送取消
                callbackCancel();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED://权限验证失败
                callbackShareFail("权限验证失败");
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED://发送失败
                callbackShareFail("发送失败");
                break;
            default:
                //未知错误
                callbackShareFail("未知错误");
                break;
        }

    }
}
