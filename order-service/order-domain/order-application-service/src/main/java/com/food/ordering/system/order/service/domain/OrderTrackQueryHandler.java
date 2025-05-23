package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.service.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderTrackQueryHandler {

  private final OrderDataMapper orderDataMapper;
  private final OrderRepository orderRepository;

  public OrderTrackQueryHandler(OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
    this.orderDataMapper = orderDataMapper;
    this.orderRepository = orderRepository;
  }

  @Transactional(readOnly = true)
  public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
    var queryResult = orderRepository.findByTrackingId(
        new TrackingId(trackOrderQuery.orderTrackingId()));
    if (queryResult.isEmpty()) {
      log.warn("Could not find order with id: {}", trackOrderQuery.orderTrackingId());
      throw new OrderNotFoundException(
          "Could not find order with tracking id: %s".formatted(trackOrderQuery.orderTrackingId()));
    }
    return orderDataMapper.orderToTrackOrderResponse(queryResult.get());
  }
}
