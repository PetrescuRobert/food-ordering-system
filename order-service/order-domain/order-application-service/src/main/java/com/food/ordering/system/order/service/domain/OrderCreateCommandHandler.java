package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.service.domain.OrderDomainService;
import com.food.ordering.system.service.domain.entity.Order;
import com.food.ordering.system.service.domain.entity.OrderItem;
import com.food.ordering.system.service.domain.entity.Restaurant;
import com.food.ordering.system.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.service.domain.exception.OrderDomainException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderCreateCommandHandler {

  private final OrderDomainService orderDomainService;
  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final RestaurantRepository restaurantRepository;
  private final OrderDataMapper orderDataMapper;
  private final ApplicationDomainEventPublisher applicationDomainEventPublisher;

  public OrderCreateCommandHandler(OrderDomainService orderDomainService,
      OrderRepository orderRepository, CustomerRepository customerRepository,
      RestaurantRepository restaurantRepository, OrderDataMapper orderDataMapper,
      ApplicationDomainEventPublisher applicationDomainEventPublisher) {
    this.orderDomainService = orderDomainService;
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.restaurantRepository = restaurantRepository;
    this.orderDataMapper = orderDataMapper;
    this.applicationDomainEventPublisher = applicationDomainEventPublisher;
  }

  @Transactional
  public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
    checkCustomer(createOrderCommand.customerId());
    var restaurant = checkRestaurant(createOrderCommand);
    var order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
    OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order,
        restaurant);
    var orderResult = saveOrder(order);
    log.info("Order is created with id: {}", orderResult.getId().getValue());
    applicationDomainEventPublisher.publish(orderCreatedEvent);
    return orderDataMapper.orderToCreateOrderResponse(orderResult, "Order created successfully!");
  }

  private Restaurant checkRestaurant(CreateOrderCommand createOrderCommand) {
    var restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
    var optionalRestaurant = restaurantRepository.findRestaurantInformation(restaurant);
    if (optionalRestaurant.isEmpty()) {
      log.warn("Could not find restaurant with restaurant id:{}",
          createOrderCommand.restaurantId());
      throw new OrderDomainException("Could not find restaurant with restaurant id: %s".formatted(
          createOrderCommand.restaurantId()));
    }
    return optionalRestaurant.get();
  }

  private void checkCustomer(UUID customerId) {

    var customer = customerRepository.findCustomer(customerId);

    if (customer.isEmpty()) {
      log.warn("Could not find customer with customer id: {}", customerId);
      throw new OrderDomainException(
          "Could not find customer with customer id: %s".formatted(customerId));
    }
  }

  private Order saveOrder(Order order) {
    var orderResult = orderRepository.save(order);
    if (orderResult == null) {
      log.error("Could not save order!");
      throw new OrderDomainException("Could not save order!");
    }
    log.info("Order is saved with id: {}", orderResult.getId().getValue());
    return orderResult;
  }
}
