package com.cu.gardnr;

public class Plant {
    private Integer pid;
    private String image;
    private String username;
    private String name;
    private String location;
    private String light;
    private String water;

    public Plant (Integer pid, String imageID, String username, String name, String location, String light, String water) {
        this.pid = pid;
        this.image = imageID;
        this.username = username;
        this.name = name;
        this.location = location;
        this.light = light;
        this.water = water;
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


}
