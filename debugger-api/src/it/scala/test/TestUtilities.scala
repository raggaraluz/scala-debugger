package test

/**
 * Created by senkwich on 5/9/15.
 */
trait TestUtilities {

  def scalaClassStringToFileString(classString: String) =
    classString.replace('.', java.io.File.separatorChar) + ".scala"
}
