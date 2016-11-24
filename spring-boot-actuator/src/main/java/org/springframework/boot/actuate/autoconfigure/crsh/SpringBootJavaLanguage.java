/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.autoconfigure.crsh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.crsh.cli.descriptor.Format;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.lang.impl.java.CompilationFailureException;
import org.crsh.lang.impl.java.JavaLanguage;
import org.crsh.lang.spi.CommandResolution;
import org.crsh.lang.spi.Compiler;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * A Spring Boot-specific {@link JavaLanguage}.
 *
 * @author Andy Wilkinson
 */
public final class SpringBootJavaLanguage extends JavaLanguage {

	@Override
	public Compiler getCompiler() {
		return new SpringBootJavaCompiler();
	}

	static class SpringBootJavaCompiler implements Compiler {

		private static final Set<String> EXTENSIONS = Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList("java")));

		@Override
		public Set<String> getExtensions() {
			return EXTENSIONS;
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public CommandResolution compileCommand(String name, byte[] source)
				throws CommandException {
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<JavaFileObject>();
			SpringBootJavaFileManager javaFileManager = new SpringBootJavaFileManager(
					compiler.getStandardFileManager(diagnosticCollector, null,
							Charset.defaultCharset()));
			CompilationTask task = compiler.getTask(null, javaFileManager,
					diagnosticCollector, null, null,
					Arrays.asList(new InMemorySourceFile(name, new String(source))));
			Boolean result = task.call();
			if (result == null || !result) {
				throw new CommandException(ErrorKind.INTERNAL,
						"Command compilation failed", new CompilationFailureException(
								getErrors(diagnosticCollector.getDiagnostics())));
			}
			Collection<InMemoryClassFile> compiledClasses = javaFileManager.compiledClasses
					.values();
			CompiledClassesClassLoader classLoader = new CompiledClassesClassLoader(
					javaFileManager.compiledClasses, getClass().getClassLoader());
			for (InMemoryClassFile compiledClass : compiledClasses) {
				String simpleName = compiledClass.className
						.substring(compiledClass.className.lastIndexOf(".") + 1);
				if (simpleName.equals(name)) {
					try {
						Class<?> commandClass = classLoader.loadClass(name);
						final Command command = new ClassShellCommand(commandClass);
						final String description = command.describe(name,
								Format.DESCRIBE);
						return new CommandResolution() {

							@Override
							public String getDescription() {
								return description;
							}

							@Override
							public Command<?> getCommand() throws CommandException {
								return command;
							}
						};
					}
					catch (Exception ex) {
						throw new CommandException(ErrorKind.INTERNAL,
								"Command '" + name + "' could not be loaded", ex);
					}
				}
			}
			throw new CommandException(ErrorKind.INTERNAL,
					"Java command '" + name + "' not found");
		}

		@Override
		public String doCallBack(ShellSession session, String name, String defaultValue) {
			return null;
		}

		private List<Diagnostic<? extends JavaFileObject>> getErrors(
				List<Diagnostic<? extends JavaFileObject>> diagnostics) {
			List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<Diagnostic<? extends JavaFileObject>>();
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
				if (diagnostic.getKind() == Kind.ERROR) {
					errors.add(diagnostic);
				}
			}
			return errors;
		}

	}

	private static class SpringBootJavaFileManager
			extends ForwardingJavaFileManager<JavaFileManager> {

		private final Map<String, InMemoryClassFile> compiledClasses = new HashMap<String, InMemoryClassFile>();

		private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		protected SpringBootJavaFileManager(JavaFileManager fileManager) {
			super(fileManager);
		}

		@Override
		public Iterable<JavaFileObject> list(Location location, String packageName,
				Set<javax.tools.JavaFileObject.Kind> kinds, boolean recurse)
						throws IOException {
			if (location == StandardLocation.PLATFORM_CLASS_PATH) {
				return super.list(location, packageName, kinds, recurse);
			}
			List<JavaFileObject> matches = new ArrayList<JavaFileObject>();
			if (location == StandardLocation.CLASS_PATH
					&& kinds.contains(JavaFileObject.Kind.CLASS)) {
				String pattern = "classpath*:/" + packageName.replace(".", "/") + "/"
						+ (recurse ? "**/" : "") + "*.class";
				Resource[] resources = this.resolver.getResources(pattern);
				for (Resource resource : resources) {
					matches.add(new ResourceClassFile(resource, packageName));
				}
			}
			return matches;
		}

		@Override
		public String inferBinaryName(Location location, JavaFileObject file) {
			if (file instanceof ResourceClassFile) {
				ResourceClassFile resourceJavaFileObject = (ResourceClassFile) file;
				String binaryName = resourceJavaFileObject.packageName + "."
						+ resourceJavaFileObject.resource.getFilename();
				binaryName = binaryName.substring(0, binaryName.length() - 6);
				return binaryName;
			}
			return super.inferBinaryName(location, file);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location, String className,
				javax.tools.JavaFileObject.Kind kind, FileObject sibling)
						throws IOException {
			InMemoryClassFile file = this.compiledClasses.get(className);
			if (file == null) {
				file = new InMemoryClassFile(className);
				this.compiledClasses.put(className, file);
			}
			return file;
		}

	}

	private static class InMemorySourceFile extends SimpleJavaFileObject {

		private String source;

		protected InMemorySourceFile(String className, String source) {
			super(URI.create("source:///" + className.replace(".", "/") + ".java"),
					Kind.SOURCE);
			this.source = source;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors)
				throws IOException {
			return this.source;
		}

	}

	private static class ResourceClassFile extends SimpleJavaFileObject {

		private final Resource resource;

		private final String packageName;

		protected ResourceClassFile(Resource resource, String packageName)
				throws IOException {
			super(URI.create("resource:///" + resource.getFilename()), Kind.CLASS);
			this.resource = resource;
			this.packageName = packageName;
		}

		@Override
		public InputStream openInputStream() throws IOException {
			return this.resource.getInputStream();
		}

		@Override
		public boolean isNameCompatible(String simpleName,
				javax.tools.JavaFileObject.Kind kind) {
			return false;
		}

	}

	private static class InMemoryClassFile extends SimpleJavaFileObject {

		private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		private final String className;

		protected InMemoryClassFile(String className) {
			super(URI.create("class:///" + className), Kind.CLASS);
			this.className = className;
		}

		@Override
		public OutputStream openOutputStream() throws IOException {
			return this.outputStream;
		}

	}

	private static class CompiledClassesClassLoader extends ClassLoader {

		private final Map<String, InMemoryClassFile> compiledClasses;

		CompiledClassesClassLoader(Map<String, InMemoryClassFile> compiledClasses,
				ClassLoader parent) {
			super(parent);
			this.compiledClasses = compiledClasses;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			InMemoryClassFile classFile = this.compiledClasses.get(name);
			if (classFile == null) {
				return super.findClass(name);
			}
			byte[] classBytes = classFile.outputStream.toByteArray();
			return super.defineClass(name, classBytes, 0, classBytes.length);
		}

	}

}
