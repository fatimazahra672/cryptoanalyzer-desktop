package com.app.CryptoAnalyser.data;

import com.app.CryptoAnalyser.model.Analysis;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnalysisData {

    private final String url = "jdbc:sqlite:C:\\Users\\hp\\Desktop\\S3 Project\\CryptoAnalyzer\\db-dump\\cryptoai.db";

    // Configuration pour éviter les problèmes de verrouillage
    static {
        try {
            // Charger le driver SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtenir une connexion à la base de données avec configuration optimisée
     */
    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        // Activer le mode WAL pour permettre les lectures pendant les écritures
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout=5000;"); // Timeout de 5 secondes
        }
        return conn;
    }

    /**
     * Insère une nouvelle analyse dans la base de données
     * @param texteOriginal Le texte original à analyser
     * @return L'ID de l'analyse créée, ou -1 en cas d'erreur
     */
    public int insertAnalysis(String texteOriginal) {
        String sql = "INSERT INTO analyses (texte_original, texte_dechiffre, alg_detecte, date_analysis) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, texteOriginal);
            pstmt.setString(2, ""); // texte_dechiffre vide au début
            pstmt.setString(3, ""); // alg_detecte vide au début
            pstmt.setString(4, LocalDateTime.now().toString());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Récupérer l'ID généré
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de l'analyse: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer toutes les ressources
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return -1;
    }

    /**
     * Met à jour une analyse existante
     * @param analysis L'objet Analysis avec les nouvelles données
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateAnalysis(Analysis analysis) {
        String sql = "UPDATE analyses SET texte_original = ?, texte_dechiffre = ?, alg_detecte = ?, date_analysis = ? WHERE id_analysis = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, analysis.getTexteOriginal());
            pstmt.setString(2, analysis.getTexteDechiffre());
            pstmt.setString(3, analysis.getAlgDetecte());
            pstmt.setString(4, analysis.getDateAnalysis());
            pstmt.setInt(5, analysis.getIdAnalysis());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'analyse: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * Récupère toutes les analyses de la base de données
     * @return Liste de toutes les analyses
     */
    public List<Analysis> getAllAnalyses() {
        List<Analysis> analyses = new ArrayList<>();
        String sql = "SELECT * FROM analyses ORDER BY date_analysis DESC";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Analysis analysis = new Analysis(
                        rs.getInt("id_analysis"),
                        rs.getString("texte_original"),
                        rs.getString("texte_dechiffre"),
                        rs.getString("alg_detecte"),
                        rs.getString("date_analysis")
                );
                analyses.add(analysis);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des analyses: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return analyses;
    }

    /**
     * Récupère une analyse par son ID
     * @param idAnalysis L'ID de l'analyse à récupérer
     * @return L'objet Analysis ou null si non trouvé
     */
    public Analysis getAnalysisById(int idAnalysis) {
        String sql = "SELECT * FROM analyses WHERE id_analysis = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, idAnalysis);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Analysis(
                        rs.getInt("id_analysis"),
                        rs.getString("texte_original"),
                        rs.getString("texte_dechiffre"),
                        rs.getString("alg_detecte"),
                        rs.getString("date_analysis")
                );
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'analyse: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Supprime une analyse de la base de données
     * @param idAnalysis L'ID de l'analyse à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteAnalysis(int idAnalysis) {
        String sql = "DELETE FROM analyses WHERE id_analysis = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, idAnalysis);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'analyse: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}