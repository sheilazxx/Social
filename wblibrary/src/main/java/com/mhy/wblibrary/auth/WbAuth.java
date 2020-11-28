package com.mhy.wblibrary.auth;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.mhy.socialcommon.AuthApi;
import com.mhy.socialcommon.SocialType;
import com.mhy.wblibrary.WbSocial;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;

/** 微博登陆 */
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
        mAuthType= SocialType.WEIBO_Auth;
        if (cpuX86()) {
            return;
        }
        AuthInfo authInfo =
                new AuthInfo(act, WbSocial.getAppKy(), WbSocial.getRedirectUrl(), WbSocial.getScope());
        mWBAPI = WBAPIFactory.createWBAPI(act);
        mWBAPI.registerApp(act, authInfo);
    }

    private boolean cpuX86() {
        String arch = System.getProperty("os.arch");
        String arc = null; // 大写
        if (arch != null) {
            arc = arch.toUpperCase();
            return arc.contains("X86");
        }
        return false;
    }

    public void doAuth() {
        if (cpuX86()) {
            return;
        }
        // auth
        mWBAPI.authorize(
                new WbAuthListener() {
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
    private void doClientAuth() {
        mWBAPI.authorizeClient(new WbAuthListener() {
            @Override
            public void onComplete(Oauth2AccessToken token) {
                setCompleteCallBack(token);
            }

            @Override
            public void onError(UiError error) {
                setErrorCallBack(error.errorMessage);
            }

            @Override
            public void onCancel() {
                setCancelCallBack();
            }
        });
    }

    private void dotWebAuth() {
        mWBAPI.authorizeWeb(new WbAuthListener() {
            @Override
            public void onComplete(Oauth2AccessToken token) {
                setCompleteCallBack(token);
            }

            @Override
            public void onError(UiError error) {
                setErrorCallBack(error.errorMessage);
            }

            @Override
            public void onCancel() {
                setCancelCallBack();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mWBAPI.authorizeCallback(requestCode, resultCode, data);
    }
}
