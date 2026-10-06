package Library;

public class EmailFailureException extends RuntimeException {
    public EmailFailureException(String message){
        super("Email Failed!" + message);
    }
}
