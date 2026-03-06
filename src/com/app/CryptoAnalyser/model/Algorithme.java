package com.app.CryptoAnalyser.model;

public class Algorithme {
    private int id;
    private String nom;
    private String description;
    private String exemple;
    private String categorie;
    private int enabled;

    public Algorithme(int id, String nom, String description, String exemple, String categorie) {
        this(id, nom, description, exemple, categorie, 0);
    }

    public Algorithme(int id, String nom, String description, String exemple, String categorie ,  int enabled) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.exemple = exemple;
        this.categorie = categorie;
        this.enabled=  enabled;
    }


    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public String getExemple() { return exemple; }
    public String getCategorie() { return categorie; }
    public int getEnabled() { return enabled; }
    public void setEnabled(int enabled) { this.enabled = enabled; }
}
