package kit.exceptions;

public class PasswordException extends Exception {
    public PasswordException(){
        super("password is not correct.");
    }
}
