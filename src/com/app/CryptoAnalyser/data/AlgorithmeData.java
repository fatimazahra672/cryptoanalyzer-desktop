package com.app.CryptoAnalyser.data;

import com.app.CryptoAnalyser.model.Algorithme;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlgorithmeData {

    private final String url = "jdbc:sqlite:C:\\Users\\hp\\Desktop\\S3 Project\\CryptoAnalyzer\\db-dump\\cryptoai.db";



    public List<Algorithme> getAll() {
        List<Algorithme> list = new ArrayList<>();
        String sql = "SELECT * FROM algorithmes";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Algorithme(
                        rs.getInt("id_alg"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("exemple"),
                        rs.getString("categorie")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void setEnabled(int idAlg, int enabled) {
        String sql = "UPDATE algorithmes SET enabled = ? WHERE id_alg = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enabled);
            pstmt.setInt(2, idAlg);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
