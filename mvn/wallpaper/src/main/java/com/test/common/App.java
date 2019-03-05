package com.test.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Hello world!
 *
 */
public class App {
	private Wallpaper wallpaper;

	public static String sendGETRequest(String path) throws Exception {
		StringBuilder url = new StringBuilder(path);
		HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == 200) {
			StringBuffer buffer = new StringBuffer();
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			bufferedReader.close();
			inputStreamReader.close();
			// 释放资源
			inputStream.close();
			inputStream = null;
			conn.disconnect();
			return buffer.toString();
		}
		return null;
	}

	public interface SPI extends StdCallLibrary {

		long SPI_SETDESKWALLPAPER = 20;
		long SPIF_UPDATEINIFILE = 0x01;
		long SPIF_SENDWININICHANGE = 0x02;

		SPI INSTANCE = (SPI) Native.loadLibrary("user32", SPI.class, new HashMap<String, Object>() {
			{
				put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
				put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
			}
		});

		boolean SystemParametersInfo(UINT_PTR uiAction, UINT_PTR uiParam, String pvParam, UINT_PTR fWinIni);
	}

	public String getThePath(String str) throws JsonSyntaxException, IOException {
		JsonParser parser = new JsonParser();
		JsonObject object = (JsonObject) parser.parse(str);
		JsonArray array = object.get("images").getAsJsonArray();
		JsonObject subObject = array.get(0).getAsJsonObject();
		String relativePath = subObject.get("url").getAsString();
		// getting the path of the bing jpg picture via analysis xml
		String path = "http://www.bing.com/" + relativePath;
		String title = null;
		String copyright = null;
		try {
			Pattern regex = Pattern.compile("(.*)\\((.*)\\)");
			Matcher regexMatcher = regex.matcher(subObject.get("copyright").getAsString());
			if (regexMatcher.find()) {
				title = regexMatcher.group(1);
				copyright = regexMatcher.group(2);
			} 
		} catch (PatternSyntaxException ex) {
			// Syntax error in the regular expression
		}		
		this.wallpaper = new Wallpaper();
		this.wallpaper.setTitle(title);
		this.wallpaper.setUrl(subObject.get("url").getAsString());
		this.wallpaper.setCopyright(copyright);
		return path;
	}

	public void downLoadWallpaper(String path) throws IOException {
		// download the jpg file
		URL url = new URL(path);
		DataInputStream dis = new DataInputStream(url.openStream());
		FileOutputStream fos = new FileOutputStream(new File("C:\\TEMP\\wallpaper.jpg"));
		byte[] buffer = new byte[1024];
		int length;
		while ((length = dis.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
		}

		dis.close();
		fos.close();
	}

	public void settingWallpaper() {
		String localpath = "C:\\TEMP\\wallpaper.jpg";

		SPI.INSTANCE.SystemParametersInfo(new UINT_PTR(SPI.SPI_SETDESKWALLPAPER), new UINT_PTR(0), localpath,
				new UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
	}

	public static Connection getConn() {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/train?useUnicode=true&characterEncoding=UTF-8";
		String username = "root";
		String password = "";
		Connection conn = null;
		try {
			Class.forName(driver); // classLoader,加载对应驱动
			conn = (Connection) DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	public static int insertWallpaper(Wallpaper wallpaper) {
		Connection conn = getConn();
		int i = 0;
		String sql = "insert into wallpaper (title, url, copyright) values (?, ?, ?)";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, wallpaper.getTitle());
			pstmt.setString(2, wallpaper.getUrl());
			pstmt.setString(3, wallpaper.getCopyright());
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}

	public static boolean existWallpaper(Wallpaper wallpaper) {
		boolean exist = false;
		Connection conn = getConn();
		String sql = "select * from wallpaper where url = ?";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, wallpaper.getUrl());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				exist = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return exist;
	}

	public static void main(String[] args) {
		try {
			String str = sendGETRequest("https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1");
			System.out.println(str);
			App app = new App();
			String path = app.getThePath(str);
			app.downLoadWallpaper(path);
			app.settingWallpaper();
			if (!App.existWallpaper(app.wallpaper)) {
				App.insertWallpaper(app.wallpaper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
