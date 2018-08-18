package com.parkpal.classes;

public class ParkingLocations {
    String parkingID;
    String parkName;
    Double latitude;
    Double longitude;
    int averageCirclingTime;
    int userRating;

    public ParkingLocations(String parkingID,String parkName,Double latitude,
                            Double longitude, int averageCirclingTime,int userRating){
        this.parkingID = parkingID;
        this.parkName = parkName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.averageCirclingTime = averageCirclingTime;
        this.userRating = userRating;
    }


}
