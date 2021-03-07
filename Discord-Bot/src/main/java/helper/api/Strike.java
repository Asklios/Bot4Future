package main.java.helper.api;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class Strike {

    private long id;
    private String locationName;
    private String localGroupName;
    private double lat;
    private double lon;
    private String state;
    private Date dateTime;
    private String note;
    private String eventLink;
    private Date lastUpdate;

    public Strike(long id, String locationName, String localGroupName, double lat, double lon, String state, Date dateTime, String note,
                  String eventLink, Date lastUpdate) {
        this.id = id;
        this.locationName = locationName;
        this.localGroupName = localGroupName;
        this.lat = lat;
        this.lon = lon;
        this.state = state;
        this.dateTime = dateTime;
        this.note = note;
        this.eventLink = eventLink;
        this.lastUpdate = lastUpdate;
    }
}
