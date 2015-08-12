#Sparse: text parsing for Scala

You can use `Sparse` to parse text blocks from files and other [sources](http://www.scala-lang.org/api/2.11.5/index.html#scala.io.Source).

[![Build Status](https://travis-ci.org/mb720/sparse.svg?branch=master)](https://travis-ci.org/mb720/sparse)
##Usage examples

###Basic
Let's say the file you want to parse is this:

    (unrelated text before first block)
    start
      first line in first block
      second line in first block
    end
    (unrelated text before second block)
    start
      first line in second block
      second line in second block
    end
    (more unrelated text)

Assuming that you're interested in the blocks that start with `start` and end with `end`, here's how you parse them:
First of all, you load that file using one of the methods in [scala.io.Source](http://www.scala-lang.org/api/2.11.5/index.html#scala.io.Source$):
```scala
val yourFile = fromFile(new File("parse/this/file"))
```

In the case of our example file above, we know exactly how the start and end of a block looks like. So we can do the following to parse the two blocks from the file:

```scala
val blocksMaybe = parse(yourFile, from("start"), to("end"))
```

And this is how you print the blocks:


```scala
blocksMaybe match {
  case Success(blocks)    => blocks.foreach { println }
  case Failure(exception) => println(exception)
}
```
We got a [`Try`](http://www.scala-lang.org/api/2.10.3/index.html#scala.util.Try) back from `parse` which contains, if the parsing was successful, the blocks from the parsed file.
The first block we got back is this:

    start
      first line in first block
      second line in first block
    end

Probably, the second block won't surprise you, but here it is for completeness' sake:

    start
      first line in second block
      second line in second block
    end

Otherwise, if something went wrong, the `Try` holds the first exception that occurred during parsing.

Should you be interested only in what's *inside* the blocks, and not in the lines that mark their beginning and their end, you might like to call `parse` like this:

```scala
parse(yourFile, after("start"), until("end"))
```

The first block returned by that call is a bit different compared to the one we made using `to` and `from`:

    first line in first block
    second line in first block

Up till now you've seen `from`, `to`, `after`, and `until` to mark the start and end point of your blocks.

There is another one, `before`, that you can use if you're interested in the line that precedes the matching line.

The resulting blocks of
```scala
parse(yourFile, before("start"), until("end"))
```
are

    (unrelated text before second block)
    start
      first line in first block
      second line in first block
and

    (unrelated text before second block)
    start
      first line in second block
      second line in second block

### Intermediate
If the starts and the ends of your blocks vary you can define __predicates__ to match them.

Let's change our example file a bit, to make parsing slightly more challenging:

    blockStartPrefix: firstBlockHeader
      first line in first block
      second line in first block
    end

    blockStartPrefix: secondBlockHeader
      first line in second block
      second line in second block
    end

Now, because the start of a block is different for each block, we can't match it verbatim as we did in the previous example. But we notice that beginnings of a block all share a common `blockStartPrefix`. Let's match that:

```scala
parse(yourFile, from(_.startsWith("blockStartPrefix"), to("end"))
```

Defining predicates is of course not limited to `from`. Imagine that block ends vary like so:

    end of block 1 ###
    end of block 2 ###

In this case, we use `to(_.endsWith("###"))` in order to match the end of a block.

If the patterns are more complicated than that, you can always resort to regular expressions:

```scala
from(_.matches(yourRegexPattern))
```
### Expert
Maybe you need to consider the __line number__ as well to determine if a line should be the beginning or the end of a block. `Sparse` lets you account for that, too:

```scala
val start = from((line, lineNr) => line.startsWith("start") && lineNr > 4 )
parse(yourFile, start, to("end")
```
This way, the line not only has to begin with the string "start" but also needs to come after the fourth line in the file.
 If it's clear in your code that the first placeholder stands for the line and the second placeholder for the line number (or if you're feeling especially succinct today), you can shorten the above example to this:

```scala
val start = from(_.startsWith("start") && _ > 4)
```
### Master
If you're not content with the predefined block markers (i.e., `from`, `to`, `after`, `until`, and `before`) you can roll your own:
```scala
/** The block begins two lines after the `predicate` matches. */
object twoLinesAfter extends MarkerFactory {
  override def apply(predicate: ((String, Int) => Boolean)) =
    BlockMarker(predicate, offset = +2)
}
```
Your custom marker is used like all the predefined ones shown above:
```scala
val blocksMaybe = parse(yourFile, twoLinesAfter("start"), to("end"))
```

If you're wondering why you could pass a simple string instead of the `((String, Int) => Boolean)` predicate to `twoLinesAfter`, have a look at the `MarkerFactory` in [`Parser.scala`](https://github.com/mb720/sparse/blob/master/src/main/scala/eu/matthiasbraun/sparse/Parser.scala)

## Dependencies of `Sparse`
* Scala 2.10 for [`Try`](http://www.scala-lang.org/api/2.10.3/index.html#scala.util.Try)
* [Scala-ARM 1.4](http://jsuereth.com/scala-arm/) for reading files
