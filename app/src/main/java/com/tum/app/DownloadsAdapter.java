package com.tum.app;

public class DownloadsAdapter {
    private String PdfTitle, PdfURI, Date;

    public DownloadsAdapter() { }

    public DownloadsAdapter(String pdfTitle,String date,String pdfUri) {
        PdfTitle = pdfTitle;
        Date = date;
        PdfURI = pdfUri;
    }

    public String getPdfTitle() { return PdfTitle;}

    public void setPdfTitle(String pdfTitle) {
        PdfTitle = pdfTitle;
    }

    public String getPdfURI(){
        return PdfURI;
    }
    public  void setPdfURI(String pdfURI){
        PdfURI = pdfURI;
    }

    public String getDate(){
        return Date;
    }
    public  void setDate(String date){
        Date = date;
    }
}
