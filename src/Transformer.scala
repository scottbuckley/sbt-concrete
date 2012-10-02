/*
 * This file is part of the sbt-concrete plugin.
 * Copyright (c) 2012 Scott Buckley, Anthony M Sloane, Macquarie University.
 * All rights reserved.
 * Distributed under the New BSD license.
 * See file LICENSE at top of distribution.
 */

import sbt._

object Transformer {

    import scala.util.matching.Regex
    
    /**
     * Transformation function. `flags` is the collection of plugin flags
     * which may modify the operation of the transformation. `in` is a
     * set of the `.cscala` files in the project that have changed since
     * the last time the transformation was run. `outDir` is the directory
     * in which the transformed files should be written. Should return a
     * set of the generated files.
     */
    def transform (flags : Flags, in : Set[File], outDir : File) : Set[File] =
        for (inFile <- in)
            yield {
                val outFileName = inFile.base + ".scala"
                val outFile = outDir / outFileName		// pathname concatenationw
                IO.write (outFile, transformContents (IO.read (inFile)))
                outFile
            }

    /**
     * Given the contents of a `.cscala` file return the new contents.
     * FIXME: this is a dummy, just to do a simple transformation for testing.
     * It turns things of the form `[[foo]]` into `println (foo)`.
     */
    def transformContents (oldContents : String) : String = {
        val pattern = new Regex ("""\[\[([^]])+\]\]""", "body")
        pattern.replaceAllIn (oldContents, m => "println (2 * " + m.group ("body") + ")")
    }

}
