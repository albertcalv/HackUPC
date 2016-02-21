package com.hackupc.cardview;

/**
 * Created by Cecil Von Karma on 20/02/2016.
 */
public class CardRoute extends Card {

    private Integer minutes;

    public CardRoute(Integer t){
        this.setName("Ruta");
        this.setExpanded(false);
        minutes = t;
    }

    public Integer getMinutes (){
        return minutes;
    }

    public void setMinutes (Integer t){
        minutes = t;
    }

}
