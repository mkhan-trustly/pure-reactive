package se.work.reactive.domain.model.song;

public record Playlist(Song previoussong, Song song) {

    public Song getAvailableSong() {
        if (song == null) {
            return previoussong;
        }
        return song;
    }

    public record Song(String title, String description, String artist, String composer, String recordlabel) {}
}
