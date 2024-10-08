package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import co.topl.brambl.servicekit.{TemplateStorageApi, WalletStateResource}
import scopt.OParser
import xyz.stratalab.strata.cli.controllers.TemplatesController
import xyz.stratalab.strata.cli.{StrataCliParams, StrataCliParamsParserModule, StrataCliSubCmd}

trait TemplateModeModule extends WalletStateResource {

  def templateModeSubcmds(
    validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val templateStorageAlgebra = TemplateStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.templatesMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.list =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .listTemplates()
      case StrataCliSubCmd.add =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .addTemplate(
            validateParams.templateName,
            validateParams.lockTemplate
          )
    }
  }
}
