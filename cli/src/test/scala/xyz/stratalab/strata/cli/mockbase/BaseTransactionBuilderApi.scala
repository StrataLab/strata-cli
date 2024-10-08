package xyz.stratalab.strata.cli.mockbase

import xyz.stratalab.sdk.builders.BuilderError
import xyz.stratalab.sdk.builders.TransactionBuilderApi
import xyz.stratalab.sdk.models.Datum
import xyz.stratalab.sdk.models.Event.{GroupPolicy, SeriesPolicy}
import xyz.stratalab.sdk.models.GroupId
import xyz.stratalab.sdk.models.LockAddress
import xyz.stratalab.sdk.models.SeriesId
import xyz.stratalab.sdk.models.box.AssetMintingStatement
import xyz.stratalab.sdk.models.box.Attestation
import xyz.stratalab.sdk.models.box.FungibilityType
import xyz.stratalab.sdk.models.box.Lock
import xyz.stratalab.sdk.models.box.QuantityDescriptorType
import xyz.stratalab.sdk.models.transaction.IoTransaction
import xyz.stratalab.sdk.models.transaction.UnspentTransactionOutput
import xyz.stratalab.sdk.syntax.ValueTypeIdentifier
import xyz.stratalab.indexer.services.Txo
import com.google.protobuf.ByteString
import com.google.protobuf.struct.Struct
import quivr.models.Int128
import xyz.stratalab.sdk.models.TransactionOutputAddress

class BaseTransactionBuilderApi[F[_]] extends TransactionBuilderApi[F] {

  override def buildTransferAllTransaction(
      txos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      recipientLockAddress: LockAddress,
      changeLockAddress: LockAddress,
      fee: Long,
      tokenIdentifier: Option[ValueTypeIdentifier]
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildTransferAmountTransaction(
      tokenIdentifier: ValueTypeIdentifier,
      txos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      amount: Long,
      recipientLockAddress: LockAddress,
      changeLockAddress: LockAddress,
      fee: Long
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def groupOutput(
      lockAddress: LockAddress,
      quantity: Int128,
      groupId: GroupId,
      fixedSeries: Option[SeriesId]
  ): F[UnspentTransactionOutput] = ???

  override def seriesOutput(
      lockAddress: LockAddress,
      quantity: Int128,
      seriesId: SeriesId,
      tokenSupply: Option[Int],
      fungibility: FungibilityType,
      quantityDescriptor: QuantityDescriptorType
  ): F[UnspentTransactionOutput] = ???

  override def assetOutput(
      lockAddress: LockAddress,
      quantity: Int128,
      groupId: GroupId,
      seriesId: SeriesId,
      fungibilityType: FungibilityType,
      quantityDescriptorType: QuantityDescriptorType,
      metadata: Option[Struct],
      commitment: Option[ByteString]
  ): F[UnspentTransactionOutput] = ???

  override def buildGroupMintingTransaction(
      txos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      groupPolicy: GroupPolicy,
      quantityToMint: Long,
      mintedAddress: LockAddress,
      changeAddress: LockAddress,
      fee: Long
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildSeriesMintingTransaction(
      txos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      seriesPolicy: SeriesPolicy,
      quantityToMint: Long,
      mintedAddress: LockAddress,
      changeAddress: LockAddress,
      fee: Long
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildAssetMintingTransaction(
      mintingStatement: AssetMintingStatement,
      txos: Seq[Txo],
      locks: Map[LockAddress, Lock.Predicate],
      fee: Long,
      mintedAssetLockAddress: LockAddress,
      changeAddress: LockAddress,
      ephemeralMetadata: Option[Struct],
      commitment: Option[ByteString]
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def buildAssetMergeTransaction(
      utxosToMerge: Seq[TransactionOutputAddress],
      txos: Seq[Txo],
      locks: Map[LockAddress, Lock.Predicate],
      fee: Long,
      mergedAssetLockAddress: LockAddress,
      changeAddress: LockAddress,
      ephemeralMetadata: Option[Struct],
      commitment: Option[ByteString]
  ): F[Either[BuilderError, IoTransaction]] = ???

  override def unprovenAttestation(
      lockPredicate: Lock.Predicate
  ): F[Attestation] = ???

  override def lockAddress(lock: Lock): F[LockAddress] = ???

  override def lvlOutput(
      predicate: Lock.Predicate,
      amount: Int128
  ): F[UnspentTransactionOutput] = ???

  override def lvlOutput(
      lockAddress: LockAddress,
      amount: Int128
  ): F[UnspentTransactionOutput] = ???

  override def datum(): F[Datum.IoTransaction] = ???

  override def buildSimpleLvlTransaction(
      lvlTxos: Seq[Txo],
      lockPredicateFrom: Lock.Predicate,
      lockPredicateForChange: Lock.Predicate,
      recipientLockAddress: LockAddress,
      amount: Long
  ): F[IoTransaction] = ???

}
