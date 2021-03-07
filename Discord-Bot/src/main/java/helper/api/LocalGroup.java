package main.java.helper.api;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LocalGroup {

    private long id;
    private String name;
    private double lat;
    private double lon;
    private String state;
    private String facebook;
    private String instagram;
    private String twitter;
    private String youtube;
    private String website;
    private String whatsapp;
    private String telegram;
    private String other;
    private String email;

    public LocalGroup(long id, String name, double lat, double lon, String state, String facebook, String instagram, String twitter,
                      String youtube, String website, String whatsapp, String telegram, String other, String email) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.state = state;
        this.facebook = facebook;
        this.instagram = instagram;
        this.twitter = twitter;
        this.youtube = youtube;
        this.website = website;
        this.whatsapp = whatsapp;
        this.telegram = telegram;
        this.other = other;
        this.email = email;
    }
}
