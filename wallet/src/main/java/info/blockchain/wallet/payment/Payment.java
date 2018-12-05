package info.blockchain.wallet.payment;

import info.blockchain.api.data.UnspentOutput;
import info.blockchain.api.data.UnspentOutputs;
import info.blockchain.balance.CryptoCurrency;
import info.blockchain.wallet.BlockchainFramework;
import info.blockchain.wallet.api.dust.data.DustInput;
import io.reactivex.annotations.NonNull;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import retrofit2.Call;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

public class Payment {

    static final BigInteger PUSHTX_MIN = BigInteger.valueOf(Coin.parseCoin("0.00001").longValue());
    public static final BigInteger DUST = BigInteger.valueOf(Coin.parseCoin("0.000005460").longValue());

    public Payment() {
        // Empty constructor for injection
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fee Handling
    ///////////////////////////////////////////////////////////////////////////
    public BigInteger estimatedFee(int inputs, int outputs, @NonNull BigInteger feePerKb) {
        return Fees.estimatedFee(inputs, outputs, feePerKb);
    }

    public int estimatedSize(int inputs, int outputs) {
        return Fees.estimatedSize(inputs, outputs);
    }

    public boolean isAdequateFee(int inputs, int outputs, @NonNull BigInteger absoluteFee) {
        return Fees.isAdequateFee(inputs, outputs, absoluteFee);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Coin selection
    ///////////////////////////////////////////////////////////////////////////
    public Call<UnspentOutputs> getUnspentCoins(@NonNull List<String> addresses) {
        return Coins.getUnspentCoins(addresses);
    }

    public Call<UnspentOutputs> getUnspentBchCoins(@NonNull List<String> addresses) {
        return Coins.getUnspentBchCoins(addresses);
    }

    public Pair<BigInteger, BigInteger> getMaximumAvailable(@NonNull UnspentOutputs unspentCoins,
                                                            @NonNull BigInteger feePerKb,
                                                            boolean addReplayProtection) {
        return Coins.getMaximumAvailable(unspentCoins, feePerKb, addReplayProtection);
    }

    public SpendableUnspentOutputs getSpendableCoins(@NonNull UnspentOutputs unspentCoins,
                                                     @NonNull BigInteger paymentAmount,
                                                     @NonNull BigInteger feePerKb,
                                                     boolean addReplayProtection) {
        return Coins.getMinimumCoinsForPayment(unspentCoins, paymentAmount, feePerKb, addReplayProtection);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Simple Transaction
    ///////////////////////////////////////////////////////////////////////////
    public Transaction makeSimpleTransaction(@NonNull NetworkParameters networkParameters,
                                             @NonNull List<UnspentOutput> unspentCoins,
                                             @NonNull HashMap<String, BigInteger> receivingAddresses,
                                             @NonNull BigInteger fee,
                                             @Nullable String changeAddress)
            throws InsufficientMoneyException, AddressFormatException {
        return PaymentTx.makeSimpleTransaction(networkParameters,
                unspentCoins,
                receivingAddresses,
                fee,
                changeAddress);
    }

    public void signSimpleTransaction(@NonNull NetworkParameters networkParameters,
                                      @NonNull Transaction transaction,
                                      @NonNull List<ECKey> keys) {
        PaymentTx.signSimpleTransaction(networkParameters, transaction, keys, false);
    }

    public void signBchTransaction(@NonNull NetworkParameters networkParameters,
                                   @NonNull Transaction transaction,
                                   @NonNull List<ECKey> keys) {
        PaymentTx.signSimpleTransaction(networkParameters, transaction, keys, true);
    }

    public Call<ResponseBody> publishSimpleTransaction(@NonNull Transaction transaction) {
        return PaymentTx.publishSimpleBtcTransaction(transaction, BlockchainFramework.getApiCode());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Non-replayable Transactions
    ///////////////////////////////////////////////////////////////////////////
    public Transaction makeNonReplayableTransaction(@NonNull NetworkParameters networkParameters,
                                                    @NonNull List<UnspentOutput> unspentCoins,
                                                    @NonNull HashMap<String, BigInteger> receivingAddresses,
                                                    @NonNull BigInteger fee,
                                                    @Nullable String changeAddress,
                                                    @NonNull DustInput dustServiceInput)
            throws InsufficientMoneyException, AddressFormatException {

        return PaymentTx.makeNonReplayableTransaction(networkParameters,
                unspentCoins,
                receivingAddresses,
                fee,
                changeAddress,
                dustServiceInput);
    }

    public Call<ResponseBody> publishTransactionWithSecret(@NonNull CryptoCurrency currency,
                                                           @NonNull Transaction transaction,
                                                           @NonNull String lockSecret) {
        return PaymentTx.publishTransactionWithSecret(currency, transaction, lockSecret, BlockchainFramework.getApiCode());
    }
}
