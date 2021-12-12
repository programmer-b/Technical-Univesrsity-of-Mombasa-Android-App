package com.tum.app;

public class NotesAdapter {
    private String Date, Unit, Topic, PdfURI, Memo, Network, Password, Location, regNo, UserId;

    public NotesAdapter() { }

    public NotesAdapter(String date, String unit, String topic, String pdfURI, String memo, String network, String password, String location, String registrationNumber, String userId) {
        Date = date;
        Unit = unit;
        Topic = topic;
        PdfURI = pdfURI;
        Memo = memo;
        Network = network;
        Password = password;
        Location = location;
        regNo = registrationNumber;
        UserId = userId;
    }

    public String getUnit() {
        return Unit;
    }
    public void setUnit(String unit) {
        Unit = unit;
    }

    public String getDate(){
        return Date;
    }
    public  void setDate(String date){
        Date = date;
    }

    public String getTopic(){
        return Topic;
    }
    public  void setTopic(String topic){
        Topic = topic;
    }

    public String getPdfURI(){
        return PdfURI;
    }
    public  void setPdfURI(String pdfURI){
        PdfURI = pdfURI;
    }

    public String getMemo(){
        return Memo;
    }
    public  void setMemo(String memo){Memo = memo;}

    public String getNetwork(){return Network;}
    public void setNetwork(String network){Network = network;}

    public String getPassword(){return Password;}
    public  void setPassword(String password){Password = password;}

    public String getLocation(){return Location;}
    public void setLocation(String location){Location = location;}

    public String getRegNo(){return regNo;}
    public void setRegistrationNumber(String registrationNumber){regNo = registrationNumber;}

    public String getUserId(){return UserId;}
    public void setUserIdr(String userId){regNo = userId;}
}
