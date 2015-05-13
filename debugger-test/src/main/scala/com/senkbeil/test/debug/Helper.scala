package com.senkbeil.test.debug

/**
 * Created by senkwich on 5/12/15.
 */
object Helper {
  def noop(a: Any): Unit = {}
  def ret[T](a: T): T = a
}
