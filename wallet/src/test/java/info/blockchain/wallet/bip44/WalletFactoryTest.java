package info.blockchain.wallet.bip44;

import com.blockchain.wallet.NoSeedException;
import com.blockchain.wallet.SeedAccess;
import info.blockchain.wallet.bip44.HDWalletFactory.Language;

import java.io.IOException;
import java.util.ArrayList;

import info.blockchain.wallet.exceptions.HDWalletException;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.BitcoinMainNetParams;
import org.junit.Assert;
import org.junit.Test;

import static org.spongycastle.util.encoders.Hex.toHexString;

public class WalletFactoryTest {

    @Test
    public void testCreateWallet() throws IOException, MnemonicException.MnemonicLengthException {

        String passphrase = "passphrase";
        int mnemonicLength = 12;
        String path = "M/44H";

        HDWallet wallet = HDWalletFactory
            .createWallet(BitcoinMainNetParams.get(), Language.US, mnemonicLength, passphrase, 1);

        Assert.assertEquals(mnemonicLength, wallet.getMnemonicOld().split(" ").length);
        Assert.assertEquals(passphrase, wallet.getPassphrase());
        Assert.assertEquals(1, wallet.getAccounts().size());
        Assert.assertEquals(path, wallet.getPath());
    }

    @Test
    public void testRestoreWallet_mnemonic() {

        String mnemonic = "all all all all all all all all all all all all";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        HDWallet wallet = null;

        try {
            wallet = HDWalletFactory
                .restoreWallet(BitcoinMainNetParams.get(), Language.US, mnemonic, passphrase,
                    accountListSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //HDWallet
        Assert.assertNotNull(wallet);
        Assert.assertEquals("edb3e309910eafe85e03c9067b82a04d59e040523810c92bac3aca8252d461d5",
                wallet.getMasterKey().getPrivateKeyAsHex());
        SeedAccess seedAccess = wallet;
        Assert.assertEquals(
                "50f4cb14e1cebfccf865118487bb3a6264f51eb410894e1458a9bc9cb241886fbce65249611c4822dc957b2da47674dbddd02e003f0507bc757c20e835b0992f",
                toHexString(seedAccess.getHdSeed())
        );
        Assert.assertEquals(16, wallet.getSeed().length);
        Assert.assertEquals("0660cc198330660cc198330660cc1983", wallet.getSeedHex());
        Assert.assertEquals("M/44H", wallet.getPath());
        Assert.assertEquals(mnemonic, wallet.getMnemonicOld());
        Assert.assertEquals(passphrase, wallet.getPassphrase());

        //HDAccount
        Assert.assertEquals(accountListSize, wallet.getAccounts().size());
        wallet.addAccount();
        Assert.assertEquals(accountListSize + 1, wallet.getAccounts().size());
    }

    @Test(expected = NoSeedException.class)
    public void restoreWatchOnlyShouldThrowOnSeedAccess() {
        HDWallet wallet = new HDWallet(BitcoinMainNetParams.get(), new ArrayList<String>());
        SeedAccess seedAccess = wallet;
        seedAccess.getHdSeed();
    }

    @Test
    public void testRestoreWallet_badMnemonic_fail() {

        HDWallet wallet = null;

        try {
            wallet = HDWalletFactory
                .restoreWallet(BitcoinMainNetParams.get(), Language.US,
                    "all all all all all all all all all all all all bogus", null, 1);
        } catch (Exception e) {
            ;
        } finally {
            Assert.assertNull(wallet);
        }
    }

    @Test
    public void testRestoreWallet_seedHex() {

        String hexSeed = "0660cc198330660cc198330660cc1983";
        String passphrase = "myPassPhrase";
        int accountListSize = 4;

        HDWallet wallet = null;

        try {
            wallet = HDWalletFactory
                .restoreWallet(BitcoinMainNetParams.get(), Language.US, hexSeed, passphrase,
                    accountListSize);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertNotNull(wallet);
        Assert.assertEquals(hexSeed, wallet.getSeedHex());
        Assert.assertEquals(accountListSize, wallet.getAccounts().size());
        Assert.assertEquals(passphrase, wallet.getPassphrase());
    }

    @Test
    public void testRestoredWallet_addressChains_withSamePassphrase_shouldBeSame() {

        HDWallet restoredWallet1 = null;
        HDWallet restoredWallet2 = null;

        String passphrase1 = "passphrase1";

        try {
            restoredWallet1 = HDWalletFactory
                .restoreWallet(BitcoinMainNetParams.get(), Language.US,
                    "all all all all all all all all all all all all", passphrase1, 1);
            restoredWallet2 = HDWalletFactory
                .restoreWallet(BitcoinMainNetParams.get(), Language.US,
                    "all all all all all all all all all all all all", passphrase1, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
            restoredWallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString(),
            restoredWallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString());

        Assert.assertEquals(
            restoredWallet2.getAccount(0).getChange().getAddressAt(0).getAddressString(),
            restoredWallet1.getAccount(0).getChange().getAddressAt(0).getAddressString());
    }

    @Test
    public void testRestoredWallet_addressChains_withDifferentPassphrase_shouldBeDifferent() {

        HDWallet wallet1 = null;
        HDWallet wallet2 = null;

        String passphrase1 = "passphrase1";
        String passphrase2 = "passphrase2";

        try {
            wallet1 = HDWalletFactory
                .restoreWallet(BitcoinMainNetParams.get(), Language.US,
                    "all all all all all all all all all all all all", passphrase1, 1);
            wallet2 = HDWalletFactory
                .restoreWallet(BitcoinMainNetParams.get(), Language.US,
                    "all all all all all all all all all all all all", passphrase2, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertNotEquals(
            wallet2.getAccount(0).getReceive().getAddressAt(0).getAddressString(),
            wallet1.getAccount(0).getReceive().getAddressAt(0).getAddressString());

        Assert.assertNotEquals(
            wallet2.getAccount(0).getChange().getAddressAt(0).getAddressString(),
            wallet1.getAccount(0).getChange().getAddressAt(0).getAddressString());
    }

    @Test
    public void testAccount() throws AddressFormatException {
        HDAccount account = new HDAccount(BitcoinMainNetParams.get(),
            "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV",
            1);
        Assert.assertEquals(
            "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV",
            account.getXpub());
        Assert.assertEquals(1, account.getId());

        account = new HDAccount(BitcoinMainNetParams.get(),
            "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV");
        Assert.assertEquals(
            "xpub6CbTPgFYkRqMQZiX2WYEiVHWGJUjAsZAvSvMq3z52KczYQrZPQ9DjKwHQBmAMJVY3kLeBQ4T818MBf2cTiGkJSkmS8CDT1Wp7Dw4vFMygEV",
            account.getXpub());
        Assert.assertEquals(0, account.getId());
    }
}
