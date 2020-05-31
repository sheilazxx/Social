package com.mhy.socialcommon;

/**
 * @author mahongyin 2020-05-29 17:41 @CopyRight mhy.work@qq.com
 * description payinfo 基类
 */
public abstract class PayContent {
    /**
     * @see SocialType
     * @return SocialType
     */
    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    protected int payType;
}
