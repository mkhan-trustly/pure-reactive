package se.work.reactive.api.mapper;

import se.work.reactive.api.response.TrafficMessageDto;
import se.work.reactive.domain.model.traffic.TrafficMessage;

public class TrafficMessageMapper {

    public static TrafficMessageDto toDto(TrafficMessage message) {
        return new TrafficMessageDto(
                message.id(),
                message.title(),
                message.description(),
                message.category(),
                message.priority()
        );
    }
}
