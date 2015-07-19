package eu.matthiasbraun.sparse

import resource._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Try
import scala.util.Failure
import scala.util.Success

/**
 * A parser for extracting text blocks from a source such as a file.
 * Created by Matthias Braun on 5/7/15.
 */
object Parser {

  /** Marks the start or end of a text block. The `linePredicate` and its `offset` define where in the text this marker is placed. */
  case class BlockMarker(linePredicate: ((String, Int) => Boolean), offset: Int = 0)

  /** Thrown when the text couldn't be properly parsed. */
  case class SparseException(msg: String) extends RuntimeException(msg)

  /** A bunch of lines are a text block. */
  case class TextBlock(lines: Vector[String]) {
    override def toString =
      lines mkString "\n"
  }

  /**
   * Inheritance allows use to reuse the constructor code defined in `MarkerFactory`'s apply methods.
   * Alternatively, we could have used implicit conversions to turn, for example, `from("start")` into `from(BlockMarker((anInt: Int, aString: String) => "start".equals"))`
   * and thus share constructors among `to`, `from`, and the rest. The drawback of these conversions is that target typing is not available,
   * requiring the user of this library to provide types explicitly, therefore having to write `from((_: String).startsWith("start"))` instead of `from(_.startsWith("start"))`.
   */
  trait MarkerFactory {
    def apply(line: String): BlockMarker = apply(line.equals _)

    def apply(linePredicate: (String => Boolean)): BlockMarker = apply((line, lineNr) => linePredicate(line))

    def apply(lineAndLineNrPredicate: ((String, Int) => Boolean)): BlockMarker
  }

  /**
   * When a line within the parsed text matches the `predicate`, we set a [[eu.matthiasbraun.sparse.Parser.BlockMarker]] at that line.
   * This marker indicates the start of a [[eu.matthiasbraun.sparse.Parser.TextBlock]].
   * Example usage:
   *
   * `parse(source, from("start line"), to("end line"))`
   * `parse(source, from(_.endsWith("start line suffix")), to(_.startsWith("end line prefix")))`
   * `parse(source, from((line, lineNr) => line == "start line" && lineNr > 10 ), to("end line))`
   */
  object from extends MarkerFactory {
    override def apply(predicate: ((String, Int) => Boolean)) = BlockMarker(predicate)
  }

  /**
   * When a line within the parsed text matches the `predicate`, we set a [[eu.matthiasbraun.sparse.Parser.BlockMarker]] at that line.
   * This marker indicates the end of a [[eu.matthiasbraun.sparse.Parser.TextBlock]].
   * Example usage:
   *
   * `parse(source, from("start line"), to("end line"))`
   * `parse(source, from(_.endsWith("start line suffix")), to(_.startsWith("end line prefix")))`
   * `parse(source, from("start line), to((line, lineNr) => line == "end line" && lineNr > 10 ))`
   */
  object to extends MarkerFactory {
    /* "to" is technically the same as "from". It exists so the user can call `parse(file, from("start"), to("end"))` */
    override def apply(predicate: ((String, Int) => Boolean)) = BlockMarker(predicate)
  }

  /**
   * When a line within the parsed text matches the `predicate`, we set a [[eu.matthiasbraun.sparse.Parser.BlockMarker]] after that line.
   * This marker may either indicate the start or end of a [[eu.matthiasbraun.sparse.Parser.TextBlock]].
   * Example usage:
   *
   * `parse(source, after("line before start"), to("end line"))
   * `parse(source, from("start line), after(_.startsWith("prefix of line before end")))
   * `parse(source, after((line, lineNr) => line == "line before start" && lineNr > 10), to("end line"))`
   */
  object after extends MarkerFactory {
    override def apply(predicate: ((String, Int) => Boolean)) = BlockMarker(predicate, offset = +1)
  }

  /**
   * When a line within the parsed text matches the `predicate`, we set a [[eu.matthiasbraun.sparse.Parser.BlockMarker]] before that line.
   * This marker indicates the start of a [[eu.matthiasbraun.sparse.Parser.TextBlock]].
   * Example usage:
   *
   * `parse(source, before("line after start"), to("end line"))
   * `parse(source, before(_.startsWith("prefix of line after start"), to("end line"))
   * `parse(source, before((line, lineNr) => line == "line after start" && lineNr > 10), to("end line"))`
   */
  object before extends MarkerFactory {
    override def apply(predicate: ((String, Int) => Boolean)) = BlockMarker(predicate, offset = -1)
  }

  /**
   * When a line within the parsed text matches the `predicate`, we set a [[eu.matthiasbraun.sparse.Parser.BlockMarker]] before that line.
   * This marker indicates the end of a [[eu.matthiasbraun.sparse.Parser.TextBlock]].
   * Example usage:
   *
   * `parse(source, from("start line"), until("line after end"))
   * `parse(source, from("start line"), until(_.startsWith("prefix of line after end")))
   * `parse(source, from("start line), until((line, lineNr) => line == "line after end " && lineNr > 10 ))`
   */
  object until extends MarkerFactory {
    /* "until" is the same as "before". It exists so the user can call `parse(file, before("start"), until("end"))` */
    override def apply(predicate: ((String, Int) => Boolean)) =  BlockMarker(predicate, offset = -1)
  }

  /**
   * Parses [[eu.matthiasbraun.sparse.Parser.TextBlock]]s from a `source`, such as a file.
   * The start and end of a text block are defined by the `start` and `end` [[eu.matthiasbraun.sparse.Parser.BlockMarker]]s.
   * These markers match lines within the text.
   * The caller can create those markers using [[eu.matthiasbraun.sparse.Parser.from]] or [[eu.matthiasbraun.sparse.Parser.until]], for example.
   * Example usage:
   * {{{
   * val file = new File("parse/this/file")
   * val blocksMaybe = parse(fromFile(file), from("start line"), to("end line"))
   *   blocksMaybe match {
   *     case Success(blocks)    => blocks.foreach { println }
   *     case Failure(exception) => println(exception)
   * }
   * }}}
   * @param source the [[scala.io.Source]] that contains the text that we want to parse
   * @param start the text blocks we want to parse begin at this [[eu.matthiasbraun.sparse.Parser.BlockMarker]]
   * @param end the text blocks we want to parse stop at this [[eu.matthiasbraun.sparse.Parser.BlockMarker]]
   * @return either a list of [[eu.matthiasbraun.sparse.Parser.TextBlock]]s wrapped in a [[scala.util.Success]] or a [[scala.util.Failure]] with the first exception that occurred during parsing.
   */
  def parse(source: Source, start: BlockMarker, end: BlockMarker): Try[List[TextBlock]] = {

    def getBlock(startAndEnd: (Int, Int), lines: Stream[String]): TextBlock = {
      val (start, end) = startAndEnd
      // slice is exclusive regarding its end index -> we want it inclusive
      val streamBlock = lines.slice(start, end + 1)
      // Get the lines from the lazy stream now so we don't have problems with a closed stream afterwards
      TextBlock(streamBlock.toVector)
    }

    def getStartAndEndPairs(lineStream: Stream[String]): List[(Int, Int)] = {
      // Appending to ArrayBuffers is O(1)
      val indices = ArrayBuffer[Int]()
      // We expect the first line to be matched a starting line
      var startExpected = true

      for ((line, lineNr) <- lineStream.zipWithIndex) {

        val atStart = start.linePredicate(line, lineNr)
        val atEnd = end.linePredicate(line, lineNr)

        if (atStart) {
          if (!startExpected) throw SparseException(s"Line $lineNr marks the start of a block although the last matched line was also a block start")

          indices += lineNr + start.offset
          startExpected = false
        }

        if (atEnd) {
          if (startExpected) throw SparseException(s"Line $lineNr marks the end of a block although the last matched line was also a block end")

          indices += lineNr + end.offset
          startExpected = true
        }
      }
      // Group the start and end indices into pairs. If there is an odd number of indices, this will ignore the last index
      indices.grouped(2).collect { case Seq(startIdx, endIdx) => (startIdx, endIdx) }.toList
    }

    val blocksMaybe = managed(source) map {
      managedSource =>
        // Convert the line iterator to a stream so we can call methods on it multiple times
        val lineStream = managedSource.getLines().toStream
        getStartAndEndPairs(lineStream).map(pair => getBlock(pair, lineStream))
    }
    blocksMaybe.either match {
      case Left(exceptions) => Failure(exceptions.head)
      case Right(blocks) => Success(blocks)
    }
  }
}