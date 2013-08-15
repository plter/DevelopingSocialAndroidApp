package com.plter.weibo.sdk.extend;

import com.weibo.sdk.android.Oauth2AccessToken;


/**
 * 
 * @author plter 
 * @website	http://plter.com 
 * @webshop	http://plter.taobao.com 
 * @blog	http://blog.plter.com
 *
 */
public interface CBGetAccessToken extends CB<Oauth2AccessToken> {

	void cancel();
}
