package exceptions;

public class BlockedAccountException extends RuntimeException{
    public BlockedAccountException(){
        super("Попытка произвести действие с заблокированным аккаунтом");
    }

    public BlockedAccountException(String s){
        super(s);
    }
}
