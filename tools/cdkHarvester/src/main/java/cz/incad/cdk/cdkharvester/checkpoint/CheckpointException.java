package cz.incad.cdk.cdkharvester.checkpoint;

public class CheckpointException extends Exception {

    public CheckpointException() {}

    public CheckpointException(String message) {
        super(message);
    }

    public CheckpointException(String message, Throwable cause) {
        super(message, cause);
    }

    public CheckpointException(Throwable cause) {
        super(cause);
    }

    public CheckpointException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
