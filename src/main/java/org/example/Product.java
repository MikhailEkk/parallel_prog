package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Класс товара
public class Product {
    private final String name;
    private final double price;
    private int quantity;
    private final Lock lock = new ReentrantLock();

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean reserveProduct(int amount) {
        lock.lock();
        try {
            if (quantity >= amount) {
                quantity -= amount;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void restockProduct(int amount) {
        lock.lock();
        try {
            quantity += amount;
        } finally {
            lock.unlock();
        }
    }

    public int getQuantity() {
        lock.lock();
        try {
            return quantity;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return String.format("Product: %s | Price: %.2f | Quantity: %d", name, price, quantity);
    }
}
