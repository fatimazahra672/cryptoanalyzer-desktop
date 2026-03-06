package com.app.CryptoAnalyser.data;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class StockageDecryption {
    private final String url = "jdbc:sqlite:C:\\Users\\hp\\Desktop\\S3 Project\\CryptoAnalyzer\\db-dump\\cryptoai.db";
    public int saveAnalysis(String originalText, String decryptedText, String algo) {
            String sql = "INSERT INTO analyses (texte_original, texte_dechiffre, alg_detecte, date_analysis) VALUES (?, ?, ?, datetime('now'))";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, originalText);
                pstmt.setString(2, decryptedText);
                pstmt.setString(3, algo);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // retourne id_analysis
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1; // erreur
        }

        public void saveWord(int idAnalysis, String encrypted, String decrypted, String algo) {
            String sql = "INSERT INTO mots_decryptes (id_analysis, mot_chiffre, mot_dechiffre, algorithme) VALUES (?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, idAnalysis);
                pstmt.setString(2, encrypted);
                pstmt.setString(3, decrypted);
                pstmt.setString(4, algo);
                pstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }



        //

     public Map<String, Boolean> getEnabledAlgos() {
         Map<String, Boolean> enabledAlgos = new HashMap<>();
         String query = "SELECT nom, enabled FROM algorithmes";

         try (Connection conn = DriverManager.getConnection(url);
              PreparedStatement pstmt = conn.prepareStatement(query);
              ResultSet rs = pstmt.executeQuery()) {

             while (rs.next()) {
                 String nom = rs.getString("nom");
                 boolean enabled = rs.getInt("enabled") == 1;
                 enabledAlgos.put(nom, enabled);
             }

         } catch (Exception e) {
             e.printStackTrace();
         }

         return enabledAlgos;
     }

 }
