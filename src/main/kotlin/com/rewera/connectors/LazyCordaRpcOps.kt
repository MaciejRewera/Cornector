package com.rewera.connectors

import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.concurrent.CordaFuture
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StateMachineRunId
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.node.NetworkParameters
import net.corda.core.node.NodeDiagnosticInfo
import net.corda.core.node.NodeInfo
import net.corda.core.node.services.AttachmentId
import net.corda.core.node.services.NetworkMapCache
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.*
import net.corda.core.transactions.SignedTransaction
import java.io.InputStream
import java.security.PublicKey
import java.time.Instant

class LazyCordaRpcOps(rpcConnection: () -> CordaRPCConnection) : CordaRPCOps {

    private val rpcOps by lazy { rpcConnection().proxy }

    override val networkParameters: NetworkParameters
        get() = rpcOps.networkParameters

    override val protocolVersion: Int
        get() = rpcOps.protocolVersion

    override fun acceptNewNetworkParameters(parametersHash: SecureHash) =
        rpcOps.acceptNewNetworkParameters(parametersHash)

    override fun addVaultTransactionNote(txnId: SecureHash, txnNote: String) =
        rpcOps.addVaultTransactionNote(txnId, txnNote)

    override fun attachmentExists(id: SecureHash): Boolean = rpcOps.attachmentExists(id)

    override fun clearNetworkMapCache() = rpcOps.clearNetworkMapCache()

    override fun currentNodeTime(): Instant = rpcOps.currentNodeTime()

    override fun finishedFlowsWithClientIds(): Map<String, Boolean> = rpcOps.finishedFlowsWithClientIds()

    override fun finishedFlowsWithClientIdsAsAdmin(): Map<String, Boolean> = rpcOps.finishedFlowsWithClientIdsAsAdmin()

    override fun getVaultTransactionNotes(txnId: SecureHash): Iterable<String> = rpcOps.getVaultTransactionNotes(txnId)

    @Deprecated("Overrides deprecated member in 'net.corda.core.messaging.CordaRPCOps'.")
    override fun internalFindVerifiedTransaction(txnId: SecureHash): SignedTransaction? =
        rpcOps.internalFindVerifiedTransaction(txnId)

    @Deprecated("Overrides deprecated member in 'net.corda.core.messaging.CordaRPCOps'.")
    override fun internalVerifiedTransactionsFeed(): DataFeed<List<SignedTransaction>, SignedTransaction> =
        rpcOps.internalVerifiedTransactionsFeed()

    @Deprecated("Overrides deprecated member in 'net.corda.core.messaging.CordaRPCOps'.")
    override fun internalVerifiedTransactionsSnapshot(): List<SignedTransaction> =
        rpcOps.internalVerifiedTransactionsSnapshot()

    override fun isFlowsDrainingModeEnabled(): Boolean = rpcOps.isFlowsDrainingModeEnabled()

    override fun isWaitingForShutdown(): Boolean = rpcOps.isWaitingForShutdown()

    override fun killFlow(id: StateMachineRunId): Boolean = rpcOps.killFlow(id)

    override fun networkMapFeed(): DataFeed<List<NodeInfo>, NetworkMapCache.MapChange> = rpcOps.networkMapFeed()

    override fun networkMapSnapshot(): List<NodeInfo> = rpcOps.networkMapSnapshot()

    override fun networkParametersFeed(): DataFeed<ParametersUpdateInfo?, ParametersUpdateInfo> =
        rpcOps.networkParametersFeed()

    override fun nodeDiagnosticInfo(): NodeDiagnosticInfo = rpcOps.nodeDiagnosticInfo()

    override fun nodeInfo(): NodeInfo = rpcOps.nodeInfo()

    override fun nodeInfoFromParty(party: AbstractParty): NodeInfo? = nodeInfoFromParty(party)

    override fun notaryIdentities(): List<Party> = rpcOps.notaryIdentities()

    override fun notaryPartyFromX500Name(x500Name: CordaX500Name): Party? = rpcOps.notaryPartyFromX500Name(x500Name)

    override fun openAttachment(id: SecureHash): InputStream = rpcOps.openAttachment(id)

    override fun partiesFromName(query: String, exactMatch: Boolean): Set<Party> =
        rpcOps.partiesFromName(query, exactMatch)

    override fun partyFromKey(key: PublicKey): Party? = rpcOps.partyFromKey(key)

    override fun queryAttachments(query: AttachmentQueryCriteria, sorting: AttachmentSort?): List<AttachmentId> =
        rpcOps.queryAttachments(query, sorting)

    override fun <T> reattachFlowWithClientId(clientId: String): FlowHandleWithClientId<T>? =
        rpcOps.reattachFlowWithClientId(clientId)

    override fun refreshNetworkMapCache() = rpcOps.refreshNetworkMapCache()

    override fun registeredFlows(): List<String> = rpcOps.registeredFlows()

    override fun removeClientId(clientId: String): Boolean = rpcOps.removeClientId(clientId)

    override fun removeClientIdAsAdmin(clientId: String): Boolean = rpcOps.removeClientIdAsAdmin(clientId)

    override fun setFlowsDrainingModeEnabled(enabled: Boolean) = rpcOps.setFlowsDrainingModeEnabled(enabled)

    override fun shutdown() = rpcOps.shutdown()

    override fun <T> startFlowDynamic(logicType: Class<out FlowLogic<T>>, vararg args: Any?): FlowHandle<T> =
        rpcOps.startFlowDynamic(logicType, *args)

    override fun <T> startFlowDynamicWithClientId(
        clientId: String,
        logicType: Class<out FlowLogic<T>>,
        vararg args: Any?
    ): FlowHandleWithClientId<T> = rpcOps.startFlowDynamicWithClientId(clientId, logicType, *args)

    override fun <T> startTrackedFlowDynamic(
        logicType: Class<out FlowLogic<T>>,
        vararg args: Any?
    ): FlowProgressHandle<T> = rpcOps.startTrackedFlowDynamic(logicType, *args)

    override fun stateMachineRecordedTransactionMappingFeed(): DataFeed<List<StateMachineTransactionMapping>, StateMachineTransactionMapping> =
        rpcOps.stateMachineRecordedTransactionMappingFeed()

    override fun stateMachineRecordedTransactionMappingSnapshot(): List<StateMachineTransactionMapping> =
        rpcOps.stateMachineRecordedTransactionMappingSnapshot()

    override fun stateMachinesFeed(): DataFeed<List<StateMachineInfo>, StateMachineUpdate> = rpcOps.stateMachinesFeed()

    override fun stateMachinesSnapshot(): List<StateMachineInfo> = rpcOps.stateMachinesSnapshot()

    override fun terminate(drainPendingFlows: Boolean) = rpcOps.terminate(drainPendingFlows)

    override fun uploadAttachment(jar: InputStream): SecureHash = rpcOps.uploadAttachment(jar)

    override fun uploadAttachmentWithMetadata(jar: InputStream, uploader: String, filename: String): SecureHash =
        rpcOps.uploadAttachmentWithMetadata(jar, uploader, filename)

    override fun <T : ContractState> vaultQuery(contractStateType: Class<out T>): Vault.Page<T> =
        rpcOps.vaultQuery(contractStateType)

    override fun <T : ContractState> vaultQueryBy(
        criteria: QueryCriteria,
        paging: PageSpecification,
        sorting: Sort,
        contractStateType: Class<out T>
    ): Vault.Page<T> = rpcOps.vaultQueryBy(criteria, paging, sorting, contractStateType)

    override fun <T : ContractState> vaultQueryByCriteria(
        criteria: QueryCriteria,
        contractStateType: Class<out T>
    ): Vault.Page<T> = rpcOps.vaultQueryByCriteria(criteria, contractStateType)

    override fun <T : ContractState> vaultQueryByWithPagingSpec(
        contractStateType: Class<out T>,
        criteria: QueryCriteria,
        paging: PageSpecification
    ): Vault.Page<T> = rpcOps.vaultQueryByWithPagingSpec(contractStateType, criteria, paging)

    override fun <T : ContractState> vaultQueryByWithSorting(
        contractStateType: Class<out T>,
        criteria: QueryCriteria,
        sorting: Sort
    ): Vault.Page<T> = rpcOps.vaultQueryByWithSorting(contractStateType, criteria, sorting)

    override fun <T : ContractState> vaultTrack(contractStateType: Class<out T>): DataFeed<Vault.Page<T>, Vault.Update<T>> =
        rpcOps.vaultTrack(contractStateType)

    override fun <T : ContractState> vaultTrackBy(
        criteria: QueryCriteria,
        paging: PageSpecification,
        sorting: Sort,
        contractStateType: Class<out T>
    ): DataFeed<Vault.Page<T>, Vault.Update<T>> = rpcOps.vaultTrackBy(criteria, paging, sorting, contractStateType)

    override fun <T : ContractState> vaultTrackByCriteria(
        contractStateType: Class<out T>,
        criteria: QueryCriteria
    ): DataFeed<Vault.Page<T>, Vault.Update<T>> = rpcOps.vaultTrackByCriteria(contractStateType, criteria)

    override fun <T : ContractState> vaultTrackByWithPagingSpec(
        contractStateType: Class<out T>,
        criteria: QueryCriteria,
        paging: PageSpecification
    ): DataFeed<Vault.Page<T>, Vault.Update<T>> = rpcOps.vaultTrackByWithPagingSpec(contractStateType, criteria, paging)

    override fun <T : ContractState> vaultTrackByWithSorting(
        contractStateType: Class<out T>,
        criteria: QueryCriteria,
        sorting: Sort
    ): DataFeed<Vault.Page<T>, Vault.Update<T>> = rpcOps.vaultTrackByWithSorting(contractStateType, criteria, sorting)

    override fun waitUntilNetworkReady(): CordaFuture<Void?> = rpcOps.waitUntilNetworkReady()

    override fun wellKnownPartyFromAnonymous(party: AbstractParty): Party? = rpcOps.wellKnownPartyFromAnonymous(party)

    override fun wellKnownPartyFromX500Name(x500Name: CordaX500Name): Party? =
        rpcOps.wellKnownPartyFromX500Name(x500Name)
}
