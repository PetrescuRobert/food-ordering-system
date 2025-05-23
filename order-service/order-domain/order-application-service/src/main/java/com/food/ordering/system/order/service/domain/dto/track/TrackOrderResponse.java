package com.food.ordering.system.order.service.domain.dto.track;

import com.food.ordering.system.valueobject.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;


@Builder
public record TrackOrderResponse(@NotNull UUID orderTrackingId, @NotNull OrderStatus orderStatus,
                                 List<String> failureMessages) {

}
