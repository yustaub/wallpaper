package com.test.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        try {
            String str = App.sendGETRequest("https://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1");
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(str);
            JsonArray array = object.get("images").getAsJsonArray();
            assertTrue(array.size() >= 1);
            JsonObject subObject = array.get(0).getAsJsonObject();
            String relativePath = subObject.get("url").getAsString();
            System.out.println(relativePath);
            boolean foundMatch = false;
            try {
                Pattern regex = Pattern.compile("/az/hprichbg/rb/.+");
                Matcher regexMatcher = regex.matcher(relativePath);
                foundMatch = regexMatcher.matches();
            } catch (PatternSyntaxException ex) {
                // Syntax error in the regular expression
            }
            assertTrue(foundMatch);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testUpdateWallpaper() throws IOException {
        System.out.println("更新一个用户");
        // 读取mybatis-config.xml文件
        InputStream resourceAsStream = Resources.getResourceAsStream("mybatis-config.xml");
        // 初始化mybatis,创建SqlSessionFactory类的实例
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        // 创建session实例
        SqlSession session = sqlSessionFactory.openSession();
        try {
            com.test.common.domain.Wallpaper wallpaper = new com.test.common.domain.Wallpaper();
            wallpaper.setId(1);
            wallpaper.setTitle("pkd888888");
            wallpaper.setUrl("pkd888888");
            wallpaper.setCopyright("pkd888888");
            int count = session.update("com.test.common.IDao.WallpaperMapper.updateByPrimaryKey", wallpaper);
            assertTrue(count == 1);
            // session.delete("com.test.common.IDao.WallpaperMapper.deleteByPrimaryKey",
            // wallpaper);
            session.commit();
            System.out.println(count);
        } finally {
            session.close();
        }

    }

}
