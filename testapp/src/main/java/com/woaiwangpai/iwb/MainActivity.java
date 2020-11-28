package com.woaiwangpai.iwb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mhy.alilibrary.auth.AliAuth;
import com.mhy.alilibrary.bean.AuthResult;
import com.mhy.alishare.AliShare;
import com.mhy.alishare.AliShareEntity;
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
import com.mhy.wxlibrary.bean.WeiXin;
import com.mhy.wxlibrary.bean.WxPayContent;
import com.mhy.wxlibrary.bean.WxShareEntity;
import com.mhy.wxlibrary.pay.WxPay;
import com.mhy.wxlibrary.share.WxShare;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;

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

        shareEntity = WxShareEntity.createImageInfo(pyq, getExternalFilesDir(null) + "/fff.jpg");

        //微信图文是分开的，但是在分享到朋友圈的web中是可以有混合的
//        shareEntity = WxShareEntity.createTextInfo(pyq, "R.mipmap.ic_launcher");

//        shareEntity = WxShareEntity.createWebPageInfo(pyq, "http://www.baidu.com", R.mipmap.ic_launcher, "title", "summary");

        return shareEntity;
    }

   private AuthApi api;
   private ShareApi spi;

   private Animation shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copy();//准备资源
        shake = AnimationUtils.loadAnimation(this, R.anim.shake);


        //微信分享
        findViewById(R.id.btn_share_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WxShare mShareApi = new WxShare(MainActivity.this, onShareListener);
                mShareApi.doShare(createWXShareEntity(false));
                v.startAnimation(shake);
            }
        });
        //微信朋友圈分享
        findViewById(R.id.btn_share_wx_circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WxShare mShareApi = new WxShare(MainActivity.this, onShareListener);
                mShareApi.doShare(createWXShareEntity(true));
                v.startAnimation(shake);
            }
        });
        //微信登录
        findViewById(R.id.btn_login_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                WxAuth wxAuth=new WxAuth(MainActivity.this, onAuthListener);
//                wxAuth.doAuth();
                WxAuth authApi = new WxAuth(MainActivity.this, onAuthListener);
                authApi.doAuth();
                v.startAnimation(shake);

            }
        });
        //qq登录
        findViewById(R.id.btn_login_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QqAuth authApi = new QqAuth(MainActivity.this, onAuthListener);
                authApi.doAuth(false);
                v.startAnimation(shake);
                api = authApi;//onActivityResult()内使用
            }
        });
        //原生分享
        findViewById(R.id.btn_share_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(shake);
                ShareUtil shareUtil =  ShareUtil.getInstance(MainActivity.this);
//                shareUtil.shareFile(new File(getExternalFilesDir(null) + "/ccc.JPG"));
                shareUtil.shareText("【flutter凉了吗?】知乎：… https://www.zhihu.com/question/374113031/answer/1253795562?utm_source=com.eg.android.alipaygphone&utm_medium=social&utm_oi=1020568397012209664 （分享自知乎网）");
            }
        });
        //长按 打开小程序
        findViewById(R.id.btn_mini_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(shake);
                QqAuth authApi = new QqAuth(MainActivity.this, onAuthListener);
                authApi.doOpenMiniApp("1108108864", "pages/tabBar/index/index", "release");
            }
        });
        //qq分享
        findViewById(R.id.btn_share_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QqShare mShareApi = new QqShare(MainActivity.this, onShareListener);
                mShareApi.doShare(QQShareEntity.createImageInfo(getExternalFilesDir(null) + "/aaa.png", "app"));
                spi = mShareApi;
                v.startAnimation(shake);
            }
        });
        //qq空间分享
        findViewById(R.id.btn_share_qq_zone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> imgUrls = new ArrayList<>();
                imgUrls.add(getExternalFilesDir(null) + "/aaa.png");
                imgUrls.add(getExternalFilesDir(null) + "/bbb.jpg");
                QqShare mShareApi = new QqShare(MainActivity.this, onShareListener);
                mShareApi.doShare(QQShareEntity.createImageTextInfoToQZone("toptitle", "http://www.baidu.com", imgUrls, "summary", "我"));
                spi = mShareApi;
                v.startAnimation(shake);
            }
        });
        //支付宝登陆
        findViewById(R.id.btn_login_ali).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AliAuth authApi = new AliAuth(MainActivity.this, onAuthListener);
                authApi.doAuth("apiname=com.alipay.account.auth&app_id=2019052365375081&app_name=mc&auth_type=AUTHACCOUNT&biz_type=openservice&pid=2088921472692589&product_id=APP_FAST_LOGIN&scope=kuaijie&target_id=kkkkk091125&sign_type=RSA2&sign=O5EBUVb5j60vt%252FJXGSiSAY1EWtrSrUx1KAXnXCPC2NmdwY35aovHzVmafuUyj%252FV57rt2PpD255OAASNojjN5hrxaDZI2snqPMfTulklwDdmUhd%252BrsziLdvmcNIdXneCPgHrrdkKTIRkg%252BBLFMdXSQ5hNZBeEEC8ySQiaJ%252Fvbd9UOofb8bxd1ZMMPCVCIeDDd34HaW8xuCl8x%252F6cMIN3n3qqcXGcegh4TVyI5nAeOr5lJ0PJv0byKxxqnZKNUsJy2Wzm7TXk3Zc1qxcedE5Giff6y2QP8wXBuY3ZOQJJByaM4vFgbBoVHpo88ElbbTy30Aex%252Fq7v7tz8850kTfvx1gA%253D%253D");//appid
                v.startAnimation(shake);
            }
        });
        //支付宝支付
        findViewById(R.id.btn_pay_alipay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //防止误支付
//                AliPay authApi = new AliPay(MainActivity.this, onPayListener);
//                authApi.doPay(new AliPayContent("alipay_sdk=alipay-sdk-php-20180705&app_id=2018111362152255&biz_content=%7B%22body%22%3A%22%E5%85%85%E5%80%BC%E7%88%B1%E8%B1%86%22%2C%22subject%22%3A+%22%E5%85%85%E5%80%BC%E7%88%B1%E8%B1%86%22%2C%22out_trade_no%22%3A+%222020060211083065315693%22%2C%22timeout_express%22%3A+%2230m%22%2C%22total_amount%22%3A+%221%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%7D&charset=UTF-8&format=json&method=alipay.trade.app.pay&notify_url=http%3A%2F%2Fapi.alpha.woaiwangpai.com%2Fapi%2FIntegral%2FnotifyHandle&sign_type=RSA2&timestamp=2020-06-02+11%3A08%3A39&version=1.0&sign=Qt0h%2BWRrK2NcmGrFQNPLEbdVQorUoX8RKRgaru87kY69gimuZzuT4ihT73CaKvKgLc7QmtRsPZYvQ1TyuxScncr%2FDRLCiaStc7YO6srNVp41ZVCmTDrUCdMVQf5wJ5zFTASOtkRlfK2ucwPedeC2I2YKj1uIoi5w79l3iELV34tLRSyZZukf73%2Bl%2FU3Xbgk4u0hgL4wfyyhMGULXer21sK4ZzBpquBJYVelIco5uQycHlN0YZOYyXYHBGufN%2Ff%2Bb6EsVaAxwPDAbdPq9EUaC7HDvOGTEVvO90so2%2FcrXR%2Fd55kj3lM67r8Xca9gqrQyVDx07XycLwjJHjiViEL3h4Q%3D%3D"));
                v.startAnimation(shake);
                ShareUtil.getInstance(MainActivity.this).alipayMe("fkx19000ssxku6zeqdfnc1f");
                //浏览器
//                ShareUtil.getInstance(MainActivity.this).openUrl("https://qr.alipay.com/fkx19000ssxku6zeqdfnc1f");
//                ShareUtil.getInstance(MainActivity.this).openUrl(ShareUtil.WX);
//                ShareUtil.getInstance(MainActivity.this).openUrl(ShareUtil.WX_Scan);
//                openUrl(AliPay_Barcode);  openUrl(AliPay_Scan);

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
                    wxApi.doPay(req);//pay
                    v.startAnimation(shake);
                } catch (JSONException ignored) {
                    ignored.printStackTrace();
                    ShareUtil.getInstance(MainActivity.this).openWxScan();//打开扫一扫
                }
            }
        });
        //小程序分享
        findViewById(R.id.btn_mini_wx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(shake);
//            WxShare wxShare=new WxShare(MainActivity.this,SocialType.WEIXIN_Share,onShareListener);
//            wxShare.doShare(WxShareEntity.createMiniApp( miniAppid, miniPath, webpageUrl, title, desc, imgUrl));

                WxAuth wxAuth = new WxAuth(MainActivity.this, onAuthListener);
                wxAuth.doOpenMiniApp("gh_d43f693ca31f", "", WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE);
            }
        });
        //微博分享
        findViewById(R.id.btn_share_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WbShare wbShare = new WbShare(MainActivity.this, onShareListener);
                wbShare.doShareStory(WbShareEntity.createImageStory(getExternalFilesDir(null) + "/aaa.png"));
                spi = wbShare;
                v.startAnimation(shake);
            }
        });
        //支付宝
        findViewById(R.id.btn_share_weibo).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AliShare aliShare = new AliShare(MainActivity.this, onShareListener);
                aliShare.doShare(AliShareEntity.createTextInfo(getExternalFilesDir(null) + "/aaa.png"));
                spi = aliShare;
                v.startAnimation(shake);
                return true;
            }
        });
        //微博登录
        findViewById(R.id.btn_login_weibo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WbAuth wbAuth = new WbAuth(MainActivity.this, onAuthListener);
                wbAuth.doAuth();
                api = wbAuth;
                v.startAnimation(shake);
            }
        });
        /**
         * 将 H5 网页版支付转换成支付宝 App 支付的示例
         */
        findViewById(R.id.btn_pay_alipay).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
                Intent intent = new Intent(MainActivity.this, H5PayDemoActivity.class);
                Bundle extras = new Bundle();

                /*
                 * URL 是要测试的网站，在 Demo App 中会使用 H5PayDemoActivity 内的 WebView 打开。
                 *
                 * 可以填写任一支持支付宝支付的网站（如淘宝或一号店），在网站中下订单并唤起支付宝；
                 * 或者直接填写由支付宝文档提供的“网站 Demo”生成的订单地址
                 * （如 https://mclient.alipay.com/h5Continue.htm?h5_route_token=303ff0894cd4dccf591b089761dexxxx）
                 * 进行测试。
                 *
                 * H5PayDemoActivity 中的 MyWebViewClient.shouldOverrideUrlLoading() 实现了拦截 URL 唤起支付宝，
                 * 可以参考它实现自定义的 URL 拦截逻辑。
                 */
                String url = "https://m.taobao.com";
                extras.putString("url", url);
                intent.putExtras(extras);
                startActivity(intent);
                return true;
            }
        });

    }

    //登陆回调
    private AuthApi.OnAuthListener onAuthListener = new AuthApi.OnAuthListener() {
        @Override
        public void onComplete(SocialType type, Object user) {
            switch (type) {
                case ALIPAY_Auth:
//                    ali AuthResult
                    AuthResult authResult = (AuthResult) user;
                    Log.e("code交给后端和登陆绑定手机等逻辑", authResult.getAuthCode());
                    Toast.makeText(MainActivity.this, "支付宝登录成功", Toast.LENGTH_SHORT).show();
                    break;
                case QQ_Auth:
                    Toast.makeText(MainActivity.this, "QQ登录成功", Toast.LENGTH_SHORT).show();
                    JSONObject data = (JSONObject) user;
                    try {
                        String openID = data.getString("openid");
                        String accessToken = data.getString("access_token");
                        String expires = data.getString("expires_in");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case WEIBO_Auth:
                    Toast.makeText(MainActivity.this, "微博登录成功", Toast.LENGTH_SHORT).show();
//                    wb（Oauth2AccessToken）user
                    String accessToken = ((Oauth2AccessToken) user).getAccessToken();
                    break;
                case WEIXIN_Auth:
                    Toast.makeText(MainActivity.this, "微信登录成功", Toast.LENGTH_SHORT).show();
//                    wx((WeiXin)user).getCode()
                    String code = ((WeiXin) user).getCode();
                    break;
            }

        }

        @Override
        public void onError(SocialType type, String error) {
            Toast.makeText(MainActivity.this, "登录失败:" + error, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(SocialType type) {
            Toast.makeText(MainActivity.this, "登录取消", Toast.LENGTH_SHORT).show();
        }
    };

    //支付回调
    private PayApi.OnPayListener onPayListener = new PayApi.OnPayListener() {
        @Override
        public void onPayOk(SocialType type) {
            Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPayFail(SocialType type, String msg) {
            Toast.makeText(MainActivity.this, "支付失败：" + msg, Toast.LENGTH_SHORT).show();
        }


    };
    //分享回调
    private ShareApi.OnShareListener onShareListener = new ShareApi.OnShareListener() {
        @Override
        public void onShareOk(SocialType type) {
            Toast.makeText(MainActivity.this, "分享成功", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onShareFail(SocialType type, String msg) {
            Toast.makeText(MainActivity.this, "分享失败:" + msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(SocialType type) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //微博和QQ需要
        if (api != null) {
            api.onActivityResult(requestCode, resultCode, data);
            api = null;
        }
        if (spi != null) {
            spi.onActivityResult(requestCode, resultCode, data);
            spi = null;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onStart() {
        Log.d("TAG", "-->onStart");
        PermissionMgr.getInstance().requestPermissions(this);
        PermissionMgr.getInstance().requestExternalStoragePermission(this);

        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionMgr.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        //xx
        if (1111 == requestCode) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(MainActivity.this, "请授予存储权限!", Toast.LENGTH_LONG).show();
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
