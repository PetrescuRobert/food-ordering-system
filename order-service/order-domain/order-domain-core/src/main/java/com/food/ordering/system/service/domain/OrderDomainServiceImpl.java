package com.food.ordering.system.service.domain;

import com.food.ordering.system.service.domain.entity.Order;
import com.food.ordering.system.service.domain.entity.Product;
import com.food.ordering.system.service.domain.entity.Restaurant;
import com.food.ordering.system.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.service.domain.exception.OrderDomainException;
import com.food.ordering.system.valueobject.ProductId;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderDomainServiceImpl implements OrderDomainService {

  private static final Logger log = LoggerFactory.getLogger(OrderDomainServiceImpl.class);
  private static final ZoneId UTC = ZoneId.of("UTC");

  @Override
  public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant) {
    validateRestaurant(restaurant);
    setOrderProductInformation(order, restaurant);
    order.validateOrder();
    order.initializeOrder();
    log.info("Order with id: {} is initiated", order.getId().getValue());
    return new OrderCreatedEvent(order, ZonedDateTime.now(UTC));
  }

  @Override
  public OrderPaidEvent payOrder(Order order) {
    order.pay();
    log.info("Order with id: {} is paid!", order.getId().getValue());
    return new OrderPaidEvent(order, ZonedDateTime.now(UTC));
  }

  @Override
  public void approveOrder(Order order) {
    order.approve();
    log.info("Order with id: {} is approved!", order.getId().getValue());
  }

  @Override
  public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
    order.initCancel(failureMessages);
    log.info("Order payment is cancelling for order id: {}", order.getId().getValue());
    return new OrderCancelledEvent(order, ZonedDateTime.now(UTC));
  }

  @Override
  public void cancelOrder(Order order, List<String> failureMessages) {
    order.cancel(failureMessages);
    log.info("Order with id: {} is cancelled!", order.getId().getValue());
  }

  private void validateRestaurant(Restaurant restaurant) {
    if (!restaurant.isActive()) {
      throw new OrderDomainException("Restaurant with id %s is currently not active!".formatted(
          restaurant.getId().getValue()));
    }
  }

  private void setOrderProductInformation(Order order, Restaurant restaurant) {
//    Time complexity O(n^2)
//    order.getItems().forEach(orderItem -> restaurant.getProducts().forEach(restaurantProduct -> {
//      var currentProduct = orderItem.getProduct();
//      if (currentProduct.equals(restaurantProduct)) {
//        currentProduct.updateWithConfirmedNameAndPrice(restaurantProduct.getName(),
//            restaurantProduct.getPrice());
//      }
//    }));

    Map<ProductId, Product> productMap = restaurant.getProducts().stream().collect(Collectors.toMap(
        Product::getId, Function.identity()));

    order.getItems().forEach(orderItem -> {
      var currentProduct = orderItem.getProduct();
      var restaurantProduct = productMap.get(currentProduct.getId());

      if (restaurantProduct != null) {
        currentProduct.updateWithConfirmedNameAndPrice(restaurantProduct.getName(),
            restaurantProduct.getPrice());
      }
    });
  }

}
