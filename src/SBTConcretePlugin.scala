/*
 * This file is part of the sbt-concrete plugin.
 * Copyright (c) 2012 Scott Buckley, Anthony M Sloane, Macquarie University.
 * All rights reserved.
 * Distributed under the New BSD license.
 * See file LICENSE at top of distribution.
 */

import sbt._
import Keys._

// FIXME: example of how to build a structure holding all flags (if we need it)

/**
 * A structure to hold the flag values so we can pass them around together.
 */
case class Flags (
    concreteDoSomething : Boolean
)

object SBTConcretePlugin extends Plugin {

    import Transformer.transform

    // FIXME: example of how to set up a Boolean setting (if we need any)

    /**
     * If true, make the concrete transformation do something special.
     */
    val concreteDoSomething = SettingKey[Boolean] (
        "concrete-do-something",
            "Make the concrete transformation do something"
    )

    /**
     * Aggregation of all flag settings.
     */
    val concreteFlags = SettingKey[Flags] (
        "concrete-flags", "All sbt-concrete flags"
    )

    /**
     * Run the transformation if there are any ".cscala" files in the project.
     * Set it up to re-run only if one of those files changes. Pass a set of
     * the changed files to the transformation.
     */
    def runTranslator =
        (concreteFlags, scalaSource, sourceManaged in Compile, streams, cacheDirectory) map {
            (flags, srcDir, smDir, str, cache) => {

                val cachedFun =
                    FileFunction.cached (cache / "sbt-concrete", //note: the / operator on file objects concatenates pathnames intelligently.
                                         FilesInfo.lastModified,
                                         FilesInfo.exists) {
                        (in: Set[File]) =>
                            val outDir = smDir / "sbt-concrete"
                            IO.createDirectory (outDir)
                            str.log.info ("Running Concrete transformation on %s, output to %s".format (
                                              in.mkString (","), outDir))
                            val out = transform (flags, in, outDir)
                            str.log.info ("Transformed Concrete files are %s".format (
                                              out.mkString (",")))
                            out
                    }

                val inputFiles = (srcDir ** "*.cscala").get.toSet
                println ("inputFiles = " + inputFiles)
                cachedFun (inputFiles).toSeq

            }
        }

    /**
     * Settings for the plugin:
     *  - run the processing as a source generator
     *  - add dependent libraries
     *  - default values for settings
     *  - group settings together to pass around
     */
    val sbtConcreteSettings = Seq (

        sourceGenerators in Compile <+= runTranslator,

        // FIXME: any dependencies that the generated code needs (probably not needed)
        // libraryDependencies ++= Seq (
        //     "xtc" % "rats-runtime" % "2.3.1"
        // ),

        // FIXME: default values for settings

        concreteDoSomething := false,

        // FIXME: initialise flags structure from settings values

        concreteFlags <<= (concreteDoSomething) {
            (lists) =>
                Flags (lists)
        }

    )

}
