package com.github.danveloper.gradle.plugins.pybuild.internal

@FunctionalInterface
interface SweeterAction<T> {

  void execute(T t) throws Exception
}
