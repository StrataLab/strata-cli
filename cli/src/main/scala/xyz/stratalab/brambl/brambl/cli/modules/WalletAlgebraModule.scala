package xyz.stratalab.brambl.cli.modules

import xyz.stratalab.brambl.cli.impl.WalletAlgebra

trait WalletAlgebraModule
    extends WalletStateAlgebraModule
    with WalletApiModule {
  def walletAlgebra(file: String) = WalletAlgebra.make(
    walletApi,
    walletStateAlgebra(file)
  )
}
