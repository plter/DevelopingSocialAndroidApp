package com.plter.weibo.sdk.extend;


/**
 * 
 * @author plter 
 * @website	http://plter.com 
 * @webshop	http://plter.taobao.com 
 * @blog	http://blog.plter.com
 *
 */
public interface CB<T> {
	void suc(T obj);
	void fail();
}
