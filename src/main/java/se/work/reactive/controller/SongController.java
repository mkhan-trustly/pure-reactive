package se.work.reactive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import se.work.reactive.api.mapper.SongMapper;
import se.work.reactive.api.response.SongDto;
import se.work.reactive.domain.ProgramService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/programs")
public class SongController {

    private final ProgramService programService;

    @GetMapping("/aggregated-songs")
    public Flux<SongDto> aggregateSongsFromAllChannels() {
       return programService.aggregateSongsFromAllChannels()
               .map(SongMapper::toDto);
    }
}
