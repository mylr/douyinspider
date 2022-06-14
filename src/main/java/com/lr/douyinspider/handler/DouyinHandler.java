package com.lr.douyinspider.handler;

import com.lr.douyinspider.util.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName:DouyinHandler
 * @Description: 抖音处理器
 * @Auther: LR
 * @Date: 2022/6/13 20:13
 */
@Service
public class DouyinHandler {

    private final static Logger logger = LoggerFactory.getLogger(DouyinHandler.class);

    @Value("${webdriver.name}")
    private String webdriverName;

    @Value("${webdriver.location}")
    private String webdriverLocation;

    @Value("${chromeOptions.isHeadless}")
    private Boolean isHeadless;

    @Value("${douyin.Cookie}")
    private String cookie;

    @Value("${video.download.location}")
    private String location;

    @Resource(name = "downloadExecutor")
    private ThreadPoolTaskExecutor downloadExecutor;

    /**
     * 功能描述: 根据UP主主页获取所有的视频节点
     * @Param: [userUrl]
     * @Return: org.jsoup.select.Elements
     * @Author: LR
     * @Date: 2022/6/13 20:28
     */
    public void getVideoMainUrlsFromUser(String userUrl) {
        try {
            System.setProperty(webdriverName, webdriverLocation);
            ChromeOptions options = new ChromeOptions();
            //无头模式，不打开浏览器
            if(isHeadless) {
                options.addArguments("--headless");
            }
            //创建Chrome driver的实例
            WebDriver driver = new ChromeDriver(options);
            // 最大化浏览器
            driver.manage().window().maximize();
            driver.get(userUrl);
            String title = driver.getTitle().replace("的主页 - 抖音", "");
            logger.info("正在打开主页：{}", title);
            //滑动脚本
            String js = "var q=document.documentElement.scrollTop=100000";
            int numsOfVideo = 0;
            int duplicateTimes = 0;
            Elements videoLis = new Elements();
            while(true){
                Document doc = Jsoup.parse(driver.getPageSource());
                Elements videoLi = doc.getElementsByClass("ECMy_Zdt");
                System.out.println("当前加载视频数量：" + videoLi.size());
                if(numsOfVideo == videoLi.size()) {
                    duplicateTimes += 1;
                }
                if(numsOfVideo == videoLi.size() && duplicateTimes > 2) {
                    videoLis = videoLi;
                    break;
                }
                numsOfVideo = videoLi.size();
                //执行向下滑动
                for(int i = 0; i < 3; i++){
                    JavascriptExecutor executor = (JavascriptExecutor) driver;
                    executor.executeScript(js);
                    Thread.sleep(1000);
                }
                Thread.sleep(1000);
            }
            driver.quit();
            //解析视频下载地址并下载
            getVideoDowanloadUrlsFromElements(title, videoLis);
        } catch (InterruptedException e){
            logger.error("睡眠中断异常：", e);
        }
    }

    /**
     * 功能描述: 根据所有视频节点解析真实下载地址并下载
     * @Param: [elements]
     * @Return: java.util.List<java.lang.String>
     * @Author: LR
     * @Date: 2022/6/13 21:04
     */
    private void getVideoDowanloadUrlsFromElements(String title, Elements elements) {
        for(Element e:elements) {
            //视频主页
            String mainUrl = e.select("a").attr("href");
            mainUrl = "https:" + mainUrl;
            //解析并下载
            downloadExecutor.submit(new DownloadRunner(title, mainUrl));
        }
    }

    /**
     * 功能描述: 下载
     * @Param:
     * @Return:
     * @Author: LR
     * @Date: 2022/6/15 1:42
     */
    class DownloadRunner implements Runnable {
        String title;
        String mainUrl;
        @Override
        public void run() {
            //请求视频主页
            String html = HttpUtil.fetchData(mainUrl, cookie);
            Document doc = Jsoup.parse(html);
            //视频名称
            String name = doc.select("title").text().replace(" - 抖音", "");
            //去除非法字符
            name = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]").matcher(name).replaceAll("");
            //String[] baseurls = {"//v26-web.douyinvod.com/", "//v3-web.douyinvod.com/", "//v3-dy-o.zjcdn.com/"};
            String reg = "v3-(web.douyinvod|dy-o.zjcdn).com(.+?)%2F%3F";
            Matcher matcher = Pattern.compile(reg).matcher(doc.toString());
            String url = "";
            if(matcher.find()) {
                url = matcher.group().replace("%2F", "/").replace("%3F", "?");
                url = "https://" + url;
                logger.info("主页：{}, 视频：{}, 地址：{}, 视频下载地址：{}", title, name, mainUrl, url);
                //下载视频
                HttpUtil.downloadFile(url, location.concat(title), name.concat(".mp4"));
            } else {
                logger.info("主页：{}, 视频：{}, 地址：{}, 没找到视频下载地址。", title, name, mainUrl);
            }
        }
        public DownloadRunner(String title, String mainUrl) {
            this.title = title;
            this.mainUrl = mainUrl;
        }
    }

    /**
     * 功能描述: 根据视频分享地址获取视频
     * @Param: [url]
     * @Return: void
     * @Author: LR
     * @Date: 2022/6/15 4:24
     */
    public void getVideoFromShareUrl(String shareToken) {
        String url = "";
        Matcher matcher = Pattern.compile(" (http[s]?:[\\w\\d./]*?) ").matcher(shareToken);
        //匹配查找分享口令中的URL
        if (matcher.find()) {
            url = matcher.group(1);
        } else {
            logger.info("解析口令失败：{}", shareToken);
        }
        //第一次重定向
        String url1 = HttpUtil.fetchRedirectUrl(url);
        //第二次重定向
        String url2 = HttpUtil.fetchRedirectUrl(url1);
        //打开视频主页
        String html = HttpUtil.fetchData(url2, cookie);
        Document doc = Jsoup.parse(html);
        String name = doc.select("title").text().replace(" - 抖音", "");
        //去除非法字符
        name = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]").matcher(name).replaceAll("");
        String reg = "v3-(web.douyinvod|dy-o.zjcdn).com(.+?)%2F%3F";
        Matcher matcher1 = Pattern.compile(reg).matcher(doc.toString());
        String downloadUrl = "";
        if(matcher1.find()) {
            downloadUrl = matcher1.group().replace("%2F", "/").replace("%3F", "?");
            downloadUrl = "https://" + downloadUrl;
            logger.info("视频:{},地址：{}, 视频下载地址：{}", name, url2, downloadUrl);
            //下载视频
            HttpUtil.downloadFile(downloadUrl, location.concat(name.trim()), name.concat(".mp4"));
        } else {
            logger.info("视频:{},地址：{}, 没找到视频下载地址。",  name, url2);
        }
    }

}
