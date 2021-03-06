// Rip from scalatra build file

package molecule

import sbt._
import Keys._
import Compiler.Keys._
import scala.xml._
import scala.util.matching.Regex
import java.net.URL
// import ls.Plugin.LsKeys
import com.typesafe.sbt.SbtScalariform
import com.typesafe.tools.mima.plugin.MimaPlugin


// check compile options 

object Build extends Build {

  import Dependencies._

  // Helpers
  def projectId(state: State) = extracted(state).currentProject.id
  def extracted(state: State) = Project extract state
  
  val buildSettings:Seq[Setting[_]] = Compiler.settings ++ Seq(
    organization       := "com.github.molecule-labs",
	version            := "0.5.2",
	manifestSetting,
    resolvers          ++= Seq(Repos.sonatypeNexusSnapshots,Repos.sonatypeNexusReleases),
    shellPrompt        := { "sbt (%s)> " format projectId(_) }
    // (LsKeys.tags in LsKeys.lsync)    := Seq("molecule")
    // (LsKeys.docsUrl in LsKeys.lsync) := Some(new URL("http://www.molecule.org/guides/"))
  )

  lazy val manifestSetting:Setting[_] = packageOptions <+= (name, version, organization) map {
    (title, version, vendor) =>
      Package.ManifestAttributes(
        "Created-By" -> "Simple Build Tool",
        "Built-By" -> System.getProperty("user.name"),
        "Build-Jdk" -> System.getProperty("java.version"),
        "Specification-Title" -> title,
        "Specification-Version" -> version,
        "Specification-Vendor" -> vendor,
        "Implementation-Title" -> title,
        "Implementation-Version" -> version,
        "Implementation-Vendor-Id" -> vendor,
        "Implementation-Vendor" -> vendor,
        "Sealed" -> "true"
      )
  }
  
  lazy val sharedSettings =
    Defaults.defaultSettings ++
      // ls.Plugin.lsSettings ++
      Collect.settings ++
      SbtScalariform.scalariformSettings ++
      MimaPlugin.mimaDefaultSettings ++
      Publish.settings ++
      buildSettings

  lazy val doNotPublish = Seq(publish := {}, publishLocal := {})

  lazy val moleculeTestSettings =
    sharedSettings ++
      doNotPublish ++
      Seq(
        libraryDependencies <++= scalaVersion(sv => Seq(Test.scalatest(sv)))
        // LsKeys.skipWrite := true
      )

  lazy val moleculeProject = Project(
    id = "molecule",
    base = file("."),
    settings = sharedSettings ++ Site.settings ++ Collect.doNotCollect ++ doNotPublish ++ Seq(
      description := "A concurrent programming library in Scala that features streaming and monadic I/O",
      Unidoc.unidocExclude := Seq("molecule-core-examples", "molecule-io-examples", "molecule-net-examples", "molecule-benchmarks")
      // LsKeys.skipWrite := true
    ),
    aggregate = Seq(
	  moleculeCore,
	  moleculeCoreExamples,
	  moleculeIo,
	  moleculeIoExamples,
	  moleculeParsers,
	  moleculeNet,
	  moleculeNetExamples,
	  moleculeBenchmarks)
  )

  lazy val moleculeCore = Project(
    id = "molecule-core",
    base = file("molecule-core"),
    settings = sharedSettings ++ Seq(
      version := "0.5.3",
	  description := "Molecule core classes",
	  // Avoid "Unsafe" warnings
      javacOptions ++= Seq("-XDignore.symbol.file")
	)
  )

  lazy val moleculeCoreExamples = Project(
     id = "molecule-core-examples",
     base = file("molecule-core-examples"),
     settings = moleculeTestSettings ++ Seq(
       version := "0.5.3",
       description := "Molecule core examples"
     )
  ) dependsOn(moleculeCore)

  lazy val moleculeIo = Project(
    id = "molecule-io",
    base = file("molecule-io"),
    settings = sharedSettings ++ Seq(
      version := "0.5.2",
      description := "Molecule support for monadic processes"
    )
  ) dependsOn(moleculeCore)

  lazy val moleculeIoExamples = Project(
     id = "molecule-io-examples",
     base = file("molecule-io-examples"),
     settings = moleculeTestSettings ++ Seq(
       description := "Molecule examples of monadic processes"
     )
  ) dependsOn(moleculeIo, moleculeCore)

  lazy val moleculeParsers: Project = Project(
    id = "molecule-parsers",
    base = file("molecule-parsers"),
    settings = sharedSettings ++ Seq(
       version := "0.5.4",
       description := "Molecule parsers"
     )
  ) dependsOn(moleculeCore)

  lazy val moleculeNet = Project(
    id = "molecule-net",
    base = file("molecule-net"),
    settings = sharedSettings ++ Seq(
      description := "Molecule support for networking interfaces"
    )
  ) dependsOn(moleculeCore)

  lazy val moleculeNetExamples = Project(
    id = "molecule-net-examples",
    base = file("molecule-net-examples"),
    settings = moleculeTestSettings ++ Seq(
      description := "Molecule examples of networked processes"
    )
  ) dependsOn(moleculeParsers, moleculeNet, moleculeCore, moleculeIo, moleculeIoExamples)


  // Note: mbench is not yet available for download for all scala versions;
  //       In case, a local compilation is required
  
  private def benchmarkLibDep(scalaVersion : String) = scalaVersion match {
    case versionXYZ("2", "9", _)  => Seq( Compilation.mbench )
    case versionXYZ("2", "10", _) => Seq( Compilation.mbench, Compilation.scalaActors(scalaVersion) )
    case _ =>  Seq( Compilation.scalaActors(scalaVersion) )
  }

  lazy val moleculeBenchmarks: Project = Project(
    id = "molecule-benchmarks",
    base = file("molecule-benchmarks"),
    settings = moleculeTestSettings ++ Seq(
       unmanagedBase := baseDirectory.value.getParentFile() / "lib",
	   libraryDependencies ++= benchmarkLibDep(scalaVersion.value),
	   fork := true, // for mbench
	   fork in test := true,
	   javaOptions <++= (fullClasspath in Runtime).map(cp => Seq("-cp", cp.files.mkString(System.getProperty("path.separator")), "-Dmbench.log.stdout=true", "-Dmbench.date.dir=sbtrun")),
      description := "Molecule benchmarks"
    )
  ) dependsOn (moleculeCore, moleculeCoreExamples, moleculeIo, moleculeIoExamples)

}
