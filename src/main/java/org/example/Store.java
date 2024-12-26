package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

// Класс магазина
public class Store implements StoreAPI {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final List<OrderRequest> pendingOrders = new ArrayList<>();
    private double balance;
    private final ReentrantLock lock = new ReentrantLock();

    public Store(double initialBalance) {
        this.balance = initialBalance;
    }

    public void addProduct(Product product) {
        products.put(product.getName(), product);
    }

    public Product getProduct(String productName){
        return products.get(productName);
    }

    public String getFormattedProductList() {
        if (products.isEmpty()) {
            return "No products available.";
        }

        return products.values().stream()
                .map(Product::toString)
                .collect(Collectors.joining("\n"));
    }

    public List<OrderRequest> getPendingOrders() {
        // Возвращаем копию списка, чтобы избежать конкурентных модификаций
        return new ArrayList<>(pendingOrders);

    }

    public void supplyNewProducts(String name, double price, int quantity){
        products.putIfAbsent(name, new Product(name, price, quantity));
    }

    public String supplyProducts(String name, int quantity) {
            // Проверяем, есть ли такой товар в магазине
            if (!products.containsKey(name)) {
               // System.out.println("Product " + name + " not found in store inventory.");
                return "Product " + name + " not found in store inventory.";
            }

            Product product = products.get(name);

            // Обработка забронированных товаров
            Iterator<OrderRequest> iterator = pendingOrders.iterator();
            while (iterator.hasNext() && quantity > 0) {
                OrderRequest pendingOrder = iterator.next();

                // Проверяем, является ли заказ на тот же товар
                if (pendingOrder.getProductName().equals(name)) {
                    Client client = pendingOrder.getClient();

                    int orderQuantity = pendingOrder.getQuantity();

                    // Если поставленный товар может покрыть весь заказ
                    if (quantity >= orderQuantity) {
                        // Списываем товар и деньги
                        quantity -= orderQuantity;
                        double totalCost = product.getPrice() * orderQuantity;

                        if (client.getBlockedAmount() >= totalCost) {
                            client.unblockAmount(totalCost);
                            client.withdraw(totalCost);
                            client.addPurchasedProduct(name, orderQuantity);
                            balance += totalCost;

                            // Удаляем выполненный заказ из списка
                            iterator.remove();
                            return "Order fulfilled for client: " + client.getName() + " with product: " + name;
                           // System.out.println("Order fulfilled for client: " + client.getName() + " with product: " + name);
                        }
                    } else {
                        // Если поставленного товара не хватает, выполняем частичный заказ
                        int fulfillableQuantity = quantity;
                        quantity = 0; // Товар закончился

                        double partialCost = product.getPrice() * fulfillableQuantity;

                        if (client.getBlockedAmount() >= partialCost) {
                            client.unblockAmount(partialCost);
                            client.withdraw(partialCost);
                            client.addPurchasedProduct(name, fulfillableQuantity);
                            balance += partialCost;

                            // Обновляем заказ
                            pendingOrder.setQuantity(orderQuantity - fulfillableQuantity);
                            return "Partial order fulfilled for client: " + client.getName() + " with product: " + name;
                          //  System.out.println("Partial order fulfilled for client: " + client.getName() + " with product: " + name);
                        }
                    }
                }
            }

            // Если остались товары после выполнения всех заказов
            if (quantity > 0) {
                product.restockProduct(quantity);
                return "Restocked " + quantity + " units of product: " + name;
                //System.out.println("Restocked " + quantity + " units of product: " + name);
            }
            return "";
    }

    public Map<String, Integer> getProductInventory() {
        Map<String, Integer> inventory = new HashMap<>();
        for (Map.Entry<String, Product> entry : products.entrySet()) {
            inventory.put(entry.getKey(), entry.getValue().getQuantity());
        }
        return inventory;
    }

    @Override
    public String processOrder(OrderRequest order) {
            Product product = products.get(order.getProductName());
            double totalCost = product.getPrice() * order.getQuantity();
            Client client = order.getClient();
            if (client.checkAmountFunds(totalCost)){
                if (product == null) {
                    if (client.blockAmount(totalCost)) {
                        pendingOrders.add(order);
                    }
                    return "Partial order processed. " + totalCost + " blocked for " + client.getName();
                } else if (product.getQuantity() < order.getQuantity()) {
                    int diffQuantity = order.getQuantity() - product.getQuantity();
                    double costPurchasedProduct = product.getPrice() * (order.getQuantity() - diffQuantity);
                    if (client.withdraw(costPurchasedProduct) && product.reserveProduct(order.getQuantity() - diffQuantity)){
                        client.addPurchasedProduct(order.getProductName(), order.getQuantity() - diffQuantity);
                        balance += costPurchasedProduct;
                    }
                    double costBlockProduct = product.getPrice() * diffQuantity;
                    if (client.blockAmount(costBlockProduct)) {
                        pendingOrders.add(new OrderRequest(order.getProductName(), diffQuantity, client));
                    }
                    return "Partial order processed. " + costBlockProduct + " blocked for " + client.getName();
                } else {
                    // double totalCost = product.getPrice() * order.getQuantity();
                    if (client.withdraw(totalCost) && product.reserveProduct(order.getQuantity())) {
                        balance += totalCost;
                        client.addPurchasedProduct(order.getProductName(), order.getQuantity());
                    }
                    return "Order successful for " + client.getName();
                }
            } else return "Insufficient funds for " + client.getName();
    }

    @Override
    public Map<String, String> getProductList() {
            Map<String, String> productList = new HashMap<>();
            for (Map.Entry<String, Product> entry : products.entrySet()) {
                Product product = entry.getValue();
                productList.put(entry.getKey(), "Price: " + product.getPrice() + ", Quantity: " + product.getQuantity());
            }
            return productList;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public String getProductInfo(String productName) {
            Product product = products.get(productName);
            if (product != null) {
                return "Name: " + product.getName() + ", Price: " + product.getPrice() + ", Quantity: " + product.getQuantity();
            }
            return "Product not found.";
    }

}
