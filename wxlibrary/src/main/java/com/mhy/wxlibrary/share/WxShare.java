package com.mhy.wxlibrary.share;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.mhy.socialcommon.ShareApi;
import com.mhy.socialcommon.ShareEntity;
import com.mhy.socialcommon.SocialType;
import com.mhy.wxlibrary.WxSocial;
import com.mhy.wxlibrary.bean.WxShareEntity;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;


/**
 * 分享平台公共组件模块-微信分享
 */
public class WxShare extends ShareApi {

    /**
     * 执行登陆操作
     *
     * @param act activity
     * @param l   回调监听
     */
    public WxShare(Activity act, OnShareListener l) {
        super(act, l);
        mActivity = act;
        setOnShareListener(l);
    }

    /*基本信息验证*/
    private boolean baseVerify() {
        if (TextUtils.isEmpty(WxSocial.getWeixinId())) {
            callbackShareFail( "appid为空");

            return true;
        }
        return false;
    }

    /**
     * 分享
     *
     * @param mShareContent 分享内容包装
     */
    @Override
    public void doShare(ShareEntity mShareContent) {
        if (null==mShareContent){return;}
        mShareType= mShareContent.getType();
        if (baseVerify()) {
            return;
        }
        IWXAPI api = WXAPIFactory.createWXAPI(mActivity, WxSocial.getWeixinId(), true);
        api.registerApp(WxSocial.getWeixinId());

        if (!api.isWXAppInstalled()) {
            Toast.makeText(mActivity, "微信未安装", Toast.LENGTH_SHORT).show();
            return;
        }
        //是否分享到朋友圈，微信4.2以下不支持朋友圈
        boolean isTimeLine = mShareContent.getType() == SocialType.WEIXIN_PYQ_Share;
        if (isTimeLine && api.getWXAppSupportAPI() < 0x21020001) {
            callbackShareFail( "微信版本过低，不支持分享朋友圈");
            return;
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = createMessage(req, mShareContent.getParams());
        if (req.message == null) {
            return;
        }
        req.scene = isTimeLine ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);

    }

    private String buildTransaction(final String type) {
        return type == null ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private byte[] bmpToByteArray(final Bitmap bmp, boolean needThumb) {
        Bitmap newBmp;
        if (needThumb) {
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            if (width > height) {
                height = height * 150 / width;
                width = 150;
            } else {
                width = width * 150 / height;
                height = 150;
            }
            newBmp = Bitmap.createScaledBitmap(bmp, width, height, true);
        } else {
            newBmp = bmp;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        newBmp.compress(Bitmap.CompressFormat.JPEG, 100, output);

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!bmp.isRecycled()) {
                bmp.recycle();
            }
            if (!newBmp.isRecycled()) {
                newBmp.recycle();
            }
        }

        return result;
    }

    private WXMediaMessage createMessage(SendMessageToWX.Req req, Bundle params) {
        WXMediaMessage msg = new WXMediaMessage();
        int type = params.getInt(WxShareEntity.KEY_WX_TYPE);
        boolean success = false;
        switch (type) {
            case WxShareEntity.TYPE_TEXT:
                success = addText(req, msg, params);
                break;
            case WxShareEntity.TYPE_IMG:
                success = addImage(req, msg, params);
                break;
            case WxShareEntity.TYPE_MUSIC:
                success = addMusic(req, msg, params);
                break;
            case WxShareEntity.TYPE_VIDEO:
                success = addVideo(req, msg, params);
                break;
            case WxShareEntity.TYPE_WEB:
                success = addWeb(req, msg, params);
                break;
            case WxShareEntity.TYPE_MINIAPP:
                success = addMiniApp(req, msg, params);
                break;
        }
        if (!success) {
            return null;
        }
        return msg;
    }

    /**
     * 分享小程序 卡片 目前不支持朋友圈
     *
     * @param req
     * @param message
     * @param params
     * @return
     */
    private boolean addMiniApp(SendMessageToWX.Req req, WXMediaMessage message, Bundle params) {
        //    第二种：App 主动分享小程序卡片：⚠️ 小程序测试版不能分享
        WXMiniProgramObject wxminiObiect = new WXMiniProgramObject();
        wxminiObiect.webpageUrl = params.getString(WxShareEntity.KEY_WX_WEB_URL); //兼容低版本的网络链接
        wxminiObiect.userName = params.getString(WxShareEntity.KEY_WX_MINI_APPID);//小程序的原始ID
        wxminiObiect.path = params.getString(WxShareEntity.KEY_WX_MINI_PATH);// 指定打开小程序的某一个页面的URL路径
//    wxminiObiect.hdImageData =  hdImageData; //小程序节点高清大图，小于128K

        message.title = params.getString(WxShareEntity.KEY_WX_TITLE);
        message.description = params.getString(WxShareEntity.KEY_WX_SUMMARY);

        Bitmap bitmap = null;
        if (params.containsKey(WxShareEntity.KEY_WX_IMG_LOCAL)) {
            //分为本地文件
            String imgUrl = params.getString(WxShareEntity.KEY_WX_IMG_LOCAL);
            if (notFoundFile(imgUrl)) {
                return false;
            }
            bitmap = BitmapFactory.decodeFile(imgUrl);
        } else {
            //应用内资源图片
            bitmap = BitmapFactory.decodeResource(mActivity.getResources(), params.getInt(WxShareEntity.KEY_WX_IMG_RES));
        }
        message.mediaObject = wxminiObiect;
        if (bitmap != null) {// 兼容旧版本节点的图片，小于32k，新版本优先
            message.thumbData = bmpToByteArray(bitmap, true);
        }
//        req.transaction = buildTransaction("miniprogram");
        return true;
    }

    private boolean addText(SendMessageToWX.Req req, WXMediaMessage msg, Bundle params) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = params.getString(WxShareEntity.KEY_WX_TEXT);

        msg.mediaObject = textObj;
        msg.description = textObj.text;

        req.transaction = buildTransaction("text");
        return true;
    }

    private boolean addImage(SendMessageToWX.Req req, WXMediaMessage msg, Bundle params) {
        WXImageObject imgObj;
        Bitmap bitmap;
        if (params.containsKey(WxShareEntity.KEY_WX_IMG_LOCAL)) {
            //分为本地文件
            String imgUrl = params.getString(WxShareEntity.KEY_WX_IMG_LOCAL);
            if (notFoundFile(imgUrl)) {
                return false;
            }

            imgObj = new WXImageObject();
            imgObj.imagePath = imgUrl;
            bitmap = BitmapFactory.decodeFile(imgUrl);
        } else {
            //应用内资源图片
            bitmap = BitmapFactory.decodeResource(mActivity.getResources(), params.getInt(WxShareEntity.KEY_WX_IMG_RES));
            imgObj = new WXImageObject(bitmap);
        }
        msg.mediaObject = imgObj;
        if (bitmap != null) {
            msg.thumbData = bmpToByteArray(bitmap, true);
        }
        req.transaction = buildTransaction("img");
        return true;
    }

    private boolean addMusic(SendMessageToWX.Req req, WXMediaMessage msg, Bundle params) {
        WXMusicObject musicObject = new WXMusicObject();
        musicObject.musicUrl = params.getString(WxShareEntity.KEY_WX_MUSIC_URL);

        msg.mediaObject = musicObject;
        if (addTitleSummaryAndThumb(msg, params)) {
            return false;
        }

        req.transaction = buildTransaction("music");
        return true;
    }

    private boolean addVideo(SendMessageToWX.Req req, WXMediaMessage msg, Bundle params) {
        WXVideoObject musicObject = new WXVideoObject();
        musicObject.videoUrl = params.getString(WxShareEntity.KEY_WX_VIDEO_URL);

        msg.mediaObject = musicObject;
        if (addTitleSummaryAndThumb(msg, params)) {
            return false;
        }

        req.transaction = buildTransaction("video");
        return true;
    }

    private boolean addWeb(SendMessageToWX.Req req, WXMediaMessage msg, Bundle params) {
        WXWebpageObject musicObject = new WXWebpageObject();
        musicObject.webpageUrl = params.getString(WxShareEntity.KEY_WX_WEB_URL);

        msg.mediaObject = musicObject;
        if (addTitleSummaryAndThumb(msg, params)) return false;

        req.transaction = buildTransaction("webpage");
        return true;
    }

    private boolean addTitleSummaryAndThumb(WXMediaMessage msg, Bundle params) {
        if (params.containsKey(WxShareEntity.KEY_WX_TITLE)) {
            msg.title = params.getString(WxShareEntity.KEY_WX_TITLE);
        }

        if (params.containsKey(WxShareEntity.KEY_WX_SUMMARY)) {
            msg.description = params.getString(WxShareEntity.KEY_WX_SUMMARY);
        }

        if (params.containsKey(WxShareEntity.KEY_WX_IMG_LOCAL) || params.containsKey(WxShareEntity.KEY_WX_IMG_RES)) {
            Bitmap bitmap;
            if (params.containsKey(WxShareEntity.KEY_WX_IMG_LOCAL)) {//分为本地文件和应用内资源图片
                String imgUrl = params.getString(WxShareEntity.KEY_WX_IMG_LOCAL);
                if (notFoundFile(imgUrl)) {
                    return true;
                }
                bitmap = BitmapFactory.decodeFile(imgUrl);
            } else {
                bitmap = BitmapFactory.decodeResource(mActivity.getResources(), params.getInt(WxShareEntity.KEY_WX_IMG_RES));
            }
            msg.thumbData = bmpToByteArray(bitmap, true);
        }
        return false;
    }

    private boolean notFoundFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (!file.exists()) {
                if (mShareListener != null) {
                    callbackShareFail("文件未找到");
                }
                return true;
            }
        } else {
            if (mShareListener != null) {
                callbackShareFail("文件未找到");
            }
            return true;
        }
        return false;
    }

}
