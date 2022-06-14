package com.lr.douyinspider;

import com.lr.douyinspider.handler.DouyinHandler;
import com.lr.douyinspider.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class DouyinSpiderApplicationTests {

    @Autowired
    private DouyinHandler douyinHandler;

    @Test
    void contextLoads() {
    }

    /**
     * 功能描述: 根据UP主主页获取所有视频
     * @Param: []
     * @Return: void
     * @Author: LR
     * @Date: 2022/6/15 4:42
     */
    @Test
    void getByUserTest() {
        //String url = "https://www.douyin.com/user/MS4wLjABAAAAKmukLDltHuaaGilpSElJSP3Y01M-DD_B98kQnnQtVi4";
        String url = "https://www.douyin.com/user/MS4wLjABAAAAvmL9E2Q0GrKqxlrEwdLpvhDroVzDA2V3yM0Jpw4ws5U";
        douyinHandler.getVideoMainUrlsFromUser(url);
    }

    /**
     * 功能描述: 根据分享链接获取所有视频
     * @Param: []
     * @Return: void
     * @Author: LR
     * @Date: 2022/6/15 4:43
     */
    @Test
    void getByShareUrl() {
        String url = "7.61 WZZ:/ 太真实了%专治不开心   https://v.douyin.com/YrcjRgk/ 复制此链接，打开Dou音搜索，直接观看视频！";
        douyinHandler.getVideoFromShareUrl(url);
    }

}
