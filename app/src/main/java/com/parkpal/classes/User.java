package com.parkpal.classes;

public class User {

        String userID;
        String userName;
        public User(String userID){
            this.userID = userID;
            this.userName = "-";
        }
        public User(String userID, String userName) {
            this.userID = userID;
            this.userName = userName;
        }

        public String getUserID() {
            return userID;
        }

        public String getUserName() {
            return userName;
        }
    }


