package com.mhy.qqlibrary.share;

        import android.app.Activity;
        import android.content.Intent;
        import android.text.TextUtils;

        import com.mhy.qqlibrary.QqSocial;
        import com.mhy.socialcommon.ShareApi;
        import com.mhy.socialcommon.ShareEntity;
        import com.mhy.socialcommon.SocialType;
        import com.tencent.tauth.IUiListener;
        import com.tencent.tauth.Tencent;
        import com.tencent.tauth.UiError;

/**
 * @author mahongyin 2020-05-29 19:32 @CopyRight mhy.work@qq.com
 * description .
 */
public class QqShare extends ShareApi {
    Tencent mTencent;

    public QqShare(Activity act, OnShareListener l) {
        super(act, l);
        if (mTencent == null) {
            mTencent = Tencent.createInstance(QqSocial.getAppId(), mActivity, mActivity.getPackageName() + ".com.fileprovider");//authInfo"101807669"
        }
    }

    @Override
    public void doShare(ShareEntity shareInfo) {

        if (shareInfo == null) {
            return;
        }
        mShareType = shareInfo.getType();
        if (baseVerify(mShareListener)) {
            return;
        }

        if (mTencent != null) {
            if (shareInfo.getType() == SocialType.QQ_Share) {
                mTencent.shareToQQ(mActivity, shareInfo.getParams(), mQQCallbackListener);
            } else if (shareInfo.getType() == SocialType.QQ_PUBLISHshare) {
                mTencent.publishToQzone(mActivity, shareInfo.getParams(), mQQCallbackListener);
            } else {
                mTencent.shareToQzone(mActivity, shareInfo.getParams(), mQQCallbackListener);
            }
        }
    }

    /*基本信息验证*/
    private boolean baseVerify(OnShareListener callback) {
        if (TextUtils.isEmpty(QqSocial.getAppId())) {
            if (callback != null) {
                callbackShareFail("appid为空");
            }
            return true;
        }
        return false;
    }


    BaseUiListener mQQCallbackListener = new BaseUiListener();

    public class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object o) {
//            Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();
            callbackShareOk();
            mTencent.logout(mActivity);//登录成功注销
        }

        @Override
        public void onError(UiError uiError) {
//            Toast.makeText(context, "登录失败", Toast.LENGTH_SHORT).show();
            callbackShareFail("登录失败" + uiError.errorMessage);
        }

        @Override
        public void onCancel() {
//            Toast.makeText(context, "取消登录", Toast.LENGTH_SHORT).show();
            callbackCancel();
        }

        @Override
        public void onWarning(int i) {

        }
    }

    public IUiListener getQQCallbackListener() {
        return mQQCallbackListener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == Constants.REQUEST_QQ_SHARE||requestCode == Constants.REQUEST_QZONE_SHARE) {
        Tencent.onActivityResultData(requestCode, resultCode, data, getQQCallbackListener());
//        }
    }
}
