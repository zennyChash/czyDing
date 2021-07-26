package com.ifugle.czy.utils;

import java.net.URLEncoder;
import java.security.MessageDigest;
public class MD5Util {
	private static final String ENCODING = "UTF-8";
	private static final String MD5 = "MD5";
	private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	public final static String MD5(String s) {
		try {
			s = URLEncoder.encode(s, ENCODING);
			byte[] btInput = s.getBytes(ENCODING);
			MessageDigest mdInst = MessageDigest.getInstance(MD5);
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = CHARS[byte0 >>> 4 & 0xf];
				str[k++] = CHARS[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
}
