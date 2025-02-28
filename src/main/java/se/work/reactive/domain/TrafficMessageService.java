package se.work.reactive.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import se.work.reactive.domain.model.traffic.TrafficMessage;
import se.work.reactive.domain.model.traffic.TrafficResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrafficMessageService {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public Flux<TrafficMessage> getLatestMessages() {
        return webClient.get()
                .uri("/traffic/messages?format=json")
                .retrieve()
                .bodyToMono(TrafficResponse.class)
                .flatMapMany(trafficResponse -> Flux.fromIterable(trafficResponse.messages()));
    }

    private Flux<TrafficMessage> getLatestMessage_DebugVersion() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/traffic/messages")
                        .queryParam("format", "json")
                        .build()
                ).exchangeToFlux(response -> {
                    return response.bodyToMono(String.class)
                            .doOnNext(json -> System.out.println("Raw: " + json))
                            .flatMapMany(json -> {
                                try {
                                    TrafficResponse trafficResponse = objectMapper.readValue(json, TrafficResponse.class);

                                    if (trafficResponse.messages() == null) {
                                        return Flux.empty();
                                    }
                                    return Flux.fromIterable(trafficResponse.messages());
                                } catch (Exception e) {
                                    log.error("Error occurred when fetching /messages due to {}", e.getMessage());
                                    return Flux.empty();
                                }
                            });
                });
    }
}
