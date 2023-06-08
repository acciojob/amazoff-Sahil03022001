package com.driver;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OrderRepository {

    HashMap<String, Order> orderDb = new HashMap<>();
    HashMap<String, DeliveryPartner> deliveryPartnerDb = new HashMap<>();
    HashMap<String, List<Order>> deliveryPartnerOrdersListDb = new HashMap<>();
    HashMap<String, String> orderToWhichDeliveryPartnerDb = new HashMap<>();

    public void addOrder(Order order) {
        orderDb.put(order.getId(), order);
    }

    public void addPartner(String partnerId) {
        DeliveryPartner deliveryPartner = new DeliveryPartner(partnerId);
        deliveryPartnerDb.put(partnerId, deliveryPartner);
    }

    public void addOrderPartnerPair(String orderId, String partnerId) {
        if(orderDb.containsKey(orderId) && deliveryPartnerDb.containsKey(partnerId)) {

            //increasing the number of order for this partner by 1
            DeliveryPartner deliveryPartner = deliveryPartnerDb.get(partnerId);
            deliveryPartner.setNumberOfOrders(deliveryPartner.getNumberOfOrders() + 1);

            //mapping this order to the partner
            orderToWhichDeliveryPartnerDb.put(orderId, partnerId);

            //adding this order to the list of partner order
            if(!deliveryPartnerOrdersListDb.containsKey(partnerId)) {
                deliveryPartnerOrdersListDb.put(partnerId, new ArrayList<>());
            }
            deliveryPartnerOrdersListDb.get(partnerId).add(orderDb.get(orderId));
        }
    }

    public Order getOrderById(String orderId) {
        return orderDb.getOrDefault(orderId, null);
    }

    public DeliveryPartner getPartnerById(String partnerId) {
        return deliveryPartnerDb.getOrDefault(partnerId, null);
    }

    public Integer getOrderCountByPartnerId(String partnerId) {
        return deliveryPartnerOrdersListDb.containsKey(partnerId) ?
                deliveryPartnerOrdersListDb.get(partnerId).size() : 0;
    }

    public List<String> getOrdersByPartnerId(String partnerId) {
        if(!deliveryPartnerOrdersListDb.containsKey(partnerId)) {
            return null;
        }

        List<String> orders = new ArrayList<>();
        for(Order order : deliveryPartnerOrdersListDb.get(partnerId)) {
            orders.add(order.getId());
        }
        return orders;
    }

    public List<String> getAllOrders() {
        return new ArrayList<>(orderDb.keySet());
    }

    public Integer getCountOfUnassignedOrders() {
        Integer countOfUnassignedOrders = 0;
        for(String orderId : orderDb.keySet()) {
            if(!orderToWhichDeliveryPartnerDb.containsKey(orderId)) {
                countOfUnassignedOrders++;
            }
        }
        return countOfUnassignedOrders;
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId) {
        if(!deliveryPartnerOrdersListDb.containsKey(partnerId)) {
            return 0;
        }

        List<Order> orders = deliveryPartnerOrdersListDb.get(partnerId);
        String[] arr = time.split(":");
        int deliveryTime = 60 * Integer.parseInt(arr[0]) + Integer.parseInt(arr[1]);

        Integer count = 0;
        for(Order order : orders) {
            if(order.getDeliveryTime() > deliveryTime) {
                count++;
            }
        }
        return count;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId) {
        if(!deliveryPartnerOrdersListDb.containsKey(partnerId)) {
            return null;
        }

        int maxTime = -1;
        List<Order> orders = deliveryPartnerOrdersListDb.get(partnerId);
        for(Order order : orders) {
            maxTime = Math.max(maxTime, order.getDeliveryTime());
        }

        String MM = (maxTime % 60) + "";
        String HH = ((maxTime - Integer.parseInt(MM)) / 60) + "";

        return HH + ":" + MM;
    }

    public void deletePartnerById(String partnerId) {
        if(!deliveryPartnerDb.containsKey(partnerId)) return;

        String orderIdToDel = "";
        for(String orderId : orderToWhichDeliveryPartnerDb.keySet()) {
            if(orderToWhichDeliveryPartnerDb.get(orderId).equals(partnerId)) {
                orderIdToDel = orderId;
                break;
            }
        }

        deliveryPartnerDb.remove(partnerId);
        if(!orderIdToDel.isBlank()) {
            orderToWhichDeliveryPartnerDb.remove(orderIdToDel);
            deliveryPartnerOrdersListDb.remove(partnerId);
        }
    }

    public void deleteOrderById(String orderId) {
        if(orderDb.containsKey(orderId)) {
            if(orderToWhichDeliveryPartnerDb.containsKey(orderId)) {
                int idx = 0;
                for(Order order : deliveryPartnerOrdersListDb.get(orderToWhichDeliveryPartnerDb.get(orderId))) {
                    if(order.getId().equals(orderId)) {
                        break;
                    }
                }
                deliveryPartnerOrdersListDb.get(orderToWhichDeliveryPartnerDb.get(orderId)).remove(idx);
                if(deliveryPartnerOrdersListDb.get(orderToWhichDeliveryPartnerDb.get(orderId)).size() == 0) {
                    deliveryPartnerOrdersListDb.remove(orderToWhichDeliveryPartnerDb.get(orderId));
                }
                orderToWhichDeliveryPartnerDb.remove(orderId);
            }
            orderDb.remove(orderId);
        }
    }
}