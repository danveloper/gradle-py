package com.github.danveloper.gradle.plugins.pybuild

import com.github.danveloper.gradle.plugins.pybuild.internal.BetterFactory
import com.github.danveloper.gradle.plugins.pybuild.internal.BetterProject
import com.github.danveloper.gradle.plugins.pybuild.internal.BigStupidFunction
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * Apply the shit out of this plugin.
 *
 * <p>
 *   buildscript {
 *     dependencies {
 *       classpath 'com.github.danveloper:gradle-py:1.0.0'
 *     }
 *   }
 *   apply plugin: 'py-build'
 * </p>
 *
 * @author Dan Woods
 * @since The Beginning.
 */
@CompileStatic
public class PyBuildPlugin implements Plugin<Project> {
  protected static ScriptEngine engine = new ScriptEngineManager().getEngineByName("jython");

  @Override
  public void apply(Project project) {
    Bindings bindings = engine.createBindings();
    bindings.put("project", new BetterProject(project));
    engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
    File buildScript = new File(project.getProjectDir(), "gradle.py");
    uncheck {
      InputStream stream = new FileInputStream(buildScript);
      byte[] buf = uncheck0 { new byte[stream.available()] }
      stream.read(buf);
      engine.eval(new String(buf));
    }
  }

  private static void uncheck(BigStupidFunction fn) {
    uncheck0 {
      fn.functionate();
      return null;
    }
  }

  private static <T> T uncheck0(BetterFactory<T> factory) {
    try {
      return factory.create();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
