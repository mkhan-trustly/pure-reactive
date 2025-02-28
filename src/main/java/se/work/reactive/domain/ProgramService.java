package se.work.reactive.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.work.reactive.domain.model.song.ChannelResponse;
import se.work.reactive.domain.model.song.Playlist;
import se.work.reactive.domain.model.song.Program;
import se.work.reactive.domain.model.song.ProgramResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramService {

    private final WebClient webClient;

    public Flux<Playlist.Song> aggregateSongsFromAllChannels() {
        return webClient.get()
                .uri("/programs/index?format=json")
                .retrieve()
                .bodyToMono(ProgramResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.programs()))
                .buffer(5)
                .flatMap(this::getSongsInBatch);
    }

    private Flux<Playlist.Song> getSongsInBatch(List<Program> batch) {
        return Flux.fromIterable(batch)
                .flatMap(program -> getSongsByChannel(program.channel()), 5);
    }

    private Mono<Playlist.Song> getSongsByChannel(Program.Channel channel) {
        String uri = "/playlists/rightnow?channelid=%d&format=json".formatted(channel.id());
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ChannelResponse.class)
                .flatMap(response -> Mono.just(response.playlist().getAvailableSong()));
    }
}
