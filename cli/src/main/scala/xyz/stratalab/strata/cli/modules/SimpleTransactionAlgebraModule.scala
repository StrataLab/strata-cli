package xyz.stratalab.strata.cli.modules

import co.topl.brambl.constants.NetworkConstants
import cats.effect.IO
import xyz.stratalab.strata.cli.impl.SimpleTransactionAlgebra

trait SimpleTransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with GenusQueryAlgebraModule {

  def simplTransactionOps(
    walletFile:       String,
    networkId:        Int,
    host:             String,
    bifrostPort:      Int,
    secureConnection: Boolean
  ) = SimpleTransactionAlgebra
    .make[IO](
      walletApi,
      walletStateAlgebra(walletFile),
      genusQueryAlgebra(
        host,
        bifrostPort,
        secureConnection
      ),
      transactionBuilderApi(
        networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      walletManagementUtils
    )
}
