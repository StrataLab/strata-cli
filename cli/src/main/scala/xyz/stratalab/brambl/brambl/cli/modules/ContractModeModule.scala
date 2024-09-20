package xyz.stratalab.brambl.cli.modules

import cats.effect.IO
import xyz.stratalab.brambl.cli.controllers.TemplatesController
import co.topl.brambl.servicekit.{TemplateStorageApi, WalletStateResource}
import xyz.stratalab.brambl.cli.BramblCliSubCmd
import xyz.stratalab.brambl.cli.BramblCliParams
import scopt.OParser
import xyz.stratalab.brambl.cli.BramblCliParamsParserModule

trait TemplateModeModule extends WalletStateResource {
  def templateModeSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = {
    val templateStorageAlgebra = TemplateStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case BramblCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              BramblCliParamsParserModule.templatesMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case BramblCliSubCmd.list =>
        new TemplatesController(
          templateStorageAlgebra
        )
          .listTemplates()
      case BramblCliSubCmd.add =>
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
