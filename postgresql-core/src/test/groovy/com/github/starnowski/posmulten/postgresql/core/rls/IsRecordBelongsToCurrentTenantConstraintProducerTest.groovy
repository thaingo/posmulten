package com.github.starnowski.posmulten.postgresql.core.rls

import com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentValue
import com.github.starnowski.posmulten.postgresql.core.rls.function.IsRecordBelongsToCurrentTenantFunctionInvocationFactory
import com.github.starnowski.posmulten.postgresql.test.utils.RandomString
import spock.lang.Unroll

import static com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentValue.forReference

class IsRecordBelongsToCurrentTenantConstraintProducerTest extends AbstractConstraintProducerTest<IsRecordBelongsToCurrentTenantConstraintProducerParameters, IsRecordBelongsToCurrentTenantConstraintProducer> {

    def tested = new IsRecordBelongsToCurrentTenantConstraintProducer()

    @Unroll
    def "should return statement (#expectedStatement) that adds '#constraintName' constraint to table (#table) and schema (#schema)"()
    {
        given:
            Map<String, FunctionArgumentValue> capturedPrimaryColumnsValuesMap = null
            IsRecordBelongsToCurrentTenantFunctionInvocationFactory isRecordBelongsToCurrentTenantFunctionInvocationFactory =
                    {arguments ->
                        capturedPrimaryColumnsValuesMap = arguments
                        conditionStatement
                    }
            def parameters = DefaultIsRecordBelongsToCurrentTenantConstraintProducerParameters.builder()
                    .withConstraintName(constraintName)
                    .withTableName(table)
                    .withTableSchema(schema)
                    .withIsRecordBelongsToCurrentTenantFunctionInvocationFactory(isRecordBelongsToCurrentTenantFunctionInvocationFactory)
                    .withPrimaryColumnsValuesMap(primaryColumnsValuesMap).build()
        when:
            def definition = tested.produce(parameters)

        then:
            definition.getCreateScript() == expectedStatement

        and: "correct map parameters should be passed to the component that implements the IsRecordBelongsToCurrentTenantFunctionInvocationFactory type"
            primaryColumnsValuesMap == capturedPrimaryColumnsValuesMap

        where:
            constraintName      |   schema      | table     |   conditionStatement              |   primaryColumnsValuesMap                                                             ||	expectedStatement
            "sss"               |   null        | "users"   |   "cccsss"                        |   [z2 : forReference("id")]                                                           ||  "ALTER TABLE \"users\" ADD CONSTRAINT sss CHECK ((id IS NULL) OR (cccsss));"
            "sss"               |   "public"    | "users"   |   "cccsss"                        |   [ff : forReference("id"), hggf: forReference("abc_user_id")]                        ||  "ALTER TABLE \"public\".\"users\" ADD CONSTRAINT sss CHECK ((abc_user_id IS NULL OR id IS NULL) OR (cccsss));"
            "sss"               |   "secondary" | "users"   |   "cccsss"                        |   [x1 : forReference("userId"), asdf: forReference("abc_user_id")]                    ||  "ALTER TABLE \"secondary\".\"users\" ADD CONSTRAINT sss CHECK ((abc_user_id IS NULL OR userId IS NULL) OR (cccsss));"
            "user_belongs_tt"   |   "secondary" | "users"   |   "cccsss"                        |   [ss : forReference("uuid")]                                                         ||  "ALTER TABLE \"secondary\".\"users\" ADD CONSTRAINT user_belongs_tt CHECK ((uuid IS NULL) OR (cccsss));"
            "user_belongs_tt"   |   "secondary" | "users"   |   "is_tenant_correct(tenant_id)"  |   [v : forReference("secondary_colId"), rv : forReference("uuid")]                    ||  "ALTER TABLE \"secondary\".\"users\" ADD CONSTRAINT user_belongs_tt CHECK ((secondary_colId IS NULL OR uuid IS NULL) OR (is_tenant_correct(tenant_id)));"
            "user_belongs_tt"   |   "secondary" | "users"   |   "is_it_really_my_tenant(t)"     |   [x1 : forReference("c"), uuu : forReference("a"), ranV : forReference("b")]         ||  "ALTER TABLE \"secondary\".\"users\" ADD CONSTRAINT user_belongs_tt CHECK ((a IS NULL OR b IS NULL OR c IS NULL) OR (is_it_really_my_tenant(t)));"
    }

    @Unroll
    def "should return statement (#expectedStatement) that drops '#constraintName' constraint for table (#table) and schema (#schema)"()
    {
        given:
            def randomString = new RandomString(5, new Random(), RandomString.lower)
            IsRecordBelongsToCurrentTenantFunctionInvocationFactory isRecordBelongsToCurrentTenantFunctionInvocationFactory =
                    {
                        randomString.nextString()
                    }
            Map<String, FunctionArgumentValue> primaryColumnsValuesMap = generateRandomPrimaryColumnsValuesMap()
            def parameters = DefaultIsRecordBelongsToCurrentTenantConstraintProducerParameters.builder()
                    .withConstraintName(constraintName)
                    .withTableName(table)
                    .withTableSchema(schema)
                    .withIsRecordBelongsToCurrentTenantFunctionInvocationFactory(isRecordBelongsToCurrentTenantFunctionInvocationFactory)
                    .withPrimaryColumnsValuesMap(primaryColumnsValuesMap).build()
        when:
            def definition = tested.produce(parameters)

        then:
            definition.getDropScript() == expectedStatement

        where:
            constraintName      |   schema      | table     ||	expectedStatement
            "sss"               |   null        | "users"   ||  "ALTER TABLE \"users\" DROP CONSTRAINT IF EXISTS sss;"
            "const_1"           |   "public"    | "users"   ||  "ALTER TABLE \"public\".\"users\" DROP CONSTRAINT IF EXISTS const_1;"
            "sss"               |   "secondary" | "users"   ||  "ALTER TABLE \"secondary\".\"users\" DROP CONSTRAINT IF EXISTS sss;"
            "user_belongs_tt"   |   "secondary" | "users"   ||  "ALTER TABLE \"secondary\".\"users\" DROP CONSTRAINT IF EXISTS user_belongs_tt;"
    }

    @Unroll
    def "should return correct definition based on the generic parameters object"()
    {
        given:
            def parameters = returnCorrectParametersMockObject()

        when:
            def definition = tested.produce(parameters)

        then:
            definition.getCreateScript() ==~ /ALTER TABLE "public"\."users" ADD CONSTRAINT const_1 CHECK \(\((.* IS NULL)+( OR )*\) OR \(current_tenant\(\)\)\);/
            definition.getDropScript() == "ALTER TABLE \"public\".\"users\" DROP CONSTRAINT IF EXISTS const_1;"
    }

    def "should throw an exception of type 'IllegalArgumentException' when the object of type IsRecordBelongsToCurrentTenantFunctionInvocationFactory is null" () {
        given:
            def parameters = returnCorrectParametersMockObject()

        when:
            tested.produce(parameters)

        then:
            _ * parameters.getIsRecordBelongsToCurrentTenantFunctionInvocationFactory() >> null
            def ex = thrown(IllegalArgumentException.class)

        and: "exception should have correct message"
            ex.message == "Object of type IsRecordBelongsToCurrentTenantFunctionInvocationFactory cannot be null"
    }

    Map<String, FunctionArgumentValue> generateRandomPrimaryColumnsValuesMap()
    {
        def randomString = new RandomString(5, new Random(), RandomString.lower)
        def random = new Random()
        Map<String, FunctionArgumentValue> primaryColumnsValuesMap = new HashMap<>()
        def mapSize = random.nextInt(5) + 1
        for (int i = 0; i < mapSize; i++)
        {
            primaryColumnsValuesMap.put(randomString.nextString(), forReference(randomString.nextString()))
        }
        primaryColumnsValuesMap
    }

    static FunctionArgumentValue randomFAV()
    {
        def randomString = new RandomString(5, new Random(), RandomString.lower)
        forReference(randomString.nextString())
    }

    IsRecordBelongsToCurrentTenantConstraintProducerParameters returnCorrectParametersMockObject() {
        IsRecordBelongsToCurrentTenantFunctionInvocationFactory isRecordBelongsToCurrentTenantFunctionInvocationFactory =
                {
                    "current_tenant()"
                }
        Map<String, FunctionArgumentValue> primaryColumnsValuesMap = generateRandomPrimaryColumnsValuesMap()
        IsRecordBelongsToCurrentTenantConstraintProducerParameters mock = Mock(IsRecordBelongsToCurrentTenantConstraintProducerParameters)
        mock.getConstraintName() >> "const_1"
        mock.getTableName() >> "users"
        mock.getTableSchema() >> "public"
        mock.getIsRecordBelongsToCurrentTenantFunctionInvocationFactory() >> isRecordBelongsToCurrentTenantFunctionInvocationFactory
        mock.getPrimaryColumnsValuesMap() >> primaryColumnsValuesMap
        mock
    }

    IsRecordBelongsToCurrentTenantConstraintProducer returnTestedObject() {
        tested
    }
}
