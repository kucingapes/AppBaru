package com.utsman.kucingapes.sejarahindonesiatoday;

public class Getter {
    private String title, date, img, body;

    public Getter(String title, String date, String img, String body) {
        this.title = title;
        this.date = date;
        this.img = img;
        this.body = body;
    }

    public Getter(){
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
