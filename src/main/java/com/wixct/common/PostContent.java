package com.wixct.common;

import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.HashMap;

public class PostContent extends Task {

    /**
     * Logger for this class
     */
    private static final Logger logger = org.apache.log4j.Logger.getLogger(PostContent.class);
    private static HashMap<String,HashMap<String,String>> curList=null;
    private static HashMap<String,HashMap<String,String>> leastList=null;
//    private static String complateUrl="https://www.powerapple.com/news/articles";
    private static String baseUrl="http://localhost:8800/one.htm";
    private static String complateUrl="http://localhost:8800/example.htm";
    private static String postUrl="http://localhost:8082/github/getmessages";

    public static PostContent setDevMode(boolean devModet) {
        PostContent.devMode = devModet;
        if(!devModet){//生产模式
            complateUrl="https://www.powerapple.com/news/articles";
            baseUrl="https://www.powerapple.com";
            postUrl="https://wixct.com/github/getmessages";
        }
        return new PostContent();
    }

    /**
     * 开发模式，默认生产模式
     */
    private static boolean devMode=false;



    /**
     * 爬取内容，获取新消息
     * @param pagesSize 页数
     * @return
     * @throws IOException
     */
    public static HashMap buildNewsPage(int pagesSize) throws IOException{

        curList=new HashMap<String,HashMap<String,String>>();
        HashMap<String,HashMap<String,String> > tempCont=new HashMap();
        if(leastList==null){
            leastList=new HashMap<String,HashMap<String,String>>();
        }
        HashMap<String, String> contentMap = new HashMap();
        for(int i=1;i<=pagesSize;i++) {
            logger.error("访问：" + complateUrl+"?page="+i);
            Document doc2 = Jsoup.connect(complateUrl+"?page="+i).get();
            Elements newsHeadlines2 = doc2.select(".list-one");
            for (Element headline2 : newsHeadlines2) {
                contentMap = new HashMap();
                String url = StringUtils.substringBetween(headline2.toString(), "<h4 class=\"title\"><a href=\"", "\">");
                String title = StringUtils.substringBetween(headline2.toString(), "<h4 class=\"title\"><a href=\"" + url + "\">", "</a></h4>");
                String title_image = StringUtils.substringBetween(headline2.toString(), "<img src=\"", "\"></a>");
                String time_categray_str = StringUtils.substringBetween(headline2.toString(), "<div class=\"info\">", "&nbsp;");
                String time_str = StringUtils.split(time_categray_str, "|")[0];
                String category_str = StringUtils.split(time_categray_str, "|")[1];
                if (StringUtils.isNotBlank(title)) {
                    title = title.trim();
                    contentMap.put("title", title);
                }
                if (StringUtils.isNotBlank(title_image)) {
                    title_image = title_image.trim();
                    contentMap.put("title_image", title_image);
                }
                if (StringUtils.isNotBlank(url)) {
                    url = url.trim();
                    contentMap.put("url", url);
                }
                if (StringUtils.isNotBlank(time_categray_str)) {
                    time_categray_str = time_categray_str.trim();
                    contentMap.put("time_categray_str", time_categray_str);
                }
                if (StringUtils.isNotBlank(time_str)) {
                    time_str = time_str.trim();
                    contentMap.put("time_str", time_str);
                }
                if (StringUtils.isNotBlank(category_str)) {
                    category_str = category_str.trim();
                    contentMap.put("category_str", category_str);
                }
                if (!leastList.containsKey(url)) {
                    curList.put(url, contentMap);
                }
                tempCont.put(url, contentMap);

            }
        }
        leastList=tempCont;
        return curList;
    }

    @Override
    public void execute(TaskExecutionContext taskExecutionContext) throws RuntimeException {
        try {
            buildNewsPage(2);
            buidContent();
            String postBody=JsonKit.toJson(curList);
            HashMap<String,String> headers=new HashMap<>();
            headers.put("Content-Type","application/json");
            String result=HttpKit.post(postUrl,postBody,headers);
            logger.error("提交数据量："+curList.size());
            logger.error("返回数据："+result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文章内容
     * @throws IOException
     */
    private static void buidContent() throws IOException {
        for(Object key : curList.keySet()){
            HashMap hm=curList.get(key);
//            logger.error("开发模式："+devMode);
            String fullUrl=StringUtils.EMPTY;
            if(devMode){//开发模式
                fullUrl=baseUrl;
            }else{
                fullUrl=baseUrl+key;
            }
//            logger.error("文章地址："+fullUrl);
            Document doc2= Jsoup.connect(fullUrl).get();
            Elements newsHeadlines2 = doc2.select(".main-content");
            String content=newsHeadlines2.html();
            if(StringUtils.isNotBlank(content)){
                content=content.trim();
                hm.put("content",content);
            }
        }
    }
}
