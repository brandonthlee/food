package app.utils.cursor;

public record PageCursor<T>(CursorRequest nextCursorRequest, T body) {
}
