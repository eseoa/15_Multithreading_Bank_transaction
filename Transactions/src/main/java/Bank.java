import exceptions.BlockedAccountException;
import exceptions.InsufficientFundsException;

import java.util.*;

public class Bank {

    private Map<String, Account> accounts = new TreeMap<>();
    private final Random random = new Random();
    private static final long AMOUNT_TO_CHECK = 50_000;
    private static long sum = 0;


    public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
        throws InterruptedException {
        Thread.sleep(1000);
        return random.nextBoolean();
    }

    public void transfer(String fromAccountNum, String toAccountNum, long amount) {
        int fromId = fromAccountNum.hashCode();
        int toId =  toAccountNum.hashCode();
        synchronized (accounts.get(fromId < toId ? fromAccountNum : toAccountNum)) {
            synchronized (accounts.get(fromId < toId ? toAccountNum : fromAccountNum)) {
                doTransfer(fromAccountNum, toAccountNum, amount);
            }
        }
    }

    public long getBalance(String accountNum) {
        return accounts.get(accountNum).getMoney();
    }

    public long getSumAllAccounts() {
        accounts.forEach((s, account) -> sum += account.getMoney());
        return sum;
    }

    public void addAccount(Account account){
        accounts.put(account.getAccNumber(), account);
    }

    public Account getAccount(String number){
        return accounts.get(number);
    }

    private void doForBigAmount(String fromAccountNum, String toAccountNum , long amount) throws InterruptedException {
        if(isFraud(fromAccountNum, toAccountNum, amount)){
            blockAccount(fromAccountNum);
            blockAccount(toAccountNum);
            throw new BlockedAccountException("Была произведена блокировка обоих аккаунтов");
        }
        else {
            transaction(fromAccountNum, toAccountNum, amount);
        }


    }

    private void transaction (String fromAccountNum, String toAccountNum, long amount){
        if(accounts.get(fromAccountNum).getAccNumber().contains("BLOCKED")  || accounts.get(toAccountNum).getAccNumber().contains("BLOCKED")){
            throw new BlockedAccountException();
        }
        else {
            accounts.get(fromAccountNum).setMoney(getBalance(fromAccountNum) - amount);
            accounts.get(toAccountNum).setMoney(getBalance(toAccountNum) + amount);
            System.out.println("Перевод успешно выполнен");
        }

    }

    private void tryToTransfer(String fromAccountNum, String toAccountNum, long amount) throws InterruptedException {

        if (AMOUNT_TO_CHECK > amount) {
            transaction(fromAccountNum, toAccountNum, amount);
        } else {
            doForBigAmount(fromAccountNum, toAccountNum, amount);
        }
    }

    private void blockAccount(String number){
        accounts.get(number).setAccNumber("BLOCKED ".concat(number));
    }

    private void doTransfer (String fromAccountNum, String toAccountNum, long amount){
        try {
            if (getBalance(fromAccountNum) >= amount) {
                tryToTransfer(fromAccountNum, toAccountNum, amount);
            } else {
                throw new InsufficientFundsException();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}

