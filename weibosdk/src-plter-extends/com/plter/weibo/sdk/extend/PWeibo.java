package com.plter.weibo.sdk.extend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.keep.AccessTokenKeeper;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;

/**
 * 
 * @author plter 
 * @website	http://plter.com 
 * @webshop	http://plter.taobao.com 
 * @blog	http://blog.plter.com
 *
 */
public class PWeibo {


	public PWeibo(Context context,String appKey,String appSec,String redirectUrl,String scope) {
		this.context = context;
		this.appKey = appKey;
		this.appSec = appSec;
		this.redirectUrl = redirectUrl;
		
		this.sinaWeibo = Weibo.getInstance(appKey, redirectUrl, scope);
		this.oauth2AccessToke = AccessTokenKeeper.readAccessToken(context);
	}

	private StatusesAPI statusesAPI = null;
	public void getStatusesAPI(final CBGetStatusesAPI statusesAPICallback){
		
		if (statusesAPI!=null) {
			if (statusesAPICallback!=null) {
				statusesAPICallback.suc(statusesAPI);
			}
		}else{
			getAccessToken(new CBGetAccessToken() {
				
				@Override
				public void suc(Oauth2AccessToken obj) {
					statusesAPI = new StatusesAPI(obj);
					if (statusesAPICallback!=null) {
						statusesAPICallback.suc(statusesAPI);
					}
				}
				
				@Override
				public void fail() {
					if (statusesAPICallback!=null) {
						statusesAPICallback.fail();
					}
				}
				
				@Override
				public void cancel() {
					if (statusesAPICallback!=null) {
						statusesAPICallback.fail();
					}
				}
			});
		}
	}
	
	
	public void getAccessToken(final CBGetAccessToken cbGetAccessToken){
		if (oauth2AccessToke.isSessionValid()) {
			if (cbGetAccessToken!=null) {
				cbGetAccessToken.suc(oauth2AccessToke);
			}
		}else{
			sinaWeibo.anthorize(context, new WeiboAuthListener() {

				@Override
				public void onWeiboException(WeiboException arg0) {
					if (cbGetAccessToken!=null) {
						cbGetAccessToken.fail();
					}
				}

				@Override
				public void onError(WeiboDialogError arg0) {
					if (cbGetAccessToken!=null) {
						cbGetAccessToken.fail();
					}
				}

				@Override
				public void onComplete(Bundle arg0) {

					String code = arg0.getString("code");
					if (code!=null) {

						WeiboParameters wp = new WeiboParameters();
						wp.add("client_id", appKey);
						wp.add("client_secret", appSec);
						wp.add("grant_type", "authorization_code");
						wp.add("redirect_uri", redirectUrl);
						wp.add("code", code);

						AsyncWeiboRunner.request(API_ACCESS_TOKEN, wp , "POST", new RequestListener() {

							@Override
							public void onIOException(IOException arg0) {
								if (cbGetAccessToken!=null) {
									cbGetAccessToken.fail();
								}
							}

							@Override
							public void onError(WeiboException arg0) {
								if (cbGetAccessToken!=null) {
									cbGetAccessToken.cancel();
								}
							}

							@Override
							public void onComplete4binary(ByteArrayOutputStream arg0) {
							}

							@Override
							public void onComplete(String arg0) {
								try {
									JSONObject jo = new JSONObject(arg0);
									String accessToken=jo.getString("access_token");
									String expires_in=jo.getString("expires_in");

									oauth2AccessToke = new Oauth2AccessToken(accessToken, expires_in);
									AccessTokenKeeper.keepAccessToken(context, oauth2AccessToke);

									if (cbGetAccessToken!=null) {
										cbGetAccessToken.suc(oauth2AccessToke);
									}
								} catch (JSONException e) {
									e.printStackTrace();

									if (cbGetAccessToken!=null) {
										cbGetAccessToken.fail();
									}
								}
							}
						});
					}else{
						if (cbGetAccessToken!=null) {
							cbGetAccessToken.fail();
						}
					}
				}

				@Override
				public void onCancel() {
					if (cbGetAccessToken!=null) {
						cbGetAccessToken.cancel();
					}
				}
			});
		}
	}

	public Weibo getSinaWeibo() {
		return sinaWeibo;
	}


	public Context getContext() {
		return context;
	}


	public String getAppKey() {
		return appKey;
	}


	public String getAppSec() {
		return appSec;
	}


	public String getRedirectUrl() {
		return redirectUrl;
	}

	private Weibo sinaWeibo=null;
	private Oauth2AccessToken oauth2AccessToke = null;
	private Context context=null;
	private String appKey = null;
	private String appSec = null;
	private String redirectUrl = null;

	public static final String API_ACCESS_TOKEN = "https://api.weibo.com/oauth2/access_token";

}
