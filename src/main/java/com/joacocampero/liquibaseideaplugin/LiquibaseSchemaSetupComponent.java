package com.joacocampero.liquibaseideaplugin;

import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.JsonMappingKind;
import com.jetbrains.jsonSchema.JsonSchemaMappingsProjectConfiguration;
import com.jetbrains.jsonSchema.UserDefinedJsonSchemaConfiguration;
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * This component creates a Json-schema file inside CONFIG_DIR
 * And then configures a JsonSchemaMappings so all .json files
 * in project are validated against it.
 *
 * @author Joaco
 */
public class LiquibaseSchemaSetupComponent implements ModuleComponent {
    private final String FILE_NAME = "liquibase-schema.json";
    private final String CONFIG_DIR = ".idea";
    private final String SCHEMA_NAME = "Liquibase";
    private final Project project;

    public LiquibaseSchemaSetupComponent(Project project) {
        this.project = project;
        final String LIQUIBASE_JSON_SCHEMA = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"$id\":\"http://redbee.io/liquibase.json\",\"title\":\"The Root Schema\",\"type\":\"object\",\"required\":[\"databaseChangeLog\"],\"properties\":{\"databaseChangeLog\":{\"$id\":\"#/properties/databaseChangeLog\",\"type\":\"array\",\"title\":\"The root of all Liquibase changes is the databaseChangeLog\",\"required\":[\"changeSet\"],\"items\":{\"type\":\"object\",\"title\":\"The Items inside databaseChangeLog Schema\",\"maxProperties\":1,\"properties\":{\"preConditions\":{\"$ref\":\"#/definitions/preConditions\"},\"changeSet\":{\"$ref\":\"#/definitions/changeSet\"}}}}},\"definitions\":{\"dbmsEnum\":{\"type\":\"string\",\"examples\":[\"MySQL\",\"PostgreSQL\",\"Oracle\",\"Sql Server\",\"Sybase_Enterprise\",\"Sybase_Anywhere\",\"DB2\",\"Apache Derby\",\"HSQL\",\"H2\",\"Informix\",\"Firebird\",\"SQLite\"]},\"typeEnum\":{\"type\":\"string\",\"description\":\"Available data types, some are mapped by Liquibase.\",\"examples\":[\"bigint\",\"blob\",\"boolean\",\"char\",\"clob\",\"currency\",\"datetime\",\"date\",\"decimal\",\"double\",\"float\",\"int\",\"mediumint\",\"nchar\",\"nvarchar\",\"number\",\"smallint\",\"time\",\"timestamp\",\"tinyint\",\"uuid\",\"varchar\"]},\"conditionalOld\":{\"allOf\":[{\"type\":\"object\",\"properties\":{\"or\":{\"allOf\":[{\"$ref\":\"#/definitions/condition\"},{\"type\":[\"array\",\"object\"],\"items\":[{\"$ref\":\"#/definitions/conditional\"},{\"$ref\":\"#/definitions/condition\"}]}]},\"and\":{\"type\":[\"array\",\"object\"],\"items\":{\"$ref\":\"#/definitions/conditional\"},\"properties\":{\"and\":{\"$ref\":\"#/definitions/conditional\"},\"not\":{\"$ref\":\"#/definitions/conditional\"},\"or\":{\"$ref\":\"#/definitions/conditional\"}}},\"not\":{\"type\":[\"array\",\"object\"],\"items\":{\"$ref\":\"#/definitions/conditional\"},\"properties\":{\"and\":{\"$ref\":\"#/definitions/conditional\"},\"not\":{\"$ref\":\"#/definitions/conditional\"},\"or\":{\"$ref\":\"#/definitions/conditional\"}}}}},{\"$ref\":\"#/definitions/condition\"}]},\"conditional\":{\"allOf\":[{\"$ref\":\"#/definitions/condition\"},{\"type\":\"object\",\"properties\":{\"and\":{\"anyOf\":[{\"type\":[\"array\",\"object\"],\"items\":{\"$ref\":\"#/definitions/conditional\"}},{\"$ref\":\"#/definitions/conditional\"}]},\"not\":{\"anyOf\":[{\"type\":[\"array\",\"object\"],\"items\":{\"$ref\":\"#/definitions/conditional\"}},{\"$ref\":\"#/definitions/conditional\"}]},\"or\":{\"anyOf\":[{\"type\":[\"array\",\"object\"],\"items\":{\"$ref\":\"#/definitions/conditional\"}},{\"$ref\":\"#/definitions/conditional\"}]}}}]},\"condition\":{\"type\":\"object\",\"properties\":{\"dbms\":{\"type\":\"object\",\"required\":[\"type\"],\"description\":\"Passes if the database executed against matches the type specified.\",\"properties\":{\"type\":{\"allOf\":[{\"$ref\":\"#/definitions/dbmsEnum\"},{\"type\":\"string\",\"description\":\"Type of database expected. Multiple dbms values can be specified using comma separated values.\"}]}}},\"runningAs\":{\"type\":\"object\",\"required\":[\"username\"],\"description\":\"Passes if the database user executed under matches the username specified.\",\"properties\":{\"username\":{\"type\":\"string\",\"description\":\"Database user script is expected to run as.\"}}},\"changeSetExecuted\":{\"type\":\"object\",\"required\":[\"id\",\"author\",\"changeLogFile\"],\"description\":\"Passes if the specified change set has already been executed. Since 1.8\",\"properties\":{\"id\":{\"type\":\"string\",\"description\":\"Change set \\\"id\\\"\"},\"author\":{\"type\":\"string\",\"description\":\"Change set \\\"author\\\".\"},\"changeLogFile\":{\"type\":\"string\",\"description\":\"File name (including classpath relative path) of change set.\"}}},\"columnExists\":{\"allOf\":[{\"$ref\":\"#/definitions/columnAction\"},{\"type\":\"object\",\"required\":[\"schemaName\",\"tableName\",\"columnName\"],\"description\":\"Passes if the specified column exists in the database. Since 1.8\"}]},\"tableExists\":{\"allOf\":[{\"$ref\":\"#/definitions/tableAction\"},{\"type\":\"object\",\"required\":[\"schemaName\",\"tableName\"],\"description\":\"Passes if the specified table exists in the database.\"}]},\"viewExists\":{\"allOf\":[{\"$ref\":\"#/definitions/viewAction\"},{\"required\":[\"schemaName\",\"viewName\"],\"description\":\"Passes if the specified view exists in the database. Since 1.8\"}]},\"foreignKeyConstraintExists\":{\"allOf\":[{\"$ref\":\"#/definitions/foreignKeyAction\"},{\"description\":\"Passes if the specified foreign key exists in the database. Since 1.8\",\"required\":[\"schemaName\",\"foreignKeyName\"]}]},\"indexExists\":{\"allOf\":[{\"$ref\":\"#/definitions/indexAction\"},{\"description\":\"Passes if the specified index exists in the database. Since 1.8\",\"required\":[\"schemaName\",\"indexName\"]}]}}},\"preConditions\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/preCondition\"}},\"preCondition\":{\"allOf\":[{\"$ref\":\"#/definitions/condition\"},{\"$ref\":\"#/definitions/conditional\"},{\"type\":\"object\",\"required\":[\"onFail\",\"onFailMessage\"],\"properties\":{\"onFail\":{\"type\":\"string\",\"enum\":[\"HALT\",\"CONTINUE\",\"MARK_RAN\",\"WARN\"]},\"onFailMessage\":{\"type\":\"string\"},\"onError\":{\"type\":\"string\",\"enum\":[\"HALT\",\"CONTINUE\",\"MARK_RAN\",\"WARN\"]},\"onErrorMessage\":{\"type\":\"string\"},\"onUpdateSQL\":{\"type\":\"string\",\"enum\":[\"RUN\",\"FAIL\",\"IGNORE\"]}}}]},\"schemaAction\":{\"type\":\"object\",\"properties\":{\"schemaName\":{\"type\":\"string\",\"description\":\" Name of the schema.\"}}},\"catalogAction\":{\"allOf\":[{\"$ref\":\"#/definitions/schemaAction\"},{\"properties\":{\"catalogName\":{\"type\":\"string\",\"description\":\"Name of the catalog\"}}}]},\"tableAction\":{\"allOf\":[{\"$ref\":\"#/definitions/schemaAction\"},{\"properties\":{\"tableName\":{\"type\":\"string\",\"description\":\"Name of the table.\"}}}]},\"columnAction\":{\"allOf\":[{\"$ref\":\"#/definitions/tableAction\"},{\"properties\":{\"columnName\":{\"type\":\"string\",\"description\":\"Name of the column.\"}}}]},\"columnsAction\":{\"allOf\":[{\"$ref\":\"#/definitions/tableAction\"},{\"properties\":{\"columnNames\":{\"type\":\"string\",\"description\":\"Comma separated name(s) of the column(s) to create the primary key on\"}}}]},\"viewAction\":{\"allOf\":[{\"$ref\":\"#/definitions/schemaAction\"},{\"properties\":{\"viewName\":{\"type\":\"string\",\"description\":\"Name of the view.\"}}}]},\"foreignKeyAction\":{\"allOf\":[{\"$ref\":\"#/definitions/schemaAction\"},{\"properties\":{\"foreignKeyName\":{\"type\":\"string\",\"description\":\"Name of the foreign key.\"}}}]},\"indexAction\":{\"allOf\":[{\"$ref\":\"#/definitions/schemaAction\"},{\"properties\":{\"indexName\":{\"type\":\"string\",\"description\":\"Name of the index.\"}}}]},\"changeSet\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"description\":\"An alpha-numeric identifier.\"},\"author\":{\"type\":\"string\",\"description\":\"The creator of the change set.\"},\"dbms\":{\"allOf\":[{\"$ref\":\"#/definitions/dbmsEnum\"},{\"type\":\"string\",\"description\":\"The type of a database which that changeSet is to be used for. When the migration step is running, it checks the database type against this attribute.\"}]},\"runAlways\":{\"type\":\"boolean\",\"description\":\"Executes the change set on every run, even if it has been run before.\"},\"runOnChange\":{\"type\":\"boolean\",\"description\":\"Executes the change the first time it is seen and each time the change set has been changed.\"},\"context\":{\"type\":\"string\",\"description\":\"Executes the change if the particular context was passed at runtime, Any string can be used for the context name and they are checked case-insensitively.\"},\"runInTransaction\":{\"type\":\"boolean\",\"description\":\"Should the changeSet be ran as a single transaction (if possible)? Defaults to true\"},\"failOnError\":{\"type\":\"boolean\",\"description\":\"Should the migration fail if an error occurs while executing the changeSet?\"},\"comment\":{\"type\":\"string\",\"description\":\"A description of the change set\"},\"preConditions\":{\"$ref\":\"#/definitions/preConditions\"},\"validCheckSum\":{\"type\":\"array\",\"title\":\"List checksums which are considered valid for this changeSet, regardless of what is stored in the database.\",\"description\":\"Used primarily when you need to change a changeSet and don't want errors thrown on databases on which it has already run (not a recommended procedure).\"},\"rollback\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/refactoring\"}},\"changes\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/refactoring\"}}},\"required\":[\"id\",\"author\"]},\"refactoring\":{\"type\":\"object\",\"maxProperties\":1,\"description\":\"Liquibase ships with a large number of refactoring/changes that can be applied to your database\",\"properties\":{\"addAutoIncrement\":{\"allOf\":[{\"$ref\":\"#/definitions/columnAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"description\":\"Converts an existing column to be an auto-increment (a.k.a ‘identity’) column\",\"properties\":{\"columnDataType\":{\"type\":\"string\",\"description\":\"Current data type of the column to make auto-increment\"},\"incrementBy\":{\"type\":\"number\",\"description\":\"Interval to increment by\"},\"startWith\":{\"type\":\"number\",\"description\":\"Initial value\"}},\"required\":[\"schemaName\",\"tableName\",\"columnName\"]}]},\"addColumn\":{\"allOf\":[{\"$ref\":\"#/definitions/tableAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"properties\":{\"columns\":{\"type\":\"array\",\"minItems\":1,\"items\":{\"$ref\":\"#/definitions/column\"}}},\"required\":[\"columns\",\"tableName\"]}]},\"addDefaultValue\":{\"allOf\":[{\"$ref\":\"#/definitions/columnAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"title\":\"Adds a default value to the database definition for the specified column\",\"description\":\"ALTER TABLE cat.file ALTER fileName SET DEFAULT 'Something Else';\",\"properties\":{\"columnDataType\":{\"type\":\"string\",\"description\":\"Current data type of the column to add default value to\"},\"defaultValue\":{\"title\":\"Default value for column, the value will be surrounded by quote marks\"},\"defaultValueNumeric\":{\"type\":\"number\",\"title\":\"Default numeric value to set the column to.\",\"description\":\"The value will not be escaped and will not be nested in quote marks.\"},\"defaultValueBoolean\":{\"type\":\"boolean\",\"title\":\"Default boolean value to set the column to\",\"description\":\"The actual value string inserted will be dependent on the database implementation\"},\"defaultValueDate\":{\"type\":\"string\",\"title\":\"\\\"YYYY-MM-DD\\\", \\\"hh:mm:ss\\\" or \\\"YYYY-MM-DDThh:mm:ss\\\"\",\"pattern\":\"^((\\\\d{4}-(((0[1-9]|1[0-2])-([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])-(29|30))|((0[13578]|1[0-2])-31)|02-29))|(([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))|(\\\\d{4}-(((0[1-9]|1[0-2])-([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])-(29|30))|((0[13578]|1[0-2])-31)|02-29))T(([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])))$\"},\"defaultValueComputed\":{\"type\":\"string\",\"title\":\"Default computed value that is returned from a function or procedure call. This attribute will contain the function to call.\"},\"defaultValueSequenceNext\":{\"type\":\"string\"}},\"anyOf\":[{\"required\":[\"defaultValue\"]},{\"required\":[\"defaultValueNumeric\"]},{\"required\":[\"defaultValueBoolean\"]},{\"required\":[\"defaultValueDate\"]}],\"required\":[\"tableName\",\"columnName\",\"columnDataType\"]}]},\"addForeignKeyConstraint\":{\"type\":\"object\",\"title\":\"Adds a foreign key constraint to an existing column\",\"required\":[\"baseColumnNames\",\"baseTableName\",\"constraintName\",\"referencedColumnNames\",\"referencedTableName\"],\"properties\":{\"baseColumnNames\":{\"type\":\"string\",\"title\":\"Comma-separated name of column(s) to place the foreign key constraint on\"},\"baseTableCatalogName\":{\"type\":\"string\",\"title\":\"Name of the catalog\"},\"baseTableName\":{\"type\":\"string\",\"title\":\"Name of the table containing the column to constrain\"},\"baseTableSchemaName\":{\"type\":\"string\",\"title\":\"Name of the schema\"},\"constraintName\":{\"type\":\"string\",\"description\":\"Name of the new foreign key constraint\"},\"deferrable\":{\"type\":\"boolean\",\"description\":\"Is the foreign key deferrable\"},\"initiallyDeferred\":{\"type\":\"boolean\",\"description\":\"Is the foreign key initially deferred\"},\"onDelete\":{\"type\":\"string\",\"description\":\"ON DELETE functionality\",\"enum\":[\"CASCADE\",\"SET NULL\",\"SET DEFAULT\",\"RESTRICT\",\"NO ACTION\"]},\"onUpdate\":{\"type\":\"string\",\"description\":\"ON UPDATE functionality\",\"enum\":[\"CASCADE\",\"SET NULL\",\"SET DEFAULT\",\"RESTRICT\",\"NO ACTION\"]},\"referencedColumnNames\":{\"type\":\"string\",\"title\":\"Comma-separated name of column(s) to place the foreign key constraint on\"},\"referencedTableCatalogName\":{\"type\":\"string\",\"title\":\"Name of the catalog\"},\"referencedTableName\":{\"type\":\"string\",\"title\":\"Name of the table the foreign key points to\"},\"referencedTableSchemaName\":{\"type\":\"string\",\"title\":\"Name of the schema fo referenced table\"},\"referencesUniqueColumn\":{\"type\":\"boolean\"}}},\"addLookupTable\":{\"type\":\"object\",\"description\":\"Creates a lookup table containing values stored in a column and creates a foreign key to the new table.\",\"required\":[\"existingColumnName\",\"existingTableName\",\"newColumnDataType\",\"newColumnName\",\"newTableName\"],\"properties\":{\"constraintName\":{\"type\":\"string\",\"description\":\"Name of the foreign-key constraint to create between the existing table and the lookup table\"},\"existingColumnName\":{\"type\":\"string\",\"description\":\"Name of the column containing the data to extract\"},\"existingTableCatalogName\":{\"type\":\"string\",\"description\":\"Name of the existing table's tag\"},\"existingTableName\":{\"type\":\"string\",\"description\":\"Name of the table containing the data to extract\"},\"existingTableSchemaName\":{\"type\":\"string\",\"description\":\"Name of the existing table's schema\"},\"newColumnDataType\":{\"type\":\"string\",\"description\":\"Data type of the new table column\"},\"newColumnName\":{\"type\":\"string\",\"description\":\"Name of the column in the new table to create\"},\"newTableCatalogName\":{\"type\":\"string\",\"description\":\"Name of the new table's catalog\"},\"newTableName\":{\"type\":\"string\",\"description\":\"Name of lookup table to create\"},\"newTableSchemaName\":{\"type\":\"string\",\"description\":\"Name of the new table's schema\"}}},\"addNotNullConstraint\":{\"allOf\":[{\"$ref\":\"#/definitions/columnAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"type\":\"object\",\"description\":\"Adds a not-null constraint to an existing table. If a defaultNullValue attribute is passed, all null values for the column will be updated to the passed value before the constraint is applied.\",\"required\":[\"columnDataType\",\"columnName\",\"tableName\"],\"properties\":{\"columnDataType\":{\"type\":\"string\",\"description\":\"Current data type of the column\"},\"defaultNullValue\":{\"type\":\"string\",\"description\":\"Value to set all currently null values to. If not set, change will fail if null values exist\"}}}]},\"addPrimaryKey\":{\"allOf\":[{\"$ref\":\"#/definitions/columnsAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"description\":\"Adds creates a primary key out of an existing column or set of columns.\",\"required\":[\"columnNames\",\"tableName\"],\"properties\":{\"constraintName\":{\"type\":\"string\",\"description\":\"Name of the primary key constraint\"},\"tableSpace\":{\"type\":\"string\"}}}]},\"addUniqueConstraint\":{\"allOf\":[{\"$ref\":\"#/definitions/columnsAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"description\":\"Adds a unique constrant to an existing column or set of columns.\",\"required\":[\"columnNames\",\"tableName\"],\"properties\":{\"constraintName\":{\"type\":\"string\",\"description\":\"Name of the unique constraint\"},\"deferrable\":{\"type\":\"boolean\",\"description\":\"Are constraints deferrable\"},\"disabled\":{\"type\":\"boolean\",\"description\":\"Is constraint disabled\"},\"initiallyDeferred\":{\"type\":\"boolean\",\"description\":\"Are constraints initially deferred\"},\"tablespace\":{\"type\":\"string\",\"description\":\"'Tablespace' to create the index in. Corresponds to file group in mssql\"}}}]},\"alterSequence\":{\"allOf\":[{\"$ref\":\"#/definitions/schemaAction\"},{\"description\":\"Alter properties of an existing sequence\",\"required\":[\"sequenceName\"],\"properties\":{\"catalogName\":{\"type\":\"string\",\"description\":\"Name of the catalog\"},\"incrementBy\":{\"type\":\"number\",\"description\":\"New amount the sequence should increment by\"},\"maxValue\":{\"type\":\"number\",\"description\":\"New maximum value for the sequence\"},\"minValue\":{\"type\":\"number\",\"description\":\"New minimum value for the sequence\"},\"ordered\":{\"type\":\"boolean\",\"description\":\"Does the sequence need to be guaranteed to be genererated inm the order of request?\"},\"sequenceName\":{\"type\":\"string\",\"description\":\"\"}}}]},\"createIndex\":{\"allOf\":[{\"$ref\":\"#/definitions/tableAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"description\":\"Creates an index on an existing column or set of columns.\",\"required\":[\"tableName\",\"columns\"],\"properties\":{\"indexName\":{\"type\":\"string\",\"description\":\"Name of the index to create\"},\"tablespace\":{\"type\":\"string\",\"description\":\"Tablespace to create the index in\"},\"unique\":{\"type\":\"boolean\",\"description\":\"Unique values index\"},\"columns\":{\"type\":\"array\",\"description\":\"Column(s) to add to the index\",\"items\":{\"allOf\":[{\"$ref\":\"#/definitions/column\"},{\"required\":[\"name\",\"type\"]}]}}}}]},\"createProcedure\":{\"allOf\":[{\"$ref\":\"#/definitions/catalogAction\"},{\"type\":\"object\",\"description\":\"Defines the definition for a stored procedure. This command is better to use for creating procedures than the raw sql command because it will not attempt to strip comments or break up lines.\",\"required\":[\"schemaName\",\"procedureName\"],\"properties\":{\"comments\":{\"type\":\"string\"},\"dbms\":{\"allOf\":[{\"$ref\":\"#/definitions/dbmsEnum\"}]},\"encoding\":{\"type\":\"string\",\"description\":\"utf8, etc\"},\"path\":{\"type\":\"string\",\"description\":\"File containing the procedure text. Either this attribute or a nested procedure text is required.\"},\"procedureName\":{\"type\":\"string\"},\"procedureText\":{\"type\":\"string\",\"description\":\"a SQL script\"},\"relativeToChangelogFile\":{\"type\":\"boolean\",\"description\":\"Is path relative to changelog file\"}}}]},\"createSequence\":{\"allOf\":[{\"$ref\":\"#/definitions/catalogAction\"},{\"description\":\"Creates a new database sequence\",\"required\":[\"sequenceName\"],\"properties\":{\"cycle\":{\"type\":\"boolean\",\"description\":\"Can the sequence cycle when it hits the max value?\"},\"incrementBy\":{\"type\":\"integer\",\"description\":\"Interval between sequence numbers\"},\"maxValue\":{\"type\":\"integer\",\"description\":\"The maximum value of the sequence\"},\"minValue\":{\"type\":\"integer\",\"description\":\"The minimum value of the sequence\"},\"ordered\":{\"type\":\"boolean\",\"description\":\"Does the sequence need to be guaranteed to be genererated inm the order of request?\"},\"sequenceName\":{\"type\":\"string\",\"description\":\"Name of the sequence to create\"},\"startValue\":{\"type\":\"integer\",\"description\":\"The first sequence number to be generated.\"}}}]},\"createTable\":{\"allOf\":[{\"$ref\":\"#/definitions/tableAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"description\":\"Create a table\",\"required\":[\"tableName\",\"columns\"],\"properties\":{\"remarks\":{\"type\":\"string\"},\"tablespace\":{\"type\":\"string\"},\"columns\":{\"type\":\"array\",\"description\":\"The column(s) of the table\",\"minItems\":1,\"items\":{\"allOf\":[{\"$ref\":\"#/definitions/column\"},{\"required\":[\"name\",\"type\"]}]}}}}]},\"createView\":{\"allOf\":[{\"$ref\":\"#/definitions/viewAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"description\":\"Create a new database view\",\"required\":[\"selectQuery\",\"viewName\"],\"properties\":{\"replaceIfExists\":{\"type\":\"boolean\",\"description\":\"Use 'create or replace' syntax\"},\"selectQuery\":{\"type\":\"string\",\"description\":\"SQL for generating the view\"}}}]},\"customChange\":{\"type\":\"object\",\"description\":\"To create your own custom refactoring, simply create a class that implements the liquibase.change.custom.CustomSqlChange or liquibase.change.custom.CustomTaskChange interface and use the <custom> tag in your change set.\"},\"delete\":{\"allOf\":[{\"$ref\":\"#/definitions/tableAction\"},{\"$ref\":\"#/definitions/catalogAction\"},{\"type\":\"object\",\"description\":\"Deletes data from an existing table\",\"required\":[\"tableName\"],\"properties\":{\"where\":{\"type\":\"string\",\"description\":\"An SQL WHERE condition (without the WHERE)\"}}}]}}},\"column\":{\"type\":\"object\",\"description\":\"The column tag is a tag that is re-used throughout Liquibase ,as a result, not all the attributes of column make sense in each context it is used in.\",\"properties\":{\"name\":{\"type\":\"string\",\"description\":\"Name of the column\"},\"type\":{\"allOf\":[{\"$ref\":\"#/definitions/typeEnum\"}]},\"value\":{\"description\":\"The value will be surrounded by quote marks and nested quote marks will be escaped.\"},\"computed\":{\"description\":\"Used if the value in \\\"name\\\" isn't actually a column name but actually a function.\"},\"valueNumeric\":{\"type\":\"number\",\"title\":\"Numeric value to set the column to.\",\"description\":\"The value will not be escaped and will not be nested in quote marks.\"},\"valueBoolean\":{\"type\":\"boolean\",\"title\":\"Boolean value to set the column to\",\"description\":\"The actual value string inserted will be dependent on the database implementation\"},\"valueDate\":{\"type\":\"string\",\"title\":\"\\\"YYYY-MM-DD\\\", \\\"hh:mm:ss\\\" or \\\"YYYY-MM-DDThh:mm:ss\\\"\",\"pattern\":\"^((\\\\d{4}-(((0[1-9]|1[0-2])-([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])-(29|30))|((0[13578]|1[0-2])-31)|02-29))|(([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))|(\\\\d{4}-(((0[1-9]|1[0-2])-([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])-(29|30))|((0[13578]|1[0-2])-31)|02-29))T(([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])))$\"},\"valueComputed\":{\"type\":\"string\",\"title\":\"A value that is returned from a function or procedure call. This attribute will contain the function to call.\"},\"valueBlobFile\":{\"type\":\"string\",\"title\":\"Path to a file, which contents will be written as a BLOB\",\"description\":\"Must be either absolute or relative to the Change Log file location\"},\"valueClobFile\":{\"type\":\"string\",\"title\":\"Path to a file, which contents will be written as a CLOB\",\"description\":\"Must be either absolute or relative to the Change Log file location\"},\"encoding\":{\"type\":\"string\",\"title\":\"Name of the encoding (as specified in java.nio.Charset javadoc, e.g. \\\"UTF-8\\\")\",\"enum\":[\"US-ASCII\",\"ISO_8859-1\",\"UTF-8\",\"UTF-16BE\",\"UTF-16LE\",\"UTF-16\"]},\"defaultValue\":{\"title\":\"Default value for column, the value will be surrounded by quote marks\"},\"defaultValueNumeric\":{\"type\":\"number\",\"title\":\"Default numeric value to set the column to.\",\"description\":\"The value will not be escaped and will not be nested in quote marks.\"},\"defaultValueBoolean\":{\"type\":\"boolean\",\"title\":\"Default boolean value to set the column to\",\"description\":\"The actual value string inserted will be dependent on the database implementation\"},\"defaultValueDate\":{\"type\":\"string\",\"title\":\"\\\"YYYY-MM-DD\\\", \\\"hh:mm:ss\\\" or \\\"YYYY-MM-DDThh:mm:ss\\\"\",\"pattern\":\"^((\\\\d{4}-(((0[1-9]|1[0-2])-([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])-(29|30))|((0[13578]|1[0-2])-31)|02-29))|(([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))|(\\\\d{4}-(((0[1-9]|1[0-2])-([01][1-9]|10|2[0-8]))|((0[13-9]|1[0-2])-(29|30))|((0[13578]|1[0-2])-31)|02-29))T(([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])))$\"},\"defaultValueComputed\":{\"type\":\"string\",\"title\":\"Default computed value that is returned from a function or procedure call. This attribute will contain the function to call.\"},\"autoIncrement\":{\"type\":\"boolean\",\"title\":\"Is column an auto-increment column, ignored on databases that do not support autoincrement/identity functionality.\"},\"remarks\":{\"type\":\"string\",\"title\":\"Short description of the column (column comment)\"},\"beforeColumn\":{\"type\":\"string\",\"title\":\"If used in an 'addColumn' command, this attribute allows you to control where in the table column order the new column goes.\"},\"afterColumn\":{\"type\":\"string\",\"title\":\"If used in an 'addColumn' command, this attribute allows you to control where in the table column order the new column goes.\"},\"position\":{\"type\":\"number\",\"title\":\"If used in an 'addColumn' command, this attribute allows you to control where in the table column order the new column goes.\"},\"constraints\":{\"type\":\"object\",\"title\":\"Information about constraints on the column.\",\"properties\":{\"nullable\":{\"type\":\"boolean\",\"title\":\"Is column nullable? (NOT NULL)\"},\"primaryKey\":{\"type\":\"boolean\",\"title\":\"Is column a primary key?\"},\"primaryKeyName\":{\"type\":\"string\",\"title\":\"A string indicating the name of the primary key\"},\"unique\":{\"type\":\"boolean\",\"title\":\"Should a unique clause be applied.\"},\"uniqueConstraintName\":{\"type\":\"string\",\"title\":\"A string indicating unique constraint name\"},\"references\":{\"type\":\"string\",\"title\":\"Foreign key definition\"},\"foreignKeyName\":{\"type\":\"string\",\"title\":\"Foreign key name\"},\"deleteCascade\":{\"type\":\"boolean\",\"title\":\"Set delete cascade\"},\"deferrable\":{\"type\":\"boolean\",\"title\":\"Are constraints deferrable\"},\"initiallyDeferred\":{\"type\":\"boolean\",\"title\":\"Are constraints initially deferred\"}}}}}}}";

        VirtualFile file = VfsUtil.findFileByIoFile(getStoredFile(), true);

        if (file == null) {
            try {
                VirtualFile dir = VfsUtil.createDirectoryIfMissing(getStorageDirectory());
                if (dir != null) {
                    file = dir.createChildData(this, FILE_NAME);
                    file.setBinaryContent(LIQUIBASE_JSON_SCHEMA.getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonSchemaMappingsProjectConfiguration instance = JsonSchemaMappingsProjectConfiguration.getInstance(project);
        if (!instance.getStateMap().containsKey(SCHEMA_NAME)) {
            instance.addConfiguration(getLiquibaseConfiguration());
        }
    }

    private UserDefinedJsonSchemaConfiguration getLiquibaseConfiguration() {
        return new UserDefinedJsonSchemaConfiguration(SCHEMA_NAME,
                JsonSchemaVersion.SCHEMA_7,
                getRelativeStorageDirectory() + FILE_NAME,
                true,
                getListOfPatterns()
        );
    }

    private List<UserDefinedJsonSchemaConfiguration.Item> getListOfPatterns() {
        return Collections.singletonList(
                new UserDefinedJsonSchemaConfiguration.Item(
                        "*.json",
                        JsonMappingKind.Pattern));
    }

    private File getStoredFile() {
        return new File(getStorageDirectory() + FILE_NAME);
    }

    private String getStorageDirectory() {
        return this.project.getBasePath() + File.separator + CONFIG_DIR + File.separator;
    }

    private String getRelativeStorageDirectory() {
        return CONFIG_DIR + File.separator;
    }
}
