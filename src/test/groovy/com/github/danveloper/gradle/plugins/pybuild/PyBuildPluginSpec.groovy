package com.github.danveloper.gradle.plugins.pybuild

import org.gradle.api.plugins.GroovyPlugin
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class PyBuildPluginSpec extends PluginSpecification {

  void 'apply plugin from build py'() {
    given:
    def project = buildPy("""\
      project.apply({'plugin': 'groovy'})
    """.stripIndent())

    when:
    project.plugins.apply(PyBuildPlugin)

    then:
    project.plugins.hasPlugin(GroovyPlugin)
  }

  void 'set repositories from build py'() {
    given:
    def project = buildPy("""
      project.repositories(lambda repos: repos.jcenter())
    """)

    when:
    project.plugins.apply(PyBuildPlugin)

    then:
    project.repositories.getByName("BintrayJCenter")
  }

  void 'set dependencies from build py'() {
    given:
    def project = buildPy("""
      project.apply({'plugin': 'java'})
      project.dependencies(lambda deps: deps.compile('io.ratpack:ratpack-core:1.3.3'))
    """.stripIndent())

    when:
    project.plugins.apply(PyBuildPlugin)

    then:
    project.configurations["compile"].dependencies.size() == 1
    project.configurations["compile"].dependencies[0].group == 'io.ratpack'
    project.configurations["compile"].dependencies[0].name == 'ratpack-core'
    project.configurations["compile"].dependencies[0].version == '1.3.3'
  }

  void 'should be able to create a task'() {
    setup:
    def project = buildPy("""
      |def printf(x):
      |  print x
      |project.task('myTask', lambda task:
      |  task.action(lambda t: printf('myTask worked'))
      |)""".stripMargin())
    project.file("build.gradle") << """
      plugins {
        id 'py-build'
      }
    """

    when:
    def result = GradleRunner.create()
        .withProjectDir(project.projectDir)
        .withArguments("myTask", '-s')
        .withPluginClasspath()
        .withDebug(true)
        .build()

    then:
    result.output.contains("myTask worked")
    result.task(":myTask").outcome == TaskOutcome.SUCCESS
  }

  void 'should respect doLast on tasks'() {
    setup:
    def project = buildPy('''
      |def printf(x):
      |  print x
      |project.task('myTask', lambda task:
      |  task.doLast(lambda t: printf("doLast worked\\n"))
      |)'''.stripMargin())
    project.file("build.gradle") << """
      plugins {
        id 'py-build'
      }
    """

    when:
    def result = GradleRunner.create()
        .withProjectDir(project.projectDir)
        .withArguments("myTask", '-s')
        .withPluginClasspath()
        .withDebug(true)
        .build()

    then:
    result.output.contains("doLast worked")
    result.task(":myTask").outcome == TaskOutcome.SUCCESS
  }

  void 'should be able to affect the buildscript'() {
    setup:
    def project = buildPy("""
      |def fn(bs):
      |  bs.repositories(lambda repos : repos.jcenter())
      |  bs.dependencies(lambda deps  : deps.classpath('io.ratpack:ratpack-gradle:1.3.3'))
      |project.buildscript(fn)""".stripMargin())

    when:
    project.plugins.apply(PyBuildPlugin)

    then:
    project.buildscript.repositories.getByName('BintrayJCenter')
    project.buildscript.configurations["classpath"].dependencies.size() == 1
    project.buildscript.configurations["classpath"].dependencies[0].group == 'io.ratpack'
    project.buildscript.configurations["classpath"].dependencies[0].name == 'ratpack-gradle'
    project.buildscript.configurations["classpath"].dependencies[0].version == '1.3.3'
  }
}
