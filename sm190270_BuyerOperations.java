/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.BuyerOperations;

/**
 *
 * @author Mateja
 */
public class sm190270_BuyerOperations implements BuyerOperations {
    
    private Connection conn = sm190270_DB.getInstance().getConnection();
    
    @Override
    public int createBuyer(String name, int cityId) {
        String query = "INSERT INTO dbo.Buyer(Name, Credit, IDCity) VALUES (?, ?, ?)";
        String checkQuery = "SELECT IDCity FROM dbo.City WHERE IDCity = ?";

        try (PreparedStatement psCheckCity = conn.prepareStatement(checkQuery);
             PreparedStatement psCreateBuyer = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            psCheckCity.setInt(1, cityId);
            try (ResultSet rsCity = psCheckCity.executeQuery()) {
                if (rsCity.next()) {
                    psCreateBuyer.setString(1, name);
                    psCreateBuyer.setInt(2, 0);
                    psCreateBuyer.setInt(3, cityId);
                    int affRows = psCreateBuyer.executeUpdate();
                    if (affRows == 1) {
                        try (ResultSet rsGenKeys = psCreateBuyer.getGeneratedKeys()) {
                            if (rsGenKeys.next()) {
                                return rsGenKeys.getInt(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public int setCity(int buyerId, int cityId) {
        String query = "UPDATE dbo.Buyer SET IDCity = ? WHERE IDBuyer = ?";
        String checkQuery = "SELECT IDBuyer FROM dbo.Buyer WHERE IDBuyer = ?";

        try (PreparedStatement psCheckBuyer = conn.prepareStatement(checkQuery);
             PreparedStatement psUpdateCity = conn.prepareStatement(query)) {

            psCheckBuyer.setInt(1, buyerId);
            try (ResultSet rsBuyer = psCheckBuyer.executeQuery()) {
                if (rsBuyer.next()) {
                    psUpdateCity.setInt(1, cityId);
                    psUpdateCity.setInt(2, buyerId);
                    int affRows = psUpdateCity.executeUpdate();
                    if (affRows == 1) {
                        return 1;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public int getCity(int buyerId) {
        
        try (PreparedStatement psBuyersCity = conn.prepareStatement("select IDCity\n" +
            "from dbo.Buyer\n" +
            "where IDBuyer = ?");) {
            
            psBuyersCity.setInt(1, buyerId);
            try (ResultSet rsBuyersCity = psBuyersCity.executeQuery();) {
                if (rsBuyersCity.next()) {
                    int buyersCity = rsBuyersCity.getInt(1);
                    return buyersCity;
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public BigDecimal increaseCredit(int buyerId, java.math.BigDecimal credit) {
        
        try (PreparedStatement psIncreaseCredit = conn.prepareStatement("update dbo.Buyer\n" +
                "set Credit = Credit + ?\n" +
                "where IDBuyer = ?");) {
            
            psIncreaseCredit.setBigDecimal(1, credit);
            psIncreaseCredit.setInt(2, buyerId);
            int affRows = psIncreaseCredit.executeUpdate();
            if (affRows == 1) {
                return this.getCredit(buyerId).setScale(3);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public int createOrder(int buyerId) {
        String query = "INSERT INTO [Order](Status, IDBuyer) VALUES (?, ?)";
        String checkQuery = "SELECT IDBuyer FROM dbo.Buyer WHERE IDBuyer = ?";

        try (PreparedStatement psCheckBuyer = conn.prepareStatement(checkQuery);
             PreparedStatement psCreateOrder = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            psCheckBuyer.setInt(1, buyerId);
            try (ResultSet rsBuyer = psCheckBuyer.executeQuery()) {
                if (rsBuyer.next()) {
                    psCreateOrder.setString(1, "created");
                    psCreateOrder.setInt(2, buyerId);
                    int affRows = psCreateOrder.executeUpdate();
                    if (affRows == 1) {
                        try (ResultSet rsGenKeys = psCreateOrder.getGeneratedKeys()) {
                            if (rsGenKeys.next()) {
                                return rsGenKeys.getInt(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }

    @Override
    public List<Integer> getOrders(int buyerId) {
        
        try (PreparedStatement psAllOrders = conn.prepareStatement("SELECT IDOrder\n" +
            "FROM dbo.[Order]\n" +
            "WHERE IDBuyer = ?");) {

            psAllOrders.setInt(1, buyerId);
            try (ResultSet rsAllOrders = psAllOrders.executeQuery();) {

                ArrayList<Integer> allOrders = new ArrayList<>();
                while (rsAllOrders.next()) {
                    allOrders.add(rsAllOrders.getInt(1));
                }
                return allOrders;

            } catch (SQLException ex) {
                Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    @Override
    public BigDecimal getCredit(int buyerId) {
        
        try (PreparedStatement psCredit = conn.prepareStatement("select Credit\n" +
            "from dbo.Buyer\n" +
            "where IDBuyer = ?");) {
            
            psCredit.setInt(1, buyerId);
            try (ResultSet rsCredit = psCredit.executeQuery();) {
                if (rsCredit.next()) {
                    BigDecimal credit = rsCredit.getBigDecimal(1).setScale(3);
                    return credit;
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_BuyerOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
}
