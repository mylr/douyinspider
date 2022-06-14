package com.lr.douyinspider.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * @ClassName OKHttpDownLoadUtil
 * @Description OKHttpDownLoadUtil
 * @Author lR
 * @Date 2020/12/18 15:20
 */
public class HttpUtil {

    private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    // 默认编码方式
    private static final String ENCODE = "UTF-8";

    /**
     * 功能描述: 请求数据
     * @Param: [url, cookie]
     * @Return: java.lang.String
     * @Author: LR
     * @Date: 2022/6/14 21:54
     */
    public static String fetchData(String url, String cookie) {
        HttpURLConnection urlConnection = null;
        try {
            URL mUrl = new URL(url);
            urlConnection = (HttpURLConnection) mUrl.openConnection();
            urlConnection.addRequestProperty("Accept-Charset", ENCODE);
            urlConnection.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");//urlConnection.addRequestProperty("Accept-Encoding", "gzip, deflate");
            urlConnection.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            urlConnection.addRequestProperty("Cookie", cookie);
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, getEncode(urlConnection)));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }


    /**
     * 下载文件到指定位置
     * @param url
     * @param dest
     */
    public static void downloadFile(String url, String dest, String fileName) {
        try {
            logger.info("开始下载：{}", url);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            //创建接收文件的流
            new File(dest).mkdirs();
            File file = new File(dest.concat("\\").concat(fileName));
            OutputStream outputStream = new FileOutputStream(file);
            //将responseBody截取并写入到指定文件路径下
            outputStream.write(response.body().bytes());
            outputStream.flush();
            outputStream.close();
            logger.info("下载完成：{},文件名：{}", url, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("下载文件异常，下载地址:{}，异常信息:{}", url, e);
        }
    }

    /**
     * 获取分享链接重定向后的链接
     * @param shareUrl
     * @return
     */
    public static String fetchRedirectUrl(String shareUrl) {
        HttpURLConnection connection = null;
        try {
            URL mUrl = new URL(shareUrl);
            connection = (HttpURLConnection) mUrl.openConnection();
            //不允许自动重定向
            connection.setInstanceFollowRedirects(false);
            //发生重定向返回重定向后的地址
            if (connection.getResponseCode() == 302)
                return connection.getHeaderField("Location");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        throw new RuntimeException("Fetch redirect url failed!");
    }

    /**
     * 确定编码方式
     * @param connection
     * @return
     */
    public static String getEncode(HttpURLConnection connection) {
        String encode = connection.getContentEncoding();
        if (encode != null) {
            return encode;
        }
        String contentType = connection.getContentType();
        for (String value : contentType.split(";")) {
            value = value.trim();
            if (value.toLowerCase(Locale.US).startsWith("charset=")) {
                return value.substring(8);
            }
        }

        return ENCODE;
    }
}
