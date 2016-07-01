package com.github.danveloper.gradle.plugins.pybuild.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.dsl.ScriptHandler
import org.python.core.PyObject

class BetterProject {

  @Delegate
  Project project

  BetterProject(Project project) {
    this.project = project
  }

  void artifacts(Action<ArtifactHandler> fn) {
    fn.execute(project.getArtifacts())
  }

  void ant(Action<AntBuilder> fn) {
    fn.execute(project.getAnt())
  }

  void configurations(Action<ConfigurationContainer> fn) {
    fn.execute(project.getConfigurations())
  }

  void buildscript(Action<BetterScriptHandler> fn) {
    fn.execute(new BetterScriptHandler(delegate: project.buildscript))
  }

  void repositories(Action<RepositoryHandler> fn) {
    fn.execute(project.getRepositories())
  }

  void dependencies(Action<PyObject> fn) {
    fn.execute(new PyObject() {
      public PyObject __findattr_ex__(String name) {
        new DelegatingDependencyHandler(handler: project.dependencies, name: name)
      }
    })
  }

  void task(String taskName, SweeterAction<Task> fn) {
    def task = project.tasks.create(taskName)
    fn.execute(new BetterTask(task: task));
  }

  static class BetterScriptHandler {
    @Delegate
    ScriptHandler delegate

    void repositories(Action<RepositoryHandler> action) {
      action.execute(getRepositories())
    }

    void dependencies(Action<PyObject> action) {
      action.execute(new PyObject() {
        public PyObject __findattr_ex__(String name) {
          new DelegatingDependencyHandler(handler: delegate.dependencies, name: name)
        }
      })
    }
  }

  static class BetterTask {
    @Delegate
    Task task

    void action(Action<Task> action) {
      actions.add(action)
    }
  }

  static class DelegatingDependencyHandler extends PyObject {
    DependencyHandler handler
    String name

    @Override
    PyObject __call__(PyObject[] args, String[] keywords) {
      handler."${name}"(args[0].toString())
      this
    }
  }
}
