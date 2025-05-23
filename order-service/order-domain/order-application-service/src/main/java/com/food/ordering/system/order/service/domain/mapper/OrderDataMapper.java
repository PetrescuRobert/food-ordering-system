package com.food.ordering.system.order.service.domain.mapper;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.service.domain.entity.Order;
import com.food.ordering.system.service.domain.entity.OrderItem;
import com.food.ordering.system.service.domain.entity.Product;
import com.food.ordering.system.service.domain.entity.Restaurant;
import com.food.ordering.system.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.valueobject.CustomerId;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.ProductId;
import com.food.ordering.system.valueobject.RestaurantId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderDataMapper {

  public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand) {
    return Restaurant.builder()
        .restaurantId(new RestaurantId(createOrderCommand.restaurantId()))
        .products(createOrderCommand.items().stream()
            .map(orderItem -> new Product(new ProductId(orderItem.getProductId()))).collect(
                Collectors.toList()))
        .build();
  }

  public CreateOrderResponse orderToCreateOrderResponse(Order order, String message) {
    return CreateOrderResponse.builder()
        .orderTrackingId(order.getTrackingId().getValue())
        .orderStatus(order.getOrderStatus())
        .message(message)
        .build();
  }

  public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
    return Order.builder()
        .customerId(new CustomerId(createOrderCommand.customerId()))
        .restaurantId(new RestaurantId(createOrderCommand.restaurantId()))
        .deliveryAddress(orderAddressToStreetAddress(createOrderCommand.address()))
        .price(new Money(createOrderCommand.price()))
        .items(orderItemsToOrderItemsEntities(createOrderCommand.items()))
        .build();
  }

  public TrackOrderResponse orderToTrackOrderResponse(Order order) {
    return TrackOrderResponse.builder()
        .orderTrackingId(order.getTrackingId().getValue())
        .orderStatus(order.getOrderStatus())
        .failureMessages(order.getFailureMessages())
        .build();
  }

  private List<OrderItem> orderItemsToOrderItemsEntities(
      List<com.food.ordering.system.order.service.domain.dto.create.OrderItem> items) {
    return items.stream().map(item -> OrderItem.builder()
            .product(new Product(new ProductId(item.getProductId())))
            .price(new Money(item.getPrice()))
            .quantity(item.getQuantity())
            .subTotal(new Money(item.getSubTotal()))
            .build())
        .collect(Collectors.toList());
  }

  private StreetAddress orderAddressToStreetAddress(OrderAddress orderAddress) {
    return new StreetAddress(
        UUID.randomUUID(),
        orderAddress.getStreet(),
        orderAddress.getPostalCode(),
        orderAddress.getCity()
    );
  }

}
