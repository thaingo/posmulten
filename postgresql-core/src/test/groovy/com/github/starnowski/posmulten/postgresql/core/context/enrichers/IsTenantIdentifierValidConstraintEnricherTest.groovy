package com.github.starnowski.posmulten.postgresql.core.context.enrichers

import com.github.starnowski.posmulten.postgresql.core.common.SQLDefinition
import com.github.starnowski.posmulten.postgresql.core.context.DefaultSharedSchemaContextBuilder
import com.github.starnowski.posmulten.postgresql.core.context.SharedSchemaContext
import com.github.starnowski.posmulten.postgresql.core.rls.IIsTenantIdentifierValidConstraintProducerParameters
import com.github.starnowski.posmulten.postgresql.core.rls.IsTenantIdentifierValidConstraintProducer
import com.github.starnowski.posmulten.postgresql.core.rls.function.IIsTenantValidFunctionInvocationFactory
import com.github.starnowski.posmulten.postgresql.core.util.Pair
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

import static com.github.starnowski.posmulten.postgresql.test.utils.MapBuilder.mapBuilder
import static com.github.starnowski.posmulten.postgresql.core.context.SharedSchemaContextRequest.DEFAULT_TENANT_ID_COLUMN
import static java.util.stream.Collectors.toSet

class IsTenantIdentifierValidConstraintEnricherTest extends Specification {

    @Unroll
    def "should enrich shared schema context with SQL definition for the constraint that checks if tenant value is correct based on default values for shares schema context builder for schema #schema"()
    {
        given:
            def builder = (new DefaultSharedSchemaContextBuilder(schema))
                .createValidTenantValueConstraint(["ADFZ", "DFZCXVZ"], null, null)
            for (Pair tableNameTenantNamePair : tableNameTenantNamePairs)
            {
                builder.createRLSPolicyForTable(tableNameTenantNamePair.key, [:], tableNameTenantNamePair.value, null)
            }
            def sharedSchemaContextRequest = builder.getSharedSchemaContextRequestCopy()
            def context = new SharedSchemaContext()
            def mockedFunction = Mock(IIsTenantValidFunctionInvocationFactory)
            context.setIIsTenantValidFunctionInvocationFactory(mockedFunction)
            List<IIsTenantIdentifierValidConstraintProducerParameters> capturedParameters = new ArrayList<>()
            def mockedSQLDefinition = Mock(SQLDefinition)
            def producer = Mock(IsTenantIdentifierValidConstraintProducer)
            IsTenantIdentifierValidConstraintEnricher tested = new IsTenantIdentifierValidConstraintEnricher(producer)

        when:
            def result = tested.enrich(context, sharedSchemaContextRequest)

        then:
            tableNameTenantNamePairs.size() * producer.produce(_) >>  {
                parameters ->
                    capturedParameters.add(parameters[0])
                    mockedSQLDefinition
            }
            result.getSqlDefinitions().size() == tableNameTenantNamePairs.size()

        and: "passed producer parameters should contains object function"
            capturedParameters.stream().allMatch({parameter -> parameter.getIIsTenantValidFunctionInvocationFactory() == mockedFunction})

        and: "passed parameters should match with expected"
            capturedParameters.stream().map({ parameters -> map(parameters) }).collect(toSet()) == new HashSet<IsTenantIdentifierValidConstraintProducerKey>(expectedPassedParameters)

        where:
            schema          |   tableNameTenantNamePairs                                        ||  expectedPassedParameters
            null            |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("tenant_identifier_valid", "leads", null, "t_xxx"), tp("tenant_identifier_valid", "users", null, "tenant_id")]
            "public"        |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("tenant_identifier_valid", "leads", "public", "t_xxx"), tp("tenant_identifier_valid", "users", "public", "tenant_id")]
            "some_schema"   |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("tenant_identifier_valid", "leads", "some_schema", "t_xxx"), tp("tenant_identifier_valid", "users", "some_schema", "tenant_id")]
    }

    @Unroll
    def "should enrich shared schema context with SQL definition for schema #schema returned by producer component"()
    {
        given:
            def builder = (new DefaultSharedSchemaContextBuilder(schema))
                    .createValidTenantValueConstraint(["sfaf", "1324a"], null, null)
            for (IsTenantIdentifierValidConstraintProducerKey identifierValidConstraintProducerKey : passedParametersSqlDefinitionsResults.keySet())
            {
                builder.createRLSPolicyForTable(identifierValidConstraintProducerKey.getTableName(), [:], identifierValidConstraintProducerKey.getTenantColumnName(), null)
            }
            def sharedSchemaContextRequest = builder.getSharedSchemaContextRequestCopy()
            def context = new SharedSchemaContext()
            def mockedFunction = Mock(IIsTenantValidFunctionInvocationFactory)
            context.setIIsTenantValidFunctionInvocationFactory(mockedFunction)
            def producer = Mock(IsTenantIdentifierValidConstraintProducer)
            IsTenantIdentifierValidConstraintEnricher tested = new IsTenantIdentifierValidConstraintEnricher(producer)

        when:
            def result = tested.enrich(context, sharedSchemaContextRequest)

        then:
            passedParametersSqlDefinitionsResults.size() * producer.produce(_) >>  {
                parameters ->
                    IsTenantIdentifierValidConstraintProducerKey key = map((IIsTenantIdentifierValidConstraintProducerParameters)parameters[0])
                    passedParametersSqlDefinitionsResults.get(key)
            }
            result.getSqlDefinitions().size() == passedParametersSqlDefinitionsResults.size()
            result.getSqlDefinitions().stream().collect(Collectors.toSet()) == new HashSet<SQLDefinition>(passedParametersSqlDefinitionsResults.values())

        where:
            schema          |   passedParametersSqlDefinitionsResults
            null            |   mapBuilder().put(tp("tenant_identifier_valid", "leads", null, "t_xxx"), Mock(SQLDefinition)).put(tp("tenant_identifier_valid", "users", null, "tenant_id"), Mock(SQLDefinition)).build()
            "public"        |   mapBuilder().put(tp("tenant_identifier_valid", "leads", "public", "t_xxx"), Mock(SQLDefinition)).put(tp("tenant_identifier_valid", "users", "public", "tenant_id"), Mock(SQLDefinition)).build()
            "some_schema"   |   mapBuilder().put(tp("tenant_identifier_valid", "leads", "some_schema", "t_xxx"), Mock(SQLDefinition)).put(tp("tenant_identifier_valid", "users", "some_schema", "tenant_id"), Mock(SQLDefinition)).build()
    }

    @Unroll
    def "should enrich shared schema context with SQL definition for the constraint that checks if tenant value is correct with custom constraint name #constraintName for shares schema context builder for schema #schema"()
    {
        given:
            def builder = (new DefaultSharedSchemaContextBuilder(schema))
                    .createValidTenantValueConstraint(["ADFZ", "DFZCXVZ"], null, constraintName)
            for (Pair tableNameTenantNamePair : tableNameTenantNamePairs)
            {
                builder.createRLSPolicyForTable(tableNameTenantNamePair.key, [:], tableNameTenantNamePair.value, null)
            }
            def sharedSchemaContextRequest = builder.getSharedSchemaContextRequestCopy()
            def context = new SharedSchemaContext()
            def mockedFunction = Mock(IIsTenantValidFunctionInvocationFactory)
            context.setIIsTenantValidFunctionInvocationFactory(mockedFunction)
            List<IIsTenantIdentifierValidConstraintProducerParameters> capturedParameters = new ArrayList<>()
            def mockedSQLDefinition = Mock(SQLDefinition)
            def producer = Mock(IsTenantIdentifierValidConstraintProducer)
            IsTenantIdentifierValidConstraintEnricher tested = new IsTenantIdentifierValidConstraintEnricher(producer)

        when:
            def result = tested.enrich(context, sharedSchemaContextRequest)

        then:
            tableNameTenantNamePairs.size() * producer.produce(_) >>  {
                parameters ->
                    capturedParameters.add(parameters[0])
                    mockedSQLDefinition
            }
            result.getSqlDefinitions().size() == tableNameTenantNamePairs.size()

        and: "passed producer parameters should contains object function"
            capturedParameters.stream().allMatch({parameter -> parameter.getIIsTenantValidFunctionInvocationFactory() == mockedFunction})

        and: "passed parameters should match with expected"
            capturedParameters.stream().map({ parameters -> map(parameters) }).collect(toSet()) == new HashSet<IsTenantIdentifierValidConstraintProducerKey>(expectedPassedParameters)

        where:
            schema          |   constraintName                  |   tableNameTenantNamePairs                                        ||  expectedPassedParameters
            null            |   "is_valid"                      |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("is_valid", "leads", null, "t_xxx"), tp("is_valid", "users", null, "tenant_id")]
            "public"        |   "is_valid"                      |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("is_valid", "leads", "public", "t_xxx"), tp("is_valid", "users", "public", "tenant_id")]
            "some_schema"   |   "is_valid"                      |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("is_valid", "leads", "some_schema", "t_xxx"), tp("is_valid", "users", "some_schema", "tenant_id")]
            null            |   "tenant_has_to_be_valid"        |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("tenant_has_to_be_valid", "leads", null, "t_xxx"), tp("tenant_has_to_be_valid", "users", null, "tenant_id")]
            "public"        |   "tenant_has_to_be_valid"        |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("tenant_has_to_be_valid", "leads", "public", "t_xxx"), tp("tenant_has_to_be_valid", "users", "public", "tenant_id")]
            "some_schema"   |   "tenant_has_to_be_valid"        |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    ||  [tp("tenant_has_to_be_valid", "leads", "some_schema", "t_xxx"), tp("tenant_has_to_be_valid", "users", "some_schema", "tenant_id")]
    }

    @Unroll
    def "should enrich shared schema context with SQL definition for the constraint that checks if tenant value is correct with custom constraint per table for shares schema context builder for schema #schema, constraint name #constraintName and custom constraint names per table (tableConstraintNames)"()
    {
        given:
            def builder = (new DefaultSharedSchemaContextBuilder(schema))
                    .createValidTenantValueConstraint(["ADFZ", "DFZCXVZ"], null, constraintName)
            for (Pair tableNameTenantNamePair : tableNameTenantNamePairs)
            {
                builder.createRLSPolicyForTable(tableNameTenantNamePair.key, [:], tableNameTenantNamePair.value, null)
            }
            for (Map.Entry<String, String> tableConstraintName: tableConstraintNames.entrySet())
            {
                builder.registerCustomValidTenantValueConstraintNameForTable(tableConstraintName.getKey(), tableConstraintName.getValue())
            }
            def sharedSchemaContextRequest = builder.getSharedSchemaContextRequestCopy()
            def context = new SharedSchemaContext()
            def mockedFunction = Mock(IIsTenantValidFunctionInvocationFactory)
            context.setIIsTenantValidFunctionInvocationFactory(mockedFunction)
            List<IIsTenantIdentifierValidConstraintProducerParameters> capturedParameters = new ArrayList<>()
            def mockedSQLDefinition = Mock(SQLDefinition)
            def producer = Mock(IsTenantIdentifierValidConstraintProducer)
            IsTenantIdentifierValidConstraintEnricher tested = new IsTenantIdentifierValidConstraintEnricher(producer)

        when:
            def result = tested.enrich(context, sharedSchemaContextRequest)

        then:
            tableNameTenantNamePairs.size() * producer.produce(_) >>  {
                parameters ->
                    capturedParameters.add(parameters[0])
                    mockedSQLDefinition
            }
            result.getSqlDefinitions().size() == tableNameTenantNamePairs.size()

        and: "passed producer parameters should contains object function"
            capturedParameters.stream().allMatch({parameter -> parameter.getIIsTenantValidFunctionInvocationFactory() == mockedFunction})

        and: "passed parameters should match with expected"
            capturedParameters.stream().map({ parameters -> map(parameters) }).collect(toSet()) == new HashSet<IsTenantIdentifierValidConstraintProducerKey>(expectedPassedParameters)

        where:
            schema          |   constraintName                  |   tableNameTenantNamePairs                                        |   tableConstraintNames                                ||  expectedPassedParameters
            null            |   null                            |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [users: "xxx_constraint_yyy"]                       ||  [tp("tenant_identifier_valid", "leads", null, "t_xxx"), tp("xxx_constraint_yyy", "users", null, "tenant_id")]
            "public"        |   null                            |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [leads: "valid", users: "xxx_constraint_yyy"]       ||  [tp("valid", "leads", "public", "t_xxx"), tp("xxx_constraint_yyy", "users", "public", "tenant_id")]
            "some_schema"   |   null                            |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [leads: "tenant_val"]                               ||  [tp("tenant_val", "leads", "some_schema", "t_xxx"), tp("tenant_identifier_valid", "users", "some_schema", "tenant_id")]
            null            |   "is_valid"                      |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [users: "xxx_constraint_yyy"]                       ||  [tp("is_valid", "leads", null, "t_xxx"), tp("xxx_constraint_yyy", "users", null, "tenant_id")]
            "public"        |   "is_valid"                      |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [leads: "valid", users: "xxx_constraint_yyy"]       ||  [tp("valid", "leads", "public", "t_xxx"), tp("xxx_constraint_yyy", "users", "public", "tenant_id")]
            "some_schema"   |   "is_valid"                      |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [leads: "tenant_val"]                               ||  [tp("tenant_val", "leads", "some_schema", "t_xxx"), tp("is_valid", "users", "some_schema", "tenant_id")]
            null            |   "tenant_has_to_be_valid"        |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [leads: "asdf"]                                     ||  [tp("asdf", "leads", null, "t_xxx"), tp("tenant_has_to_be_valid", "users", null, "tenant_id")]
            "public"        |   "tenant_has_to_be_valid"        |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [users: "const_tenant"]                             ||  [tp("tenant_has_to_be_valid", "leads", "public", "t_xxx"), tp("const_tenant", "users", "public", "tenant_id")]
            "some_schema"   |   "tenant_has_to_be_valid"        |   [new Pair("users", "tenant_id"), new Pair("leads", "t_xxx")]    |   [users: "const_tenant", leads: "valid"]             ||  [tp("valid", "leads", "some_schema", "t_xxx"), tp("const_tenant", "users", "some_schema", "tenant_id")]
    }

    @Unroll
    def "should enrich shared schema context with SQL definition for the constraint that checks if tenant value with usage of default tenant column name when column na #schema"()
    {
        given:
            def builder = (new DefaultSharedSchemaContextBuilder(schema))
                    .createValidTenantValueConstraint(["ADFZ", "DFZCXVZ"], null, null)
            for (Pair tableNameTenantNamePair : tableNameTenantNamePairs)
            {
                builder.createRLSPolicyForTable(tableNameTenantNamePair.key, [:], tableNameTenantNamePair.value, null)
            }
            def sharedSchemaContextRequest = builder.getSharedSchemaContextRequestCopy()
            def context = new SharedSchemaContext()
            def mockedFunction = Mock(IIsTenantValidFunctionInvocationFactory)
            context.setIIsTenantValidFunctionInvocationFactory(mockedFunction)
            List<IIsTenantIdentifierValidConstraintProducerParameters> capturedParameters = new ArrayList<>()
            def mockedSQLDefinition = Mock(SQLDefinition)
            def producer = Mock(IsTenantIdentifierValidConstraintProducer)
            IsTenantIdentifierValidConstraintEnricher tested = new IsTenantIdentifierValidConstraintEnricher(producer)

        when:
            tested.enrich(context, sharedSchemaContextRequest)

        then:
            tableNameTenantNamePairs.size() * producer.produce(_) >>  {
                parameters ->
                    capturedParameters.add(parameters[0])
                    mockedSQLDefinition
            }
            capturedParameters.stream().map({ parameters -> map(parameters) }).collect(toSet()) == new HashSet<IsTenantIdentifierValidConstraintProducerKey>(expectedPassedParameters)

        where:
            schema          |   tableNameTenantNamePairs                                    ||  expectedPassedParameters
            null            |   [new Pair("users", null), new Pair("leads", "t_xxx")]       ||  [tp("tenant_identifier_valid", "leads", null, "t_xxx"), tp("tenant_identifier_valid", "users", null, DEFAULT_TENANT_ID_COLUMN)]
            "public"        |   [new Pair("users", "tenant_id"), new Pair("leads", null)]   ||  [tp("tenant_identifier_valid", "leads", "public", DEFAULT_TENANT_ID_COLUMN), tp("tenant_identifier_valid", "users", "public", "tenant_id")]
            "some_schema"   |   [new Pair("users", null), new Pair("leads", null)]          ||  [tp("tenant_identifier_valid", "leads", "some_schema", DEFAULT_TENANT_ID_COLUMN), tp("tenant_identifier_valid", "users", "some_schema", DEFAULT_TENANT_ID_COLUMN)]
    }

    static IsTenantIdentifierValidConstraintProducerKey tp(String constraintName, String tableName, String tableSchema, String tenantColumnName)
    {
        new IsTenantIdentifierValidConstraintProducerKey(constraintName, tableName, tableSchema, tenantColumnName)
    }

    static IsTenantIdentifierValidConstraintProducerKey map(IIsTenantIdentifierValidConstraintProducerParameters parameters)
    {
        new IsTenantIdentifierValidConstraintProducerKey(parameters.getConstraintName(), parameters.getTableName(), parameters.getTableSchema(), parameters.getTenantColumnName())
    }

    static class IsTenantIdentifierValidConstraintProducerKey
    {
        private final String constraintName
        private final String tableName
        private final String tableSchema
        private final String tenantColumnName

        String getConstraintName() {
            return constraintName
        }

        String getTableName() {
            return tableName
        }

        String getTableSchema() {
            return tableSchema
        }

        String getTenantColumnName() {
            return tenantColumnName
        }
        IsTenantIdentifierValidConstraintProducerKey(String constraintName, String tableName, String tableSchema, String tenantColumnName)
        {
            this.constraintName = constraintName
            this.tableName = tableName
            this.tableSchema = tableSchema
            this.tenantColumnName = tenantColumnName
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            IsTenantIdentifierValidConstraintProducerKey that = (IsTenantIdentifierValidConstraintProducerKey) o

            if (constraintName != that.constraintName) return false
            if (tableName != that.tableName) return false
            if (tableSchema != that.tableSchema) return false
            if (tenantColumnName != that.tenantColumnName) return false

            return true
        }

        int hashCode() {
            int result
            result = (constraintName != null ? constraintName.hashCode() : 0)
            result = 31 * result + (tableName != null ? tableName.hashCode() : 0)
            result = 31 * result + (tableSchema != null ? tableSchema.hashCode() : 0)
            result = 31 * result + (tenantColumnName != null ? tenantColumnName.hashCode() : 0)
            return result
        }

        @Override
        public String toString() {
            return "IsTenantIdentifierValidConstraintProducerKey{" +
                    "constraintName='" + constraintName + '\'' +
                    ", tableName='" + tableName + '\'' +
                    ", tableSchema='" + tableSchema + '\'' +
                    ", tenantColumnName='" + tenantColumnName + '\'' +
                    '}';
        }
    }
}
