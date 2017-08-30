package org.scaladebugger.docs.layouts

import java.net.URL

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.layouts.partials.common.vendor._
import org.scaladebugger.docs.styles.{PageStyle, TabsStyle, TopbarNavStyle}
import org.senkbeil.grus.layouts.{Context, Page}

import scalatags.Text.all._
import org.scaladebugger.docs.styles.Implicits._

import scalatags.Text

/**
 * Represents the layout for a common site page.
 *
 * @param selectedMenuItems Will mark each menu item whose name is provided
 *                          as selected
 * @param syntaxHighlightTheme The theme to use for syntax highlighting; themes
 *                             are from the highlight.js list
 */
abstract class SitePage(
  val selectedMenuItems: Seq[String] = Nil,
  val syntaxHighlightTheme: String = "agate"
) extends Page {
  private val pixelId: String = "1622567071086905"

  override protected def preHeadContent(context: Context): Seq[Modifier] =
    super.preHeadContent(context) ++ Seq(
      FontAwesomeCSS(),
      HighlightCSS(theme = syntaxHighlightTheme),
      PageStyle.styleSheetText.toStyleTag,
      TopbarNavStyle.styleSheetText.toStyleTag,
      TabsStyle.styleSheetText.toStyleTag
    )

  override protected def preBodyContent(context: Context): Seq[Modifier] =
    super.preBodyContent(context) ++ Seq(
      Header(
        MainMenuBar(
          MainMenuLogo(),
          MainMenu(context.mainMenuItems: _*)
        )
      )
    )

  override protected def postHeadContent(context: Context): Seq[Text.all.Modifier] =
    super.postHeadContent(context) ++ Seq(
      script(
        raw(s"""
           |  !function(f,b,e,v,n,t,s)
           |  {if(f.fbq)return;n=f.fbq=function(){n.callMethod?
           |  n.callMethod.apply(n,arguments):n.queue.push(arguments)};
           |  if(!f._fbq)f._fbq=n;n.push=n;n.loaded=!0;n.version='2.0';
           |  n.queue=[];t=b.createElement(e);t.async=!0;
           |  t.src=v;s=b.getElementsByTagName(e)[0];
           |  s.parentNode.insertBefore(t,s)}(window, document,'script',
           |  'https://connect.facebook.net/en_US/fbevents.js');
           |  fbq('init', '$pixelId');
           |  fbq('track', 'PageView');
        """.stripMargin)
      ),
      tag("noscript")(img(
        height := "1",
        width := "1",
        style := "display: none;",
        src := s"https://www.facebook.com/tr?id=$pixelId&ev=PageView&noscript=1"
      )())
    )

  override protected def postBodyContent(context: Context): Seq[Modifier] =
    Seq(
      Footer(
        authorName = "Chip Senkbeil",
        authorUrl = new URL("https://chipsenkbeil.com/"),
        startYear = 2015
      ),
      ClipboardJS(),
      HighlightJS(),
      ClipboardJSInit(),
      HighlightJSInit()
    ) ++ super.postBodyContent(context)
}
