package com.cu.gardnr;

public class Plant {
    private Integer pid;
    private String image;
    private String username;
    private String name;
    private String location;
    private String light;
    private String water;
    private boolean notification;

    public Plant (Integer pid, String imageID){
        this.pid = pid;
        this.image = imageID;
    }

    public Plant (Integer pid, String imageID, String username, String name, String location, String light, String water, String notification) {
        this.pid = pid;
        this.image = imageID;
        this.username = username;
        this.name = name;
        this.location = location;
        this.light = light;
        this.water = water;
        setNotificationFromString(notification);
    }

    public Plant (Integer pid, String imageID, String username, String name, String location, String light, String water, boolean notification) {
        this.pid = pid;
        this.image = imageID;
        this.username = username;
        this.name = name;
        this.location = location;
        this.light = light;
        this.water = water;
        this.notification = notification;
    }

    public Integer getPID() { return pid; }
    public void setPID(Integer pid) { this.pid = pid; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getLight() {
        return light;
    }
    public void setLight(String light) {
        this.light = light;
    }

    public String getWater() {
        return water;
    }
    public void setWater(String water) {
        this.water = water;
    }

    public boolean getNotification() { return notification; }
    public void setNotification(boolean notification) { this.notification = notification; }

    public String getNotificationString() {
        if (notification){
            return "true";
        }
        else {
            return "false";
        }
    }
    public void setNotificationFromString(String string) {
        if (string.equalsIgnoreCase("true")){
            notification = true;
        }
        else {
            notification = false;
        }
    }


}
