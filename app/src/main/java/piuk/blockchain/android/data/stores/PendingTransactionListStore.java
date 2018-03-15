package piuk.blockchain.android.data.stores;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import piuk.blockchain.android.data.contacts.models.ContactTransactionModel;

@Singleton
public class PendingTransactionListStore extends ListStore<ContactTransactionModel> {

    @Inject
    public PendingTransactionListStore() {
        // Empty constructor
    }

    public void insertTransaction(ContactTransactionModel transaction) {
        insertObjectIntoList(transaction);
    }

    public void insertTransactions(List<ContactTransactionModel> transactions) {
        insertBulk(transactions);
    }

    public void removeTransaction(ContactTransactionModel object) {
        removeObjectFromList(object);
    }
}

