package com.example.newnote;

public class Note {
    private  long id;
    private String title;
    private String content;

    public Note() {
    }

    public Note(long id,String title, String content) {
        this.id=id;
        this.title = title;
        this.content = content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
