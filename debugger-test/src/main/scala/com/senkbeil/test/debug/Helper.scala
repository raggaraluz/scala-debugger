package com.senkbeil.test.debug

object Helper {

  def noop(a: Any) {
  }

  def ret[B](a: B): B = {
    a
  }

}