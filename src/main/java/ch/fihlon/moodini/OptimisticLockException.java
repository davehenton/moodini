package ch.fihlon.moodini;

public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }

}
