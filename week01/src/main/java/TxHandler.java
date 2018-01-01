import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TxHandler {

    private final UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures onx each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        // (1) all outputs claimed by {@code tx} are in the current UTXO pool
        if (tx.getInputs().stream()
                .anyMatch(in -> !pool.contains(new UTXO(in.prevTxHash, in.outputIndex)))) {
            return false;
        }
        // (2) the signatures onx each input of {@code tx} are valid,
        AtomicInteger i = new AtomicInteger();
        if (tx.getInputs().stream()
                .anyMatch(in -> {
                    Transaction.Output out = pool.getTxOutput(new UTXO(in.prevTxHash, in.outputIndex));
                    return !Crypto.verifySignature(out.address, tx.getRawDataToSign(i.getAndIncrement()), in.signature);
                })) {
            return false;
        }
        // (3) no UTXO is claimed multiple times by {@code tx}
        Set<UTXO> uniq = new HashSet<>();
        if (tx.getInputs().stream()
                .anyMatch(in -> !uniq.add(new UTXO(in.prevTxHash, in.outputIndex)))) {
            return false;
        }
        // (4) all of {@code tx}s output values are non-negative
        if (tx.getOutputs().stream()
                .anyMatch(out -> out.value < 0)) {
            return false;
        }
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
        final double inputAmount = tx.getInputs().stream()
                .mapToDouble(in -> pool.getTxOutput(new UTXO(in.prevTxHash, in.outputIndex)).value)
                .sum();
        final double outputAmount = tx.getOutputs().stream()
                .mapToDouble(out -> out.value)
                .sum();
        if (inputAmount < outputAmount) {
            return false;
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> candidateTxs = Arrays.asList(possibleTxs);
        List<Transaction> acceptedTxs = new ArrayList<>();
        List<Transaction> nextTxs = new ArrayList<>();

        candidateTxs.stream().forEach(tx -> {
            if (isValidTx(tx)) {
                for (int i = 0; i < tx.numOutputs(); i++) {
                    pool.addUTXO(new UTXO(tx.getHash(), i), tx.getOutput(i));
                }
                acceptedTxs.add(tx);
            } else {
                nextTxs.add(tx);
            }
        });

        nextTxs.stream().forEach(tx -> {
            if (isValidTx(tx)) {
                for (int i = 0; i < tx.numOutputs(); i++) {
                    pool.addUTXO(new UTXO(tx.getHash(), i), tx.getOutput(i));
                }
                acceptedTxs.add(tx);
            } else {
//                nextTxs.add(tx);
            }
        });

        return acceptedTxs.toArray(new Transaction[acceptedTxs.size()]);
    }

}
