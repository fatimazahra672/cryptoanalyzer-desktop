package com.app.CryptoAnalyser.model;

import java.time.LocalDateTime;

public class Analysis {
    private int idAnalysis;
    private String texteOriginal;
    private String texteDechiffre;
    private String algDetecte;
    private String dateAnalysis;

    public Analysis() {}

    // Constructeur complet
    public Analysis(int idAnalysis, String texteOriginal, String texteDechiffre, String algDetecte, String dateAnalysis) {
        this.idAnalysis = idAnalysis;
        this.texteOriginal = texteOriginal;
        this.texteDechiffre = texteDechiffre;
        this.algDetecte = algDetecte;
        this.dateAnalysis = dateAnalysis;
    }

    // Constructeur pour une nouvelle analyse (sans ID)
    public Analysis(String texteOriginal) {
        this.texteOriginal = texteOriginal;
        this.texteDechiffre = "";
        this.algDetecte = "";
        this.dateAnalysis = LocalDateTime.now().toString();
    }

    // Getters
    public int getIdAnalysis() {
        return idAnalysis;
    }

    public String getTexteOriginal() {
        return texteOriginal;
    }

    public String getTexteDechiffre() {
        return texteDechiffre;
    }

    public String getAlgDetecte() {
        return algDetecte;
    }

    public String getDateAnalysis() {
        return dateAnalysis;
    }

    // Setters
    public void setIdAnalysis(int idAnalysis) {
        this.idAnalysis = idAnalysis;
    }

    public void setTexteOriginal(String texteOriginal) {
        this.texteOriginal = texteOriginal;
    }

    public void setTexteDechiffre(String texteDechiffre) {
        this.texteDechiffre = texteDechiffre;
    }

    public void setAlgDetecte(String algDetecte) {
        this.algDetecte = algDetecte;
    }

    public void setDateAnalysis(String dateAnalysis) {
        this.dateAnalysis = dateAnalysis;
    }

    @Override
    public String toString() {
        return "Analysis{" +
                "idAnalysis=" + idAnalysis +
                ", texteOriginal='" + texteOriginal + '\'' +
                ", texteDechiffre='" + texteDechiffre + '\'' +
                ", algDetecte='" + algDetecte + '\'' +
                ", dateAnalysis='" + dateAnalysis + '\'' +
                '}';
    }


}