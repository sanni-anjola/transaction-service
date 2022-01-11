package com.example.transactionservice.data.repository;

import com.example.transactionservice.data.dao.TransactionStat;
import com.example.transactionservice.data.model.Transaction;
import com.example.transactionservice.exception.FutureTransactionException;
import com.example.transactionservice.exception.StaleTransactionException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {
    private TransactionRepository transactionRepository;
    @BeforeEach
    void setUp() {
        transactionRepository = TransactionRepository.getInstance();
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Test that Repository can save")
    void save() {
        // Given
        assertThat(transactionRepository.getSize()).isEqualTo(0);
        // When

        Transaction t = new Transaction(BigDecimal.TEN, LocalDateTime.now());
        transactionRepository.save(t);
        // Assert
        assertThat(transactionRepository.getSize()).isEqualTo(1);
    }
//    Transaction t = new Transaction(BigDecimal.TEN, LocalDateTime.parse("2022-01-07T16:31:56.000"));


    @Test
    @DisplayName("Test throws error for timestamp over 30 seconds")
    void save1() {
        Transaction t = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(35));
        assertThrows(StaleTransactionException.class, () -> transactionRepository.save(t));
    }

    @Test
    @DisplayName("Test throws error for future time")
    void save2() {
        Transaction t = new Transaction(BigDecimal.TEN, LocalDateTime.now().plusSeconds(1));
        assertThrows(FutureTransactionException.class, () -> transactionRepository.save(t));
    }

    @Test
    void findAll() {
        // Given
        assertThat(transactionRepository.getSize()).isEqualTo(0);
        //When
        Transaction t1 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(20));
        Transaction t2 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(15));
        Transaction t3 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(5));

        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        List<Transaction> all = transactionRepository.findAll();
        assertThat(all.size()).isEqualTo(transactionRepository.getSize());
        assertThat(all).contains(t1);
        assertThat(all).contains(t2);
        assertThat(all).contains(t3);
    }

    @Test
    void deleteAll() {
        // Given
        assertThat(transactionRepository.getSize()).isEqualTo(0);
        //When
        Transaction t1 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(20));
        Transaction t2 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(15));
        Transaction t3 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(5));

        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);

        assertThat(transactionRepository.getSize()).isEqualTo(3);

        transactionRepository.deleteAll();
        assertThat(transactionRepository.getSize()).isEqualTo(0);
        assertThat(transactionRepository.findAll()).isEmpty();
        assertThat(transactionRepository.findAll()).doesNotContain(t1);

    }

    @Test
    @Disabled
    @DisplayName("Test that the app can get Statistics of transactions")
    void getStats() throws InterruptedException {
        // Given
        assertThat(transactionRepository.getSize()).isEqualTo(0);
        //When
        Transaction t1 = new Transaction(BigDecimal.ONE, LocalDateTime.now().minusSeconds(20));
        Transaction t2 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(15));
        Transaction t3 = new Transaction(BigDecimal.TEN, LocalDateTime.now().minusSeconds(5));

        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);

        System.out.println(transactionRepository.getSize());
        TransactionStat stats = transactionRepository.getStats();

        assertThat(stats.getCount()).isEqualTo(3);
        assertThat(stats.getSum()).isEqualTo(BigDecimal.valueOf(21));
        assertThat(stats.getAvg()).isEqualTo(BigDecimal.valueOf(7));
        assertThat(stats.getMax()).isEqualTo(BigDecimal.TEN);
        assertThat(stats.getMin()).isEqualTo(BigDecimal.ONE);
    }

    @DisplayName("Test that Only one instance of the repository exists")
    @Test
    void getInstance() {
        TransactionRepository anotherTransactionRepository = TransactionRepository.getInstance();
        assertEquals(transactionRepository, anotherTransactionRepository);
    }
}