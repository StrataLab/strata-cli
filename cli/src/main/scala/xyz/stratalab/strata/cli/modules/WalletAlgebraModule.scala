package xyz.stratalab.strata.cli.modules

import xyz.stratalab.strata.cli.impl.WalletAlgebra

trait WalletAlgebraModule
    extends WalletStateAlgebraModule
    with WalletApiModule {
  def walletAlgebra(file: String) = WalletAlgebra.make(
    walletApi,
    walletStateAlgebra(file)
  )
}
