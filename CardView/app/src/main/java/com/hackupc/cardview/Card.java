package com.hackupc.cardview;

/**
 * Created by Cecil Von Karma on 20/02/2016.
 */
public class Card{
    private String name;
    private String img;
    private boolean expanded;

    public Card(){
        name = "site";
        expanded = false;
    }

    public Card(String n){
        name = n;
        expanded = false;
    }

    public String getName(){
        return name;
    }
    public String getImg(){
        return "none";
    }
    public Integer getMinutes(){
        return 0;
    }
    public void setName(String n){
        name = n;
    }
    public void setExpanded(boolean b){
        expanded = b;
    }

    public boolean changeExpanded(){
        expanded = !expanded;
        return !expanded;
    }

}
