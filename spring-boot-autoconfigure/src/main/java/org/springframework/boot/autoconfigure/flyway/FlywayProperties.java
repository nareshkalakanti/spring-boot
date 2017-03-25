/*
 * Copyright 2012-2017 the original author or authors.
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

package org.springframework.boot.autoconfigure.flyway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.flywaydb.core.Flyway;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Flyway database migrations. These are only the properties
 * that Spring needs to validate and enable the migrations. If you want to control the
 * location or format of the scripts you can use the same prefix ("flyway") to inject
 * properties into the {@link Flyway} instance.
 *
 * @author Dave Syer
 * @since 1.1.0
 */
@ConfigurationProperties(prefix = "flyway", ignoreUnknownFields = true)
public class FlywayProperties {

	/**
	 * Allow transactional and non-transactional statements in the same migration.
	 */
	private Boolean allowMixedMigrations;

	/**
	 * The description with which a schema is tagged when applying a baseline.
	 */
	private String baselineDescription;

	/**
	 * Automatically call baseline when migrating a non-empty schema.
	 */
	private Boolean baselineOnMigrate;

	/**
	 * The version with which a scheme is tagged when applying a baseline.
	 */
	private String baselineVersion;

	/**
	 * Check that migration scripts location exists.
	 */
	private boolean checkLocation = false;

	/**
	 * Disables cleaning of the database.
	 */
	private Boolean cleanDisabled;

	/**
	 * Automatically call clean on a validation error.
	 */
	private Boolean cleanOnValidationError;

	/**
	 * Enable flyway.
	 */
	private boolean enabled = true;

	/**
	 * Encoding of SQL migrations.
	 */
	private String encoding;

	/**
	 * Ignore future migrations.
	 */
	private Boolean ignoreFutureMigrations;

	/**
	 * Ignore missing migrations.
	 */
	private Boolean ignoreMissingMigrations;

	/**
	 * SQL statements to execute to initialize a connection immediately after obtaining
	 * it.
	 */
	private List<String> initSqls = new ArrayList<String>();

	/**
	 * Username recorded in the metadata table for the applied migrations.
	 */
	private String installedBy;

	/**
	 * Locations of migrations scripts. Can contain the special "{vendor}" placeholder to
	 * use vendor-specific locations.
	 */
	private List<String> locations = new ArrayList<>(
			Collections.singletonList("db/migration"));

	/**
	 * Allow migrations to be run out of order.
	 */
	private Boolean outOfOrder;

	/**
	 * Login password of the database to migrate.
	 */
	private String password;

	/**
	 * Prefix of placeholders in migration scripts.
	 */
	private String placeholderPrefix;

	/**
	 * Perform placeholder replacement in migration scripts.
	 */
	private Boolean placeholderReplacement;

	/**
	 * Suffix of placeholders in migration scripts.
	 */
	private String placeholderSuffix;

	/**
	 * Placeholders and their replacements.
	 */
	private Map<String, String> placeholders = new HashMap<String, String>();

	/**
	 * File name prefix for repeatable SQL migrations.
	 */
	private String repeatableSqlMigrationPrefix;

	/**
	 * Scheme names managed by Flyway (case-sensitive).
	 */
	private List<String> schemas;

	/**
	 * Skip default callbacks.
	 */
	private Boolean skipDefaultCallbacks;

	/**
	 * Skip default resolvers;
	 */
	private Boolean skipDefaultResolvers;

	/**
	 * File name prefix for SQL migrations.
	 */
	private String sqlMigrationPrefix;

	/**
	 * File name separator for SQL migrations.
	 */
	private String sqlMigrationSeparator;

	/**
	 * File name suffix for SQL migrations.
	 */
	private String sqlMigrationSuffix;

	/**
	 * Name of the schema metadata table that will be used by Flyway.
	 */
	private String table;

	/**
	 * Target version up to which migrations should be considered.
	 */
	private String target;

	/**
	 * JDBC url of the database to migrate. If not set, the primary configured data source
	 * is used.
	 */
	private String url;

	/**
	 * Login user of the database to migrate.
	 */
	private String user;

	/**
	 * Automatically call validate when performing a migration.
	 */
	private Boolean validateOnMigrate;

	public Boolean getAllowMixedMigrations() {
		return this.allowMixedMigrations;
	}

	public void setAllowMixedMigrations(Boolean allowMixedMigrations) {
		this.allowMixedMigrations = allowMixedMigrations;
	}

	public String getBaselineDescription() {
		return this.baselineDescription;
	}

	public void setBaselineDescription(String baselineDescription) {
		this.baselineDescription = baselineDescription;
	}

	public Boolean getBaselineOnMigrate() {
		return this.baselineOnMigrate;
	}

	public void setBaselineOnMigrate(Boolean baselineOnMigrate) {
		this.baselineOnMigrate = baselineOnMigrate;
	}

	public String getBaselineVersion() {
		return this.baselineVersion;
	}

	public void setBaselineVersion(String baselineVersion) {
		this.baselineVersion = baselineVersion;
	}

	public boolean isCheckLocation() {
		return this.checkLocation;
	}

	public void setCheckLocation(boolean checkLocation) {
		this.checkLocation = checkLocation;
	}

	public Boolean getCleanDisabled() {
		return this.cleanDisabled;
	}

	public void setCleanDisabled(Boolean cleanDisabled) {
		this.cleanDisabled = cleanDisabled;
	}

	public Boolean getCleanOnValidationError() {
		return this.cleanOnValidationError;
	}

	public void setCleanOnValidationError(Boolean cleanOnValidationError) {
		this.cleanOnValidationError = cleanOnValidationError;
	}

	public boolean isCreateDataSource() {
		return this.url != null && this.user != null;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Boolean getIgnoreFutureMigrations() {
		return this.ignoreFutureMigrations;
	}

	public void setIgnoreFutureMigrations(Boolean ignoreFutureMigrations) {
		this.ignoreFutureMigrations = ignoreFutureMigrations;
	}

	public Boolean getIgnoreMissingMigrations() {
		return this.ignoreMissingMigrations;
	}

	public void setIgnoreMissingMigrations(Boolean ignoreMissingMigrations) {
		this.ignoreMissingMigrations = ignoreMissingMigrations;
	}

	public List<String> getInitSqls() {
		return this.initSqls;
	}

	public void setInitSqls(List<String> initSqls) {
		this.initSqls = initSqls;
	}

	public String getInstalledBy() {
		return this.installedBy;
	}

	public void setInstalledBy(String installedBy) {
		this.installedBy = installedBy;
	}

	public List<String> getLocations() {
		return this.locations;
	}

	public void setLocations(List<String> locations) {
		this.locations = locations;
	}

	public Boolean getOutOfOrder() {
		return this.outOfOrder;
	}

	public void setOutOfOrder(Boolean outOfOrder) {
		this.outOfOrder = outOfOrder;
	}

	public String getPassword() {
		return (this.password == null ? "" : this.password);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPlaceholderPrefix() {
		return this.placeholderPrefix;
	}

	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	public Boolean getPlaceholderReplacement() {
		return this.placeholderReplacement;
	}

	public void setPlaceholderReplacement(Boolean placeholderReplacement) {
		this.placeholderReplacement = placeholderReplacement;
	}

	public String getPlaceholderSuffix() {
		return this.placeholderSuffix;
	}

	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	public Map<String, String> getPlaceholders() {
		return this.placeholders;
	}

	public void setPlaceholders(Map<String, String> placeholders) {
		this.placeholders = placeholders;
	}

	public String getRepeatableSqlMigrationPrefix() {
		return this.repeatableSqlMigrationPrefix;
	}

	public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix) {
		this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
	}

	public List<String> getSchemas() {
		return this.schemas;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	public Boolean getSkipDefaultCallbacks() {
		return this.skipDefaultCallbacks;
	}

	public void setSkipDefaultCallbacks(Boolean skipDefaultCallbacks) {
		this.skipDefaultCallbacks = skipDefaultCallbacks;
	}

	public boolean getSkipDefaultResolvers() {
		return this.skipDefaultResolvers;
	}

	public void setSkipDefaultResolvers(Boolean skipDefaultResolvers) {
		this.skipDefaultResolvers = skipDefaultResolvers;
	}

	public String getSqlMigrationPrefix() {
		return this.sqlMigrationPrefix;
	}

	public void setSqlMigrationPrefix(String sqlMigrationPrefix) {
		this.sqlMigrationPrefix = sqlMigrationPrefix;
	}

	public String getSqlMigrationSeparator() {
		return this.sqlMigrationSeparator;
	}

	public void setSqlMigrationSeparator(String sqlMigrationSeparator) {
		this.sqlMigrationSeparator = sqlMigrationSeparator;
	}

	public String getSqlMigrationSuffix() {
		return this.sqlMigrationSuffix;
	}

	public void setSqlMigrationSuffix(String sqlMigrationSuffix) {
		this.sqlMigrationSuffix = sqlMigrationSuffix;
	}

	public String getTable() {
		return this.table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getTarget() {
		return this.target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Boolean getValidateOnMigrate() {
		return this.validateOnMigrate;
	}

	public void setValidateOnMigrate(Boolean validateOnMigrate) {
		this.validateOnMigrate = validateOnMigrate;
	}

	void apply(Flyway flyway) {
		setIfNotNull(this.allowMixedMigrations, flyway::setAllowMixedMigrations);
		setIfNotNull(this.baselineDescription, flyway::setBaselineDescription);
		setIfNotNull(this.baselineOnMigrate, flyway::setBaselineOnMigrate);
		setIfNotNull(this.baselineVersion, flyway::setBaselineVersionAsString);
		setIfNotNull(this.cleanDisabled, flyway::setCleanDisabled);
		setIfNotNull(this.cleanOnValidationError, flyway::setCleanOnValidationError);
		setIfNotNull(this.encoding, flyway::setEncoding);
		setIfNotNull(this.ignoreFutureMigrations, flyway::setIgnoreFutureMigrations);
		setIfNotNull(this.ignoreMissingMigrations, flyway::setIgnoreMissingMigrations);
		setIfNotNull(this.installedBy, flyway::setInstalledBy);
		setIfNotNull(this.locations, flyway::setLocations);
		setIfNotNull(this.outOfOrder, flyway::setOutOfOrder);
		setIfNotNull(this.placeholderPrefix, flyway::setPlaceholderPrefix);
		setIfNotNull(this.placeholderReplacement, flyway::setPlaceholderReplacement);
		setIfNotNull(this.placeholders, flyway::setPlaceholders);
		setIfNotNull(this.placeholderSuffix, flyway::setPlaceholderSuffix);
		setIfNotNull(this.repeatableSqlMigrationPrefix,
				flyway::setRepeatableSqlMigrationPrefix);
		setIfNotNull(this.schemas, flyway::setSchemas);
		setIfNotNull(this.skipDefaultCallbacks, flyway::setSkipDefaultCallbacks);
		setIfNotNull(this.skipDefaultResolvers, flyway::setSkipDefaultResolvers);
		setIfNotNull(this.sqlMigrationPrefix, flyway::setSqlMigrationPrefix);
		setIfNotNull(this.sqlMigrationSeparator, flyway::setSqlMigrationSeparator);
		setIfNotNull(this.sqlMigrationSuffix, flyway::setSqlMigrationSuffix);
		setIfNotNull(this.table, flyway::setTable);
		setIfNotNull(this.target, flyway::setTargetAsString);
		setIfNotNull(this.validateOnMigrate, flyway::setValidateOnMigrate);
	}

	private <T> void setIfNotNull(T value, Consumer<T> consumer) {
		if (value != null) {
			consumer.accept(value);
		}
	}

	private <T> void setIfNotNull(List<String> value, Consumer<String[]> consumer) {
		if (value != null) {
			consumer.accept(value.toArray(new String[value.size()]));
		}
	}

}
