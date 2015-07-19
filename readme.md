You can use Sparse to parse text blocks from sources such as files.

Usage examples:

 Simple:
 val blocks = parse(yourFile, from("###"), to("~~~"))
 This is for situations where you exactly know what your starting and your ending lines are.
 If your starting and ending lines vary, you can define __predicates__ for them. For example from(_.contains("###")) shows a predicate that matches when a line contains the string "###".
 Your predicates can be simple like the example before or from("_.startsWith("###")) or more complex: from(_.contains("###) && ! _.endsWith("---"))
 As you probably have figured out, `to(###)` from the example above, is equivalent `to(_.equals("###")` and exists just for convenience.

 Intermediate:
    val start = from(_.startsWith("###"))
    val end = to(_.startsWith("~~~"))
 Or if you'd like to give the line-to-be-matched a name
    val end = to((line) => line.startsWith("~~~"))
 If you want to find your starting and ending lines using regular expressions, here's how you'd do it: 
    val start = from(_.matches(".*###.*"))
   val end = to(_.matches(".*~~~.*"))

 Detailed:
 You can include the line number of the file in your predicates as well:
    val start = from((lineNr, line) => lineNr > 4 && line.startsWith("###"))
 If it's clear in your code, that the first placeholder stands for the line number and the second placeholder for the line itself (or if you feel especially succinct today), you can shorten the above example to this:
    val start = from(_ > 4 && _.startsWith("###"))
 Other example with placeholders for line number and line content:
    val end = after(_ > 4 && _.contains("~~~"))

 Expert:
 Create your own MatchingLine:
val myLine = MatchingLine((lineNr, line) => lineNr > 0 && line.endsWith("~~~"), offset = 3)

Dependencies of Sparse:
Scala 2.10 for Try
Scala-ARM 1.4 for reading files. http://jsuereth.com/scala-arm/
