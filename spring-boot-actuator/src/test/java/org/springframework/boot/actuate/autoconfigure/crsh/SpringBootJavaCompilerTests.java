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

import java.io.FileInputStream;
import java.io.IOException;

import org.crsh.shell.impl.command.spi.CommandException;
import org.junit.Test;

import org.springframework.boot.actuate.autoconfigure.crsh.SpringBootJavaLanguage.SpringBootJavaCompiler;
import org.springframework.util.StreamUtils;

/**
 * Tests for {@link SpringBootJavaCompiler}.
 *
 * @author Andy Wilkinson
 */
public class SpringBootJavaCompilerTests {

	@Test
	public void foo() throws CommandException, IOException {
		new SpringBootJavaCompiler().compileCommand("app", StreamUtils
				.copyToByteArray(new FileInputStream("src/test/resources/app.java")));
	}

}
