package com.app.CryptoAnalyser.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {
    public static String loadApiKey() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("api_key.txt"));
            return new String(bytes).trim();
        } catch (IOException e) {
            System.err.println("Erreur: Fichier api_key.txt non trouvé");
            return null;
        }
    }
}