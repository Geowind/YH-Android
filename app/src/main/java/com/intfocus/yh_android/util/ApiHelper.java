package com.intfocus.yh_android.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.OpenUDID.OpenUDID_manager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ApiHelper {

	/*
	 * 用户登录验证
	 * params: {device: {name, platform, os, os_version, uuid}}
	 */
	public static String authentication(Context context, String username, String password) {
		String ret = "success";

		String urlString = String.format(URLs.API_USER_PATH, URLs.HOST, "android", username, password);
		try {
    		Map<String, String> device = new HashMap();
    		device.put("name", android.os.Build.MODEL);
    		device.put("platform", "android");
    		device.put("os", android.os.Build.MODEL);
    		device.put("os_version", Build.VERSION.RELEASE);
    		device.put("uuid", OpenUDID_manager.getOpenUDID());
    		Map<String, Map<String, String>> params = new HashMap();
    		params.put("device", device);
    		
			Map<String, String> response = HttpUtil.httpPost(urlString, params);

			JSONObject responseJSON = new JSONObject(response.get("body").toString());

			String userConfigPath = String.format("%s/%s", FileUtil.basePath(context), URLs.USER_CONFIG_FILENAME);
			JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);
			userJSON.put("password", password);
			userJSON.put("is_login", response.get("code").equals("200"));

			// FileUtil.dirPath 需要优先写入登录用户信息
			userJSON = ApiHelper.merge(userJSON, responseJSON);
			FileUtil.writeFile(userConfigPath, userJSON.toString());
			String settingsConfigPath = FileUtil.dirPath(context, URLs.CONFIG_DIRNAME, URLs.SETTINGS_CONFIG_FILENAME);
			if((new File(settingsConfigPath)).exists()) {
				JSONObject settingJSON = FileUtil.readConfigFile(settingsConfigPath);
				if(settingJSON.has("use_gesture_password")) {
					userJSON.put("use_gesture_password", settingJSON.getBoolean("use_gesture_password"));
				} else {
					userJSON.put("use_gesture_password",false);
				}
				if(settingJSON.has("gesture_password")) {
					userJSON.put("gesture_password", settingJSON.getString("gesture_password"));
				} else {
					userJSON.put("gesture_password", "");
				}
			} else {
				userJSON.put("use_gesture_password", false);
				userJSON.put("gesture_password", "");
			}
			FileUtil.writeFile(userConfigPath, userJSON.toString());

			Log.i("CurrentUser", userJSON.toString());
			if(response.get("code").equals("200")) {
				FileUtil.writeFile(settingsConfigPath, userJSON.toString());
			}
			else {
				ret = responseJSON.getString("info");
			}
		} catch(Exception e) {
			e.printStackTrace();
			ret = e.getMessage();
		}
		return ret;
	}

	/*
	 *  获取报表网页数据
	 */
	public static void reportData(Context context, String groupID, String reportID) {
		String assetsPath = FileUtil.sharedPath(context);
		String urlPath   = String.format(URLs.API_DATA_PATH, groupID, reportID);
		String urlString = String.format("%s%s", URLs.HOST, urlPath);

		String fileName  = String.format(URLs.REPORT_DATA_FILENAME, groupID, reportID);
		String filePath  = String.format("%s/assets/javascripts/%s", assetsPath, fileName);

		Map<String, String> headers = ApiHelper.checkResponseHeader(urlString, assetsPath);
		Map<String, String> response = HttpUtil.httpGet(urlString, headers);

		if(response.get("code").equals("200")) {
			try {
				ApiHelper.storeResponseHeader(urlString, assetsPath, response);

				FileUtil.writeFile(filePath, response.get("body").toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			Log.i("Code", response.get("code").toString());
		}
	}

	/*
	 * 发表评论
	 */
	public static void writeComment(int userID, int objectType, int objectID, Map params) throws UnsupportedEncodingException, JSONException {
		String urlPath   = String.format(URLs.API_COMMENT_PATH, userID, objectID, objectType);
		String urlString = String.format("%s%s", URLs.HOST, urlPath);

		Map<String, String> response = HttpUtil.httpPost(urlString, params);
		Log.i("WriteComment", response.get("code").toString());
		Log.i("WriteComment", response.get("body").toString());
	}

	public static Map<String, String> httpGetWithHeader(String urlString, String assetsPath, String relativeAssetsPath) {
		Map<String, String> retMap = new HashMap<String, String>();

		String urlKey = urlString.indexOf("?") != -1 ? TextUtils.split(urlString, "?")[0] : urlString;

		try {
			Map<String, String> headers = ApiHelper.checkResponseHeader(urlString, assetsPath);

			Map<String, String> response = HttpUtil.httpGet(urlKey, headers);
			String statusCode = response.get("code").toString();
			retMap.put("code", statusCode);

			String htmlName = HttpUtil.UrlToFileName(urlString);
			String htmlPath = String.format("%s/%s", assetsPath, htmlName);
			retMap.put("path", htmlPath);

			if (statusCode.equals("200")) {
				ApiHelper.storeResponseHeader(urlKey, assetsPath, response);

				String htmlContent = response.get("body").toString();
				htmlContent = htmlContent.replace("/javascripts/", String.format("%s/javascripts/", relativeAssetsPath));
				htmlContent = htmlContent.replace("/stylesheets/", String.format("%s/stylesheets/", relativeAssetsPath));
				htmlContent = htmlContent.replace("/images/", String.format("%s/images/", relativeAssetsPath));
				FileUtil.writeFile(htmlPath, htmlContent);
			}
		} catch (Exception e) {
			retMap.put("code", "500");
			e.printStackTrace();
		}

		return retMap;
	}

	public static Map<String, String> resetPassword(String userID, String newPassword) {
		Map<String, String> retMap = new HashMap<String, String>();

		try {
			String urlPath = String.format(URLs.API_RESET_PASSWORD_PATH, userID);
			String urlString = String.format("%s/%s", URLs.HOST, urlPath);

			Map<String, String> params = new HashMap<String, String>();
			params.put("password", newPassword);
			retMap = HttpUtil.httpPost(urlString, params);
		} catch(Exception e) {
			e.printStackTrace();
			retMap.put("code", "500");
			retMap.put("body", e.getLocalizedMessage());
		}
		return retMap;
	}
	/*
	 * assistant methods
	 */
	public static void clearResponseHeader(String urlKey, String assetsPath) {
//		File file = new File(headersFilePath);
//		if(file.exists()) {
//			file.delete();
//		}

		String headersFilePath = String.format("%s/%s", assetsPath, URLs.CACHED_HEADER_FILENAME);
		if(!(new File(headersFilePath)).exists()) {
			return;
		}

		JSONObject headersJSON = FileUtil.readConfigFile(headersFilePath);

		if(headersJSON.has(urlKey)) {
			headersJSON.remove(urlKey);

			Log.i("clearResponseHeader", headersFilePath);
			Log.i("clearResponseHeader", urlKey);
			try {
				FileUtil.writeFile(headersFilePath, headersJSON.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static Map<String, String> checkResponseHeader(String urlKey, String assetsPath) {
		Map<String, String> headers = new HashMap<String, String>();

		try {
			JSONObject headersJSON = new JSONObject();

			String headersFilePath = String.format("%s/%s", assetsPath, URLs.CACHED_HEADER_FILENAME);
			if((new File(headersFilePath)).exists()) {
				headersJSON = FileUtil.readConfigFile(headersFilePath);
			}
			JSONObject headerJSON = new JSONObject();

			if(headersJSON.has(urlKey)) {
				headerJSON = (JSONObject)headersJSON.get(urlKey);
				if(headerJSON.has("ETag")) {
					headers.put("ETag", headerJSON.getString("ETag"));
				}
				if(headerJSON.has("Last-Modified")) {
					headers.put("Last-Modified", headerJSON.getString("Last-Modified"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return headers;
	}

	public static void storeResponseHeader(String urlKey, String assetsPath, Map<String, String> response) {
		try {
			JSONObject headersJSON = new JSONObject();

			String headersFilePath = String.format("%s/%s", assetsPath, URLs.CACHED_HEADER_FILENAME);
			if((new File(headersFilePath)).exists()) {
				headersJSON = FileUtil.readConfigFile(headersFilePath);
			}
			JSONObject headerJSON = new JSONObject();

			if(response.containsKey("ETag")) {
				headerJSON.put("ETag", response.get("ETag").toString());
			}
			if(response.containsKey("Last-Modified")) {
				headerJSON.put("Last-Modified", response.get("Last-Modified").toString());
			}

			headersJSON.put(urlKey, headerJSON);
			FileUtil.writeFile(headersFilePath, headersJSON.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 合并两个JSONObject
	 */
	public static JSONObject merge(JSONObject obj1, JSONObject obj2) {
		try {
			Iterator it = obj2.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				obj1.put(key, obj2.get(key));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return obj1;
	}


	public static void downloadFile(Context context, String urlString, File outputFile) {
		try {
			URL url = new URL(urlString);
			String headerPath = String.format("%s/%s/%s", FileUtil.basePath(context), URLs.CACHED_DIRNAME, URLs.CACHED_HEADER_FILENAME);

			JSONObject headerJSON = new JSONObject();
			if ((new File(headerPath)).exists()) {
				headerJSON = FileUtil.readConfigFile(headerPath);
			}

			URLConnection conn = url.openConnection();
			String etag = conn.getHeaderField("ETag");

			boolean isDownloaded = outputFile.exists() && headerJSON.has(urlString) && etag != null && !etag.isEmpty() && headerJSON.getString(urlString).equals(etag);

			if(isDownloaded) {
				Log.i("downloadFile", "exist - " + outputFile.getAbsolutePath());
			} else {
				InputStream in = url.openStream();
				FileOutputStream fos = new FileOutputStream(outputFile);

				int length = -1;
				byte[] buffer = new byte[1024*10];// buffer for portion of data from connection
				while ((length = in.read(buffer)) > -1) {
					fos.write(buffer, 0, length);
				}
				fos.close();
				in.close();

				if(etag != null && !etag.isEmpty()) {
					headerJSON.put(urlString, etag);
					FileUtil.writeFile(headerPath, headerJSON.toString());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void screenLock(String deviceID, String password, boolean state) {
		String urlPath   = String.format(URLs.API_SCREEN_LOCK_PATH, deviceID);
		String urlString = String.format("%s%s", URLs.HOST, urlPath);

		try {
			Map<String, String> params = new HashMap();
			params.put("screen_lock_state", "1");
			params.put("screen_lock_type", "4位数字");
			params.put("screen_lock", password);

			HttpUtil.httpPost(urlString, params);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
