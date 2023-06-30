/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.util.Calendar;
import rs.etf.sab.operations.GeneralOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mateja
 */
public class sm190270_GeneralOperations implements GeneralOperations {
    
    private Connection conn = sm190270_DB.getInstance().getConnection();
    public static Calendar currentTime;
    
    public sm190270_GeneralOperations() {
        currentTime = null;
    }
    
    @Override
    public void setInitialTime(Calendar time) {
        currentTime = Calendar.getInstance();
        currentTime.clear();
        currentTime.setTimeInMillis(time.getTimeInMillis());
    }

    @Override
    public Calendar time(int days) {
        currentTime.add(Calendar.DATE, days);
        
        try (PreparedStatement psSentOrders = conn.prepareStatement("select distinct IDOrder\n" +
            "from [Order]\n" +
            "where Status = ?");) {
            
            psSentOrders.setString(1, "sent");
            
            ArrayList<Integer> sentOrders = new ArrayList<>();
            try (ResultSet rsSentOrders = psSentOrders.executeQuery();) {
                while (rsSentOrders.next()) {
                    sentOrders.add(rsSentOrders.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            
            for (int i = 0; i < sentOrders.size(); i++) {
                // dohvatiti za svaki order max koliko dana je putovala narudzbina
                int IDOrder = sentOrders.get(i);
                
                try (PreparedStatement psItems = conn.prepareStatement("select max(OrderTravels)\n" +
                    "from OrderItems\n" +
                    "where IDOrder = ?");) {
                    
                    psItems.setInt(1, IDOrder);
                    try (ResultSet rsItems = psItems.executeQuery();) {
                        if (rsItems.next()) {
                            int orderTravelsMaxDays = rsItems.getInt(1);
                            if (orderTravelsMaxDays > days) {
                                updateOrderItems(IDOrder, days, days);
                                return currentTime;
                            }else {
                                int orderTravelsDaysToFinish = days - orderTravelsMaxDays;
                                updateOrderItems(IDOrder, orderTravelsMaxDays, 0);
                                updateOrder(IDOrder, days - orderTravelsDaysToFinish);
                            }
                        }else {
                            return null;
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    } 
                    
                } catch (SQLException ex) {
                    Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }    
                
                String orderTravelRoute = null;
                int orderTravelsDays = -1;
                
                do {
                    try (PreparedStatement psOrderTravelsDays = conn.prepareStatement("select O.Route, O.OrderTravels\n" +
                        "from [Order] O\n" +
                        "where O.IDOrder = ?");) {

                        psOrderTravelsDays.setInt(1, IDOrder);
                        try (ResultSet rsOrderTravelsDays = psOrderTravelsDays.executeQuery();) {
                            if (rsOrderTravelsDays.next()) {
                                orderTravelRoute = rsOrderTravelsDays.getString(1);
                                orderTravelsDays = rsOrderTravelsDays.getInt(2);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if (orderTravelsDays > 0) break;
                       
                    String[] orderTravelRoutesArr = orderTravelRoute.split(String.valueOf("\\|"));
                    int city1 = Integer.parseInt(orderTravelRoutesArr[0]);
                    if (orderTravelRoutesArr.length < 2) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.setTime(sm190270_GeneralOperations.currentTime.getTime());
                        calendar.add(Calendar.DATE, orderTravelsDays);
                        
                        // narudzbina je pristigla
                        try (PreparedStatement psOrderArrived = conn.prepareStatement("update [Order]\n" +
                            "set Status = ?, IDCity = ?,\n" +
                            "ReceivedTime = ?, OrderTravels = ?\n" +
                            "where IDOrder = ?");) {
                        
                            psOrderArrived.setString(1, "arrived");
                            psOrderArrived.setInt(2, city1);
                            Date receivedTime = new Date(calendar.getTimeInMillis());
                            psOrderArrived.setDate(3, receivedTime);
                            psOrderArrived.setInt(4, 0);
                            psOrderArrived.setInt(5, IDOrder);
                            psOrderArrived.executeUpdate();
                            
                        } catch (SQLException ex) {
                            Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        }
                        
                        return calendar;
                    }else {
                        
                        String[] partsOfOrderTravelRoute = orderTravelRoute.split("\\|", 2);
                        String orderTravelRouteNew = partsOfOrderTravelRoute.length > 1 ? partsOfOrderTravelRoute[1] : "";
                        int city2 = Integer.parseInt(orderTravelRoutesArr[1]);
                        
                        try (PreparedStatement psUpdateOrder2 = conn.prepareStatement("update [Order]\n" +
                            "set OrderTravels = OrderTravels + (\n" +
                            "	select Distance\n" +
                            "	from Line\n" +
                            "	where City1 = ? and City2 = ?\n" +
                            "), IDCity = ?, Route = ?\n" +
                            "where IDOrder = ?");) {
                        
                            psUpdateOrder2.setInt(1, city1);
                            psUpdateOrder2.setInt(2, city2);
                            psUpdateOrder2.setInt(3, city1);
                            psUpdateOrder2.setString(4, orderTravelRouteNew);
                            psUpdateOrder2.setInt(5, IDOrder);
                            psUpdateOrder2.executeUpdate();
                            
                        } catch (SQLException ex) {
                            Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
                            return null;
                        }
                        
                    }
                    
                } while (true);
                
            }   
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return currentTime;
    }

    private void updateOrderItems(int orderID, int orderTravelsDays, int subtractOrderTravelsDays) {
    
        try (PreparedStatement psUpdateOrderItemsTravelsDays = conn.prepareStatement("update OrderItems\n" +
            "set OrderTravels = GREATEST(? - ?, 0)\n" +
            "where IDOrder = ?");) {
            
            psUpdateOrderItemsTravelsDays.setInt(1, orderTravelsDays);
            psUpdateOrderItemsTravelsDays.setInt(2, subtractOrderTravelsDays);
            psUpdateOrderItemsTravelsDays.setInt(3, orderID);
            psUpdateOrderItemsTravelsDays.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void updateOrder(int orderID, int orderTravelsDays) {
        try (PreparedStatement psUpdateOrderTravelsDays = conn.prepareStatement("update [Order]\n" +
            "set OrderTravels = ?\n" +
            "where IDOrder = ?");) {

            psUpdateOrderTravelsDays.setInt(1, orderTravelsDays);
            psUpdateOrderTravelsDays.setInt(2, orderID);
            psUpdateOrderTravelsDays.executeUpdate();

            } catch (SQLException ex) {
                Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public Calendar getCurrentTime() {
        return currentTime;
    }

    @Override
    public void eraseAll() {
        try (PreparedStatement psEraseAll = conn.prepareStatement("exec sp_MSForEachTable 'alter table ? nocheck constraint all'\n" +
            "exec sp_MSforeachtable 'delete from ?'\n" +
            "exec sp_MSforeachtable '\n" +
            "    if exists (\n" +
            "        select 1\n" +
            "        from sys.columns\n" +
            "        where object_id = OBJECT_ID(''?'') and is_identity = 1\n" +
            "    )\n" +
            "    begin\n" +
            "        DBCC CHECKIDENT(''?'', RESEED, 0)\n" +
            "    end\n" +
            "'");) {
            
            psEraseAll.executeUpdate();
        
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
