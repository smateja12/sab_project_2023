/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.ShopOperations;

/**
 *
 * @author Mateja
 */
public class sm190270_ShopOperations implements ShopOperations {
    
    private Connection conn = sm190270_DB.getInstance().getConnection();
    
    @Override
    public int createShop(java.lang.String name, java.lang.String cityName) {
        
        try (PreparedStatement psCity = conn.prepareStatement("select IDCity\n" +
            "from City\n" +
            "where Name = ?");
            PreparedStatement psCheckShop = conn.prepareStatement("select IDShop from Shop where Name = ?");    
            PreparedStatement psShop = conn.prepareStatement("insert into Shop(Name, IDCity, DiscountPercentage)\n" +
            "values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
                ) {
            
            psCheckShop.setString(1, name);
            try (ResultSet rsCheckShop = psCheckShop.executeQuery();) {
                if (!rsCheckShop.next()) {
                    psCity.setString(1, cityName);
                    try (ResultSet rsCity = psCity.executeQuery();) {
                        if (rsCity.next()) {
                            int IDCity = rsCity.getInt(1);
                            psShop.setString(1, name);
                            psShop.setInt(2, IDCity);
                            psShop.setInt(3, 0);
                            int affRows = psShop.executeUpdate();
                            if (affRows == 1) {
                                
                                try (ResultSet rsGenKeys = psShop.getGeneratedKeys();) {
                                    if (rsGenKeys.next()) {
                                        return rsGenKeys.getInt(1);
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
                            }
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else {
                    return rsCheckShop.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public int setCity(int shopId, String cityName) {
        String query1 = "SELECT IDCity FROM City WHERE Name = ?";
        String query2 = "UPDATE Shop SET IDCity = ? WHERE IDShop = ?";

        try (PreparedStatement psSelectCity = conn.prepareStatement(query1);
             PreparedStatement psUpdateShop = conn.prepareStatement(query2)) {

            psSelectCity.setString(1, cityName);
            try (ResultSet rsCity = psSelectCity.executeQuery()) {
                if (rsCity.next()) {
                    int cityId = rsCity.getInt(1);
                    psUpdateShop.setInt(1, cityId);
                    psUpdateShop.setInt(2, shopId);
                    int rowsAffected = psUpdateShop.executeUpdate();
                    if (rowsAffected == 1) {
                        return 1;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public int getCity(int shopId) {
        String query = "SELECT IDCity FROM Shop WHERE IDShop = ?";

        try (PreparedStatement psGetCity = conn.prepareStatement(query)) {
            psGetCity.setInt(1, shopId);
            try (ResultSet rsCity = psGetCity.executeQuery()) {
                if (rsCity.next()) {
                    return rsCity.getInt("IDCity");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public int setDiscount(int shopId, int discountPercentage) {
        
        if (discountPercentage < 0 || discountPercentage > 100) {
            System.out.println("Popust mora biti u opsegu od 0% do 100%");
            return -1;
        }
        
        String query = "UPDATE Shop SET DiscountPercentage = ? WHERE IDShop = ?";

        try (PreparedStatement psSetDiscount = conn.prepareStatement(query)) {
            psSetDiscount.setInt(1, discountPercentage);
            psSetDiscount.setInt(2, shopId);

            int rowsAffected = psSetDiscount.executeUpdate();
            if (rowsAffected == 1) {
                return 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public int increaseArticleCount(int articleId, int increment) {
        String query = "UPDATE Article SET ShopQuantity = ShopQuantity + ? WHERE IDArticle = ?";

        try (PreparedStatement psIncreaseCount = conn.prepareStatement(query)) {
            psIncreaseCount.setInt(1, increment);
            psIncreaseCount.setInt(2, articleId);

            int affectedRows = psIncreaseCount.executeUpdate();
            if (affectedRows > 0) {
                int newCount = getArticleCount(articleId);
                return newCount;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public int getArticleCount(int articleId) {
        String query = "SELECT ShopQuantity FROM Article WHERE IDArticle = ?";

        try (PreparedStatement psGetArticleCount = conn.prepareStatement(query)) {
            psGetArticleCount.setInt(1, articleId);

            try (ResultSet rsArticleCount = psGetArticleCount.executeQuery()) {
                if (rsArticleCount.next()) {
                    return rsArticleCount.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public List<Integer> getArticles(int shopId) {
    
    String query = "SELECT IDArticle FROM Article WHERE IDShop = ?";

    try (PreparedStatement psGetArticles = conn.prepareStatement(query)) {
        psGetArticles.setInt(1, shopId);

        try (ResultSet rsArticles = psGetArticles.executeQuery()) {
            List<Integer> articles = new ArrayList<>();
            while (rsArticles.next()) {
                articles.add(rsArticles.getInt("IDArticle"));
            }
            return articles;
        }
        
    } catch (SQLException ex) {
        Logger.getLogger(sm190270_ArticleOperations.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
}

    @Override
    public int getDiscount(int shopId) {
        String query = "SELECT DiscountPercentage FROM Shop WHERE IDShop = ?";

        try (PreparedStatement psGetDiscount = conn.prepareStatement(query)) {
            psGetDiscount.setInt(1, shopId);

            try (ResultSet rsDiscount = psGetDiscount.executeQuery()) {
                if (rsDiscount.next()) {
                    return rsDiscount.getInt("DiscountPercentage");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_ShopOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }
    
//    public static void main(String[] args) {
//        testShopArticleOperations();
//    }
//    
//    public static void testShopCreation() {
//        sm190270_CityOperations cityOperations = new sm190270_CityOperations();
//        sm190270_ShopOperations shopOperations = new sm190270_ShopOperations();
//        sm190270_GeneralOperations genOperations = new sm190270_GeneralOperations();
//        
//        genOperations.eraseAll();
//        cityOperations.createCity("Kragujevac");
//        final int shopId = shopOperations.createShop("Gigatron", "Kragujevac");
//        final int cityId2 = cityOperations.createCity("Subotica");
//        shopOperations.setCity(shopId, "Subotica");
//
//        List<Integer> shops = cityOperations.getShops(cityId2);
//        if (!shops.isEmpty() && shops.get(0) == shopId) {
//            System.out.println("Shop ID matches the expected value.");
//        } else {
//            System.out.println("Shop ID does not match the expected value.");
//        }
//        genOperations.eraseAll();
//    }
//    
//    public static void testShopDiscount() {
//        sm190270_CityOperations cityOperations = new sm190270_CityOperations();
//        sm190270_ShopOperations shopOperations = new sm190270_ShopOperations();
//        sm190270_GeneralOperations genOperations = new sm190270_GeneralOperations();
//        
//        genOperations.eraseAll();
//        cityOperations.createCity("Kragujevac");
//        final int shopId = shopOperations.createShop("Gigatron", "Kragujevac");
//        shopOperations.setDiscount(shopId, 20);
//
//        int discount = shopOperations.getDiscount(shopId);
//        if (discount == 20) {
//            System.out.println("Shop discount matches the expected value.");
//        } else {
//            System.out.println("Shop discount does not match the expected value.");
//        }
//        genOperations.eraseAll();
//    }
//    
//    public static void testShopArticleOperations() {
//        sm190270_CityOperations cityOperations = new sm190270_CityOperations();
//        sm190270_ShopOperations shopOperations = new sm190270_ShopOperations();
//        sm190270_ArticleOperations articleOperations = new sm190270_ArticleOperations();
//        sm190270_GeneralOperations genOperations = new sm190270_GeneralOperations();
//        
//        genOperations.eraseAll();
//        
//        cityOperations.createCity("Kragujevac");
//        final int shopId = shopOperations.createShop("Gigatron", "Kragujevac");
//        final int articleId = articleOperations.createArticle(shopId, "Olovka", 10);
//        if (articleId != -1) {
//            System.out.println("Article ID is not -1.");
//        } else {
//            System.out.println("Article ID is -1.");
//        }
//        final int articleId2 = articleOperations.createArticle(shopId, "Gumica", 5);
//        if (articleId2 != -1) {
//            System.out.println("Article ID 2 is not -1.");
//        } else {
//            System.out.println("Article ID 2 is -1.");
//        }
//
//        shopOperations.increaseArticleCount(articleId, 5);
//        shopOperations.increaseArticleCount(articleId, 2);
//        final int articleCount = shopOperations.getArticleCount(articleId);
//        if (articleCount == 7) {
//            System.out.println("Article count matches the expected value.");
//        } else {
//            System.out.println("Article count does not match the expected value.");
//        }
//
//        List<Integer> articles = shopOperations.getArticles(shopId);
//        if (articles.size() == 2 && articles.contains(articleId) && articles.contains(articleId2)) {
//            System.out.println("Articles list is as expected.");
//        } else {
//            System.out.println("Articles list is not as expected.");
//        }
//        
//        genOperations.eraseAll();
//    }
    
}
