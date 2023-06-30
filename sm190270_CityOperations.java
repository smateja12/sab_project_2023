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
import rs.etf.sab.operations.CityOperations;

/**
 *
 * @author Mateja
 */
public class sm190270_CityOperations implements CityOperations {
    
    private Connection conn = sm190270_DB.getInstance().getConnection();
    
    @Override
    public int createCity(String name) {
        String query = "INSERT INTO dbo.City(Name) VALUES (?)";
        String checkQuery = "SELECT IDCity FROM dbo.City WHERE Name = ?";

        try (PreparedStatement psCheckCity = conn.prepareStatement(checkQuery);
             PreparedStatement psCreateCity = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            psCheckCity.setString(1, name);
            try (ResultSet rsCity = psCheckCity.executeQuery()) {
                if (rsCity.next()) {
                    return rsCity.getInt(1);
                } else {
                    psCreateCity.setString(1, name);
                    int affRows = psCreateCity.executeUpdate();
                    if (affRows == 1) {
                        try (ResultSet rsGenKeys = psCreateCity.getGeneratedKeys()) {
                            if (rsGenKeys.next()) {
                                return rsGenKeys.getInt(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }


    @Override
    public List<Integer> getCities() {
        
        try (PreparedStatement psAllCities = conn.prepareStatement("select IDCity\n" +
                "from dbo.City");) {
            
            try (ResultSet rsAllCities = psAllCities.executeQuery();) {
                ArrayList<Integer> allCities = new ArrayList<>();
                while (rsAllCities.next()) {
                    allCities.add(rsAllCities.getInt(1));
                }
                return allCities;
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }


//    public int connectCities(int cityId1, int cityId2, int distance) {
//        // max 1 linija izmedju 2 grada
//        
//        // check 
//        /*
//        if not exists (select IDLine from Line 
//        where (City1 = ? and City2 = ?) and not exists (
//                select IDLine 
//                from Line 
//                where (City1 = ? and City2 = ?)
//        )
//        begin
//                insert into Line(City1, City2, Distance)
//                values (?, ?, ?)
//
//                insert into Line(City1, City2, Distance)
//                values (?, ?, ?)
//        end
//        */
//        
//        try (PreparedStatement psConnectCities = conn.prepareStatement("if not exists (select IDLine from Line \n" +
//            "where (City1 = ? and City2 = ?) or (City1 = ? and City2 = ?)) \n" +
//            "begin\n" +
//            "	insert into Line(City1, City2, Distance)\n" +
//            "	values (?, ?, ?)\n" +
//            "end", PreparedStatement.RETURN_GENERATED_KEYS);) {
//            
//            psConnectCities.setInt(1, cityId1);
//            psConnectCities.setInt(2, cityId2);
//            psConnectCities.setInt(3, cityId2);
//            psConnectCities.setInt(4, cityId1);
//            psConnectCities.setInt(5, cityId1);
//            psConnectCities.setInt(6, cityId2);
//            psConnectCities.setInt(7, distance);
//            
//            int affRows = psConnectCities.executeUpdate();
//            if (affRows == 0) {
//                return -1;
//            }else {
//                try (ResultSet rsConnectCities = psConnectCities.getGeneratedKeys();) {
//                    if (rsConnectCities.next()) {
//                        return rsConnectCities.getInt(1);
//                    }
//                } catch (SQLException ex) {
//                    Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            
//        } catch (SQLException ex) {
//            Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
//        return -1;
//    }
    
    @Override
    public int connectCities(int cityId1, int cityId2, int distance) {
        String query = "INSERT INTO dbo.Line(City1, City2, Distance) VALUES (?, ?, ?)";
        String checkQuery = "SELECT IDLine FROM dbo.Line WHERE (City1 = ? AND City2 = ?) OR (City1 = ? AND City2 = ?)";

        try (PreparedStatement psCheckLine = conn.prepareStatement(checkQuery);
             PreparedStatement psConnectCities = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            psCheckLine.setInt(1, cityId1);
            psCheckLine.setInt(2, cityId2);
            psCheckLine.setInt(3, cityId2);
            psCheckLine.setInt(4, cityId1);

            try (ResultSet rsLine = psCheckLine.executeQuery()) {
                if (!rsLine.next()) {
                    psConnectCities.setInt(1, cityId1);
                    psConnectCities.setInt(2, cityId2);
                    psConnectCities.setInt(3, distance);

                    int affRows = psConnectCities.executeUpdate();
                    if (affRows == 1) {
                        try (ResultSet rsGenKeys = psConnectCities.getGeneratedKeys()) {
                            if (rsGenKeys.next()) {
                                return rsGenKeys.getInt(1);
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }
    
    @Override
    public List<Integer> getConnectedCities(int cityId) {
        String query = "select City1, City2 from Line where City1 = ? or City2 = ?";
        ArrayList<Integer> connectedCities = new ArrayList<>();

        try (PreparedStatement psConnectedCities = conn.prepareStatement(query)) {
            psConnectedCities.setInt(1, cityId);
            psConnectedCities.setInt(2, cityId);

            try (ResultSet rsConnectedCities = psConnectedCities.executeQuery()) {
                while (rsConnectedCities.next()) {
                    int city1 = rsConnectedCities.getInt(1);
                    int city2 = rsConnectedCities.getInt(2);

                    int connectedCity = (city1 == cityId) ? city2 : city1;
                    connectedCities.add(connectedCity);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return connectedCities;
    }


    @Override
    public List<Integer> getShops(int cityId) {
        
        try (PreparedStatement psShops = conn.prepareStatement("select IDShop\n" +
            "from dbo.Shop\n" +
            "where IDCity = ?");) {
            
            psShops.setInt(1, cityId);
            try (ResultSet rsShops = psShops.executeQuery();) {
                ArrayList<Integer> shops = new ArrayList<>();
                while (rsShops.next()) {
                    shops.add(rsShops.getInt(1));
                }
                return shops;
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_CityOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
}
