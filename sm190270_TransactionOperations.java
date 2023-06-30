/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.TransactionOperations;

/**
 *
 * @author Mateja
 */
public class sm190270_TransactionOperations implements TransactionOperations {
    
    private Connection conn = sm190270_DB.getInstance().getConnection();
    
    @Override
    public BigDecimal getBuyerTransactionsAmmount(int orderId) {
        
        try (PreparedStatement psBuyerTransactionsAmmount = conn.prepareStatement("select coalesce(\n" +
                "	sum(T.Quantity)\n" +
                ", 0)\n" +
                "from [Transaction] T join OrderTransaction OT on (T.IDTransaction = OT.IDTransaction)\n" +
                "join [Order] O on (T.IDOrder = O.IDOrder)\n" +
                "where O.IDBuyer = ?");) {
            
            psBuyerTransactionsAmmount.setInt(1, orderId);
            try (ResultSet rsBuyerTransactionsAmmount = psBuyerTransactionsAmmount.executeQuery();) {
                if (rsBuyerTransactionsAmmount.next()) {
                    return rsBuyerTransactionsAmmount.getBigDecimal(1).setScale(3);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getShopTransactionsAmmount(int shopId) {
        
        try (PreparedStatement psShopTransactionsAmmount = conn.prepareStatement("select coalesce(\n" +
            "	sum(T.Quantity)\n" +
            ", 0)\n" +
            "from [Transaction] T join ShopTransaction ST on (T.IDTransaction = ST.IDTransaction)\n" +
            "where ST.IDShop = ?");) {
            
            psShopTransactionsAmmount.setInt(1, shopId);
            try (ResultSet rsShopTransactionsAmmount = psShopTransactionsAmmount.executeQuery();) {
                if (rsShopTransactionsAmmount.next()) {
                    return rsShopTransactionsAmmount.getBigDecimal(1).setScale(3);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public List<Integer> getTransationsForBuyer(int buyerId) {
        
        try (PreparedStatement psTransationsForBuyer = conn.prepareStatement("select T.IDTransaction\n" +
            "from [Transaction] T join OrderTransaction OT on (T.IDTransaction = OT.IDTransaction)\n" +
            "join [Order] O on (O.IDOrder = T.IDOrder)\n" +
            "where O.IDBuyer = ?");) {
            
            psTransationsForBuyer.setInt(1, buyerId);
            try (ResultSet rsTransationsForBuyer = psTransationsForBuyer.executeQuery();) {
                
                ArrayList<Integer> transactions = new ArrayList<>();
                while (rsTransationsForBuyer.next()) {
                    transactions.add(rsTransationsForBuyer.getInt(1));
                }
                return transactions;
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public int getTransactionForBuyersOrder(int orderId) {
        
        String query = "SELECT IDTransaction FROM [Transaction] WHERE IDOrder = ?";

        try (PreparedStatement psTransactionForBuyersOrder = conn.prepareStatement(query)) {

            psTransactionForBuyersOrder.setInt(1, orderId);
            
            try (ResultSet rs = psTransactionForBuyersOrder.executeQuery();) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public int getTransactionForShopAndOrder(int orderId, int shopId) {
        
        String query = "select T.IDTransaction\n" +
            "from [Transaction] T join ShopTransaction ST on (T.IDTransaction = ST.IDTransaction)\n" +
            "join [Order] O on (O.IDOrder = T.IDOrder)\n" +
            "where ST.IDShop = ? and O.IDOrder = ?";

        try (PreparedStatement psTransactionForBuyersOrder = conn.prepareStatement(query)) {

            psTransactionForBuyersOrder.setInt(1, shopId);
            psTransactionForBuyersOrder.setInt(2, orderId);

            try (ResultSet rsTransactionForBuyersOrder = psTransactionForBuyersOrder.executeQuery();) {
                if (rsTransactionForBuyersOrder.next()) {
                    return rsTransactionForBuyersOrder.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public List<Integer> getTransationsForShop(int shopId) {
        
        try (PreparedStatement psTransationsForShop = conn.prepareStatement("select T.IDTransaction\n" +
            "from [Transaction] T join ShopTransaction ST on (T.IDTransaction = ST.IDTransaction)\n" +
            "where ST.IDShop = ?");) {
            
            psTransationsForShop.setInt(1, shopId);
            try (ResultSet rsTransationsForShop = psTransationsForShop.executeQuery();) {
                
                ArrayList<Integer> transactions = new ArrayList<>();
                while (rsTransationsForShop.next()) {
                    transactions.add(rsTransationsForShop.getInt(1));
                }
                return transactions;
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
        
    }

    @Override
    public Calendar getTimeOfExecution(int transactionId) {
        
        try (PreparedStatement psTimeOfExecution = conn.prepareStatement("select ExecutionTime\n" +
                "from [Transaction]\n" +
                "where IDTransaction = ?");) {
            
            try (ResultSet rsTimeOfExecution = psTimeOfExecution.executeQuery();) {
                if (rsTimeOfExecution.next()) {
                    Date execTime = rsTimeOfExecution.getDate(1);
                    if (execTime == null) {
                        return null;
                    }else {
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.setTime(execTime);
                        return calendar;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getAmmountThatBuyerPayedForOrder(int orderId) {
        
        try (PreparedStatement psBuyerPayedForOrder = conn.prepareStatement("select Price\n" +
            "from [Order]\n" +
            "where IDOrder = ?");) {
            
            psBuyerPayedForOrder.setInt(1, orderId);
            try (ResultSet rsBuyerPayedForOrder = psBuyerPayedForOrder.executeQuery();) {
                if (rsBuyerPayedForOrder.next()) {
                    return rsBuyerPayedForOrder.getBigDecimal(1).setScale(3);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getAmmountThatShopRecievedForOrder(int orderId, int shopId) {
        
        try (PreparedStatement psShopRecievedForOrder = conn.prepareStatement("select T.Quantity\n" +
            "from [Transaction] T join ShopTransaction ST ON (T.IDTransaction = ST.IDTransaction)\n" +
            "join [Order] O ON (O.IDOrder = T.IDOrder)\n" +
            "where O.IDOrder = ? and ST.IDShop = ?");) {
            
            psShopRecievedForOrder.setInt(1, orderId);
            psShopRecievedForOrder.setInt(2, shopId);
            try (ResultSet rsShopRecievedForOrder = psShopRecievedForOrder.executeQuery();) {
                if (rsShopRecievedForOrder.next()) {
                    return rsShopRecievedForOrder.getBigDecimal(1).setScale(3);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getTransactionAmount(int transactionId) {
        
        try (PreparedStatement psTransactionAmount = conn.prepareStatement("select Quantity\n" +
            "from [Transaction] \n" +
            "where IDTransaction = ?");) {
            
            psTransactionAmount.setInt(1, transactionId);
            try (ResultSet rsTransactionAmount = psTransactionAmount.executeQuery()) {
                if (rsTransactionAmount.next()) {
                    return rsTransactionAmount.getBigDecimal(1).setScale(3);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public BigDecimal getSystemProfit() {
        
        try (PreparedStatement psSystemProfit = conn.prepareStatement("select coalesce((\n" +
            "	sum(\n" +
            "		case when O.Status = 'arrived' \n" +
            "		then O.Price * case when O.DiscountPercentageSystem = 1\n" +
            "			then 0.03 \n" +
            "			else 0.05\n" +
            "			end\n" +
            "		else 0\n" +
            "		end\n" +
            "	)\n" +
            "), 0)\n" +
            "from [Transaction] T join OrderTransaction OT on (T.IDTransaction = OT.IDTransaction)\n" +
            "join [Order] O on (O.IDOrder = T.IDOrder)");) {
            
            try (ResultSet rsSystemProfit = psSystemProfit.executeQuery();) {
                if (rsSystemProfit.next()) {
                    return rsSystemProfit.getBigDecimal(1).setScale(3);
                }
            } catch (SQLException ex) {
                Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(sm190270_TransactionOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
}
