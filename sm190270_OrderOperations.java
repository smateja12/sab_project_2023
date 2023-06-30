/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.OrderOperations;

/**
 *
 * @author Mateja
 */
public class sm190270_OrderOperations implements OrderOperations {

    private Connection conn = sm190270_DB.getInstance().getConnection();
    
    @Override
    public int addArticle(int orderId, int articleId, int count) {
        
        try (PreparedStatement psArticlesInOrder = conn.prepareStatement("select count(*)\n" +
                "from OrderItems \n" +
                "where IDOrder = ? and IDArticle = ?");
            PreparedStatement psAddArticlesToOrder = conn.prepareStatement("insert into OrderItems(OrderQuantity, IDOrder, IDArticle)\n" +
                "values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement psIncreaseArticlesInOrder = conn.prepareStatement("update OrderItems\n" +
                "set OrderQuantity = OrderQuantity + ?\n" +
                "output inserted.IDItem\n" +
                "where IDOrder = ? and IDArticle = ?");
                ) {
            // proveriti da li ima artikala u narudzbini
            
            psArticlesInOrder.setInt(1, orderId);
            psArticlesInOrder.setInt(2, articleId);
            try (ResultSet rsArticlesInOrder = psArticlesInOrder.executeQuery()) {
                if (rsArticlesInOrder.next()) {
                    int numOfArticlesInOrder = rsArticlesInOrder.getInt(1);
                    
                    if (numOfArticlesInOrder > 0) {
                        // povecaj kolicinu
                        psIncreaseArticlesInOrder.setInt(1, count);
                        psIncreaseArticlesInOrder.setInt(2, orderId);
                        psIncreaseArticlesInOrder.setInt(3, articleId);
                        int affRows = psIncreaseArticlesInOrder.executeUpdate();
                        if (affRows == 0) {
                            return -1;
                        }else {
                            try (ResultSet rsGenKeys = psIncreaseArticlesInOrder.getResultSet()) {
                                if (rsGenKeys.next()) {
                                    return rsGenKeys.getInt("IDItem");
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                return -1;
                            }
                        }   
                    } else {
                        // dodaj kolicinu
                        psAddArticlesToOrder.setInt(1, count);
                        psAddArticlesToOrder.setInt(2, orderId);
                        psAddArticlesToOrder.setInt(3, articleId);
                        int affRows = psAddArticlesToOrder.executeUpdate();
                        if (affRows == 0) {
                            return -1;
                        }else {
                            try (ResultSet rsGenKeys = psIncreaseArticlesInOrder.getResultSet()) {
                                if (rsGenKeys.next()) {
                                    return rsGenKeys.getInt(1);
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                                return -1;
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                return -1;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public int removeArticle(int orderId, int articleId) {
        
        try (PreparedStatement psRemoveArticle = conn.prepareStatement("delete from OrderItems\n" +
                "where IDOrder = ? and IDArticle = ?");) {
            
            psRemoveArticle.setInt(1, orderId);
            psRemoveArticle.setInt(2, articleId);
            int affRows = psRemoveArticle.executeUpdate();
            return (affRows == 1) ? 1 : -1;
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public List<Integer> getItems(int orderId) {
        
        try (PreparedStatement psGetItems = conn.prepareStatement("select IDArticle\n" +
            "from OrderItems\n" +
            "where IDOrder = ?");) {
            
            psGetItems.setInt(1, orderId);
            try (ResultSet rsGetItems = psGetItems.executeQuery();) {
                ArrayList<Integer> items = new ArrayList<>();
                while (rsGetItems.next()) {
                    items.add(rsGetItems.getInt(1));
                }
                return items;
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    private Boolean checkForOrderSystemDiscount() {
        boolean orderSystemDiscount = false;
        try (PreparedStatement psOrderSystemDiscount = conn.prepareStatement("select case \n" +
            "when count(*) <= 0 then 0\n" +
            "else 1 end as orderSystemDiscountFlag\n" +
            "from [Order] O\n" +
            "where O.Price > 10000 and O.SentTime > ?");) {
            
            // u poslednjih 30 dana se proverava da li smo ostvarili kupovinu vecu od 10000
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTime(sm190270_GeneralOperations.currentTime.getTime());
            calendar.add(Calendar.DATE, -30);
            Date last30Days = new Date(calendar.getTimeInMillis());

            psOrderSystemDiscount.setDate(1, last30Days);

            try (ResultSet rsOrderSystemDiscount = psOrderSystemDiscount.executeQuery()) {
                if (rsOrderSystemDiscount.next()) {
                    orderSystemDiscount = (rsOrderSystemDiscount.getInt("orderSystemDiscountFlag") == 1);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            return null;
        }
        
        return orderSystemDiscount;
    }
    
    public boolean checkBuyerCredit(int orderId, BigDecimal finalPrice) {
        try (PreparedStatement psCheckBuyer = conn.prepareStatement("select B.Credit\n" +
            "from Buyer B join [Order] O ON (B.IDBuyer = O.IDBuyer)\n" +
            "where O.IDOrder = ?");) {

            psCheckBuyer.setInt(1, orderId);
            try (ResultSet rsCheckBuyer = psCheckBuyer.executeQuery()) {
                return rsCheckBuyer.next() && rsCheckBuyer.getBigDecimal(1).compareTo(finalPrice) >= 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }
    
    private int createTransaction(int orderId, BigDecimal quantity, Date executionTime) {
        
        try (PreparedStatement psInsertTransaction = conn.prepareStatement("insert into [Transaction](IDOrder, Quantity, ExecutionTime)\n" +
            "values (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);) {
            
            psInsertTransaction.setInt(1, orderId);
            psInsertTransaction.setBigDecimal(2, quantity);
            psInsertTransaction.setDate(3, executionTime);
            int affRows = psInsertTransaction.executeUpdate();
            if (affRows == 0) {
                return -1;
            }else {
                try (ResultSet rsGenKeys = psInsertTransaction.getGeneratedKeys()) {
                    if (rsGenKeys.next()) {
                        return rsGenKeys.getInt(1);
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }
    
    private boolean createOrderTransaction(int transactionId) {
        
        try (PreparedStatement psInsertOrderTransaction = conn.prepareStatement("insert into OrderTransaction(IDTransaction)\n" +
            "values (?)");) {
            
            psInsertOrderTransaction.setInt(1, transactionId);
            
            int affRows = psInsertOrderTransaction.executeUpdate();
            return (affRows == 0) ? false : true;
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public int getBuyerCity(int orderId) {
        try (PreparedStatement psBuyerCity = conn.prepareStatement("SELECT B.IDCity\n" +
            "FROM [Order] O join Buyer B ON (O.IDBuyer = B.idBuyer) \n" +
            "WHERE O.IDOrder = ?");) {
            psBuyerCity.setInt(1, orderId);

            try (ResultSet rsBuyerCity = psBuyerCity.executeQuery()) {
                if (rsBuyerCity.next()) {
                    return rsBuyerCity.getInt(1);
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    private Map<String, Object> getOrderPathAndClosestCity(int buyerCity) {
        Map<String, Object> result = new HashMap<>();
        int orderClosestCity = -1;
        String orderPath = "";
        
        try (PreparedStatement psOrderPathAndClosestCity = conn.prepareStatement("select SP.StartCity, SP.CurrentRoute\n" +
            "from ShortestPaths SP\n" +
            "where SP.DistanceBetween = (\n" +
            "	select min(SP2.DistanceBetween)\n" +
            "	from ShortestPaths SP2\n" +
            "	where SP2.FinalCity = ? \n" +
            "	and SP2.StartCity IN (select distinct IDCity from Shop)\n" +
            ") and SP.StartCity IN (select distinct IDCity from Shop)\n" +
            "and SP.FinalCity = ?");) {
            
            psOrderPathAndClosestCity.setInt(1, buyerCity);
            psOrderPathAndClosestCity.setInt(2, buyerCity);
            try (ResultSet rsOrderPathAndClosestCity = psOrderPathAndClosestCity.executeQuery();) {
                if (rsOrderPathAndClosestCity.next()) {
                    orderClosestCity = rsOrderPathAndClosestCity.getInt(1);
                    orderPath = rsOrderPathAndClosestCity.getString(2);
                    
                    result.put("orderClosestCity", orderClosestCity);
                    result.put("orderPath", orderPath);
                    return result;
                }else {
                    return null;
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    @Override
    public int completeOrder(int orderId) {
        
        // proveriti kolicinu artikala u order-u
        try (PreparedStatement psCheckOrderCompleteness = conn.prepareStatement("select 1 \n" +
            "from OrderItems OT\n" +
            "where exists (\n" +
            "	select 1 \n" +
            "	from Article A\n" +
            "	where A.IDArticle = OT.IDArticle\n" +
            "	and A.ShopQuantity < OT.OrderQuantity\n" +
            ") and OT.IDOrder = ?");) {
            
            psCheckOrderCompleteness.setInt(1, orderId);
            try (ResultSet rsCheckOrderCompleteness = psCheckOrderCompleteness.executeQuery();) {
                if (rsCheckOrderCompleteness.next()) {
                    return -1;
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
                return -1;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        
        Boolean orderSystemDiscountFlag = checkForOrderSystemDiscount();
        if (orderSystemDiscountFlag == null) {
            return -1;
        }
        
        BigDecimal finalPrice = this.getFinalPrice(orderId);
        
        boolean flagBuyerCredit = checkBuyerCredit(orderId, finalPrice);
        if (flagBuyerCredit) {
            return -1;
        }
        
        Date execTime = new Date(sm190270_GeneralOperations.currentTime.getTimeInMillis());
        int IDTransaction = createTransaction(orderId, finalPrice, execTime);
        if (IDTransaction == -1) {
            return -1;
        }
        
        boolean orderTransactionCreated = createOrderTransaction(IDTransaction);
        if (!orderTransactionCreated) {
            return -1;
        }
        
        int buyerCity = getBuyerCity(orderId);
        if (buyerCity == -1) {
            return -1;
        }
        
        Map<String, Object> result = getOrderPathAndClosestCity(buyerCity);
        if (result == null) {
            return -1;
        }
        
        int orderClosestCity = (int)result.get("orderClosestCity");
        String orderPath = (String)result.get("orderPath");    
        
        // TODO update OrdersItems table
        
        // TODO update Article table
        
        // TODO update Order table
        
        return 1;
    }

    @Override
    public BigDecimal getFinalPrice(int orderId) {
        
        Date currentTime = new Date(sm190270_GeneralOperations.currentTime.getTimeInMillis());
        
        String query = "{call [dbo].[SP_FINAL_PRICE](?, ?, ?)}";
        
        try (CallableStatement csFinalPrice = conn.prepareCall(query);) {
            csFinalPrice.setInt(1, orderId);
            csFinalPrice.setDate(2, currentTime);
            csFinalPrice.registerOutParameter(3, Types.DECIMAL);
            csFinalPrice.execute();
            
            return csFinalPrice.getBigDecimal(3).setScale(3);
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getDiscountSum(int orderId) {
        
        try (PreparedStatement psDiscountSum = conn.prepareStatement("select coalesce(\n" +
            "	sum((OI.DiscountPercentage * OI.OrderQuantity * OI.Price) / 100)\n" +
            ", 0)\n" +
            "from OrderItems OI\n" +
            "where IDOrder = ?");) {
            
            psDiscountSum.setInt(1, orderId);
            try (ResultSet rsDiscountSum = psDiscountSum.executeQuery();) {
                if (rsDiscountSum.next()) {
                    return rsDiscountSum.getBigDecimal(1).setScale(3);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public String getState(int orderId) {
        
        try (PreparedStatement psOrderState = conn.prepareStatement("select Status \n" +
            "from [Order]\n" +
            "where IDOrder = ?");) {
            
            psOrderState.setInt(1, orderId);
            try (ResultSet rsOrderState = psOrderState.executeQuery();) {
                if (rsOrderState.next()) {
                    return rsOrderState.getString(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public Calendar getSentTime(int orderId) {
        
        try (PreparedStatement psSentTime = conn.prepareStatement("select SentTime\n" +
            "from [Order]\n" +
            "where IDOrder = ?");) {
            
            psSentTime.setInt(1, orderId);
            try (ResultSet rsSentTime = psSentTime.executeQuery();) {
                if (rsSentTime.next()) {
                    Date sentTime = rsSentTime.getDate(1);
                    if (sentTime != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.setTime(sentTime);
                        return calendar;
                    }else {
                        return null;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public Calendar getRecievedTime(int orderId) {
        try (PreparedStatement psReceivedTime = conn.prepareStatement("select ReceivedTime\n" +
            "from [Order]\n" +
            "where IDOrder = ?");) {
            
            psReceivedTime.setInt(1, orderId);
            try (ResultSet rsReceivedTime = psReceivedTime.executeQuery();) {
                if (rsReceivedTime.next()) {
                    Date receivedTime = rsReceivedTime.getDate(1);
                    if (receivedTime != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.setTime(receivedTime);
                        return calendar;
                    }else {
                        return null;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public int getBuyer(int orderId) {
        
        try (PreparedStatement psBuyer = conn.prepareStatement("select IDBuyer\n" +
            "from [Order]\n" +
            "where IDOrder = ?");) {
            
            psBuyer.setInt(1, orderId);
            try (ResultSet rsBuyer = psBuyer.executeQuery();) {
                if (rsBuyer.next()) {
                    return rsBuyer.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }

    @Override
    public int getLocation(int orderId) {
        try (PreparedStatement psLocation = conn.prepareStatement("select IDCity\n" +
            "from [Order]\n" +
            "where IDOrder = ?");) {
            
            psLocation.setInt(1, orderId);
            try (ResultSet rsLocation = psLocation.executeQuery();) {
                if (rsLocation.next()) {
                    return rsLocation.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_OrderOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
    }
    
}
