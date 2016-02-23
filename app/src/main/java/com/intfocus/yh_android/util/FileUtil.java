package com.intfocus.yh_android.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
	public static String basePath(Context context) {
		String basePath = URLs.storage_base(context);
		FileUtil.makeSureFolderExist(basePath);

		return basePath;
	}

    public  static boolean checkIsLocked(Context context) {
        try {
            String userConfigPath = String.format("%s/%s", FileUtil.basePath(context), URLs.USER_CONFIG_FILENAME);
            if((new File(userConfigPath)).exists()) {
                JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);
                if(!userJSON.has("use_gesture_password")) {
                    userJSON.put("use_gesture_password", false);
                    Log.i("ScreenLock", "use_gesture_password not set");
                }
                if(!userJSON.has("gesture_password")) {
                    userJSON.put("gesture_password", false);
                    Log.i("ScreenLock", "gesture_password not set");
                }
                if(!userJSON.has("is_login")) {
                    userJSON.put("is_login", false);
                    Log.i("ScreenLock", "is_login not set");
                }

                FileUtil.writeFile(userConfigPath, userJSON.toString());

                return userJSON.getBoolean("use_gesture_password") && userJSON.getBoolean("is_login");
            } else {
                Log.i("ScreenLock", "userConfigPath not exist");

                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
    }

	public static String userspace(Context context) {
		String spacePath = "";
		try {
	        String userConfigPath = String.format("%s/%s", FileUtil.basePath(context), URLs.USER_CONFIG_FILENAME);
	        JSONObject json = FileUtil.readConfigFile(userConfigPath);

			spacePath = String.format("%s/User-%d", FileUtil.basePath(context), json.getInt("user_id"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return spacePath;
	}

	/**
	 *  传递目录名取得沙盒中的绝对路径(一级),不存在则创建，请慎用！
	 *
	 *  @param dirName  目录名称，不存在则创建
	 *
	 *  @return 沙盒中的绝对路径
	 */
	public static String dirPath(Context context, String dirName) {
		String pathName = String.format("%s/%s", FileUtil.userspace(context), dirName);
		FileUtil.makeSureFolderExist(pathName);
		
		return pathName;
	}
	
	public static String dirPath(Context context, String dirName, String fileName) {
		String pathName = FileUtil.dirPath(context, dirName);
		
		return String.format("%s/%s", pathName, fileName);
	}

	public static String dirsPath(Context context, String[] dirNames) {

		return FileUtil.dirPath(context, TextUtils.join("/", dirNames));
	}

	/*
	 * 读取本地文件内容
	 */
	public static String readFile(String pathName) {
		String string = null;
		try {
			InputStream inputStream = new FileInputStream(new File(pathName));
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			StringBuilder stringBuilder = new StringBuilder();
			while((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
			bufferedReader.close();
			inputStreamReader.close();
			string = stringBuilder.toString();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return string;
	}

	/*
	 * 读取本地文件内容，并转化为json
	 */
	public static JSONObject readConfigFile(String pathName) {
		JSONObject jsonObject = null;
		try {
			String string = FileUtil.readFile(pathName);
	        jsonObject = new JSONObject(string);
	    } catch (JSONException e) {
	      e.printStackTrace();
	    }
       return jsonObject;
	}

	/*
	 * 字符串写入本地文件
	 */
	public static void writeFile(String pathName, String content) throws IOException {
		Log.i("PathName", pathName);
		File file = new File(pathName);

		if(file.exists()) {
			file.delete();
		}

		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file, true);
		out.write(content.toString().getBytes("utf-8"));
		out.close();
	}

	/*
	 *  共享资源
	 *  1. assets资源
	 *  2. loading页面
	 *  3. 登录缓存页面
	 */
	public static String sharedPath(Context context) {
		String pathName = FileUtil.basePath(context) + "/" + URLs.SHARED_DIRNAME;
        FileUtil.makeSureFolderExist(pathName);

		return pathName;
	}

	public static boolean makeSureFolderExist(String pathName) {
		File folder = new File(pathName);
		if(folder.exists() && folder.isDirectory()) {
			return true;
		} else {
			return folder.mkdirs();
		}
	}

	/*
	 * 共享资源中的文件（夹）（忽略是否存在）
	 */
	public static String sharedPath(Context context, String folderName) {
		if(!folderName.startsWith("/")) {
			folderName = String.format("/%s", folderName);
		}

		String pathName = String.format("%s%s", FileUtil.sharedPath(context), folderName);
		return pathName;
	}


	/*
	 * Generage MD5 value for ZIP file
	 */
	private static String convertByteArrayToHexString(byte[] arrayBytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return stringBuffer.toString();
	}

	/*
	 * algorithm can be "MD5", "SHA-1", "SHA-256"
	 */
	private static String hashFile(File file, String algorithm) {
		try {
			FileInputStream inputStream = new FileInputStream(file);
			MessageDigest digest = MessageDigest.getInstance(algorithm);

			byte[] bytesBuffer = new byte[1024];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
				digest.update(bytesBuffer, 0, bytesRead);
			}

			byte[] hashedBytes = digest.digest();
			return convertByteArrayToHexString(hashedBytes);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "hashFile - exception catched";
	}
	public static String MD5(File file) {
		return hashFile(file, "MD5");
	}
	public static String MD5(InputStream inputStream) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			byte[] bytesBuffer = new byte[1024];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
				digest.update(bytesBuffer, 0, bytesRead);
			}

			byte[] hashedBytes = digest.digest();
			return convertByteArrayToHexString(hashedBytes);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return "MD5 - exception catched";
	}


	/**
	 * 解压assets的zip压缩文件到指定目录
	 * @throws IOException
	 */
	public static void unZip(InputStream inputStream, String outputDirectory, boolean isReWrite) throws IOException {
		// 创建解压目标目录
		File file = new File(outputDirectory);
		// 如果目标目录不存在，则创建
		if (!file.exists()) {
			file.mkdirs();
		}
		// 打开压缩文件
		//InputStream inputStream = getApplicationContext().getAssets().open(assetName);
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		// 读取一个进入点
		ZipEntry zipEntry = zipInputStream.getNextEntry();
		// 使用1Mbuffer
		byte[] buffer = new byte[10*1024 * 1024];
		// 解压时字节计数
		int count = 0;
		// 如果进入点为空说明已经遍历完所有压缩包中文件和目录
		while (zipEntry != null) {
			// 如果是一个目录
			if (zipEntry.isDirectory()) {
				file = new File(outputDirectory + File.separator + zipEntry.getName());
				// 文件需要覆盖或者是文件不存在
				if (isReWrite || !file.exists()) {
					file.mkdir();
				}
			} else {
				// 如果是文件
				file = new File(outputDirectory + File.separator + zipEntry.getName());
				// 文件需要覆盖或者文件不存在，则解压文件
				if (isReWrite || !file.exists()) {
					file.createNewFile();
					FileOutputStream fileOutputStream = new FileOutputStream(file);
					while ((count = zipInputStream.read(buffer)) > 0) {
						fileOutputStream.write(buffer, 0, count);
					}
					fileOutputStream.close();
				}
			}
			// 定位到下一个文件入口
			zipEntry = zipInputStream.getNextEntry();
		}
		zipInputStream.close();
	}

	public static void checkAssets(Context mContext, String fileName) {
		try {
			String sharedPath = FileUtil.sharedPath(mContext);
			String zipName = String.format("%s.zip", fileName);

			// InputStream zipStream = mContext.getApplicationContext().getAssets().open(zipName);
			String zipPath = String.format("%s/%s", sharedPath, zipName);
			InputStream zipStream = new FileInputStream(zipPath);

			String MD5String = FileUtil.MD5(zipStream);
			String keyName = String.format("local_%s_md5", fileName);

			String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
			boolean isShouldUnZip = true;
			JSONObject userJSON = new JSONObject();
			if((new File(userConfigPath)).exists()) {
				userJSON = FileUtil.readConfigFile(userConfigPath);
				if(userJSON.has(keyName) && userJSON.getString(keyName).equals(MD5String)) {
					isShouldUnZip = false;
				} else {
					Log.i("checkAssets", String.format("%s: %s != %s", zipName, userJSON.getString(keyName), MD5String));
				}
			}

			if(isShouldUnZip) {
				File file = new File(String.format("%s/%s", sharedPath, fileName));
				if(file.exists()) {
					Log.i("deleteDirectory", file.getAbsolutePath());
					FileUtils.deleteDirectory(file);
				}

				zipStream = mContext.getApplicationContext().getAssets().open(zipName);
				FileUtil.unZip(zipStream, FileUtil.sharedPath(mContext), true);
				Log.i("unZip", String.format("%s, %s", zipName, MD5String));

				userJSON.put(keyName, MD5String);
				FileUtil.writeFile(userConfigPath, userJSON.toString());
			}

			zipStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝程序自带的文件至指定文件
	 *
	 * @param assetName 程序自带的文件名称
	 * @param outputPath 拷贝到指定文件的路径
	 */
	public static void copyAssetFile(Context mContext, String assetName, String outputPath) {
		try {
			InputStream zipStream = mContext.getApplicationContext().getAssets().open(assetName);
			FileOutputStream fos = new FileOutputStream(outputPath);
			byte[] b = new byte[1024];
			while((zipStream.read(b)) != -1){
				fos.write(b);
			}
			zipStream.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
