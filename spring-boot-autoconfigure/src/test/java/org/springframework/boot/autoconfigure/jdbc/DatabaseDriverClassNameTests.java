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

package org.springframework.boot.autoconfigure.jdbc;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.sql.XADataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.springframework.asm.ClassReader;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the class names in the {@link DatabaseDriver} enumeration.
 *
 * @author Andy Wilkinson
 */
@RunWith(Parameterized.class)
public class DatabaseDriverClassNameTests {

	private static final EnumSet<DatabaseDriver> excludedDrivers = EnumSet.of(
			DatabaseDriver.UNKNOWN, DatabaseDriver.ORACLE, DatabaseDriver.SQLSERVER,
			DatabaseDriver.DB2, DatabaseDriver.AS400);

	private final String className;

	private final Class<?> requiredType;

	@Parameters(name = "{0} {2}")
	public static List<Object[]> parameters() {
		DatabaseDriver[] databaseDrivers = DatabaseDriver.values();
		List<Object[]> parameters = new ArrayList<Object[]>();
		for (DatabaseDriver databaseDriver : databaseDrivers) {
			if (excludedDrivers.contains(databaseDriver)) {
				continue;
			}
			parameters.add(new Object[] { databaseDriver,
					databaseDriver.getDriverClassName(), Driver.class });
			if (databaseDriver.getXaDataSourceClassName() != null) {
				parameters.add(new Object[] { databaseDriver,
						databaseDriver.getXaDataSourceClassName(), XADataSource.class });
			}
		}
		return parameters;
	}

	public DatabaseDriverClassNameTests(DatabaseDriver driver, String className,
			Class<?> requiredType) {
		this.className = className;
		this.requiredType = requiredType;
	}

	@Test
	public void databaseClassIsOfRequiredType() throws Exception {
		// Use ASM to avoid unwanted side-effects of loading JDBC drivers
		ClassReader classReader = new ClassReader(getClass()
				.getResourceAsStream("/" + this.className.replace('.', '/') + ".class"));
		assertThat(Arrays.asList(classReader.getInterfaces()),
				hasItem(this.requiredType.getName().replace('.', '/')));
	}

}
