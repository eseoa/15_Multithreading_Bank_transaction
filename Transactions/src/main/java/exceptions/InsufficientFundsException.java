package exceptions;

public class InsufficientFundsException extends RuntimeException{

    public InsufficientFundsException(){
        super("Недостаточно средств для совершения операции");
    }
}
