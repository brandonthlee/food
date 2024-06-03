package app.model;

@FunctionalInterface
public interface MyFunction {
    MessageId apply(String message);
}
