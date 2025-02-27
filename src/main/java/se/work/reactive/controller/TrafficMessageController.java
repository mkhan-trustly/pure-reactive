package se.work.reactive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import se.work.reactive.api.mapper.TrafficMessageMapper;
import se.work.reactive.api.response.TrafficMessageDto;
import se.work.reactive.domain.TrafficMessageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/traffic")
public class TrafficMessageController {

    private final TrafficMessageService trafficMessageService;

    @GetMapping("/messages")
    public Flux<TrafficMessageDto> getLatestMessages() {
        return trafficMessageService.getLatestMessages()
                .map(TrafficMessageMapper::toDto);
    }
}
