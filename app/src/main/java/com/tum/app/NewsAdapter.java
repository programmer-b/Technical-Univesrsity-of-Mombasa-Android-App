package com.tum.app;

public class NewsAdapter {
    private String News, Date, Title;

    public NewsAdapter() { }

    public NewsAdapter(String news,String date, String title) {
        News = news;
        Date = date;
        Title = title;
    }

    public String getNews() {
        return News;
    }

    public void setNews(String news) {
        News = news;
    }

    public String getDate(){
        return Date;
    }
    public  void setDate(String date){
        Date = date;
    }

    public String getTitle(){
        return Title;
    }
    public  void setTitle(String title){
        Title = title;
    }
}
