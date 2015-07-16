import sbt.inc.Analysis


object CustomizedTasks {
  import sbt.Keys._
  import sbt._

  def weaveTask = (compile in Compile, dependencyClasspath in Compile, ivyPaths in Compile, classDirectory in Compile) map { case (analysis, classpathTask, ivy, bin) =>
    val weaveCmd = mkCommand(classpath(classpathTask))(osBasedPath(ivy.ivyHome.get.getAbsolutePath))(osBasedPath(bin.getAbsolutePath))
    val folder = createWeaveShellFolder

    if ((!folder.exists() && folder.mkdir()) || folder.exists()) {
        val weaveShell = generateShellFile(folder, weaveCmd)
        executeWeaveShell(weaveShell)
    } else {
      throw new IllegalStateException("Cannot create folder: " + folder.getAbsolutePath)
    }

    Analysis.Empty
  }

  private def isWindows: Boolean = System.getProperty("os.name").toLowerCase.startsWith("window")

  private def osBasedPath(rawBinPath: String): String = {
    if (isWindows) rawBinPath.replaceAll("""\\""", "/")
    else rawBinPath
  }

  private def createWeaveShellFolder = {
    val tmpPath = System.getProperty("java.io.tmpdir")
    val userHome = System.getProperty("user.home")
    val shellFolder = if (null != tmpPath) tmpPath else userHome
    new java.io.File(shellFolder, String.valueOf(System.currentTimeMillis()));
  }

  private def classpath(cp: Keys.Classpath): String = {
    if (isWindows) cp.map(_.data).mkString(";")
    else cp.map(_.data).mkString(":")
  }

  private def mkCommand(classpath: String)(ivy: String)(classses: String): String = {
    if (isWindows) "java -cp \"" + classses + ";" + classpath + "\" -javaagent:" + ivy + "/cache/net.imadz/Lifecycle/jars/Lifecycle-" + "0.9.10" + ".jar -Dnet.imadz.bcel.save.original=true net.imadz.lifecycle.StaticWeaver \"" + classses + "\""
    else "java -cp \"" + classses + ":" + classpath + "\" -javaagent:" + ivy + "/cache/net.imadz/Lifecycle/jars/Lifecycle-" + "0.9.10" + ".jar -Dnet.imadz.bcel.save.original=true net.imadz.lifecycle.StaticWeaver \"" + classses + "\""
  }

  private def generateShellFile(folder: File, weaveCmd: String) = {
    val shell = new File(folder, "weave.sh")
    shell.setExecutable(true)
    if ((!shell.exists() || shell.delete()) && shell.createNewFile()) {
      val writer = new java.io.FileWriter(shell)
      writer.write(weaveCmd)
      writer.flush()
      writer.close()
      println(shell.getAbsolutePath)
      shell
    } else {
      throw new IllegalStateException("Fail to delete or create file: " + shell.getAbsolutePath)
    }
  }
  private def executeWeaveShell(weaveShell: File) {
    ("bash " + weaveShell.getAbsolutePath) !!
  }

}
