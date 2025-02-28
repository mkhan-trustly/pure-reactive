package se.work.reactive.domain.model.song;

public record Program(Channel channel) {

    public record Channel(int id, String name) {}
}
