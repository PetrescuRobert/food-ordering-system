package com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval;

import com.food.ordering.system.event.publisher.DomainEventPublisher;
import com.food.ordering.system.service.domain.event.OrderPaidEvent;

public interface OrderPaidRestaurantRequestMessagePublisher extends
    DomainEventPublisher<OrderPaidEvent> {

}
