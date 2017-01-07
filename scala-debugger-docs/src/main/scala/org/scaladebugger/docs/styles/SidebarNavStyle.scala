package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for the side bar.
 */
object SidebarNavStyle extends CascadingStyleSheet {
  import scalatags.Text.styles2.{content => pseudoContent}

  lazy val navbar: Cls = cls(
    display := "inline-block",

    fontFamily := "'Helvetica Neue', Helvetica, Arial, sans-serif",
    background := "#D8D8D8",
    color := "#696969",

    width := "16em",

    a(
      textDecoration := "none",
      color := "#696969"
    )
  )

  lazy val navLinks: Cls = cls(
    maxHeight := "100%",
    overflowY := "auto",
    padding := "0.5em",
    listStyleType := "none",

    ul(
      paddingLeft := "0em",
      listStyleType := "none",
      navLink(
        fontSize := "1.1em",
        textTransform := "uppercase",
        &.pseudoExtend(":before")(
          pseudoContent := "':: '"
        ),
        &.pseudoExtend(":after")(
          pseudoContent := "' ::'"
        )
      ),
      li(
        margin := "1.5em 0em",
        ul(
          paddingLeft := "0.6125em",
          navLink(
            fontSize := "1em",
            textTransform := "capitalize",
            &.pseudoExtend(":before")(
              pseudoContent := "'** '"
            ),
            &.pseudoExtend(":after")(
              pseudoContent := "''"
            )
          ),
          li(
            margin := "1em 0em",
            ul(
              paddingLeft := "1.25em",
              navLink(
                fontSize := "0.9em",
                textTransform := "none",
                &.pseudoExtend(":before")(
                  pseudoContent := "'... '"
                ),
                &.pseudoExtend(":after")(
                  pseudoContent := "''"
                )
              )
            )
          )
        )
      )
    ),

    a(
      textDecoration := "none",
      color := "#696969"
    )
  )

  lazy val navLink: Cls = cls(
    display := "inline-block",
    width := "100%",
    &.hover(
      background := "#3B3E43",
      color := "#D8D8D8"
    )
  )

  lazy val selectedNavLink: Cls = cls(
    padding := "0em",
    background := "#EBF0F1",
    borderRadius := "8px",

    a(
      color := "#3B3E43",
      border := "initial"
    )
  )

  lazy val summary: Cls = cls(
    display := "block",
    cursor := "pointer",

    &.pseudoExtend(":-webkit-details-marker")(
      display := "none"
    )
  )
}