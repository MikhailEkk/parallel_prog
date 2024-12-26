package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Client implements Callable<String> {
    private String name;
    private final Map<String, Integer> purchasedProducts;
    private double blockedAmount;
    private double balance;

    public Client(String name, double initialBalance){
        this.name = name;
        this.purchasedProducts = new HashMap<>();
        this.balance = initialBalance;
        this.blockedAmount = 0;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getPurchasedProducts() {
        return purchasedProducts;
    }

    public double getBalance() {
        return balance;
    }

    public double getBlockedAmount() {
        return blockedAmount;
    }

    // Метод для пополнения баланса
    public void deposit(double amount) {
        this.balance += amount;
    }

    public boolean checkAmountFunds(double amount){
        return balance >= amount;
    }

    // Метод для снятия средств с баланса
    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    // Метод для блокировки средств
    public boolean blockAmount(double amount) {
        if (withdraw(amount)) {
            blockedAmount += amount;
            return true;
        } else {
            return false;
        }

    }

    // Метод для разблокировки средств
    public void unblockAmount(double amount) {
        if (blockedAmount >= amount) {
            blockedAmount -= amount;
            deposit(amount);
        }
    }

    // Метод для добавления купленных товаров
    public void addPurchasedProduct(String product, int quantity) {
        purchasedProducts.put(product, purchasedProducts.getOrDefault(product, 0) + quantity);
    }

    // Метод для создания и обработки заказа
//    public String placeOrder(OrderRequest orderRequest) {
//        return store.processOrder(orderRequest);
//    }

    @Override
    public String call() {
        return "Client " + name + " is ready for orders.";
    }
}
