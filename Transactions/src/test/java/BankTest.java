import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

public class BankTest {

    Bank bank;
    private final double DELTA = 0.01;
    List<Thread> threads;

    @BeforeEach
    public void setUp(){
        bank = new Bank();
       for(int i = 0; i < 10_000; i++){
           bank.addAccount(new Account(100, String.valueOf(i)));
       }
       threads = new ArrayList<>();
    }

    @Test
    @DisplayName("Один перевод")
    public void oneTransactionArrivalCheck(){

        long actualSender = 0;
        long actualRecipient = 0;
        long expectedSender = 0;
        long expectedRecipient = 200;
        threads.add(new Thread(() -> bank.transfer("0", "1", 100)));
        threads.get(threads.size()-1).start();
        waitThreadsFinish();
        actualRecipient = bank.getBalance("1");
        actualSender = bank.getBalance("0");
        Assertions.assertEquals(expectedRecipient, actualRecipient, String.valueOf(DELTA));
        Assertions.assertEquals(expectedSender, actualSender, String.valueOf(DELTA));

    }

    @Test
    @DisplayName("Множество переводов с разных аккаунтов")
    public void manyTransactionsArrivalCheck(){

        int recipientId = 9_999;
        long actualRecipient = 0;
        long actualSender = 0;
        long expectedSender = 0;
        long expectedRecipient = 1_000_000;

        for(int i = 0; i < 5_000; i++){
            int finalI = i;
            int finalRecipientId = recipientId;
            threads.add(new Thread(() -> bank.transfer(String.valueOf(finalI),String.valueOf(finalRecipientId),100)));
            threads.get(threads.size()-1).start();
            recipientId--;
        }

       waitThreadsFinish();

        for (int i = 5_000; i < 10_000; i++){
            actualRecipient += bank.getBalance(String.valueOf(i));
        }

        for (int i = 0; i < 5_000; i++){
            actualSender += bank.getBalance(String.valueOf(i));
        }
        Assertions.assertEquals(expectedRecipient, actualRecipient, DELTA);
        Assertions.assertEquals(expectedSender, actualSender, DELTA);
    }

    @Test
    @DisplayName("Взаимодействие с заблокированными аккаунтами")
    public void blockTransferCheck(){

        int recipientId = 9_999;
        long actual = 0;
        long expected = 500_000;

        doBlockedList();

        for(int i = 0; i < 5_000; i++){
            int finalRecipientId = recipientId;
            int finalI = i;
            threads.add(new Thread(() ->bank.transfer(String.valueOf(finalI), String.valueOf(finalRecipientId),100)));
            threads.get(threads.size()-1).start();
            recipientId--;
        }

       waitThreadsFinish();

        for (int i = 5_000; i < 10_000; i++){
            actual += bank.getBalance(String.valueOf(i));
        }
        Assertions.assertEquals(expected, actual, DELTA);
    }

    @Test
    @DisplayName("Множество переводов с одного аккаунта")
    public void manyTransactionsFromOneAccount(){

        long actualRecipient = 0;
        long actualSender = 0;
        long expectedSender = 0;
        long expectedRecipient = 500_100;
        int recipientId = 9_999;

        for(int i = 0; i < 1_000; i++){
            int finalRecipientId = recipientId;
            threads.add(new Thread(() ->bank.transfer("1",String.valueOf(finalRecipientId),1)));
            threads.get(threads.size()-1).start();
            recipientId--;
        }

        waitThreadsFinish();

        for (int i = 5_000; i < 10_000; i++){
            actualRecipient += bank.getBalance(String.valueOf(i));
        }
        actualSender = bank.getBalance("1");
        Assertions.assertEquals(expectedRecipient, actualRecipient, DELTA);
        Assertions.assertEquals(expectedSender, actualSender, DELTA);

    }

    @Test
    @DisplayName("Множество переводов крупных сумм, проверка количества денег в банке")
    public void largeAmountTransfer(){

        int recipientId = 9_999;
        long expectedTotalSum = 1_000_000_000;

        for(int i = 0; i < 10_000; i++){
            bank.getAccount(String.valueOf(i)).setMoney(100_000);
        }

        for(int i = 0; i < 5_000; i++){
            int finalRecipientId = recipientId;
            int finalI = i;
            threads.add(new Thread(() -> bank.transfer(String.valueOf(finalI), String.valueOf(finalRecipientId),100_000)));
            threads.get(threads.size()-1).start();
            recipientId--;
        }
        waitThreadsFinish();
        long actualTotalSum = bank.getSumAllAccounts();
        Assertions.assertEquals(expectedTotalSum, actualTotalSum, DELTA);

    }

    @Test
    @DisplayName("Полный тест, попытка выполнить все")
    public void fullTest(){
        int recipientId = 9_999;
        long actualRecipient = 0;
        long actualSender = 0;
        long expectedSender = 449_910_000;
        long expectedRecipient = 550_090_000;

        for(int i = 0; i < 10_000; i++){
            bank.getAccount(String.valueOf(i)).setMoney(100_000);
        }

        for(int i = 0; i < 1_000; i++){
            threads.add(new Thread(() -> bank.transfer("1","9999",1000)));
            threads.get(threads.size()-1).start();
        }

        for(int i = 0; i < 5_000; i++){
            int finalRecipientId = recipientId;
            int finalI = i;
            threads.add (new Thread (() -> bank.transfer(String.valueOf(finalI),String.valueOf(finalRecipientId),10000)));
            threads.get(threads.size()-1).start();
            recipientId--;
        }

        waitThreadsFinish();

        for (int i = 5_000; i < 10_000; i++){
            actualRecipient += bank.getBalance(String.valueOf(i));
        }

        for (int i = 0; i < 5_000; i++){
            actualSender += bank.getBalance(String.valueOf(i));
        }

        Assertions.assertEquals(expectedRecipient, actualRecipient, DELTA);
        Assertions.assertEquals(expectedSender, actualSender, DELTA);
    }

    @AfterEach
    public void clear(){
        threads.clear();
    }

    private void doBlockedList(){
        for (int i = 0; i< 5_000; i++){
            bank.getAccount(String.valueOf(i)).setAccNumber("BLOCKED ".concat(String.valueOf(i)));
        }

    }

    private void waitThreadsFinish(){
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
