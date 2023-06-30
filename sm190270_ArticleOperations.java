/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import rs.etf.sab.operations.ArticleOperations;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mateja
 */
public class sm190270_ArticleOperations implements ArticleOperations {

    private Connection conn = sm190270_DB.getInstance().getConnection();
    
    @Override
    public int createArticle(int shopId, String articleName, int articlePrice) {
        String query = "INSERT INTO dbo.Article(Name, ShopQuantity, Price, IDShop) VALUES (?, ?, ?, ?)";
        String checkQuery = "SELECT IDShop FROM dbo.Shop WHERE IDShop = ?";

        try (PreparedStatement psCheckShop = conn.prepareStatement(checkQuery);
             PreparedStatement psCreateArticle = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            psCheckShop.setInt(1, shopId);
            try (ResultSet rsShop = psCheckShop.executeQuery()) {
                if (rsShop.next()) {
                    psCreateArticle.setString(1, articleName);
                    psCreateArticle.setInt(2, 0);
                    psCreateArticle.setInt(3, articlePrice);
                    psCreateArticle.setInt(4, shopId);
                    int affRows = psCreateArticle.executeUpdate();
                    if (affRows == 1) {
                        try (ResultSet rsGenKeys = psCreateArticle.getGeneratedKeys()) {
                            if (rsGenKeys.next()) {
                                return rsGenKeys.getInt(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    
}
