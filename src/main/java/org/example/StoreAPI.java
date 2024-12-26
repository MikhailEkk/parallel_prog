package org.example;

import java.util.Map;

public interface StoreAPI {

    //Метод обработки заказа
    public String processOrder(OrderRequest order);

    // Получить список всех доступных товаров с их количеством и ценами
    public Map<String, String> getProductList();

    // Получить текущий баланс магазина
    public double getBalance();

    // Получить информацию о конкретном товаре
    public String getProductInfo(String productName);
}
