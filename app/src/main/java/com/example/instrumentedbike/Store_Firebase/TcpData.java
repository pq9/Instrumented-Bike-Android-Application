package com.example.instrumentedbike.Store_Firebase;



import org.litepal.crud.DataSupport;

import java.util.Date;


public class TcpData extends DataSupport {
    /**
     * Data Received
     */
    private String data;
    /**
     * Store IP address
     */
    private String ip;
    /**
     * Store Time
     */
    private Date releaseDate;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
