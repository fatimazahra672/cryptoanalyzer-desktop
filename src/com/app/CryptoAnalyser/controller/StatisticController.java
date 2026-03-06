package com.app.CryptoAnalyser.controller;

import com.app.CryptoAnalyser.data.AnalysisData;
import com.app.CryptoAnalyser.model.Analysis;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticController {

    @FXML private BarChart<String, Number> dailyChart;
    @FXML private AreaChart<String, Number> cumulativeChart;
    @FXML private PieChart algorithmPieChart;
    @FXML private Label totalAnalysesLabel;
    @FXML private Label successRateLabel;
    @FXML private Label topAlgorithmLabel;
    @FXML private ScrollPane statsContainer;
    @FXML private VBox recentAnalysesContainer;
    @FXML private Text iconTotal;
    @FXML private Text iconSuccess;
    @FXML private Text iconTop;
    @FXML private Text iconPeriod;

    // Unicode icons that work well with JavaFX
    private final String[] icons = {
            "\uD83D\uDCCA",  // 📊
            "\u2705",        // ✅
            "\uD83C\uDFC6",  // 🏆
            "\uD83D\uDCC5"   // 📅
    };

    private AnalysisData analysisData;
    private List<Analysis> analyses;

    @FXML
    public void initialize() {
        try {
            System.out.println("=== START STATISTICS INITIALIZATION ===");
            analysisData = new AnalysisData();
            analyses = analysisData.getAllAnalyses();

            System.out.println("Number of analyses loaded: " + analyses.size());
            if (!analyses.isEmpty()) {
                System.out.println("First analysis: " + analyses.get(0));
            }

            setupStatistics();
            setupDailyChart();
            setupCumulativeChart();
            setupAlgorithmChart();
            setupRecentAnalysesTable();

            // Set up icons after initialization
            setupCardIcons();

            System.out.println("=== END STATISTICS INITIALIZATION ===");
        } catch (Exception e) {
            System.err.println("Error in initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCardIcons() {
        iconTotal.setText("\uD83D\uDCCA");
        iconSuccess.setText("\u2705");
        iconTop.setText("\uD83C\uDFC6");
        iconPeriod.setText("\uD83D\uDCC5");

        Font emojiFont = Font.font("Segoe UI Emoji", 52);

        iconTotal.setFont(emojiFont);
        iconSuccess.setFont(emojiFont);
        iconTop.setFont(emojiFont);
        iconPeriod.setFont(emojiFont);
    }

    private void setupStatistics() {
        try {
            // Total number of analyses
            int total = analyses.size();
            totalAnalysesLabel.setText(String.valueOf(total));

            // Success rate (analyses with decrypted text)
            long successful = analyses.stream()
                    .filter(a -> a.getTexteDechiffre() != null && !a.getTexteDechiffre().isEmpty())
                    .count();
            double successRate = total > 0 ? (successful * 100.0) / total : 0;
            successRateLabel.setText(String.format("%.1f%%", successRate));

            // Most used algorithm
            Map<String, Long> algoCount = analyses.stream()
                    .filter(a -> a.getAlgDetecte() != null && !a.getAlgDetecte().isEmpty())
                    .collect(Collectors.groupingBy(Analysis::getAlgDetecte, Collectors.counting()));

            if (!algoCount.isEmpty()) {
                String topAlgo = Collections.max(algoCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                topAlgorithmLabel.setText(topAlgo);
            } else {
                topAlgorithmLabel.setText("None");
            }
        } catch (Exception e) {
            System.err.println("Error in setupStatistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupDailyChart() {
        try {
            dailyChart.setTitle("Daily Analyses");
            dailyChart.getXAxis().setLabel("Date");
            dailyChart.getYAxis().setLabel("Number of analyses");

            // Group analyses by date (without time)
            Map<String, Long> dailyCount = analyses.stream()
                    .collect(Collectors.groupingBy(
                            a -> extractDate(a.getDateAnalysis()),
                            Collectors.counting()
                    ));

            // Sort by date
            List<String> sortedDates = new ArrayList<>(dailyCount.keySet());
            sortedDates.sort(Comparator.naturalOrder());

            // Create data series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Analyses");

            for (String date : sortedDates) {
                series.getData().add(new XYChart.Data<>(formatDateForDisplay(date), dailyCount.get(date)));
            }

            dailyChart.getData().clear();
            dailyChart.getData().add(series);
            dailyChart.setLegendVisible(false);

        } catch (Exception e) {
            System.err.println("Error in setupDailyChart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCumulativeChart() {
        try {
            cumulativeChart.setTitle("Text Complexity Evolution");
            cumulativeChart.getXAxis().setLabel("Date");
            cumulativeChart.getYAxis().setLabel("Distinct Characters");

            // Group analyses by date
            Map<String, List<Analysis>> analysesByDate = analyses.stream()
                    .collect(Collectors.groupingBy(
                            a -> extractDate(a.getDateAnalysis())
                    ));

            // Sort dates
            List<String> sortedDates = new ArrayList<>(analysesByDate.keySet());
            sortedDates.sort(Comparator.naturalOrder());

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Average Complexity");

            for (String date : sortedDates) {
                List<Analysis> dailyAnalyses = analysesByDate.get(date);

                // Calculate average daily complexity
                double avgComplexity = dailyAnalyses.stream()
                        .mapToInt(a -> calculateTextComplexity(a.getTexteOriginal()))
                        .average()
                        .orElse(0);

                series.getData().add(
                        new XYChart.Data<>(formatDateForDisplay(date), avgComplexity)
                );
            }

            cumulativeChart.getData().clear();
            cumulativeChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("Error in setupCumulativeChart (complexity): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupAlgorithmChart() {
        try {
            algorithmPieChart.setTitle("Algorithm Distribution");

            // DISABLE LEGEND
            algorithmPieChart.setLegendVisible(false);

            // Count algorithms
            Map<String, Long> algoCount = analyses.stream()
                    .filter(a -> a.getAlgDetecte() != null && !a.getAlgDetecte().isEmpty())
                    .collect(Collectors.groupingBy(Analysis::getAlgDetecte, Collectors.counting()));

            // Sort by descending frequency
            List<Map.Entry<String, Long>> sortedAlgos = new ArrayList<>(algoCount.entrySet());
            sortedAlgos.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            algorithmPieChart.getData().clear();

            // Add to PieChart (limit to top 5 + "Others")
            int limit = Math.min(5, sortedAlgos.size());
            long othersCount = 0;

            for (int i = 0; i < sortedAlgos.size(); i++) {
                Map.Entry<String, Long> entry = sortedAlgos.get(i);
                if (i < limit) {
                    algorithmPieChart.getData().add(new PieChart.Data(
                            entry.getKey() + " (" + entry.getValue() + ")",
                            entry.getValue()
                    ));
                } else {
                    othersCount += entry.getValue();
                }
            }

            if (othersCount > 0) {
                algorithmPieChart.getData().add(new PieChart.Data(
                        "Others (" + othersCount + ")",
                        othersCount
                ));
            }

            algorithmPieChart.setLabelsVisible(true);
            algorithmPieChart.setLabelLineLength(10);

            // ADD THIS LINE to apply blue colors
            applyBluePieChartColors();

        } catch (Exception e) {
            System.err.println("Error in setupAlgorithmChart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyBluePieChartColors() {
        Platform.runLater(() -> {
            try {
                // Wait for chart to be fully rendered
                Thread.sleep(150);

                // List of consistent blue colors
                String[] blueColors = {
                        "#00E5FF",  // Light cyan
                        "#0099CC",  // Medium blue
                        "#0077A3",  // Dark blue
                        "#005580",  // Navy blue
                        "#7ec8ff",  // Sky blue
                        "#4da6ff",  // Electric blue
                        "#1a75ff",  // Bright blue
                        "#2a4d69"   // Slate blue
                };

                // 1. APPLY COLORS TO SEGMENTS
                int segmentColorIndex = 0;
                for (PieChart.Data data : algorithmPieChart.getData()) {
                    Node segmentNode = data.getNode();
                    if (segmentNode != null) {
                        int colorIdx = segmentColorIndex % blueColors.length;
                        String color = blueColors[colorIdx];

                        // Apply color to segment
                        segmentNode.setStyle(
                                "-fx-pie-color: " + color + " !important; " +
                                        "-fx-border-color: rgba(0, 229, 255, 0.5) !important; " +
                                        "-fx-border-width: 1.5 !important;"
                        );
                    }
                    segmentColorIndex++;
                }

                // 2. BUILD CSS FOR LEGEND
                StringBuilder cssBuilder = new StringBuilder();

                // Reset all default colors
                for (int i = 0; i < 8; i++) {
                    cssBuilder.append(".default-color").append(i).append(".chart-pie { ")
                            .append("-fx-pie-color: transparent; } ");
                }

                // Apply our colors to segments and legend
                for (int i = 0; i < algorithmPieChart.getData().size(); i++) {
                    int colorIdx = i % blueColors.length;
                    String color = blueColors[colorIdx];

                    // For segments
                    cssBuilder.append(".data").append(i).append(".chart-pie { ")
                            .append("-fx-pie-color: ").append(color).append(" !important; } ");

                    // For legend symbols - VERY IMPORTANT
                    cssBuilder.append(".data").append(i).append(".chart-legend-symbol { ")
                            .append("-fx-background-color: ").append(color).append(" !important; ")
                            .append("-fx-background-radius: 4; ")
                            .append("-fx-border-color: rgba(255, 255, 255, 0.3); ")
                            .append("-fx-border-width: 1; } ");
                }

                algorithmPieChart.setStyle(cssBuilder.toString());

                // 3. MANUALLY APPLY COLORS TO LEGEND SYMBOLS
                int legendSymbolIndex = 0;
                for (Node node : algorithmPieChart.lookupAll(".chart-legend-symbol")) {
                    if (node != null) {
                        int colorIdx = legendSymbolIndex % blueColors.length;
                        node.setStyle(
                                "-fx-background-color: " + blueColors[colorIdx] + " !important; " +
                                        "-fx-background-radius: 4; " +
                                        "-fx-border-color: rgba(255, 255, 255, 0.3) !important; " +
                                        "-fx-border-width: 1 !important;"
                        );
                        legendSymbolIndex++;
                    }
                }

                // 4. CUSTOMIZE CHART LABELS
                for (Node node : algorithmPieChart.lookupAll(".chart-pie-label")) {
                    if (node instanceof Text) {
                        Text text = (Text) node;
                        text.setFill(javafx.scene.paint.Color.WHITE);
                        text.setStyle(
                                "-fx-font-size: 11px; " +
                                        "-fx-font-weight: bold; " +
                                        "-fx-fill: white !important; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.7), 2, 0.5, 0, 1);"
                        );
                    }
                }

                // 5. CUSTOMIZE LEGEND TEXTS
                for (Node node : algorithmPieChart.lookupAll(".chart-legend-item")) {
                    Label label = (Label) node.lookup(".label");
                    if (label != null) {
                        label.setTextFill(javafx.scene.paint.Color.WHITE);
                        label.setStyle(
                                "-fx-text-fill: white !important; " +
                                        "-fx-font-size: 12px; " +
                                        "-fx-font-weight: normal; " +
                                        "-fx-padding: 0 5px 0 5px;"
                        );
                    }
                }

                // 6. FORCE CHART UPDATE
                algorithmPieChart.requestLayout();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupRecentAnalysesTable() {
        try {
            // Clear container
            recentAnalysesContainer.getChildren().clear();

            // Check if analyses exist
            if (analyses == null || analyses.isEmpty()) {
                Label noDataLabel = new Label("No analysis available");
                noDataLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-style: italic;");
                recentAnalysesContainer.getChildren().add(noDataLabel);
                return;
            }

            // Filter only successful analyses (non-empty decrypted text)
            List<Analysis> successfulAnalyses = analyses.stream()
                    .filter(a -> a.getTexteDechiffre() != null && !a.getTexteDechiffre().isEmpty())
                    .collect(Collectors.toList());

            // Check if there are successful analyses
            if (successfulAnalyses.isEmpty()) {
                Label noSuccessLabel = new Label("No successful decryption");
                noSuccessLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-style: italic;");
                recentAnalysesContainer.getChildren().add(noSuccessLabel);
                return;
            }

            // Take the 5 most recent successful analyses
            List<Analysis> recentSuccessfulAnalyses = successfulAnalyses.stream()
                    .sorted((a1, a2) -> {
                        try {
                            return LocalDate.parse(extractDate(a2.getDateAnalysis()))
                                    .compareTo(LocalDate.parse(extractDate(a1.getDateAnalysis())));
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .limit(5)
                    .collect(Collectors.toList());

            // Add each successful analysis to the table
            for (Analysis analysis : recentSuccessfulAnalyses) {
                HBox row = createAnalysisRow(analysis);
                recentAnalysesContainer.getChildren().add(row);
            }

        } catch (Exception e) {
            System.err.println("Error in setupRecentAnalysesTable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createAnalysisRow(Analysis analysis) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setSpacing(0);
        row.setPrefHeight(35);

        // Date Column
        Label dateLabel = new Label(formatDateForTable(analysis.getDateAnalysis()));
        dateLabel.getStyleClass().add("table-cell");
        dateLabel.setPrefWidth(150);
        dateLabel.setAlignment(Pos.CENTER);

        // Algorithm Column
        String algo = analysis.getAlgDetecte();
        Label algoLabel = new Label(algo != null && !algo.isEmpty() ? algo : "Not detected");
        algoLabel.getStyleClass().add("table-cell");
        algoLabel.setPrefWidth(200);
        algoLabel.setAlignment(Pos.CENTER);

        // Original Text Column (truncated)
        String originalText = analysis.getTexteOriginal();
        if (originalText.length() > 30) {
            originalText = originalText.substring(0, 27) + "...";
        }
        Label textLabel = new Label(originalText);
        textLabel.getStyleClass().add("table-cell");
        textLabel.setPrefWidth(350);
        textLabel.setAlignment(Pos.CENTER_LEFT);
        textLabel.setPadding(new Insets(0, 10, 0, 10));

        Tooltip tooltip = new Tooltip(analysis.getTexteOriginal());
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: rgba(30, 30, 40, 0.95); -fx-text-fill: white;");
        textLabel.setTooltip(tooltip);

        // Status Column
        String status = "Failed";
        String statusColor = "#ff6b6b"; // Red for failure
        if (analysis.getTexteDechiffre() != null && !analysis.getTexteDechiffre().isEmpty()) {
            status = "Success";
            statusColor = "#51cf66"; // Green for success
        }
        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add("table-cell");
        statusLabel.setPrefWidth(150);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-weight: bold;");

        // Length Column
        int length = analysis.getTexteOriginal().length();
        Label lengthLabel = new Label(String.valueOf(length));
        lengthLabel.getStyleClass().add("table-cell");
        lengthLabel.setPrefWidth(100);
        lengthLabel.setAlignment(Pos.CENTER);

        row.getChildren().addAll(dateLabel, algoLabel, textLabel, statusLabel, lengthLabel);

        return row;
    }

    private String extractDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return LocalDate.now().toString();
        }
        try {
            // Extract date YYYY-MM-DD
            return dateTime.substring(0, Math.min(dateTime.length(), 10));
        } catch (Exception e) {
            return LocalDate.now().toString();
        }
    }

    private String formatDateForDisplay(String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            return localDate.format(formatter);
        } catch (Exception e) {
            return date;
        }
    }

    private String formatDateForTable(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "N/A";
        }
        try {
            // Format as DD/MM HH:mm
            String datePart = dateTime.substring(0, Math.min(dateTime.length(), 10));
            String timePart = dateTime.length() > 11 ? dateTime.substring(11, Math.min(dateTime.length(), 16)) : "";

            LocalDate date = LocalDate.parse(datePart);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            return formatter.format(date) + (timePart.isEmpty() ? "" : " " + timePart);

        } catch (Exception e) {
            return dateTime;
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/CryptoAnalyser/View/main.fxml"));
            Parent mainRoot = loader.load();

            Stage currentStage = (Stage) dailyChart.getScene().getWindow();
            currentStage.getScene().setRoot(mainRoot);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshStatistics() {
        try {
            analyses = analysisData.getAllAnalyses();
            clearCharts();
            setupStatistics();
            setupDailyChart();
            setupCumulativeChart();
            setupAlgorithmChart();
            setupRecentAnalysesTable();
            // Reconfigure icons after refresh
            setupCardIcons();
        } catch (Exception e) {
            System.err.println("Error in refreshStatistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearCharts() {
        dailyChart.getData().clear();
        cumulativeChart.getData().clear();
        algorithmPieChart.getData().clear();
    }

    private int calculateTextComplexity(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // Set to store distinct characters
        Set<Character> uniqueChars = new HashSet<>();

        for (char c : text.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                uniqueChars.add(c);
            }
        }

        return uniqueChars.size();
    }
}