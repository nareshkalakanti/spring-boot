/*
 * Copyright 2012-2015 the original author or authors.
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

package org.springframework.boot.devtools.restart.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.boot.devtools.restart.classloader.ClassLoaderFile;
import org.springframework.boot.devtools.restart.classloader.ClassLoaderFile.Kind;
import org.springframework.boot.devtools.restart.classloader.ClassLoaderFiles;
import org.springframework.boot.devtools.restart.classloader.ClassLoaderFiles.SourceFolder;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

/**
 * Server used to {@link Restarter restart} the current application with updated
 * {@link ClassLoaderFiles}.
 *
 * @author Phillip Webb
 * @since 1.3.0
 */
public class RestartServer {

	private static final Log logger = LogFactory.getLog(RestartServer.class);

	private final SourceFolderUrlFilter sourceFolderUrlFilter;

	private final ClassLoader classLoader;

	/**
	 * Create a new {@link RestartServer} instance.
	 * @param sourceFolderUrlFilter the source filter used to link remote folder to the
	 * local classpath
	 */
	public RestartServer(SourceFolderUrlFilter sourceFolderUrlFilter) {
		this(sourceFolderUrlFilter, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Create a new {@link RestartServer} instance.
	 * @param sourceFolderUrlFilter the source filter used to link remote folder to the
	 * local classpath
	 * @param classLoader the application classloader
	 */
	public RestartServer(SourceFolderUrlFilter sourceFolderUrlFilter,
			ClassLoader classLoader) {
		Assert.notNull(sourceFolderUrlFilter, "SourceFolderUrlFilter must not be null");
		Assert.notNull(classLoader, "ClassLoader must not be null");
		this.sourceFolderUrlFilter = sourceFolderUrlFilter;
		this.classLoader = classLoader;
	}

	/**
	 * Update the current running application with the specified {@link ClassLoaderFiles}
	 * and trigger a reload.
	 * @param files updated class loader files
	 */
	public void updateAndRestart(ClassLoaderFiles files) {
		Set<URL> urls = new LinkedHashSet<URL>();
		Set<URL> classLoaderUrls = getClassLoaderUrls();
		for (SourceFolder folder : files.getSourceFolders()) {
			for (Entry<String, ClassLoaderFile> entry : folder.getFilesEntrySet()) {
				if (entry.getValue().getKind() == ClassLoaderFile.Kind.ADDED) {
					URL url = findSpringBootClassesLocation(classLoaderUrls);
					if (url != null) {
						if (updateFileSystem(url, entry.getKey(), entry.getValue())) {
							urls.add(url);
						}
					}
				}
				else {
					for (URL url : classLoaderUrls) {
						if (updateFileSystem(url, entry.getKey(), entry.getValue())) {
							urls.add(url);
						}
					}
				}
			}
			urls.addAll(getMatchingUrls(classLoaderUrls, folder.getName()));
		}
		updateTimeStamp(urls);
		restart(urls, files);
	}

	private URL findSpringBootClassesLocation(Set<URL> classLoaderUrls) {
		File manifestFile = new File("META-INF/MANIFEST.MF");
		if (!manifestFile.isFile()) {
			logger.warn("Failed to find classes location. Manifest '"
					+ manifestFile.getAbsolutePath() + "' does not exist");
			return null;
		}
		try {
			Manifest manifest = readManifest(manifestFile);
			String value = manifest.getMainAttributes().getValue("Spring-Boot-Classes");
			if (value == null) {
				logger.warn("Failed to find classes location. Manifest '"
						+ manifestFile.getAbsolutePath()
						+ "' does not contain a Spring-Boot-Classes attribute");
				return null;
			}
			File springBootClasses = new File(value).getAbsoluteFile();
			for (URL url : classLoaderUrls) {
				if (isFolderUrl(url.toString())
						&& ResourceUtils.getFile(url).equals(springBootClasses)) {
					return url;
				}
			}
			logger.warn("Failed to find classes location. '" + springBootClasses
					+ "' is not on the class path");
			return null;
		}
		catch (IOException ex) {
			logger.warn("Failed to find classes location. Failed to read manifest '"
					+ manifestFile.getAbsolutePath() + "'", ex);
			return null;
		}
	}

	private Manifest readManifest(File manifestFile) throws IOException {
		FileInputStream input = null;
		try {
			input = new FileInputStream(manifestFile);
			return new Manifest(input);
		}
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException ex) {
					// Continue
				}
			}
		}
	}

	private boolean updateFileSystem(URL url, String name,
			ClassLoaderFile classLoaderFile) {
		if (!isFolderUrl(url.toString())) {
			return false;
		}
		try {
			File folder = ResourceUtils.getFile(url);
			File file = new File(folder, name);
			if (classLoaderFile.getKind() == Kind.DELETED) {
				return file.delete();
			}
			if (classLoaderFile.getKind() == Kind.MODIFIED && !file.canWrite()) {
				return false;
			}
			if (classLoaderFile.getKind() == Kind.ADDED
					&& !file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			FileCopyUtils.copy(classLoaderFile.getContents(), file);
			return true;
		}
		catch (IOException ex) {
			// Ignore
		}
		return false;
	}

	private boolean isFolderUrl(String urlString) {
		return urlString.startsWith("file:") && urlString.endsWith("/");
	}

	private Set<URL> getMatchingUrls(Set<URL> urls, String sourceFolder) {
		Set<URL> matchingUrls = new LinkedHashSet<URL>();
		for (URL url : urls) {
			if (this.sourceFolderUrlFilter.isMatch(sourceFolder, url)) {
				if (logger.isDebugEnabled()) {
					logger.debug("URL " + url + " matched against source folder "
							+ sourceFolder);
				}
				matchingUrls.add(url);
			}
		}
		return matchingUrls;
	}

	private Set<URL> getClassLoaderUrls() {
		Set<URL> urls = new LinkedHashSet<URL>();
		ClassLoader classLoader = this.classLoader;
		while (classLoader != null) {
			if (classLoader instanceof URLClassLoader) {
				for (URL url : ((URLClassLoader) classLoader).getURLs()) {
					urls.add(url);
				}
			}
			classLoader = classLoader.getParent();
		}
		return urls;

	}

	private void updateTimeStamp(Iterable<URL> urls) {
		for (URL url : urls) {
			updateTimeStamp(url);
		}
	}

	private void updateTimeStamp(URL url) {
		try {
			URL actualUrl = ResourceUtils.extractJarFileURL(url);
			File file = ResourceUtils.getFile(actualUrl, "Jar URL");
			file.setLastModified(System.currentTimeMillis());
		}
		catch (Exception ex) {
			// Ignore
		}
	}

	/**
	 * Called to restart the application.
	 * @param urls the updated URLs
	 * @param files the updated files
	 */
	protected void restart(Set<URL> urls, ClassLoaderFiles files) {
		Restarter restarter = Restarter.getInstance();
		restarter.addUrls(urls);
		restarter.addClassLoaderFiles(files);
		restarter.restart();
	}

}
