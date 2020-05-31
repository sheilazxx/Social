package com.mhy.socialcommon;

/**
 * @author mahongyin 2020-05-29 15:50 @CopyRight mhy.work@qq.com
 * description .
 */
public interface SocialType {

    /** QQ */
    int QQ_Share = 1;

    /** QQ空间 */
    int QQ_ZONE_Share = 2;

    /** 微博 */
    int WEIBO_Share = 3;

    /** 微信 */
    int WEIXIN_Share = 4;

    /** 微信朋友圈 */
    int WEIXIN_CIRCLE_Share = 5;

    /** 支付宝 */
    int ALIPAY_Share = 6;

    /** QQ */
    int QQ_Auth = 11;

    /** 微博 */
    int WEIBO_Auth = 13;

    /** 微信 */
    int WEIXIN_Auth = 12;

    /** 支付宝 */
    int ALIPAY_Auth = 14;

    /** 微信 */
    int WEIXIN_Pay = 21;

    /** 支付宝 */
    int ALIPAY_Pay = 22;
}
