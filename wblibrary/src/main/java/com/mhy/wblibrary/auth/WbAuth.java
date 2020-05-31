package com.mhy.wblibrary.auth;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.mhy.socialcommon.AuthApi;
import com.mhy.socialcommon.SocialType;
import com.mhy.wblibrary.WbSocial;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;


/**
 * 微博登陆
 */
public class WbAuth extends AuthApi {
    private IWBAPI mWBAPI;

    /*
     * 执行登陆操作
     * @param act activity
     * @param l 回调监听
     */
    public WbAuth(Activity act, OnAuthListener l) {
        super(act, l);
        mActivity = act;
        setAuthListener(l);
        setAuthType(SocialType.WEIBO_Auth);

        AuthInfo authInfo = new AuthInfo(act, WbSocial.getAppKy(), WbSocial.getRedirectUrl(), WbSocial.getScope());
        mWBAPI = WBAPIFactory.createWBAPI(act);
        mWBAPI.registerApp(act, authInfo);
    }


    public void doAuth() {
        //auth
        mWBAPI.authorize(new WbAuthListener() {
            @Override
            public void onComplete(Oauth2AccessToken token) {
                setCompleteCallBack(token);
//                Toast.makeText(mActivity, "微博授权成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(UiError error) {
//                Toast.makeText(mActivity, "微博授权出错", Toast.LENGTH_SHORT).show();
                setErrorCallBack(error.errorMessage);
            }

            @Override
            public void onCancel() {
//                Toast.makeText(mActivity, "微博授权取消", Toast.LENGTH_SHORT).show();
                setCancelCallBack();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mWBAPI.authorizeCallback(requestCode, resultCode, data);
    }


}
