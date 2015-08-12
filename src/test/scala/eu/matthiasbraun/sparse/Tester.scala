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

  def testLineAndLineNrPredicate() {
    val file = fromURL(getClass.getResource("/testFile.txt"))

    val start = after((line, lineNr) => line == "###" && lineNr > 4)
    val end = to((line, lineNr) => line == "~~~" && lineNr > 4)

    val blocksMaybe = parse(file, start, end)

    blocksMaybe match {
      case Success(blocks)    => blocks.foreach { println }
      case Failure(exception) => println(exception)
    }
  }
  def testBefore() {
    val file = fromURL(getClass.getResource("/testFile.txt"))


    val blocksMaybe = parse(file, before("###"), until("~~~"))

    blocksMaybe match {
      case Success(blocks)    => blocks.foreach { println }
      case Failure(exception) => println(exception)
    }
  }

  testBefore()
  testLineAndLineNrPredicate()
}