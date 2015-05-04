package com.senkbeil.test.stepping

import com.senkbeil.test.debug.Helper._

object ForComprehensionListString2 {

  def main(args: Array[String]) {

    val l = List("un")

    for (n <- l) {
      n.size
    }

    noop(None)
  }
}

class ForComprehensionListString2 {

}