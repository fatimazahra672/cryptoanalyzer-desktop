package com.app.CryptoAnalyser.controller;

import com.app.CryptoAnalyser.data.StockageDecryption;
import com.app.CryptoAnalyser.model.DecryptWord;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DecryptModalController {

    @FXML
    private TextFlow textFlow;

    @FXML
    private VBox progressContainer;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label progressLabel;

    private Runnable onClose;

    @FXML
    private HBox splitContainer;

    @FXML
    private VBox leftPane;

    @FXML
    private VBox rightPane;

    @FXML
    private StackPane textContainer;
    private String originalText;

    @FXML
    private TableView<DecryptWord> decryptedTable;

    @FXML
    private TableColumn<DecryptWord, String> encryptedCol;

    @FXML
    private TableColumn<DecryptWord, String> decryptedCol;

    @FXML
    private TableColumn<DecryptWord, String> algorithmCol;

    @FXML
    private Button generateReportButton;

    @FXML
    private Button closeButton;

    // Nouveaux éléments pour la progression PDF
    @FXML
    private StackPane pdfProgressOverlay;

    @FXML
    private ProgressBar pdfProgressBar;

    @FXML
    private Label pdfProgressLabel;

    @FXML
    private Label pdfStatusLabel;

    private Timeline pdfProgressTimeline;
    private ExecutorService pdfExecutor;

    // 📂 Dictionnaire anglais
    private static final Set<String> ENGLISH_DICT = new HashSet<>();

    static {
        try (InputStream is = DecryptModalController.class.getResourceAsStream("/dict/english.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = br.readLine()) != null) {
                ENGLISH_DICT.add(line.trim().toLowerCase());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Impossible de charger le dictionnaire anglais.");
        }
    }

    @FXML
    private void handleGenerateReport() {
        if (decryptedTable == null || decryptedTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Generate report", "Aucune donnée à exporter.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le rapport");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        String defaultName = "CryptoReport_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        chooser.setInitialFileName(defaultName);

        Stage stage = (Stage) (generateReportButton != null ? generateReportButton.getScene().getWindow() : null);
        File target = chooser.showSaveDialog(stage);
        if (target == null) return;

        // Démarrer la progression PDF
        startPdfGeneration(target);
    }

    private void startPdfGeneration(File target) {
        // Afficher l'overlay de progression
        pdfProgressOverlay.setVisible(true);
        pdfProgressOverlay.setManaged(true);
        pdfProgressBar.setProgress(0);
        pdfProgressLabel.setText("Generating PDF report...");
        pdfStatusLabel.setText("Initializing PDF generation...");

        // Désactiver le bouton pendant la génération
        generateReportButton.setDisable(true);

        // Initialiser l'exécuteur pour le traitement en arrière-plan
        if (pdfExecutor == null) {
            pdfExecutor = Executors.newSingleThreadExecutor();
        }

        // Démarrer l'animation de progression
        startPdfProgressAnimation();

        // Lancer la génération PDF dans un thread séparé
        pdfExecutor.submit(() -> {
            try {
                // Simuler un petit délai pour l'initialisation
                Thread.sleep(300);

                Platform.runLater(() -> {
                    pdfStatusLabel.setText("Preparing document structure...");
                });
                updatePdfProgress(0.1);

                Thread.sleep(500);

                Platform.runLater(() -> {
                    pdfStatusLabel.setText("Processing decrypted text...");
                });
                updatePdfProgress(0.2);

                // Générer le PDF
                String decryptedFull = decryptFullText(originalText != null ? originalText : "");

                Platform.runLater(() -> {
                    pdfStatusLabel.setText("Generating PDF content...");
                });
                updatePdfProgress(0.4);

                Thread.sleep(600);

                byte[] pdfBytes = generateProfessionalPdf(decryptedFull, decryptedTable.getItems());

                Platform.runLater(() -> {
                    pdfStatusLabel.setText("Saving PDF file...");
                });
                updatePdfProgress(0.7);

                Thread.sleep(400);

                try (FileOutputStream fos = new FileOutputStream(target)) {
                    fos.write(pdfBytes);
                }

                // Finalisation
                Platform.runLater(() -> {
                    pdfStatusLabel.setText("PDF generated successfully!");
                    updatePdfProgress(1.0);
                });

                Thread.sleep(800);

                // Terminer avec succès
                Platform.runLater(() -> {
                    completePdfGeneration(true, target);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    completePdfGeneration(false, null);
                    showAlert(Alert.AlertType.ERROR, "Generate report",
                            "Erreur lors de l'export PDF : " + e.getMessage());
                });
            }
        });
    }

    private void startPdfProgressAnimation() {
        if (pdfProgressTimeline != null) {
            pdfProgressTimeline.stop();
        }

        pdfProgressTimeline = new Timeline(
                new KeyFrame(Duration.millis(50), e -> {
                    double currentProgress = pdfProgressBar.getProgress();
                    // Animation de progression subtile pendant le traitement
                    if (currentProgress < 0.9) {
                        double newProgress = currentProgress + 0.005;
                        if (newProgress < 0.9) {
                            pdfProgressBar.setProgress(newProgress);
                        }
                    }
                })
        );
        pdfProgressTimeline.setCycleCount(Timeline.INDEFINITE);
        pdfProgressTimeline.play();
    }

    private void updatePdfProgress(double progress) {
        Platform.runLater(() -> {
            pdfProgressBar.setProgress(progress);
        });
    }

    private void completePdfGeneration(boolean success, File target) {
        // Arrêter l'animation
        if (pdfProgressTimeline != null) {
            pdfProgressTimeline.stop();
            pdfProgressTimeline = null;
        }

        // Cacher l'overlay
        pdfProgressOverlay.setVisible(false);
        pdfProgressOverlay.setManaged(false);

        // Réactiver le bouton
        generateReportButton.setDisable(false);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Generate report",
                    "Rapport PDF exporté avec succès :\n" + target.getAbsolutePath());

            // Ajouter un effet visuel de confirmation
            Timeline successFlash = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(generateReportButton.styleProperty(), "-fx-background-color: linear-gradient(135deg, #00ff41 0%, #00d4ff 50%, #00ff41 100%);")
                    ),
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(generateReportButton.styleProperty(), "-fx-background-color: linear-gradient(135deg, #00ffff 0%, #00ff41 50%, #00ffff 100%);")
                    ),
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(generateReportButton.styleProperty(), "-fx-background-color: linear-gradient(135deg, #00ff41 0%, #00d4ff 50%, #00ff41 100%);")
                    )
            );
            successFlash.setCycleCount(3);
            successFlash.play();
        }
    }

    private void writeProfessionalPdf(File target) throws Exception {
        String decryptedFull = decryptFullText(originalText != null ? originalText : "");
        byte[] pdfBytes = generateProfessionalPdf(decryptedFull, decryptedTable.getItems());

        try (FileOutputStream fos = new FileOutputStream(target)) {
            fos.write(pdfBytes);
        }

        System.out.println("✅ PDF professionnel généré avec succès: " + target.getAbsolutePath());
        System.out.println("   - Taille: " + pdfBytes.length + " bytes");
        System.out.println("   - Mots analysés: " + decryptedTable.getItems().size());
    }

    private String describeAlgorithm(String algo) {
        if (algo == null) return "Inconnu";
        switch (algo.toUpperCase()) {
            case "HEX":
                return "Dechiffrement Hexadecimal";
            case "BASE64":
                return "Decodage Base64";
            case "CAESAR":
                return "Chiffre de Cesar (ROT-3)";
            default:
                return algo;
        }
    }

    /**
     * Génère un PDF professionnel avec diagramme en barres corrigé
     */
    private byte[] generateProfessionalPdf(String decryptedText, java.util.List<DecryptWord> rows) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        java.util.List<Integer> xrefPositions = new java.util.ArrayList<>();

        baos.write("%PDF-1.4\n".getBytes(StandardCharsets.UTF_8));
        baos.write("%âãÏÓ\n".getBytes(StandardCharsets.UTF_8));

        xrefPositions.add(baos.size());
        String catalog = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        baos.write(catalog.getBytes(StandardCharsets.UTF_8));

        xrefPositions.add(baos.size());
        String pages = "2 0 obj\n<< /Type /Pages /Count 1 /Kids [3 0 R] >>\nendobj\n";
        baos.write(pages.getBytes(StandardCharsets.UTF_8));

        xrefPositions.add(baos.size());
        String page = "3 0 obj\n" +
                "<< /Type /Page\n" +
                "   /Parent 2 0 R\n" +
                "   /MediaBox [0 0 595 842]\n" +
                "   /Contents 4 0 R\n" +
                "   /Resources << /Font << /F1 5 0 R /F2 6 0 R >>\n" +
                "                 /ProcSet [/PDF /Text] >>\n" +
                ">>\nendobj\n";
        baos.write(page.getBytes(StandardCharsets.UTF_8));

        StringBuilder content = new StringBuilder();
        double currentY = 800;

        // EN-TÊTE
        content.append("BT\n");
        content.append("/F2 22 Tf\n0 0 0 rg\n");
        content.append("100 ").append(currentY).append(" Td\n");
        content.append("(RAPPORT DE DECHIFFREMENT) Tj\n");
        currentY -= 30;

        content.append("/F1 11 Tf\n0.2 0.2 0.2 rg\n");
        content.append("-100 ").append(currentY).append(" Td\n");
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        content.append("170 ").append(currentY).append(" Td\n");
        content.append("(Date: ").append(escapePdf(dateStr)).append(") Tj\nET\n");

        currentY -= 15;
        content.append("q\n1 w\n0.8 0.8 0.8 RG\n");
        content.append("50 ").append(currentY).append(" m\n545 ").append(currentY).append(" l\nS\nQ\n");
        currentY -= 25;

        // SECTION 1
        content.append("BT\n/F2 14 Tf\n0 0 0 rg\n50 ").append(currentY).append(" Td\n");
        content.append("(1. TEXTE CHIFFRE ORIGINAL) Tj\nET\n");
        currentY -= 20;

        double sectionHeight = 0;
        if (originalText != null && !originalText.isEmpty()) {
            String[] lines = splitTextIntoLines(originalText, 80);
            sectionHeight = (lines.length * 14) + 10;
        } else {
            sectionHeight = 24;
        }

        content.append("q\n0.95 0.95 0.95 rg\n");
        content.append("45 ").append(currentY - sectionHeight + 5).append(" 505 ").append(sectionHeight).append(" re\nf\nQ\n");

        content.append("BT\n/F1 9 Tf\n0 0 0 rg\n50 ").append(currentY - 5).append(" Td\n");

        if (originalText != null && !originalText.isEmpty()) {
            String[] lines = splitTextIntoLines(originalText, 80);
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) content.append("0 -14 Td\n");
                content.append("(").append(escapePdf(lines[i])).append(") Tj\n");
            }
            currentY -= sectionHeight + 10;
        } else {
            content.append("(Aucun texte chiffre) Tj\n");
            currentY -= 30;
        }
        content.append("ET\n");

        // SECTION 2
        currentY -= 15;
        content.append("BT\n/F2 14 Tf\n0 0 0 rg\n50 ").append(currentY).append(" Td\n");
        content.append("(2. TEXTE DECHIFFRE) Tj\nET\n");
        currentY -= 20;

        double decryptedHeight = 0;
        if (decryptedText != null && !decryptedText.isEmpty()) {
            String[] lines = splitTextIntoLines(decryptedText, 80);
            decryptedHeight = (lines.length * 14) + 10;
        } else {
            decryptedHeight = 24;
        }

        content.append("q\n0.9 0.98 0.9 rg\n");
        content.append("45 ").append(currentY - decryptedHeight + 5).append(" 505 ").append(decryptedHeight).append(" re\nf\nQ\n");

        content.append("BT\n/F1 9 Tf\n0 0.3 0 rg\n50 ").append(currentY - 5).append(" Td\n");

        if (decryptedText != null && !decryptedText.isEmpty()) {
            String[] lines = splitTextIntoLines(decryptedText, 80);
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) content.append("0 -14 Td\n");
                content.append("(").append(escapePdf(lines[i])).append(") Tj\n");
            }
            currentY -= decryptedHeight + 10;
        } else {
            content.append("(Aucun texte dechiffre) Tj\n");
            currentY -= 30;
        }
        content.append("ET\n");

        // SECTION 3: TABLEAU
        currentY -= 15;
        content.append("BT\n/F2 14 Tf\n0 0 0 rg\n50 ").append(currentY).append(" Td\n");
        content.append("(3. ANALYSE DETAILLEE) Tj\n/F1 10 Tf\n0 -18 Td\n");
        content.append("(Mots dechiffres: ").append(rows.size()).append(") Tj\nET\n");
        currentY -= 38;

        content.append("q\n0.2 0.3 0.5 rg\n45 ").append(currentY - 10).append(" 505 18 re\nf\nQ\n");
        content.append("BT\n/F2 9 Tf\n1 1 1 rg\n50 ").append(currentY - 4).append(" Td\n");
        content.append("(MOT CHIFFRE) Tj\n180 0 Td\n(MOT DECHIFFRE) Tj\n180 0 Td\n(ALGORITHME) Tj\nET\n");
        currentY -= 22;

        for (int i = 0; i < rows.size(); i++) {
            DecryptWord word = rows.get(i);

            content.append("q\n");
            if (i % 2 == 0) {
                content.append("0.98 0.98 0.98 rg\n");
            } else {
                content.append("0.92 0.92 0.96 rg\n");
            }
            content.append("45 ").append(currentY - 5).append(" 505 13 re\nf\nQ\n");

            content.append("BT\n/F1 8 Tf\n0 0 0 rg\n50 ").append(currentY).append(" Td\n");

            String encrypted = truncate(word.getEncrypted(), 25);
            String decrypted = truncate(word.getDecrypted(), 25);
            // MODIFICATION ICI : Affichage complet des noms d'algorithmes
            String algo = describeAlgorithm(word.getAlgorithm());

            content.append("(").append(escapePdf(encrypted)).append(") Tj\n180 0 Td\n");
            content.append("(").append(escapePdf(decrypted)).append(") Tj\n180 0 Td\n");
            content.append("(").append(escapePdf(algo)).append(") Tj\nET\n");

            currentY -= 13;
        }

        // SECTION 4: STATISTIQUES AVEC DIAGRAMME CORRIGÉ
        currentY -= 20;
        content.append("BT\n/F2 14 Tf\n0 0 0 rg\n50 ").append(currentY).append(" Td\n");
        content.append("(4. STATISTIQUES) Tj\nET\n");
        currentY -= 25;

        // Calcul des statistiques
        java.util.Map<String, Integer> algoStats = new java.util.HashMap<>();
        for (DecryptWord w : rows) {
            algoStats.put(w.getAlgorithm(), algoStats.getOrDefault(w.getAlgorithm(), 0) + 1);
        }

        // Fond du cadre statistiques
        double statsBoxHeight = 180;
        content.append("q\n0.98 0.98 0.95 rg\n");
        content.append("45 ").append(currentY - statsBoxHeight).append(" 505 ").append(statsBoxHeight).append(" re\nf\n");
        content.append("0.9 0.9 0.8 RG\n1 w\n");
        content.append("45 ").append(currentY - statsBoxHeight).append(" 505 ").append(statsBoxHeight).append(" re\nS\nQ\n");

        // Titre du graphique
        content.append("BT\n/F2 12 Tf\n0.3 0.3 0.6 rg\n190 ").append(currentY - 15).append(" Td\n");
        content.append("(DIAGRAMME EN BARRES) Tj\nET\n");

        // Coordonnées du graphique
        double graphX = 70;
        double graphY = currentY - 40;
        double graphWidth = 455;
        double graphHeight = 110;

        // Fond du graphique
        content.append("q\n0.96 0.96 0.98 rg\n");
        content.append(graphX).append(" ").append(graphY - graphHeight).append(" ");
        content.append(graphWidth).append(" ").append(graphHeight).append(" re\nf\n");
        content.append("0.7 0.7 0.8 RG\n1 w\n");
        content.append(graphX).append(" ").append(graphY - graphHeight).append(" ");
        content.append(graphWidth).append(" ").append(graphHeight).append(" re\nS\nQ\n");

        // Axes
        content.append("q\n0.3 0.3 0.3 RG\n1.5 w\n");
        content.append(graphX).append(" ").append(graphY - graphHeight).append(" m\n");
        content.append(graphX + graphWidth).append(" ").append(graphY - graphHeight).append(" l\n");
        content.append(graphX).append(" ").append(graphY - graphHeight).append(" m\n");
        content.append(graphX).append(" ").append(graphY - 5).append(" l\nS\nQ\n");

        // Trouver max
        int maxCount = Math.max(2, algoStats.values().stream().max(Integer::compare).orElse(1));
        if (maxCount <= 5) maxCount = 5;
        else if (maxCount <= 10) maxCount = 10;
        else maxCount = ((maxCount / 5) + 1) * 5;

        // Graduations Y
        for (int i = 0; i <= 5; i++) {
            double yPos = graphY - graphHeight + (i * graphHeight / 5);
            double labelValue = maxCount * i / 5;

            content.append("q\n0.7 0.7 0.7 RG\n0.3 w\n");
            content.append(graphX).append(" ").append(yPos).append(" m\n");
            content.append(graphX + graphWidth).append(" ").append(yPos).append(" l\nS\nQ\n");

            content.append("BT\n/F1 8 Tf\n0.3 0.3 0.3 rg\n");
            content.append(graphX - 20).append(" ").append(yPos - 3).append(" Td\n");
            content.append("(").append(String.format("%.0f", labelValue)).append(") Tj\nET\n");
        }

        // Label axe Y
        content.append("BT\n/F1 9 Tf\n0.3 0.3 0.6 rg\n");
        content.append("q\n1 0 0 1 ").append(graphX - 40).append(" ").append(graphY - graphHeight/2).append(" cm\n");
        content.append("0 1 -1 0 0 0 cm\n");
        content.append("BT\n/F1 9 Tf\n0.3 0.3 0.6 rg\n0 0 Td\n(Nombre de mots) Tj\nET\nQ\n");

        // Dessiner les barres
        String[][] barColors = {
                {"0.2 0.4 0.8", "0.3 0.5 0.9"},
                {"0.8 0.4 0.2", "0.9 0.5 0.3"},
                {"0.4 0.6 0.2", "0.5 0.7 0.3"},
                {"0.8 0.2 0.5", "0.9 0.3 0.6"},
                {"0.6 0.2 0.8", "0.7 0.3 0.9"}
        };

        java.util.List<java.util.Map.Entry<String, Integer>> sortedEntries = new java.util.ArrayList<>(algoStats.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        double barWidth = 35;
        double spacing = Math.min(80, (graphWidth - 100 - (sortedEntries.size() * barWidth)) / Math.max(1, sortedEntries.size() - 1));
        double startX = graphX + 50;

        for (int idx = 0; idx < sortedEntries.size(); idx++) {
            java.util.Map.Entry<String, Integer> entry = sortedEntries.get(idx);
            double barHeight = (entry.getValue() * graphHeight) / (double)maxCount;
            double barX = startX + (idx * (barWidth + spacing));

            String[] colors = barColors[idx % barColors.length];

            // Barre principale
            content.append("q\n").append(colors[0]).append(" rg\n");
            content.append(barX).append(" ").append(graphY - graphHeight).append(" ");
            content.append(barWidth).append(" ").append(barHeight).append(" re\nf\n");

            // Effet 3D
            content.append(colors[1]).append(" rg\n");
            content.append(barX + barWidth - 3).append(" ").append(graphY - graphHeight).append(" ");
            content.append("3 ").append(barHeight).append(" re\nf\n");

            // Contour
            content.append("0.1 0.1 0.2 RG\n0.5 w\n");
            content.append(barX).append(" ").append(graphY - graphHeight).append(" ");
            content.append(barWidth).append(" ").append(barHeight).append(" re\nS\nQ\n");

            // Valeur au-dessus
            content.append("BT\n/F1 9 Tf\n0 0 0 rg\n");
            content.append(barX + barWidth/2 - 5).append(" ").append(graphY - graphHeight + barHeight + 5).append(" Td\n");
            content.append("(").append(entry.getValue()).append(") Tj\nET\n");

            // Nom de l'algo en dessous - AFFICHAGE COMPLET
            String algoName = describeAlgorithm(entry.getKey());
            // SUPPRESSION DU TRONCATAGE : afficher le nom complet
            String shortName = algoName; // Supprimer le tronquage

            content.append("BT\n/F1 7 Tf\n0.2 0.2 0.4 rg\n");
            // Ajuster la position pour les noms longs
            content.append(barX - 10).append(" ").append(graphY - graphHeight - 20).append(" Td\n");
            content.append("(").append(escapePdf(shortName)).append(") Tj\nET\n");
        }

        // PIED DE PAGE
        currentY = currentY - statsBoxHeight - 15;
        content.append("q\n0.5 w\n0.8 0.8 0.8 RG\n50 ").append(currentY).append(" m\n545 ").append(currentY).append(" l\nS\nQ\n");
        content.append("BT\n/F1 8 Tf\n0.4 0.4 0.4 rg\n50 ").append(currentY - 15).append(" Td\n");
        content.append("(Genere par CryptoAnalyser - Rapport automatique de dechiffrement) Tj\n0 -10 Td\n");
        content.append("(").append(rows.size()).append(" mots analyses avec succes) Tj\nET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.UTF_8);

        xrefPositions.add(baos.size());
        String contentObj = "4 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n";
        baos.write(contentObj.getBytes(StandardCharsets.UTF_8));
        baos.write(contentBytes);
        baos.write("\nendstream\nendobj\n".getBytes(StandardCharsets.UTF_8));

        xrefPositions.add(baos.size());
        baos.write("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.UTF_8));

        xrefPositions.add(baos.size());
        baos.write("6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.UTF_8));

        int xrefPos = baos.size();
        baos.write("xref\n".getBytes(StandardCharsets.UTF_8));
        baos.write(("0 " + (xrefPositions.size() + 1) + "\n").getBytes(StandardCharsets.UTF_8));
        baos.write("0000000000 65535 f \n".getBytes(StandardCharsets.UTF_8));

        for (int pos : xrefPositions) {
            baos.write(String.format("%010d 00000 n \n", pos).getBytes(StandardCharsets.UTF_8));
        }

        baos.write("trailer\n".getBytes(StandardCharsets.UTF_8));
        baos.write(("<< /Size " + (xrefPositions.size() + 1) + " /Root 1 0 R >>\n").getBytes(StandardCharsets.UTF_8));
        baos.write("startxref\n".getBytes(StandardCharsets.UTF_8));
        baos.write(String.valueOf(xrefPos).getBytes(StandardCharsets.UTF_8));
        baos.write("\n%%EOF\n".getBytes(StandardCharsets.UTF_8));

        return baos.toByteArray();
    }

    private String[] splitTextIntoLines(String text, int maxLength) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    private String truncate(String s, int maxLength) {
        if (s == null) return "";
        if (s.length() <= maxLength) return s;
        return s.substring(0, maxLength - 3) + "...";
    }

    private String escapePdf(String text) {
        if (text == null) return "";

        text = text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", "")
                .replace("\n", " ");

        text = text.replace("é", "e").replace("è", "e").replace("ê", "e").replace("ë", "e")
                .replace("à", "a").replace("â", "a").replace("ä", "a")
                .replace("ù", "u").replace("û", "u").replace("ü", "u")
                .replace("î", "i").replace("ï", "i")
                .replace("ô", "o").replace("ö", "o")
                .replace("ç", "c")
                .replace("É", "E").replace("È", "E").replace("Ê", "E").replace("Ë", "E")
                .replace("À", "A").replace("Â", "A").replace("Ä", "A")
                .replace("Ù", "U").replace("Û", "U").replace("Ü", "U")
                .replace("Î", "I").replace("Ï", "I")
                .replace("Ô", "O").replace("Ö", "O")
                .replace("Ç", "C");

        return text;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    public void initialize() {
        if (progressContainer != null) {
            progressContainer.setVisible(false);
            progressContainer.setManaged(false);
        }

        if (pdfProgressOverlay != null) {
            pdfProgressOverlay.setVisible(false);
            pdfProgressOverlay.setManaged(false);
        }

        encryptedCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEncrypted()));
        decryptedCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDecrypted()));
        algorithmCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAlgorithm()));

        if (generateReportButton != null) {
            generateReportButton.setOnAction(e -> handleGenerateReport());
        }
    }

    public void setOriginalText(String text) {
        this.originalText = text;
        displayTextWithAutoScroll(text);
    }

    public void displayTextWithAutoScroll(String text) {
        textFlow.getChildren().clear();
        String[] words = text.split("\\s+");

        Duration speed = Duration.millis(35);
        SequentialTransition seq = new SequentialTransition();

        for (String word : words) {
            PauseTransition pause = new PauseTransition(speed);
            pause.setOnFinished(event -> {
                if (isEncryptedWord(word)) {
                    Label encrypted = new Label(word + " ");
                    encrypted.getStyleClass().add("encrypted-word");
                    applyScannerEffect(encrypted);
                    textFlow.getChildren().add(encrypted);
                } else {
                    Text normal = new Text(word + " ");
                    normal.getStyleClass().add("normal-text");
                    textFlow.getChildren().add(normal);
                }

                if (scrollPane != null) {
                    Platform.runLater(() -> {
                        double h = textFlow.getBoundsInLocal().getHeight();
                        double vh = scrollPane.getViewportBounds().getHeight();
                        if (h > vh) scrollPane.setVvalue(1.0);
                    });
                }
            });
            seq.getChildren().add(pause);
        }

        seq.setOnFinished(e -> {
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(ev -> launchProgressAfterDetection());
            delay.play();
        });

        seq.play();
    }

    private void applyScannerEffect(Label label) {
        PauseTransition flashOn = new PauseTransition(Duration.millis(200));
        flashOn.setOnFinished(e -> label.getStyleClass().add("encrypted-word-scanning"));

        PauseTransition flashOff = new PauseTransition(Duration.millis(200));
        flashOff.setOnFinished(e -> label.getStyleClass().remove("encrypted-word-scanning"));

        SequentialTransition seq = new SequentialTransition(flashOn, flashOff);
        seq.setCycleCount(SequentialTransition.INDEFINITE);
        seq.play();
    }

    private boolean isEncryptedWord(String word) {
        String clean = word.replaceAll("[^A-Za-z0-9\\-+/=]", "");
        if (clean.length() < 1) return false;

        boolean suspect = false;

        if (isCaesarEncryptedWord(clean)) {
            suspect = true;
        }

        String hexClean = word.replaceAll("[^0-9A-Fa-f]", "");
        if (hexClean.length() >= 4 && hexClean.length() % 2 == 0) {
            if (hexClean.matches("[0-9A-Fa-f]+")) {
                try {
                    hexToAscii(hexClean);
                    String wordWithoutPunctuation = word.replaceAll("[^A-Za-z0-9]", "");
                    if (hexClean.equalsIgnoreCase(wordWithoutPunctuation)) {
                        suspect = true;
                    }
                } catch (Exception e) {
                }
            }
        }

        if (clean.matches(".*[a-zA-Z].*") && clean.matches(".*\\d.*") && clean.length() < 8) {
            suspect = false;
        }

        String base64Clean = clean.replaceAll("[^A-Za-z0-9+/=]", "");
        if (!suspect && base64Clean.length() >= 8 && base64Clean.length() % 4 == 0
                && base64Clean.matches("^[A-Za-z0-9+/]+={0,2}$")) {

            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(base64Clean);
                String decodedText = new String(decoded);

                if (decodedText.matches("[ -~]+") && decodedText.matches(".*[aeiouAEIOU].*")) {
                    suspect = true;
                }
            } catch (IllegalArgumentException e) {
            }
        }

        if (!suspect) {
            double entropy = calculateEntropy(clean);
            if (entropy >= 4.2) {
                suspect = true;
            }

            long digits = clean.chars().filter(Character::isDigit).count();
            long letters = clean.chars().filter(Character::isLetter).count();
            if ((double) digits / clean.length() >= 0.4 && letters < clean.length() / 2) {
                suspect = true;
            }

            long vowels = clean.chars().filter(c -> "aeiouAEIOU".indexOf(c) >= 0).count();
            if (vowels == 0 && clean.length() > 8) {
                suspect = true;
            }
        }

        return suspect;
    }

    private boolean isCaesarEncryptedWord(String word) {
        if (word == null || word.trim().isEmpty()) return false;

        if (word.contains("-")) {
            String[] parts = word.split("-");
            for (String part : parts) {
                if (isRot3EncryptedSingleWord(part)) {
                    return true;
                }
            }
            return false;
        }

        return isRot3EncryptedSingleWord(word);
    }

    private static final Set<String> SHORT_ROT3_WORDS = Set.of(
            "lv", "d", "ph", "br", "xq", "rx", "l", "lq"
    );

    private static final Set<String> SHORT_ROT3_DECRYPTED = Set.of(
            "is", "a", "me", "yo", "up", "ou", "i", "in"
    );

    private boolean isRot3EncryptedSingleWord(String word) {
        String clean = word.replaceAll("[^A-Za-z]", "").toLowerCase();

        if (clean.length() < 3) {
            if (!SHORT_ROT3_WORDS.contains(clean)) {
                return false;
            }
            String decrypted = rot3Decrypt(clean);
            return SHORT_ROT3_DECRYPTED.contains(decrypted);
        }

        String decrypted = rot3Decrypt(clean);
        return ENGLISH_DICT.contains(decrypted.toLowerCase()) &&
                !ENGLISH_DICT.contains(clean.toLowerCase());
    }

    private String rot3Decrypt(String word) {
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                sb.append((char) ((c - 'A' + 23) % 26 + 'A'));
            } else if (c >= 'a' && c <= 'z') {
                sb.append((char) ((c - 'a' + 23) % 26 + 'a'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private double calculateEntropy(String word) {
        int[] counts = new int[256];
        for (char c : word.toCharArray()) counts[c]++;
        double entropy = 0.0;
        int len = word.length();
        for (int count : counts) {
            if (count == 0) continue;
            double p = (double) count / len;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    private void launchProgressAfterDetection() {
        PauseTransition wait = new PauseTransition(Duration.seconds(3));
        wait.setOnFinished(event -> {
            textFlow.setVisible(false);
            textFlow.setManaged(false);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            progressContainer.setVisible(true);
            progressContainer.setManaged(true);
            runProgressBar();
        });
        wait.play();
    }

    private Timeline progressTimeline;

    private void runProgressBar() {
        progressBar.setProgress(0);
        progressLabel.setText("Decrypting in progress...");

        progressTimeline = new Timeline(
                new KeyFrame(Duration.millis(30), e -> {
                    double p = progressBar.getProgress();
                    if (p < 1.0) {
                        p += 0.01;
                        progressBar.setProgress(p);
                        progressLabel.setText("Decrypting in progress...    " + (int)(p*100) + "%");
                    } else {
                        progressTimeline.stop();
                        animateSplit();
                    }
                })
        );

        progressTimeline.setCycleCount(Animation.INDEFINITE);
        progressTimeline.play();
    }

    private void animateSplit() {
        progressContainer.setVisible(false);
        progressContainer.setManaged(false);
        textContainer.setVisible(false);
        textContainer.setManaged(false);
        splitContainer.setVisible(true);
        splitContainer.setManaged(true);

        leftPane.prefWidthProperty().bind(splitContainer.widthProperty().multiply(0.5));
        leftPane.maxWidthProperty().bind(splitContainer.widthProperty().multiply(0.5));

        leftPane.setScaleX(0);
        rightPane.setScaleX(0);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(leftPane.scaleXProperty(), 0),
                        new KeyValue(rightPane.scaleXProperty(), 0)
                ),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(leftPane.scaleXProperty(), 1),
                        new KeyValue(rightPane.scaleXProperty(), 1)
                )
        );
        timeline.play();

        leftPane.getChildren().clear();
        TextFlow decryptedFlow = createDecryptedTextFlow(originalText);
        decryptedFlow.prefWidthProperty().bind(leftPane.widthProperty());
        leftPane.getChildren().add(decryptedFlow);
        fillDecryptedTable(originalText);

        String decryptedText = decryptFullText(originalText);
        String algoGlobal = "MIXED";
        if (!decryptedTable.getItems().isEmpty()) {
            algoGlobal = decryptedTable.getItems().get(0).getAlgorithm();
        }

        StockageDecryption stockage = new StockageDecryption();
        int idAnalysis = stockage.saveAnalysis(originalText, decryptedText, algoGlobal);

        for (DecryptWord w : decryptedTable.getItems()) {
            stockage.saveWord(idAnalysis, w.getEncrypted(), w.getDecrypted(), w.getAlgorithm());
        }

        System.out.println("🎉 Analyse enregistrée avec succès !");
    }

    private enum EncryptionType {
        HEX, BASE64, CAESAR, UNKNOWN
    }

    private EncryptionType detectEncryptionType(String word) {
        String clean = word.replaceAll("[^A-Za-z0-9\\-+/=]", "");

        String hexClean = clean.replaceAll("[^0-9A-Fa-f]", "");
        if (hexClean.length() >= 4 && hexClean.length() % 2 == 0
                && hexClean.matches("[0-9A-Fa-f]+") && hexClean.equals(clean)) {
            try {
                hexToAscii(hexClean);
                return EncryptionType.HEX;
            } catch (Exception e) {
            }
        }

        String base64Clean = clean.replaceAll("[^A-Za-z0-9+/=]", "");
        if (base64Clean.length() >= 8 && base64Clean.matches("^[A-Za-z0-9+/]+={0,2}$")
                && base64Clean.length() % 4 == 0 && base64Clean.equals(clean)) {
            try {
                byte[] decoded = java.util.Base64.getDecoder().decode(base64Clean);
                String decodedText = new String(decoded);
                if (decodedText.matches("[ -~]+") && decodedText.matches(".*[aeiouAEIOU].*")) {
                    return EncryptionType.BASE64;
                }
            } catch (Exception ignored) {}
        }

        if (isCaesarEncryptedWord(clean)) {
            return EncryptionType.CAESAR;
        }

        return EncryptionType.UNKNOWN;
    }

    private String decryptWordByType(String word) {
        EncryptionType type = detectEncryptionType(word);
        String clean = word.replaceAll("[^A-Za-z0-9\\-+/=]", "");

        try {
            switch (type) {
                case HEX:
                    return hexToAscii(clean.replaceAll("[^0-9A-Fa-f]", ""));
                case BASE64:
                    return new String(java.util.Base64.getDecoder().decode(clean));
                case CAESAR:
                    return rot3Decrypt(clean);
                default:
                    return word;
            }
        } catch (Exception e) {
            return word;
        }
    }

    private String hexToAscii(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    public String decryptFullText(String text) {
        StringBuilder decryptedText = new StringBuilder();
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (isEncryptedWord(word)) {
                String decrypted = decryptWordByType(word);
                decryptedText.append(decrypted).append(" ");
            } else {
                decryptedText.append(word).append(" ");
            }
        }
        return decryptedText.toString().trim();
    }

    private TextFlow createDecryptedTextFlow(String text) {
        TextFlow flow = new TextFlow();
        flow.setPrefWidth(leftPane.getWidth());
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setLineSpacing(2);

        String[] words = text.split("\\s+");
        for (String word : words) {
            Text t = new Text();

            if (isEncryptedWord(word)) {
                String displayWord = decryptWordByType(word);
                t.setText(displayWord + " ");
                t.getStyleClass().add("decrypted-word");
            } else {
                t.setText(word + " ");
                t.getStyleClass().add("normal-text");
            }

            flow.getChildren().add(t);
        }

        return flow;
    }

    private void fillDecryptedTable(String text) {
        decryptedTable.getItems().clear();
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (isEncryptedWord(word)) {
                String decrypted = decryptWordByType(word);
                String algo = detectEncryptionType(word).name();
                decryptedTable.getItems().add(new DecryptWord(word, decrypted, algo));
            }
        }
    }

    public void cleanup() {
        if (progressTimeline != null) {
            progressTimeline.stop();
            progressTimeline = null;
        }

        if (pdfProgressTimeline != null) {
            pdfProgressTimeline.stop();
            pdfProgressTimeline = null;
        }

        if (pdfExecutor != null) {
            pdfExecutor.shutdownNow();
            pdfExecutor = null;
        }
    }

    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }

    @FXML
    private void closeModal() {
        cleanup();

        Timeline closeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(leftPane.scaleXProperty(), 1),
                        new KeyValue(rightPane.scaleXProperty(), 1),
                        new KeyValue(leftPane.opacityProperty(), 1),
                        new KeyValue(rightPane.opacityProperty(), 1)
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(leftPane.scaleXProperty(), 0),
                        new KeyValue(rightPane.scaleXProperty(), 0),
                        new KeyValue(leftPane.opacityProperty(), 0),
                        new KeyValue(rightPane.opacityProperty(), 0)
                )
        );

        closeTimeline.setOnFinished(event -> {
            try {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                double currentWidth = stage.getWidth();
                double currentHeight = stage.getHeight();
                double currentX = stage.getX();
                double currentY = stage.getY();

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/app/CryptoAnalyser/View/main.fxml")
                );
                Parent root = loader.load();

                root.minWidth(1200);
                root.minHeight(800);

                if (root instanceof Pane) {
                    ((Pane) root).setPrefSize(currentWidth, currentHeight);
                }

                Scene scene = new Scene(root, currentWidth, currentHeight);

                try {
                    String css1 = getClass().getResource("/css/style.css").toExternalForm();
                    scene.getStylesheets().add(css1);
                } catch (Exception e1) {
                    try {
                        String css2 = getClass().getResource("/com/app/CryptoAnalyser/View/main.css").toExternalForm();
                        scene.getStylesheets().add(css2);
                    } catch (Exception e2) {
                    }
                }

                stage.setScene(scene);
                stage.setX(currentX);
                stage.setY(currentY);
                stage.setWidth(currentWidth);
                stage.setHeight(currentHeight);
                stage.setMinWidth(1200);
                stage.setMinHeight(800);

                if (currentWidth < 1200 || currentHeight < 800) {
                    Platform.runLater(() -> {
                        stage.setWidth(Math.max(currentWidth, 1200));
                        stage.setHeight(Math.max(currentHeight, 800));
                    });
                }

                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        closeTimeline.play();
    }
}