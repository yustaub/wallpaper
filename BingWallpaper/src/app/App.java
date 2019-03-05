package app;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

public class App {

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

		SPI INSTANCE = (SPI) Native.loadLibrary("user32", SPI.class, new HashMap<Object, Object>() {
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
		return path;
	}

	public void downLoadWallpaper(String path) throws IOException {
		// download the jpg file
		URL url = new URL(path);
		DataInputStream dis = new DataInputStream(url.openStream());
		FileOutputStream fos = new FileOutputStream(new File("D:\\TEMP\\wallpaper.jpg"));
		byte[] buffer = new byte[1024];
		int length;
		while ((length = dis.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
		}

		dis.close();
		fos.close();
	}

	public void settingWallpaper() {
		String localpath = "D:\\TEMP\\wallpaper.jpg";

		SPI.INSTANCE.SystemParametersInfo(new UINT_PTR(SPI.SPI_SETDESKWALLPAPER), new UINT_PTR(0), localpath,
				new UINT_PTR(SPI.SPIF_UPDATEINIFILE | SPI.SPIF_SENDWININICHANGE));
	}
    public static void main(String[] args) {
        try {
            String str = sendGETRequest("https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1");
            System.out.println(str);
            App wallpaper = new App();
            String path = wallpaper.getThePath(str);
			wallpaper.downLoadWallpaper(path);
			wallpaper.settingWallpaper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
