package com.app.CryptoAnalyser.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class AboutUsController {

    @FXML
    private void goBack(ActionEvent event) {
        try {
            System.out.println("=== DÉBUT Navigation vers Home ===");

            // Test 1: Vérifier si le fichier existe
            URL fxmlUrl = getClass().getResource("/com/app/CryptoAnalyser/view/main.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ ERREUR: Fichier main.fxml introuvable!");
                System.err.println("Chemin testé: /com/app/CryptoAnalyser/view/main.fxml");

                // Essayer avec un chemin alternatif
                fxmlUrl = getClass().getResource("/view/main.fxml");
                if (fxmlUrl != null) {
                    System.out.println("✓ Fichier trouvé avec chemin alternatif: /view/main.fxml");
                } else {
                    // Afficher tous les chemins disponibles
                    System.err.println("\n=== Recherche de ressources ===");
                    System.err.println("ClassLoader: " + getClass().getClassLoader());
                    System.err.println("Class location: " + getClass().getProtectionDomain().getCodeSource().getLocation());

                    // Essayer de lister les ressources
                    URL testUrl = getClass().getResource("/");
                    System.err.println("Racine des ressources: " + testUrl);
                    return;
                }
            } else {
                System.out.println("✓ Fichier FXML trouvé: " + fxmlUrl);
            }

            // Test 2: Charger le FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            System.out.println("Chargement du FXML...");

            Parent root;
            try {
                root = loader.load();
                System.out.println("✓ FXML chargé avec succès");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du chargement du FXML: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Test 3: Récupérer la scène actuelle
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            System.out.println("✓ Stage actuel récupéré");

            // Test 4: Créer la nouvelle scène
            Scene scene = new Scene(root);
            System.out.println("✓ Nouvelle scène créée");

            // Test 5: Appliquer les styles CSS
            String[] cssFiles = {
                    "/css/style.css",
                    "/css/styleMain.css",
                    "/css/application.css",
                    "/css/styles.css"
            };

            boolean cssLoaded = false;
            for (String cssPath : cssFiles) {
                URL cssResource = getClass().getResource(cssPath);
                if (cssResource != null) {
                    scene.getStylesheets().add(cssResource.toExternalForm());
                    System.out.println("✓ CSS appliqué: " + cssPath);
                    cssLoaded = true;
                    break;
                }
            }

            if (!cssLoaded) {
                System.err.println("⚠ Aucun fichier CSS trouvé!");
                System.err.println("CSS cherchés:");
                for (String cssPath : cssFiles) {
                    System.err.println("  - " + cssPath);
                }
            }

            // Test 6: Définir la taille de la scène (optionnel)
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            // Si le stage a déjà une taille, la conserver
            if (currentWidth > 0 && currentHeight > 0) {
                stage.setWidth(currentWidth);
                stage.setHeight(currentHeight);
            } else {
                // Sinon, définir une taille par défaut
                stage.setWidth(1200);
                stage.setHeight(800);
            }

            // Test 7: Appliquer et afficher
            stage.setScene(scene);
            stage.show();

            System.out.println("✓ Navigation terminée avec succès");
            System.out.println("=== FIN Navigation ===");

        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE lors de la navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openLinkedIn(ActionEvent event) {
        // Cette méthode gère le clic sur les liens LinkedIn
        if (event.getSource() instanceof Hyperlink) {
            Hyperlink link = (Hyperlink) event.getSource();

            // Récupère l'URL définie dans le 'userData' du fichier FXML
            String url = (String) link.getUserData();

            if (url != null && !url.isEmpty()) {
                try {
                    // Ouvre le navigateur par défaut
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(url));
                    } else {
                        System.err.println("L'ouverture du navigateur n'est pas supportée sur ce système.");
                    }
                } catch (IOException | URISyntaxException e) {
                    System.err.println("Impossible d'ouvrir le lien : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}