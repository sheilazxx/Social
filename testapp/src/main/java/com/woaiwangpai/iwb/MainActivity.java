package com.woaiwangpai.iwb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.mhy.alilibrary.auth.AliAuth;
import com.mhy.alilibrary.bean.AliPayContent;
import com.mhy.alilibrary.pay.AliPay;
import com.mhy.qqlibrary.auth.QqAuth;
import com.mhy.qqlibrary.bean.QQShareEntity;
import com.mhy.qqlibrary.share.QqShare;
import com.mhy.socialcommon.AuthApi;
import com.mhy.socialcommon.PayApi;
import com.mhy.socialcommon.ShareApi;
import com.mhy.socialcommon.ShareEntity;
import com.mhy.socialcommon.ShareUtil;
import com.mhy.socialcommon.SocialType;
import com.mhy.wblibrary.auth.WbAuth;
import com.mhy.wblibrary.bean.WbShareEntity;
import com.mhy.wblibrary.share.WbShare;
import com.mhy.wxlibrary.auth.WxAuth;
import com.mhy.wxlibrary.bean.WxPayContent;
import com.mhy.wxlibrary.bean.WxShareEntity;
import com.mhy.wxlibrary.pay.WxPay;
import com.mhy.wxlibrary.share.WxShare;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ShareEntity createWXShareEntity(boolean pyq) {
        ShareEntity shareEntity = null;

//        shareEntity = WxShareEntity.createImageInfo(pyq, Environment.getExternalStorageDirectory() + "/fdmodel.jpg");

        //微信图文是分开的，但是在分享到朋友圈的web中是可以有混合的
//        shareEntity = WxShareEntity.createTextInfo(pyq, "R.mipmap.ic_launcher");

        shareEntity = WxShareEntity.createWebPageInfo(pyq, "http://www.baidu.com", R.mipmap.ic_launcher, "title", "summary");

        return shareEntity;
    }

    AuthApi api;
    ShareApi spi;
    Animation shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copy();//准备资源
        shake= AnimationUtils.loadAnimation(this, R.anim.shake);


        //微信分享
        findViewById(R.id.btn_share_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WxShare mShareApi = new WxShare(MainActivity.this, ShareEntity.TYPE_WX, onShareListener);
                mShareApi.doShare(createWXShareEntity(false));
v.startAnimation(shake);
            }
        });
        //微信朋友圈分享
        findViewById(R.id.btn_share_wx_circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WxShare mShareApi = new WxShare(MainActivity.this, ShareEntity.TYPE_PYQ, onShareListener);
                mShareApi.doShare(createWXShareEntity(true));v.startAnimation(shake);
            }
        });
        //微信登录
        findViewById(R.id.btn_login_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                WxAuth wxAuth=new WxAuth(MainActivity.this, onAuthListener);
//                wxAuth.doAuth();
                WxAuth authApi = new WxAuth(MainActivity.this, onAuthListener);
                authApi.doAuth();v.startAnimation(shake);

            }
        });
        //qq登录
        findViewById(R.id.btn_login_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QqAuth authApi = new QqAuth(MainActivity.this, onAuthListener);
                authApi.doAuth(false);v.startAnimation(shake);
                api = authApi;//onActivityResult()内使用
            }
        });
        //原生分享
        findViewById(R.id.btn_share_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               v.startAnimation(shake);
                ShareUtil shareUtil=new ShareUtil(MainActivity.this);
//                shareUtil.shareFile(new File(getExternalFilesDir(null) + "/ccc.JPG"));
                shareUtil.shareText("【flutter凉了吗?】知乎：… https://www.zhihu.com/question/374113031/answer/1253795562?utm_source=com.eg.android.alipaygphone&utm_medium=social&utm_oi=1020568397012209664 （分享自知乎网）");
            }
        });
        //长按 打开小程序
        findViewById(R.id.btn_login_qq).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.startAnimation(shake);
                QqAuth authApi = new QqAuth(MainActivity.this, onAuthListener);
                authApi.doOpenMiniApp("1108108864","pages/tabBar/index/index","release");
                return true;//不响应其他事件
            }
        });
        //qq分享
        findViewById(R.id.btn_share_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QqShare mShareApi = new QqShare(MainActivity.this, SocialType.QQ_Share, onShareListener);
                mShareApi.doShare(QQShareEntity.createImageInfo(getExternalFilesDir(null) + "/aaa.png", "app"));
                spi = mShareApi;v.startAnimation(shake);
            }
        });
        //qq空间分享
        findViewById(R.id.btn_share_qq_zone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> imgUrls = new ArrayList<>();
                imgUrls.add(getExternalFilesDir(null) + "/aaa.png");
                imgUrls.add(getExternalFilesDir(null) + "/bbb.jpg");
                QqShare mShareApi = new QqShare(MainActivity.this, SocialType.QQ_ZONE_Share, onShareListener);
                mShareApi.doShare(QQShareEntity.createImageTextInfoToQZone("toptitle", "http://www.baidu.com", imgUrls, "summary", "我"));
                spi = mShareApi;v.startAnimation(shake);
            }
        });
        //支付宝登陆
        findViewById(R.id.btn_login_ali).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AliAuth authApi = new AliAuth(MainActivity.this, onAuthListener);
                authApi.doAuth("");v.startAnimation(shake);
            }
        });
        //支付宝支付
        findViewById(R.id.btn_pay_alipay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AliPay authApi = new AliPay(MainActivity.this, onPayListener);
                authApi.doPay(new AliPayContent(""));
                v.startAnimation(shake);
            }
        });
        //微信支付
        findViewById(R.id.btn_pay_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonObject = new JSONObject("source json data...");
                    //服务端获取
                    jsonObject = jsonObject.getJSONObject("pay_message");

                    WxPayContent req = new WxPayContent(
                    jsonObject.getString("appid"),
                    jsonObject.getString("partnerid"),
                    jsonObject.getString("prepayid"),
                    jsonObject.getString("packagestr"),
                    jsonObject.getString("noncestr"),
                    jsonObject.getString("timestamp"),
                    jsonObject.getString("sign"));
                    PayApi wxApi = new WxPay(MainActivity.this, onPayListener);
                    wxApi.doPay(req);
                    v.startAnimation(shake);
                } catch (JSONException ignored) {

                }
            }
        });
        //小程序分享
        findViewById(R.id.btn_pay_wx).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.startAnimation(shake);
//               WxShareEntity.createMiniApp(String miniAppid,String miniPath,String webpageUrl,String title,String desc,String imgUrl);
                return true;//no >
            }
        });
        //微博分享
        findViewById(R.id.btn_share_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WbShare wbShare = new WbShare(MainActivity.this, SocialType.WEIBO_Share, onShareListener);
                wbShare.doShareStory(WbShareEntity.createImageStory(getExternalFilesDir(null) + "/aaa.png"));
                spi = wbShare;v.startAnimation(shake);
            }
        });
        //微博登录
        findViewById(R.id.btn_login_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WbAuth wbAuth = new WbAuth(MainActivity.this, onAuthListener);
                wbAuth.doAuth();
                api = wbAuth;v.startAnimation(shake);
            }
        });
    }

    //登陆回调
    private AuthApi.OnAuthListener onAuthListener = new AuthApi.OnAuthListener() {
        @Override
        public void onComplete(int type, Object user) {
//            wx((WeiXin)user).getCode(); wb（Oauth2AccessToken）user ;ali AuthResult
            Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(int type, String error) {
            Toast.makeText(MainActivity.this, "登录失败:" + error, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(int type) {
            Toast.makeText(MainActivity.this, "登录取消", Toast.LENGTH_SHORT).show();
        }
    };

    //支付回调
    private PayApi.OnPayListener onPayListener = new PayApi.OnPayListener() {
        @Override
        public void onPayOk() {
            Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPayFail(String code, String msg) {
            Toast.makeText(MainActivity.this, "支付失败：" + msg, Toast.LENGTH_SHORT).show();
        }
    };
    //分享回调
    private ShareApi.OnShareListener onShareListener = new ShareApi.OnShareListener() {
        @Override
        public void onShareOk(int type) {
            Toast.makeText(MainActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShareFail(int type, String msg) {
            Toast.makeText(MainActivity.this, "分享失败:" + msg, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //微博和QQ需要
        if (api != null) {
            api.onActivityResult(requestCode, resultCode, data);
            api=null;
        }
        if (spi != null) {
            spi.onActivityResult(requestCode, resultCode, data);
            spi=null;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    @Override
    protected void onStart() {
        Log.d("TAG", "-->onStart");
        PermissionMgr.getInstance().requestPermissions(this);

        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionMgr.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        //xx
        if(1111==requestCode){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(MainActivity.this,"Please give me storage permission!",Toast.LENGTH_LONG).show();
            }
            return;
        }
    }
//xx
    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1111);
        }
    }

    private void copy() {
        copyFile("eeee.mp4");
        copyFile("aaa.png");
        copyFile("bbb.jpg");
        copyFile("ccc.JPG");
        copyFile("eee.jpg");
        copyFile("ddd.jpg");
        copyFile("fff.jpg");
        copyFile("ggg.JPG");
        copyFile("hhhh.jpg");
        copyFile("kkk.JPG");
    }

    private void copyFile(final String fileName) {
        final File file = new File(getExternalFilesDir(null).getPath() + "/" + fileName);
        if (!file.exists()) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream inputStream = getAssets().open(fileName);
                        OutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1444];
                        int readSize;
                        while ((readSize = inputStream.read(buffer)) != 0) {
                            outputStream.write(buffer, 0, readSize);
                        }
                        inputStream.close();
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }


}
