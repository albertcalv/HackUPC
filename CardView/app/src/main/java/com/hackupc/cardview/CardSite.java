package com.hackupc.cardview;

/**
 * Created by Cecil Von Karma on 20/02/2016.
 */
public class CardSite extends Card {

    private String img;

    public CardSite(String n, String ni){
        this.setName(n);
        this.setExpanded(false);
        img = ni;
    }

    public String getImg (){
        return img;
    }

}
