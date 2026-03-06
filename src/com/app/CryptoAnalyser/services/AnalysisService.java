package com.app.CryptoAnalyser.services;
import com.app.CryptoAnalyser.model.Analysis;
import java.sql.*;
public class AnalysisService {

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

    public Analysis getLastInsertedAnalysis() {
        String sql = "SELECT * FROM analyses ORDER BY id_analysis DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Analysis m = new Analysis();
                m.setIdAnalysis(rs.getInt("id_analysis"));
                m.setTexteOriginal(rs.getString("texte_original"));
                m.setTexteDechiffre(rs.getString("texte_dechiffre"));
                m.setAlgDetecte(rs.getString("alg_detecte"));

                // Récupérer la date sous forme de texte brut
                String rawDate = rs.getString("date_analysis");

                if (rawDate != null) {
                    String cleanDate = rawDate.replace("T", " ");

                    // Couper après les millisecondes pour éviter les nanosecondes
                    if (cleanDate.contains(".")) {
                        cleanDate = cleanDate.substring(0, cleanDate.indexOf('.') + 4);
                    }

                    m.setDateAnalysis(cleanDate);
                } else {
                    m.setDateAnalysis(null);
                }

                return m;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
