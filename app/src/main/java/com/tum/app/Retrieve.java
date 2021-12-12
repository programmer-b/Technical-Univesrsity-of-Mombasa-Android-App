package com.tum.app;

public class Retrieve {
    private String regNo;
    private String yearOfStudy;
    private String EregisterPassword;
    private String ElearningPassword;
    private String profileImage;
    private String fullName;

    public Retrieve(){};

    public Retrieve(String regNo,String yearOfStudy, String EregisterPassword, String ElearningPassword,String profileImage,String fullName){
        this.regNo = regNo;
        this.yearOfStudy = yearOfStudy;
        this.EregisterPassword = EregisterPassword;
        this.ElearningPassword = ElearningPassword;
        this.profileImage = profileImage;
        this.fullName = fullName;
    }

    public String getRegNo(){return regNo;}
    public String getYearOfStudy(){return yearOfStudy;}
    public String getEregisterPassword(){return EregisterPassword;}
    public String getElearningPassword(){return ElearningPassword;}
    public String getProfileImage(){return profileImage;}
    public String getFullName(){return fullName;}

}
