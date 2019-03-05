import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.*;

public class MyWallpaper {
	public static void main(String[] argc) throws ParserConfigurationException, SAXException, IOException {
		MyWallpaper wallpaper = new MyWallpaper();

		do {
			String path = wallpaper.getThePath();
			wallpaper.downLoadWallpaper(path);
			wallpaper.settingWallpaper();
		} while (wallpaper.isConnect() != true);
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

	public boolean isConnect() throws IOException {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec("ping www.baidu.com");
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		if (br.readLine() == null) {
			// System.out.println("The network is wrong!");
			return false;
		} else {
			// System.out.println("The network is well");
			return true;
		}
	}

	public String getThePath() throws ParserConfigurationException, SAXException, IOException {
		// getting the path of the bing jpg picture via analysis xml
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse("http://www.bing.com/HPImageArchive.aspx?format=xml&idx=0&n=8");
		document.normalize();
		String relativePath = document.getElementsByTagName("url").item(0).getTextContent();
		String path = "http://www.bing.com/" + relativePath;
		return path;
	}

	public void downLoadWallpaper(String path) throws IOException {
		// download the jpg file
		path = path.replace("1366x768", "1920x1080");
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
}