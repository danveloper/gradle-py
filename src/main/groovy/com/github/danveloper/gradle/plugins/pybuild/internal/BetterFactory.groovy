package com.github.danveloper.gradle.plugins.pybuild.internal;

@FunctionalInterface
public interface BetterFactory<T> {

  T create() throws Exception;

}
