package xyz.stratalab.brambl.cli

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import xyz.stratalab.brambl.cli.modules.BifrostQueryModeModule
import xyz.stratalab.brambl.cli.modules.TemplateModeModule
import xyz.stratalab.brambl.cli.modules.GenusQueryModeModule
import xyz.stratalab.brambl.cli.modules.FellowshipsModeModule
import xyz.stratalab.brambl.cli.modules.SimpleTransactionModeModule
import xyz.stratalab.brambl.cli.modules.TxModeModule
import xyz.stratalab.brambl.cli.modules.WalletModeModule
import scopt.OParser
import xyz.stratalab.brambl.cli.modules.SimpleMintingModeModule
import xyz.stratalab.brambl.cli.modules.ServerModule

object Main
    extends IOApp
    with GenusQueryModeModule
    with BifrostQueryModeModule
    with TemplateModeModule
    with FellowshipsModeModule
    with WalletModeModule
    with SimpleTransactionModeModule
    with TxModeModule
    with SimpleMintingModeModule
    with ServerModule {

  import BramblCliParamsParserModule._

  override def run(args: List[String]): IO[ExitCode] = {
    OParser.runParser(paramParser, args, BramblCliParams()) match {
      case (Some(params), effects) =>
        val op: IO[Either[String, String]] =
          params.mode match {
            case BramblCliMode.tx =>
              txModeSubcmds(params)
            case BramblCliMode.templates =>
              templateModeSubcmds(params)
            case BramblCliMode.fellowships =>
              fellowshipsModeSubcmds(params)
            case BramblCliMode.wallet =>
              walletModeSubcmds(params)
            case BramblCliMode.simpletransaction =>
              simpleTransactionSubcmds(params)
            case BramblCliMode.simpleminting =>
              simpleMintingSubcmds(params)
            case BramblCliMode.genusquery =>
              genusQuerySubcmd(params)
            case BramblCliMode.bifrostquery =>
              bifrostQuerySubcmd(params)
            case BramblCliMode.server =>
              serverSubcmd(params)
            case _ =>
              IO(OParser.runEffects(effects)) >> IO.pure(Left("Invalid mode"))
          }
        import cats.implicits._
        for {
          output <- op
          res <- output.fold(
            x => IO.consoleForIO.errorln(x).map(_ => ExitCode.Error),
            x => IO.consoleForIO.println(x).map(_ => ExitCode.Success)
          )
        } yield res
      case (None, effects) =>
        IO(OParser.runEffects(effects.reverse.tail.reverse)) >> IO.pure(
          ExitCode.Error
        )
    }
  }

}
