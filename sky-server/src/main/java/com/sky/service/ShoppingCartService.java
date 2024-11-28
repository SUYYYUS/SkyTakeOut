package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    void cleanCart();


    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showCart();

    void delete(ShoppingCartDTO shoppingCartDTO);

}
