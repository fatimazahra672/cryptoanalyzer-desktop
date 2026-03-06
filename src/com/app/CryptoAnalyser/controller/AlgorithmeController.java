package com.app.CryptoAnalyser.controller;

import com.app.CryptoAnalyser.data.AlgorithmeData;
import com.app.CryptoAnalyser.data.AnalysisData;
import com.app.CryptoAnalyser.model.Algorithme;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.animation.PauseTransition;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import com.app.CryptoAnalyser.model.Analysis;
import com.app.CryptoAnalyser.services.AnalysisService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import javafx.application.Platform;

// Imports pour PDF
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

// Imports pour DOCX
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AlgorithmeController {

    // === Éléments communs aux deux interfaces ===
    @FXML
    private HBox cardContainer;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label algTitle;

    // === Éléments de la première interface (Import/Decrypt) ===
    @FXML
    private TextArea cipherInputField;
    @FXML
    private Button importButton;
    @FXML
    private Button decryptButton;
    @FXML
    private GridPane algOptionsContainer;

    // === Éléments de la deuxième interface (Search/Chat) ===
    @FXML
    private TextField searchField;
    @FXML
    private Label searchIconLabel;
    @FXML
    private TextField chatInputField;
    @FXML
    private VBox chatMessagesContainer;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Label flashLabel;



    private final AlgorithmeData data = new AlgorithmeData();
    private final AnalysisData analysisData = new AnalysisData();
    private List<Algorithme> algos;
    private List<Algorithme> filteredAlgos;
    private int currentPage = 0;
    private final int cardsPerPage = 5;
    private int currentAnalysisId = -1; // ID de l'analyse en cours
    private GeminiService geminiService;

    @FXML
    public void initialize() {
        algos = data.getAll();
        filteredAlgos = algos;

        System.out.println("=== DEBUG ALGORITHMES ===");
        System.out.println("Nombre d'algorithmes chargés: " + algos.size());
        System.out.println("cardContainer: " + cardContainer);
        // Initialisation de base

        initializeSearchChatSection();
        updateCards();
        updateButtons();

        prevButton.setOnAction(e -> previousPage());
        nextButton.setOnAction(e -> nextPage());

        // ⚠️ INITIALISATION DIFFÉRÉE pour le bouton d'import
        Platform.runLater(() -> {
            System.out.println("=== SCENE READY ===");
            System.out.println("Import button scene: " + importButton.getScene());
            System.out.println("Import button in scene: " + (importButton.getScene() != null));

            initializeImportDecryptSection();
        });
    }
    // === MÉTHODES POUR LA PREMIÈRE INTERFACE (IMPORT/DECRYPT) ===

    private void initializeImportDecryptSection() {
        // S'assurer que le champ est éditable
        cipherInputField.setEditable(true);

        // DEBUG: Vérifier que le bouton existe
        System.out.println("=== DEBUG IMPORT BUTTON ===");
        System.out.println("Import button: " + importButton);
        System.out.println("Import button visible: " + importButton.isVisible());
        System.out.println("Import button disabled: " + importButton.isDisabled());
        System.out.println("Import button parent: " + importButton.getParent());

        // Initialiser le bouton d'importation avec debug
        importButton.setOnAction(e -> {
            System.out.println("✅✅✅ BOUTON IMPORT CLIQUE ! ✅✅✅");
            handleImportFile();
        });

        // Debug des événements de souris
        importButton.setOnMouseEntered(e -> System.out.println("🐭 Souris SUR le bouton import"));
        importButton.setOnMouseExited(e -> System.out.println("🐭 Souris QUITTE le bouton import"));
        importButton.setOnMousePressed(e -> System.out.println("🐭 Souris PRESSE sur le bouton import"));

        importButton.setTooltip(new Tooltip("Import cipher text from file"));

        // Initialiser le bouton Decrypt
        decryptButton.setTooltip(new Tooltip("Decrypt the cipher text"));

        // Remplir la section des checkboxes d'algorithmes
        populateAlgOptions();
    }

    @FXML
    private void handleImportFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File (TXT, CSV, PDF, DOCX)");

        // Ajouter des filtres pour les types de fichiers
        FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("All Supported Files", "*.txt", "*.csv", "*.pdf", "*.docx");
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
        FileChooser.ExtensionFilter pdfFilter = new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf");
        FileChooser.ExtensionFilter docxFilter = new FileChooser.ExtensionFilter("Word Files (*.docx)", "*.docx");
        FileChooser.ExtensionFilter anyFilter = new FileChooser.ExtensionFilter("All Files (*.*)", "*.*");
        fileChooser.getExtensionFilters().addAll(allFilter, txtFilter, csvFilter, pdfFilter, docxFilter, anyFilter);
        fileChooser.setSelectedExtensionFilter(allFilter);  // Définir comme filtre par défaut

        try {
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) importButton.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                String fileName = selectedFile.getName().toLowerCase();
                String content = "";

                // Déterminer le type de fichier et lire le contenu
                if (fileName.endsWith(".txt")) {
                    content = readTextFile(selectedFile);
                } else if (fileName.endsWith(".csv")) {
                    content = readCsvFile(selectedFile);
                } else if (fileName.endsWith(".pdf")) {
                    content = readPdfFile(selectedFile);
                } else if (fileName.endsWith(".docx")) {
                    content = readDocxFile(selectedFile);
                } else {
                    // Par défaut, essayer de lire comme texte
                    content = readTextFile(selectedFile);
                }

                // Mettre le contenu dans le TextArea
                if (cipherInputField != null && !content.isEmpty()) {
                    cipherInputField.setText(content.trim());
                    // Sauvegarder le texte dans la base de données
                    saveTextToDatabase(content.trim());
                } else if (cipherInputField == null) {
                    System.err.println("ERROR: cipherInputField is null");
                }
            }
        } catch (Exception e) {
            System.err.println("Import Error: " + e.getMessage());
            e.printStackTrace();
            // Afficher une erreur en cas de problème
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText("Failed to import file");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private String readTextFile(File file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String readCsvFile(File file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String readPdfFile(File file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            content.append(stripper.getText(document));
        }
        return content.toString();
    }

    private String readDocxFile(File file) throws Exception {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
        }
        return content.toString();
    }

    private void saveTextToDatabase(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            currentAnalysisId = analysisData.insertAnalysis(text);
            if (currentAnalysisId != -1) {
                System.out.println("✅ Nouvelle analyse sauvegardée avec l'ID: " + currentAnalysisId);
            } else {
                System.err.println("❌ Erreur lors de la sauvegarde du texte dans la base de données");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde du texte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDecrypt() {
        String text = cipherInputField.getText();

        if (text == null || text.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun texte");
            alert.setHeaderText("Veuillez saisir ou importer un texte");
            alert.setContentText("Le champ de texte est vide. Veuillez saisir ou importer un texte à décrypter.");
            alert.showAndWait();
            return;
        }

        saveTextToDatabase(text.trim());
        System.out.println("Décryptage du texte (ID analyse: " + currentAnalysisId + ")");

        // Optionnel: Demander une analyse à Gemini
        if (geminiService != null) {
            String analysisPrompt = "Analyse ce texte chiffré et suggère des méthodes de décryptage: " + text;
            new Thread(() -> {
                try {
                    String response = geminiService.askQuestion(analysisPrompt);
                    javafx.application.Platform.runLater(() -> {
                        addMessageToChat("**Analyse du texte:** " + response, "bot");
                    });
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'analyse Gemini: " + e.getMessage());
                }
            }).start();
        }
    }

    private void populateAlgOptions() {
        algOptionsContainer.getChildren().clear();

        // SUPPRIMEZ toute la création du GridPane
        // GridPane grid = new GridPane();  ← SUPPRIMEZ

        // AJOUTEZ directement à algOptionsContainer (qui est maintenant GridPane)
        algOptionsContainer.setHgap(24);
        algOptionsContainer.setVgap(10);
        algOptionsContainer.setPadding(new Insets(6, 0, 0, 0));

        // Ajoutez des contraintes de colonnes
        for (int c = 0; c < 3; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            algOptionsContainer.getColumnConstraints().add(cc);
        }

        List<Algorithme> algosList = algos;

        for (int i = 0; i < algosList.size(); i++) {
            Algorithme algo = algosList.get(i);
            int row = i / 3;
            int col = i % 3;

            HBox algBox = createAlgoCheckBoxRow(algo);
            algBox.setPrefWidth(220);
            GridPane.setHgrow(algBox, Priority.ALWAYS);
            algOptionsContainer.add(algBox, col, row);  // ← CHANGEZ "grid.add" en "algOptionsContainer.add"
        }
    }

    private HBox createAlgoCheckBoxRow(Algorithme algo) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(algo.getNom());
        label.getStyleClass().add("toggle-label");

        StackPane labelBg = new StackPane(label);
        labelBg.getStyleClass().add("algo-item-bg");
        labelBg.setMinWidth(130);
        labelBg.setMaxWidth(130);

        ToggleButton toggle = new ToggleButton();
        toggle.setUserData(algo);
        toggle.setSelected(algo.getEnabled() == 1);

        StackPane switchGraphic = new StackPane();
        Region outer = new Region();
        outer.getStyleClass().add("circular-toggle-outer");
        Region inner = new Region();
        inner.getStyleClass().add("circular-toggle-inner");
        StackPane.setAlignment(inner, Pos.CENTER);
        switchGraphic.getChildren().addAll(outer, inner);

        TranslateTransition slide = new TranslateTransition(Duration.millis(140), inner);
        final double offPos = -12;
        final double onPos = 12;
        toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                outer.getStyleClass().add("toggle-active");
                slide.setToX(onPos);
            } else {
                outer.getStyleClass().remove("toggle-active");
                slide.setToX(offPos);
            }
            slide.playFromStart();

            int newState = toggle.isSelected() ? 1 : 0;
            algo.setEnabled(newState);
            data.setEnabled(algo.getId(), newState);
            refreshFilteredAlgos();
        });

        if (toggle.isSelected()) {
            outer.getStyleClass().add("toggle-active");
            inner.setTranslateX(onPos);
        } else {
            inner.setTranslateX(offPos);
        }

        toggle.setGraphic(switchGraphic);
        toggle.setStyle("-fx-background-color: transparent; -fx-padding: 0 6 0 6;");

        row.getChildren().addAll(labelBg, toggle);
        return row;
    }

    // CORRECTION: Ajoutez cette méthode manquante
    private void refreshFilteredAlgos() {
        List<Algorithme> selected = new ArrayList<>();
        // Recursively traverse the node tree under algOptionsContainer and collect selected toggles
        collectSelectedToggles(algOptionsContainer, selected);

        filteredAlgos = selected;
        currentPage = 0;
        updateCards();
        updateButtons();
    }

    private void collectSelectedToggles(javafx.scene.Node node, List<Algorithme> selected) {
        if (node instanceof ToggleButton) {
            ToggleButton btn = (ToggleButton) node;
            if (btn.isSelected() && btn.getUserData() instanceof Algorithme) {
                selected.add((Algorithme) btn.getUserData());
            }
            return;
        }

        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                collectSelectedToggles(child, selected);
            }
        }
    }

    // === MÉTHODES POUR LA DEUXIÈME INTERFACE (SEARCH/CHAT) ===

    private void initializeSearchChatSection() {
        initializeGeminiService();
        initializeSearchIcon();
        initializeChatInterface();
    }

    private void initializeGeminiService() {
        String apiKey = ConfigManager.loadApiKey();
        if (apiKey != null) {
            geminiService = new GeminiService(apiKey);
            System.out.println("Service Gemini initialisé avec succès");
        } else {
            addMessageToChat("Erreur: Clé API non trouvée. Créez un fichier 'api_key.txt' avec votre clé Gemini.", "error");
            System.err.println("Erreur: Clé API Gemini non trouvée");
        }
    }

    private void initializeChatInterface() {
        chatInputField.setOnAction(event -> sendMessageToGemini());
        flashLabel.setOnMouseClicked(event -> sendMessageToGemini());
        configureScrollPane();
    }

    private void configureScrollPane() {
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setFitToHeight(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        chatMessagesContainer.setFillWidth(true);
    }

    private void sendMessageToGemini() {
        String userMessage = chatInputField.getText().trim();
        if (userMessage.isEmpty()) return;

        addMessageToChat(userMessage, "user");
        chatInputField.clear();
        flashLabel.setText("⏳");
        chatInputField.setDisable(true);

        new Thread(() -> {
            try {
                String response = geminiService.askQuestion(userMessage);

                // Boucle automatique si la réponse est tronquée
                StringBuilder completeResponse = new StringBuilder(response);

                String finalResponse = completeResponse.toString();

                javafx.application.Platform.runLater(() -> {
                    addMessageToChat(finalResponse, "bot");
                    flashLabel.setText("➤");
                    chatInputField.setDisable(false);
                    scrollToBottom();
                    chatInputField.requestFocus();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    addMessageToChat("Désolé, une erreur s'est produite. Vérifiez votre connexion internet et votre clé API.", "error");
                    flashLabel.setText("➤");
                    chatInputField.setDisable(false);
                    scrollToBottom();
                    chatInputField.requestFocus();
                });
                e.printStackTrace();
            }
        }).start();
    }


    private void showHelp() {
        String help = "🆘 **AIDE - Commandes disponibles:**\n\n"
                + "**Conversation intelligente:**\n"
                + "• 'plus détaillé' - Développe la réponse précédente\n"
                + "• 'continue' - Suite du sujet en cours\n"
                + "• 'nouveau sujet' - Réinitialise la conversation\n\n"
                + "**Commandes techniques:**\n"
                + "• '/models' - Liste les modèles disponibles\n"
                + "• '/help' - Affiche cette aide\n\n"
                + "**Exemple d'utilisation:**\n"
                + "1. 'Explique le déchiffrement'\n"
                + "2. 'plus détaillé' ← Développe automatiquement !";

        addMessageToChat(help, "bot");
        chatInputField.clear();
    }

    private void addMessageToChat(String message, String type) {
        Platform.runLater(() -> {
            // Créer un HBox pour l'alignement
            HBox messageContainer = new HBox();
            messageContainer.setAlignment(Pos.CENTER_LEFT);

            if ("user".equals(type)) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
            }

            // Utiliser TextFlow au lieu de Label pour les longs textes
            TextFlow textFlow = new TextFlow();
            textFlow.setMaxWidth(500); // Largeur maximale
            textFlow.setPadding(new Insets(0, 0, 0, 0));

            // Formater le texte (gérer les retours à la ligne)
            String formattedMessage = formatMessageForDisplay(message);

            // Créer le texte avec la bonne couleur
            Text textNode = new Text(formattedMessage);
            textNode.setFont(Font.font("Segoe UI", 14));

            if ("user".equals(type)) {
                textNode.setFill(Color.web("#00E5FF")); // Bleu pour l'utilisateur
                textFlow.getStyleClass().add("message-on-image-user");
            } else {
                textNode.setFill(Color.WHITE); // Blanc pour l'IA
                textFlow.getStyleClass().add("message-on-image-bot");
            }

            // Ajouter l'effet d'ombre pour la lisibilité
            textNode.setEffect(new DropShadow(1, Color.BLACK));

            textFlow.getChildren().add(textNode);

            // Créer un conteneur final pour forcer la taille
            VBox finalContainer = new VBox(textFlow);
            finalContainer.setAlignment(Pos.CENTER_LEFT);
            if ("user".equals(type)) {
                finalContainer.setAlignment(Pos.CENTER_RIGHT);
            }

            // FORCER la taille
            finalContainer.setPrefHeight(-1);
            finalContainer.setMinHeight(Region.USE_PREF_SIZE);
            finalContainer.setMaxHeight(Region.USE_PREF_SIZE);

            messageContainer.getChildren().add(finalContainer);
            chatMessagesContainer.getChildren().add(messageContainer);

            // Scroll vers le bas
            scrollToBottom();
        });
    }
    private String formatMessageForDisplay(String text) {
        // Remplacer les doubles retours à la ligne
        text = text.replace("\n\n", "\n \n");

        // Ajouter des retours à la ligne après les points si la ligne est longue
        text = text.replace(". ", ".\n");

        // Limiter la longueur des lignes (optionnel)
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        int lineLength = 0;

        for (String word : words) {
            if (lineLength + word.length() > 70) { // ~70 caractères par ligne
                result.append("\n");
                lineLength = 0;
            }
            result.append(word).append(" ");
            lineLength += word.length() + 1;
        }

        return result.toString().trim();
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            // Forcer le layout
            chatMessagesContainer.applyCss();
            chatMessagesContainer.layout();

            // Attendre un peu que le layout se fasse
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> {
                // Calculer la hauteur totale
                double totalHeight = 0;
                for (javafx.scene.Node node : chatMessagesContainer.getChildren()) {
                    totalHeight += node.getBoundsInParent().getHeight();
                }

                // Défiler
                chatScrollPane.setVvalue(1.0);

                // Forcer un deuxième scroll après un court délai
                PauseTransition pause2 = new PauseTransition(Duration.millis(50));
                pause2.setOnFinished(e -> chatScrollPane.setVvalue(1.0));
                pause2.play();
            });
            pause.play();
        });
    }

    private void initializeSearchIcon() {
        searchIconLabel.setText("🔍");
        searchIconLabel.setOnMouseClicked(event -> performSearch());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterAlgorithms(newValue));
        searchField.setOnAction(event -> performSearch());
    }

    private void filterAlgorithms(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredAlgos = algos;
        } else {
            String lowercaseSearch = searchText.toLowerCase();
            filteredAlgos = algos.stream()
                    .filter(algo ->
                            algo.getNom().toLowerCase().contains(lowercaseSearch) ||
                                    algo.getDescription().toLowerCase().contains(lowercaseSearch))
                    .collect(Collectors.toList());
        }

        currentPage = 0;
        updateCards();
        updateButtons();
    }

    private void performSearch() {
        String searchText = searchField.getText().trim();
        filterAlgorithms(searchText);

        if (!searchText.isEmpty()) {
            searchIconLabel.setText("⏳");
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    javafx.application.Platform.runLater(() -> {
                        searchIconLabel.setText("🔍");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    // === MÉTHODES COMMUNES ===

    private void updateCards() {
        cardContainer.getChildren().clear();

        int startIndex = currentPage * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, filteredAlgos.size()); // CORRECTION: filteredAlgos

        String[] icons = {
                "\uD83D\uDD12", "\uD83D\uDD13", "\uD83D\uDD10", "\u2694\uFE0F",
                "\uD83D\uDD27", "\uD83D\uDD28", "\uD83D\uDCCA", "\uD83D\uDD2A"
        };

        for (int i = startIndex; i < endIndex; i++) {
            Algorithme algo = filteredAlgos.get(i); // CORRECTION: filteredAlgos

            VBox card = new VBox(8);
            card.setPrefSize(220, 100);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().add("card");
            card.setPadding(new Insets(12, 15, 12, 15));

            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setMaxWidth(190);

            Label icon = new Label(icons[i % icons.length]);
            icon.getStyleClass().add("card-icon");

            Label title = new Label(algo.getNom());
            title.getStyleClass().add("card-title");
            title.setWrapText(true);
            title.setMaxWidth(150);

            headerBox.getChildren().addAll(icon, title);

            Label description = new Label(algo.getDescription());
            description.getStyleClass().add("card-description");
            description.setWrapText(true);
            description.setMaxWidth(190);

            Tooltip tooltip = new Tooltip(algo.getDescription());
            Tooltip.install(description, tooltip);

            card.getChildren().addAll(headerBox, description);
            cardContainer.getChildren().add(card);
        }

        if (filteredAlgos.isEmpty()) { // CORRECTION: filteredAlgos
            Label noResults = new Label("Aucun algorithme trouvé");
            noResults.getStyleClass().add("no-results-message");
            cardContainer.getChildren().add(noResults);
        }
    }

    private void updateButtons() {
        prevButton.setDisable(currentPage == 0);
        int totalPages = (int) Math.ceil((double) filteredAlgos.size() / cardsPerPage);
        nextButton.setDisable(currentPage >= totalPages - 1 || filteredAlgos.isEmpty());
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            updateCards();
            updateButtons();
        }
    }

    private void nextPage() {
        int totalPages = (int) Math.ceil((double) filteredAlgos.size() / cardsPerPage);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateCards();
            updateButtons();
        }
    }

    // Méthode utilitaire pour utiliser Gemini depuis d'autres parties de l'application
    public String getCryptographyAnalysis(String scenario) {
        if (geminiService != null) {
            return geminiService.askQuestion(
                    "Analyse this cryptography scenario and provide technical advice: " + scenario
            );
        }
        return "Gemini service not available";
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private StackPane mainContainer;

    private Node openedModal = null;

    @FXML
    private void decryptButtonClicked() {

        handleDecrypt();
        System.out.println("TEST ➜ decryptButtonClicked()");

        try {
            AnalysisService analysisService = new AnalysisService();
            Analysis lastAnalysis = analysisService.getLastInsertedAnalysis();

            if (lastAnalysis == null) {
                showAlert("Aucun texte trouvé dans la base.");
                return;
            }

            // 🚫 Empêcher plusieurs modales
            if (openedModal != null) {
                return;  // La modale est déjà ouverte
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/app/CryptoAnalyser/View/DecryptModal.fxml")
            );

            StackPane modalRoot = loader.load();
            DecryptModalController modalController = loader.getController();

            // Envoyer texte
            modalController.setOriginalText(lastAnalysis.getTexteOriginal());

            // Callback fermeture
            modalController.setOnClose(() -> {
                mainContainer.getChildren().remove(modalRoot);
                openedModal = null;
            });

            // Ajout au StackPane
            mainContainer.getChildren().add(modalRoot);
            openedModal = modalRoot;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void showStatistics() {
        try {
            // Charger la page des statistiques
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/CryptoAnalyser/View/statistics.fxml"));
            Parent statisticsRoot = loader.load();

            // Remplacer le contenu de la scène actuelle
            Scene currentScene = mainContainer.getScene();
            currentScene.setRoot(statisticsRoot);

        } catch (Exception e) {
            e.printStackTrace();

            // Message d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger les statistiques");
            alert.setContentText("Vérifiez que le fichier statistics.fxml existe.");
            alert.showAndWait();
        }
    }
    @FXML
    public void showAboutUs() {
        try {
            // Charge la vue "À propos de nous"
            // NOTE : Vérifiez si votre dossier s'appelle "view" (minuscule) ou "View" (majuscule)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/app/CryptoAnalyser/view/AboutUsView.fxml"));
            Parent root = loader.load();

            // Remplace la scène actuelle par la nouvelle page
            Scene currentScene = mainContainer.getScene();
            currentScene.setRoot(root);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Impossible d'ouvrir la page À propos : " + e.getMessage());
            alert.showAndWait();
        }
    }
}