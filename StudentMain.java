/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package rs.etf.sab.student;

/**
 *
 * @author Mateja
 */
import rs.etf.sab.operations.*;
import org.junit.Test;
import rs.etf.sab.student.*;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;

import java.util.Calendar;
import java.util.List;

public class StudentMain {
    public static void main(String[] args) {

        ArticleOperations articleOperations = new sm190270_ArticleOperations(); // Change this for your implementation (points will be negative if interfaces are not implemented).
        BuyerOperations buyerOperations = new sm190270_BuyerOperations();
        CityOperations cityOperations = new sm190270_CityOperations();
        GeneralOperations generalOperations = new sm190270_GeneralOperations();
        OrderOperations orderOperations = null;
        ShopOperations shopOperations = new sm190270_ShopOperations();
        TransactionOperations transactionOperations = null;

        TestHandler.createInstance(
                articleOperations,
                buyerOperations,
                cityOperations,
                generalOperations,
                orderOperations,
                shopOperations,
                transactionOperations
        );

        TestRunner.runTests();
    }

}

