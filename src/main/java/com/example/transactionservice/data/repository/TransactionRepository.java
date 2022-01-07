package com.example.transactionservice.data.repository;

import com.example.transactionservice.data.model.Transaction;
import com.example.transactionservice.exception.FutureTransactionException;
import com.example.transactionservice.exception.StaleTransactionException;
import com.example.transactionservice.exception.TransactionException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class TransactionRepository {
    private int idCount;
    private final ConcurrentMap<Long, Transaction> transactiondb;
    public static TransactionRepository instance;


    private TransactionRepository() {
        transactiondb = new ConcurrentHashMap<>();
    }

    private Long generateId(){
        return (long) (transactiondb.size() + 1);
    }

    public Transaction save(Transaction transaction){
        long timeLapse = Duration.between(transaction.getTimestamp(), LocalDateTime.now()).toSeconds();
        if(timeLapse > 30)
            throw new StaleTransactionException("Transaction over 30seconds cannot be registered");
        if(timeLapse < 0)
            throw new FutureTransactionException("Transaction cannot be done in the future");
        long id = generateId();
        transaction.setId(id);
        transactiondb.put(id, transaction);
        return transaction;
    }

    public List<Transaction> findAll(){

        return transactiondb.values().stream().toList();
    }

    public void deleteAll(){
        transactiondb.clear();
    }

    public


    private static class TransactionRepositorySingletonHelper{
        private static final TransactionRepository instance = new TransactionRepository();
    }

    public static TransactionRepository getInstance(){
        return TransactionRepositorySingletonHelper.instance;
    }
}
