
# CryptoAnalyzer 

**Application de Décryptage de Textes Chiffrés assistée par IA**

Projet académique réalisé dans le cadre de la formation **Génie Informatique** à l'**École Nationale des Sciences Appliquées d'Al Hoceima (ENSAH)**.

---

##  Présentation

**CryptoAnalyzer** est une application Desktop développée en **Java / JavaFX** suivant l'architecture **MVC**. Elle est conçue pour analyser, détecter et déchiffrer automatiquement des messages contenant plusieurs types de chiffrements imbriqués.

L'application se distingue par l'intégration d'un **Assistant IA** (Gemini) et d'une base de données locale **SQLite** pour l'historique et les statistiques.

### Fonctionnalités Clés
*   **Multi-Algorithmes :** Supporte César (ROT), Base64, Hexadécimal.
*   **Détection Automatique :** Identifie les parties chiffrées dans un texte naturel.
*   **Assistant Intelligent :** Chatbot intégré pour expliquer les concepts cryptographiques.
*   **Reporting :** Génération de rapports d'analyse au format PDF.
*   **Statistiques :** Visualisation des données d'utilisation.

---

## Prérequis et Configuration

Tous les fichiers nécessaires au bon fonctionnement du projet sont **déjà inclus** dans l'archive :

1.  **Bibliothèques Java (.JAR) :**
    *   Toutes les dépendances (Apache POI, PDFBox, Commons, etc.) se trouvent dans le dossier **`lib`** à la racine du projet.
    *   **Action requise :** Dans votre IDE (IntelliJ), allez dans *Project Structure > Modules > Dependencies* et ajoutez tous les fichiers `.jar` présents dans ce dossier `lib`.

2.  **Ressources (Images & Styles) :**
    *   Les fichiers CSS, images et dictionnaires sont situés dans le dossier **`ressources`**. Assurez-vous que ce dossier est bien marqué comme "Resources Root" si nécessaire.

3.  **Base de Données :**
    *   Le fichier **SQLite** (`cryptoai.db`) est déjà configuré et inclus dans le projet. Aucune installation de serveur n'est requise.

---

##  Guide de Test (Scénario de Démonstration)

Pour valider le fonctionnement de l'application et la détection multi-formats, veuillez utiliser le scénario suivant :

**1. Lancez l'application** (via la classe `Main`).

**2. Copiez le texte ci-dessous :**

```text
To access the vault, follow these instructions:

The first clue is fdw.
The second clue is 62697264.
The third clue is U3Vubnk=.

Combine them to get the password.
Then use khoor to enter.
The final code is 736563726574.
```     
## Procédure :
Collez ce texte dans la zone de saisie principale ("Cipher Input").
Assurez-vous que les options (César, Base64, Hex) sont activées.
Cliquez sur le bouton Decrypt. 
## Résultats attendus :
L'application doit détecter les segments spécifiques et afficher leur traduction :
fdw ➔ cat (César)
62697264 ➔ bird (Hex)
U3Vubnk= ➔ Sunny (Base64)
khoor ➔ hello (César)
736563726574 ➔ secret (Hex)
## Équipe de Réalisation
* Année Universitaire : 2024-2025
* Filière : Génie Informatique
* ELBOUKHARI Fatima Zahra
* ELHAMDANI Fatima Zohra
* CHTIOUI Hiba
* Encadré par : Mr. BAHRI Abdelkhalak