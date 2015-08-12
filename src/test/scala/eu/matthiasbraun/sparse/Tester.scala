package eu.matthiasbraun.sparse

import java.io.File
import scala.util.Failure
import scala.util.Success
import scala.io.Source._
import eu.matthiasbraun.sparse.Parser._
/**
 * @author Matthias Braun
 */
object Tester extends App {

  def testFunc() {
    val file = fromURL(getClass.getResource("/testFile.txt"))

    val start = after((line, lineNr) => line == "###" && lineNr > 3)
    val end = to((line, lineNr) => line == "~~~" && lineNr > 3)

    val blocksMaybe = parse(file, start, end)

    blocksMaybe match {
      case Success(blocks)    => blocks.foreach { println }
      case Failure(exception) => println(exception)
    }
  }

  testFunc()
}