import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.security.PublicKey;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class TxHandlerTest {

    static Random R = new Random();
    static AtomicInteger TXID = new AtomicInteger();

    TxHandler handler;
    @Mock PublicKey pk1;
    @Mock Transaction.Output txOutA1;
    @Mock Transaction.Output txOutA2;
    @Mock UTXO utxoA;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void isValidTx1() {
        UTXOPool pool = new UTXOPool();
//        byte[] txHashA = nextTxHash();
//        int index1 = nextTxId();
//        UTXO utxoA = new UTXO(txHashA, index1);
        Transaction tx1 = new Transaction();
        double value1a = nextValue();
        tx1.addOutput(value1a, pk1);

        pool.addUTXO(utxoA, txOutA1);
        pool.addUTXO(utxoA, txOutA2);
        handler = new TxHandler(pool);

        assertTrue(handler.isValidTx(tx1));
    }

    private double nextValue() {
        return R.nextDouble();
    }

    private int nextTxId() {
        return TXID.getAndIncrement();
    }

    public static byte[] nextTxHash() {
        byte[] hash = new byte[8];
        R.nextBytes(hash);
        return hash;
    }

    @Test
    public void handleTxs() {
    }
}
