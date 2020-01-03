package com.demo.common;

import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.cron4j.ITask;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PostContent extends Task {

    /**
     * Logger for this class
     */
    private static final Logger logger = org.apache.log4j.Logger.getLogger(PostContent.class);
    private static HashMap curList=null;
    private static HashMap leastList=null;
//    private static String complateUrl="https://www.powerapple.com/news/articles";
    private static String complateUrl="http://localhost:8080/example.htm";

    public static HashMap buildNewsPage() throws IOException{
        logger.error("访问："+complateUrl);
        Document doc2= Jsoup.connect(complateUrl).get();
        curList=new HashMap();
        HashMap tempCont=new HashMap();
        if(leastList==null){
            leastList=new HashMap();
        }
//        logger.debug(doc2.html());
        Elements newsHeadlines2 = doc2.select(".list-one");
        HashMap contentMap=new HashMap();
        for (Element headline2 : newsHeadlines2) {
            String url=StringUtils.substringBetween(headline2.toString(), "<h4 class=\"title\"><a href=\"", "\">");


            String title= StringUtils.substringBetween(headline2.toString(), "<h4 class=\"title\"><a href=\""+url+"\">", "</a></h4>");
            String title_image=StringUtils.substringBetween(headline2.toString(), "<img src=\"", "\"></a>");
            String time_categray_str=StringUtils.substringBetween(headline2.toString(), "<div class=\"info\">", "&nbsp;");
//            String star=StringUtils.substringBetween(headline2.toString(), "<td class=\"table-change\">", "</td>");
            String time_str=StringUtils.split(time_categray_str,"|")[0];
            String category_str=StringUtils.split(time_categray_str,"|")[1];
            if(StringUtils.isNotBlank(title)){
                title=title.trim();
                contentMap.put("title",title);
            }
            if(StringUtils.isNotBlank(title_image)){
                title_image=title_image.trim();
                contentMap.put("title_image",title_image);
            }
            if(StringUtils.isNotBlank(url)){
                url=url.trim();
                contentMap.put("url",url);
            }
            if(StringUtils.isNotBlank(time_categray_str)){
                time_categray_str=time_categray_str.trim();
                contentMap.put("time_categray_str",time_categray_str);
            }
            if(StringUtils.isNotBlank(time_str)){
                time_str=time_str.trim();
                contentMap.put("time_str",time_str);
            }
            if(StringUtils.isNotBlank(category_str)){
                category_str=category_str.trim();
                contentMap.put("category_str",category_str);
            }
            if(!leastList.containsKey(url)){
                curList.put(url,contentMap);
            }
            tempCont.put(url,contentMap);

        }
        leastList=tempCont;
        return curList;
    }

    @Override
    public void execute(TaskExecutionContext taskExecutionContext) throws RuntimeException {
        try {
            buildNewsPage();
            //now=DateUtil.addDate(now, -1);
            logger.error(JsonKit.toJson(curList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
