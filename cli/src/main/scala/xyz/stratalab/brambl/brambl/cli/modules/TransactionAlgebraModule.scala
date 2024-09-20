package xyz.stratalab.brambl.cli.modules

import cats.effect.IO
import xyz.stratalab.brambl.cli.impl.TransactionAlgebra
import co.topl.brambl.dataApi.RpcChannelResource

trait TransactionAlgebraModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with RpcChannelResource {
      
  def transactionOps(
      walletFile: String,
      host: String,
      port: Int,
      secureConnection: Boolean
  ) = TransactionAlgebra
    .make[IO](
      walletApi,
      walletStateAlgebra(walletFile),
      walletManagementUtils,
      channelResource(
        host,
        port,
        secureConnection
      )
    )
}
