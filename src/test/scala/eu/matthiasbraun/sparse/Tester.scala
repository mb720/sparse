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

    /** Gets the line numbers where the line matches the `start` or `end` of a text block inside the `lineStream` as pairs. */
    println("sparse test started")
    val home = System.getProperty("user.home")
    val journalDir = s"$home/workspace/scala/Sparse"
    val journalPath = s"$journalDir/journal_for_scala_testing.txt"
    val file = fromFile(new File(journalPath))

    val start = after((line, lineNr) => line == "###" && lineNr > 10)
    val end = to((line, lineNr) => line == "~~~" && lineNr > 10)

    val blocksMaybe = parse(file, start, end)

    blocksMaybe match {
      case Success(blocks)    => blocks.foreach { println }
      case Failure(exception) => println(exception)
    }
  }

  testFunc()
}