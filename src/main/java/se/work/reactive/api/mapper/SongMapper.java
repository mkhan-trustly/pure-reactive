package se.work.reactive.api.mapper;

import se.work.reactive.api.response.SongDto;
import se.work.reactive.domain.model.song.Playlist;

public class SongMapper {

    public static SongDto toDto(Playlist.Song song) {
        return new SongDto(
                song.title(),
                song.description(),
                song.artist(),
                song.composer(),
                song.recordlabel()
        );
    }
}
