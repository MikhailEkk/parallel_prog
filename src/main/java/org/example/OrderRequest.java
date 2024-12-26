package org.example;

// Класс заявки на покупку
public class OrderRequest {
    private final String productName;
    private int quantity;
    private final Client client;

    public OrderRequest(String productName, int quantity, Client userName) {
        this.productName = productName;
        this.quantity = quantity;
        this.client = userName;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public Client getClient() {
        return client;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString(){
        return String.format("Product: %s | Quantity: %d | Client: %s | ", productName, quantity, client.getName());
    }
}