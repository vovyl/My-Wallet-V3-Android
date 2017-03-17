package piuk.blockchain.android.data.datamanagers

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.wallet.contacts.data.Contact
import info.blockchain.wallet.contacts.data.FacilitatedTransaction
import info.blockchain.wallet.contacts.data.PaymentRequest
import info.blockchain.wallet.contacts.data.RequestForPaymentRequest
import info.blockchain.wallet.metadata.MetadataNodeFactory
import info.blockchain.wallet.metadata.data.Message
import info.blockchain.wallet.payload.PayloadManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.bitcoinj.crypto.DeterministicKey
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.RxTest
import piuk.blockchain.android.data.contacts.ContactTransactionModel
import piuk.blockchain.android.data.services.ContactsService
import piuk.blockchain.android.data.stores.PendingTransactionListStore
import piuk.blockchain.android.equals

class ContactsDataManagerTest : RxTest() {

    private lateinit var subject: ContactsDataManager
    private val mockContactsService: ContactsService = mock()
    private val mockPayloadManager: PayloadManager = mock()
    private val mockPendingTransactionListStore: PendingTransactionListStore = mock()

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        subject = ContactsDataManager(mockContactsService, mockPayloadManager, mockPendingTransactionListStore)
    }

    @Test
    @Throws(Exception::class)
    fun loadNodes() {
        // Arrange
        whenever(mockPayloadManager.loadNodes()).thenReturn(true)
        // Act
        val testObserver = subject.loadNodes().test()
        // Assert
        verify(mockPayloadManager).loadNodes()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals true
    }

    @Test
    @Throws(Exception::class)
    fun generateNodes() {
        // Arrange
        val secondPassword = "SECOND_PASSWORD"
        // Act
        val testObserver = subject.generateNodes(secondPassword).test()
        // Assert
        verify(mockPayloadManager).generateNodes(secondPassword)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun initContactsService() {
        // Arrange
        val mockMetadataNode: DeterministicKey = mock()
        val mockSharedMetadataNode: DeterministicKey = mock()
        whenever(mockContactsService.initContactsService(mockMetadataNode, mockSharedMetadataNode))
                .thenReturn(Completable.complete())
        // Act
        val testObserver = subject.initContactsService(mockMetadataNode, mockSharedMetadataNode).test()
        // Assert
        verify(mockContactsService).initContactsService(mockMetadataNode, mockSharedMetadataNode)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun getMetadataNodeFactory() {
        // Arrange
        val mockMetadataNodeFactory: MetadataNodeFactory = mock()
        whenever(mockPayloadManager.metadataNodeFactory).thenReturn(mockMetadataNodeFactory)
        // Act
        val testObserver = subject.metadataNodeFactory.test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals mockMetadataNodeFactory
    }

    @Test
    @Throws(Exception::class)
    fun registerMdid() {
        // Arrange
        val mockMetadataNodeFactory: MetadataNodeFactory = mock()
        val mockSharedMetadataNode: DeterministicKey = mock()
        whenever(mockMetadataNodeFactory.sharedMetadataNode).thenReturn(mockSharedMetadataNode)
        whenever(mockPayloadManager.metadataNodeFactory).thenReturn(mockMetadataNodeFactory)
        // Act
        val testObserver = subject.registerMdid().test()
        // Assert
        verify(mockPayloadManager).registerMdid(mockSharedMetadataNode)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun unregisterMdid() {
        // Arrange
        val mockMetadataNodeFactory: MetadataNodeFactory = mock()
        val mockSharedMetadataNode: DeterministicKey = mock()
        whenever(mockMetadataNodeFactory.sharedMetadataNode).thenReturn(mockSharedMetadataNode)
        whenever(mockPayloadManager.metadataNodeFactory).thenReturn(mockMetadataNodeFactory)
        // Act
        val testObserver = subject.unregisterMdid().test()
        // Assert
        verify(mockPayloadManager).unregisterMdid(mockSharedMetadataNode)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun fetchContacts() {
        // Arrange
        val contact0 = Contact()
        val contact1 = Contact()
        val contact2 = Contact()
        contact2.name = "Has completed transactions"
        val facilitatedTransaction0 = FacilitatedTransaction()
        val facilitatedTransaction1 = FacilitatedTransaction()
        facilitatedTransaction1.txHash = "TX_HASH"
        contact2.addFacilitatedTransaction(facilitatedTransaction0)
        contact2.addFacilitatedTransaction(facilitatedTransaction1)
        whenever(mockContactsService.fetchContacts()).thenReturn(Completable.complete())
        whenever(mockContactsService.contactList).thenReturn(
                Observable.fromIterable(listOf(contact0, contact1, contact2)))
        // Act
        val testObserver = subject.fetchContacts().test()
        // Assert
        verify(mockContactsService).fetchContacts()
        verify(mockContactsService).contactList
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        subject.contactsTransactionMap.size equals 1
        subject.contactsTransactionMap[facilitatedTransaction1.txHash] as String equals contact2.name
    }

    @Test
    @Throws(Exception::class)
    fun saveContacts() {
        // Arrange
        whenever(mockContactsService.saveContacts()).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.saveContacts().test()
        // Assert
        verify(mockContactsService).saveContacts()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun wipeContacts() {
        // Arrange
        whenever(mockContactsService.wipeContacts()).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.wipeContacts().test()
        // Assert
        verify(mockContactsService).wipeContacts()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun getContactList() {
        // Arrange
        val contact0 = Contact()
        val contact1 = Contact()
        val contact2 = Contact()
        whenever(mockContactsService.contactList).thenReturn(
                Observable.fromIterable(listOf(contact0, contact1, contact2)))
        // Act
        val testObserver = subject.contactList.toList().test()
        // Assert
        verify(mockContactsService).contactList
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0].size equals 3
    }

    @Test
    @Throws(Exception::class)
    fun getContactsWithUnreadPaymentRequests() {
        // Arrange
        val contact0 = Contact()
        val contact1 = Contact()
        val contact2 = Contact()
        whenever(mockContactsService.contactsWithUnreadPaymentRequests)
                .thenReturn(Observable.fromIterable(listOf(contact0, contact1, contact2)))
        // Act
        val testObserver = subject.contactsWithUnreadPaymentRequests.toList().test()
        // Assert
        verify(mockContactsService).contactsWithUnreadPaymentRequests
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0].size equals 3
    }

    @Test
    @Throws(Exception::class)
    fun addContact() {
        // Arrange
        val contact0 = Contact()
        whenever(mockContactsService.addContact(contact0)).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.addContact(contact0).test()
        // Assert
        verify(mockContactsService).addContact(contact0)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun removeContact() {
        // Arrange
        val contact0 = Contact()
        whenever(mockContactsService.removeContact(contact0)).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.removeContact(contact0).test()
        // Assert
        verify(mockContactsService).removeContact(contact0)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun renameContact() {
        // Arrange
        val contactId = "CONTACT ID"
        val contactName = "CONTACT_NAME"
        whenever(mockContactsService.renameContact(contactId, contactName))
                .thenReturn(Completable.complete())
        // Act
        val testObserver = subject.renameContact(contactId, contactName).test()
        // Assert
        verify(mockContactsService).renameContact(contactId, contactName)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun createInvitation() {
        // Arrange
        val sender = Contact()
        val recipient = Contact()
        whenever(mockContactsService.createInvitation(sender, recipient))
                .thenReturn(Observable.just(sender))
        // Act
        val testObserver = subject.createInvitation(sender, recipient).test()
        // Assert
        verify(mockContactsService).createInvitation(sender, recipient)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals sender
    }

    @Test
    @Throws(Exception::class)
    fun acceptInvitation() {
        // Arrange
        val invitationUrl = "INVITATION_URL"
        val sender = Contact()
        whenever(mockContactsService.acceptInvitation(invitationUrl))
                .thenReturn(Observable.just(sender))
        // Act
        val testObserver = subject.acceptInvitation(invitationUrl).test()
        // Assert
        verify(mockContactsService).acceptInvitation(invitationUrl)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals sender
    }

    @Test
    @Throws(Exception::class)
    fun readInvitationLink() {
        // Arrange
        val invitationUrl = "INVITATION_URL"
        val sender = Contact()
        whenever(mockContactsService.readInvitationLink(invitationUrl))
                .thenReturn(Observable.just(sender))
        // Act
        val testObserver = subject.readInvitationLink(invitationUrl).test()
        // Assert
        verify(mockContactsService).readInvitationLink(invitationUrl)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals sender
    }

    @Test
    @Throws(Exception::class)
    fun readInvitationSent() {
        // Arrange
        val recipient = Contact()
        whenever(mockContactsService.readInvitationSent(recipient))
                .thenReturn(Observable.just(true))
        // Act
        val testObserver = subject.readInvitationSent(recipient).test()
        // Assert
        verify(mockContactsService).readInvitationSent(recipient)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals true
    }

    @Test
    @Throws(Exception::class)
    fun requestSendPayment() {
        // Arrange
        val mdid = "MDID"
        val paymentRequest = PaymentRequest()
        whenever(mockContactsService.requestSendPayment(mdid, paymentRequest))
                .thenReturn(Completable.complete())
        // Act
        val testObserver = subject.requestSendPayment(mdid, paymentRequest).test()
        // Assert
        verify(mockContactsService).requestSendPayment(mdid, paymentRequest)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun requestReceivePayment() {
        // Arrange
        val mdid = "MDID"
        val paymentRequest = RequestForPaymentRequest()
        whenever(mockContactsService.requestReceivePayment(mdid, paymentRequest))
                .thenReturn(Completable.complete())
        // Act
        val testObserver = subject.requestReceivePayment(mdid, paymentRequest).test()
        // Assert
        verify(mockContactsService).requestReceivePayment(mdid, paymentRequest)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun sendPaymentRequestResponse() {
        // Arrange
        val mdid = "MDID"
        val paymentRequest = PaymentRequest()
        val fctxId = "FCTX_ID"
        whenever(mockContactsService.sendPaymentRequestResponse(mdid, paymentRequest, fctxId))
                .thenReturn(Completable.complete())
        // Act
        val testObserver = subject.sendPaymentRequestResponse(mdid, paymentRequest, fctxId).test()
        // Assert
        verify(mockContactsService).sendPaymentRequestResponse(mdid, paymentRequest, fctxId)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun sendPaymentBroadcasted() {
        // Arrange
        val mdid = "MDID"
        val txHash = "TX_HASH"
        val fctxId = "FCTX_ID"
        whenever(mockContactsService.sendPaymentBroadcasted(mdid, txHash, fctxId))
                .thenReturn(Completable.complete())
        // Act
        val testObserver = subject.sendPaymentBroadcasted(mdid, txHash, fctxId).test()
        // Assert
        verify(mockContactsService).sendPaymentBroadcasted(mdid, txHash, fctxId)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun fetchXpub() {
        // Arrange
        val mdid = "MDID"
        val xpub = "XPUB"
        whenever(mockContactsService.fetchXpub(mdid)).thenReturn(Observable.just(xpub))
        // Act
        val testObserver = subject.fetchXpub(mdid).test()
        // Assert
        verify(mockContactsService).fetchXpub(mdid)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals xpub
    }

    @Test
    @Throws(Exception::class)
    fun publishXpub() {
        // Arrange
        whenever(mockContactsService.publishXpub()).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.publishXpub().test()
        // Assert
        verify(mockContactsService).publishXpub()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun getMessages() {
        // Arrange
        val message0 = Message()
        val message1 = Message()
        val message2 = Message()
        val onlyNew = true
        whenever(mockContactsService.getMessages(onlyNew))
                .thenReturn(Observable.just(listOf(message0, message1, message2)))
        // Act
        val testObserver = subject.getMessages(onlyNew).test()
        // Assert
        verify(mockContactsService).getMessages(onlyNew)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0].size equals 3
    }

    @Test
    @Throws(Exception::class)
    fun readMessage() {
        // Arrange
        val messageId = "MESSAGE_ID"
        val message = Message()
        whenever(mockContactsService.readMessage(messageId)).thenReturn(Observable.just(message))
        // Act
        val testObserver = subject.readMessage(messageId).test()
        // Assert
        verify(mockContactsService).readMessage(messageId)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals message
    }

    @Test
    @Throws(Exception::class)
    fun markMessageAsRead() {
        // Arrange
        val messageId = "MESSAGE_ID"
        val markAsRead = true
        whenever(mockContactsService.markMessageAsRead(messageId, markAsRead))
                .thenReturn(Completable.complete())
        // Act
        val testObserver = subject.markMessageAsRead(messageId, markAsRead).test()
        // Assert
        verify(mockContactsService).markMessageAsRead(messageId, markAsRead)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun refreshFacilitatedTransactions() {
        // Arrange
        // Has completed transaction
        val contact0 = Contact()
        contact0.name = "contact0"
        val facilitatedTransaction0 = FacilitatedTransaction()
        facilitatedTransaction0.txHash = "TX_HASH"
        contact0.addFacilitatedTransaction(facilitatedTransaction0)
        // Has pending transaction, ie not completed
        val contact1 = Contact()
        contact1.name = "contact1"
        val facilitatedTransaction1 = FacilitatedTransaction()
        contact1.addFacilitatedTransaction(facilitatedTransaction1)
        // Has no transactions
        val contact2 = Contact()
        contact2.name = "contact2"
        whenever(mockContactsService.contactList).thenReturn(
                Observable.fromIterable(listOf(contact0, contact1, contact2)))
        // Act
        val testObserver = subject.refreshFacilitatedTransactions().toList().test()
        // Assert
        verify(mockContactsService).contactList
        verify(mockPendingTransactionListStore).insertTransaction(any<ContactTransactionModel>())
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0].size equals 1
        val contactTransactionModel = testObserver.values()[0][0]
        contactTransactionModel.contactName equals contact1.name
        contactTransactionModel.facilitatedTransaction equals facilitatedTransaction1
    }

    @Test
    @Throws(Exception::class)
    fun getFacilitatedTransactions() {
        // Arrange
        val contactTransactionModel0 = ContactTransactionModel("", mock<FacilitatedTransaction>())
        val contactTransactionModel1 = ContactTransactionModel("", mock<FacilitatedTransaction>())
        val contactTransactionModel2 = ContactTransactionModel("", mock<FacilitatedTransaction>())
        whenever(mockPendingTransactionListStore.list)
                .thenReturn(listOf(contactTransactionModel0, contactTransactionModel1, contactTransactionModel2))
        // Act
        val testObserver = subject.facilitatedTransactions.toList().test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0].size equals 3
    }

    @Test
    @Throws(Exception::class)
    fun getContactFromFctxId() {
        // Arrange
        val fctxId = "FCTX_ID"
        val facilitatedTransaction0 = FacilitatedTransaction()
        val facilitatedTransaction1 = FacilitatedTransaction()
        val facilitatedTransaction2 = FacilitatedTransaction()
        facilitatedTransaction2.id = fctxId
        val contact0 = Contact()
        contact0.addFacilitatedTransaction(facilitatedTransaction0)
        contact0.addFacilitatedTransaction(facilitatedTransaction1)
        contact0.addFacilitatedTransaction(facilitatedTransaction2)
        val contact1 = Contact()
        val contact2 = Contact()
        whenever(mockContactsService.contactList)
                .thenReturn(Observable.fromIterable(listOf(contact0, contact1, contact2)))
        // Act
        val testObserver = subject.getContactFromFctxId(fctxId).test()
        // Assert
        verify(mockContactsService).contactList
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.values()[0] equals contact0
    }

    @Test
    @Throws(Exception::class)
    fun deleteFacilitatedTransaction() {
        // Arrange
        val mdid = "MDID"
        val fctxId = "FCTX_ID"
        whenever(mockContactsService.deleteFacilitatedTransaction(mdid, fctxId)).thenReturn(Completable.complete())
        // Act
        val testObserver = subject.deleteFacilitatedTransaction(mdid, fctxId).test()
        // Assert
        verify(mockContactsService).deleteFacilitatedTransaction(mdid, fctxId)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
    }

    @Test
    @Throws(Exception::class)
    fun getContactsTransactionMap() {
        // Arrange

        // Act
        val result = subject.getContactsTransactionMap()
        // Assert
        result equals subject.contactsTransactionMap
    }

}