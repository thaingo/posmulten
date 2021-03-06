package com.github.starnowski.posmulten.postgresql.core.rls.function

import com.github.starnowski.posmulten.postgresql.core.common.function.AbstractFunctionFactoryTest
import spock.lang.Unroll

class SetCurrentTenantIdFunctionProducerTest extends AbstractFunctionFactoryTest {

    private static String VALID_CURRENT_TENANT_ID_PROPERTY_NAME = "c.c_ten"

    def tested = new SetCurrentTenantIdFunctionProducer()

    @Unroll
    def "should generate statement that creates function '#testFunctionName' for schema '#testSchema' with argument type '#testArgumentType' (null means 'text') which sets value for property '#testCurrentTenantIdProperty'" () {
        expect:
            tested.produce(new SetCurrentTenantIdFunctionProducerParameters(testFunctionName, testCurrentTenantIdProperty, testSchema, testArgumentType)).getCreateScript() == expectedStatement

        where:
            testSchema              |   testFunctionName            |   testCurrentTenantIdProperty             |   testArgumentType    || expectedStatement
            null                    |   "set_current_tenant"        |   VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   null                ||  "CREATE OR REPLACE FUNCTION set_current_tenant(text) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('c.c_ten', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            "public"                |   "set_current_tenant"        |   VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   null                ||  "CREATE OR REPLACE FUNCTION public.set_current_tenant(text) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('c.c_ten', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            "non_public_schema"     |   "set_current_tenant"        |   VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   null                ||  "CREATE OR REPLACE FUNCTION non_public_schema.set_current_tenant(text) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('c.c_ten', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            null                    |   "set_current_tenant"        |   VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   "text"              ||  "CREATE OR REPLACE FUNCTION set_current_tenant(text) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('c.c_ten', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            "public"                |   "set_current_tenant"        |   VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   "text"              ||  "CREATE OR REPLACE FUNCTION public.set_current_tenant(text) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('c.c_ten', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            "non_public_schema"     |   "set_current_tenant"        |   VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   "text"              ||  "CREATE OR REPLACE FUNCTION non_public_schema.set_current_tenant(text) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('c.c_ten', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            null                    |   "this_is_tenant"            |   "con.tenant_id"                         |   "VARCHAR(128)"      ||  "CREATE OR REPLACE FUNCTION this_is_tenant(VARCHAR(128)) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('con.tenant_id', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            "public"                |   "this_is_tenant"            |   "pos.tenant"                            |   "VARCHAR(32)"       ||  "CREATE OR REPLACE FUNCTION public.this_is_tenant(VARCHAR(32)) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('pos.tenant', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
            "non_public_schema"     |   "this_is_tenant"            |   "t.id"                                  |   "text"              ||  "CREATE OR REPLACE FUNCTION non_public_schema.this_is_tenant(text) RETURNS VOID AS \$\$\nBEGIN\nPERFORM set_config('t.id', \$1, false);\nEND\n\$\$ LANGUAGE plpgsql\nVOLATILE;"
    }

    @Unroll
    def "should generate statement that invokes function '#testFunctionName' and set current tenant value ('#currentTenantValue') for schema '#testSchema' " () {
        expect:
            tested.produce(new SetCurrentTenantIdFunctionProducerParameters(testFunctionName, VALID_CURRENT_TENANT_ID_PROPERTY_NAME, testSchema, null)).generateStatementThatSetTenant(currentTenantValue) == expectedStatement

        where:
            testSchema              |   testFunctionName            |   currentTenantValue      || expectedStatement
            null                    |   "set_current_tenant"        |   "DFSDGFSG"              ||  "SELECT set_current_tenant('DFSDGFSG');"
            "public"                |   "set_current_tenant"        |   "DFSDGFSG"              ||  "SELECT public.set_current_tenant('DFSDGFSG');"
            "non_public_schema"     |   "set_current_tenant"        |   "DFSDGFSG"              ||  "SELECT non_public_schema.set_current_tenant('DFSDGFSG');"
            null                    |   "set_current_tenant"        |   "xxxsdfadf"             ||  "SELECT set_current_tenant('xxxsdfadf');"
            "public"                |   "set_current_tenant"        |   "xxxsdfadf"             ||  "SELECT public.set_current_tenant('xxxsdfadf');"
            "non_public_schema"     |   "set_current_tenant"        |   "xxxsdfadf"             ||  "SELECT non_public_schema.set_current_tenant('xxxsdfadf');"
            null                    |   "this_is_tenant"            |   "1234433"               ||  "SELECT this_is_tenant('1234433');"
            "public"                |   "this_is_tenant"            |   "1234433"               ||  "SELECT public.this_is_tenant('1234433');"
            "non_public_schema"     |   "this_is_tenant"            |   "1234433"               ||  "SELECT non_public_schema.this_is_tenant('1234433');"
    }

    @Unroll
    def "should generate statement that invokes function '#testFunctionName' and could be used by prepared statement ('#currentTenantValue') for schema '#testSchema' " () {
        expect:
            tested.produce(new SetCurrentTenantIdFunctionProducerParameters(testFunctionName, VALID_CURRENT_TENANT_ID_PROPERTY_NAME, testSchema, null)).returnPreparedStatementThatSetCurrentTenant() == expectedStatement

        where:
            testSchema              |   testFunctionName            || expectedStatement
            null                    |   "set_current_tenant"        ||  "SELECT set_current_tenant(?);"
            "public"                |   "set_current_tenant"        ||  "SELECT public.set_current_tenant(?);"
            "non_public_schema"     |   "set_current_tenant"        ||  "SELECT non_public_schema.set_current_tenant(?);"
            null                    |   "this_is_tenant"            ||  "SELECT this_is_tenant(?);"
            "public"                |   "this_is_tenant"            ||  "SELECT public.this_is_tenant(?);"
            "non_public_schema"     |   "this_is_tenant"            ||  "SELECT non_public_schema.this_is_tenant(?);"
    }

    @Unroll
    def "should throw exception of type 'IllegalArgumentException' when tenant id property name is null, even if the rest of parameters are correct, function name #functionName, schema #testSchema, argument type #testArgumentType"()
    {
        when:
            tested.produce(new SetCurrentTenantIdFunctionProducerParameters(functionName, null, testSchema, testArgumentType))

        then:
            def ex = thrown(IllegalArgumentException.class)

        and: "exception should have correct message"
            ex.message == "Tenant id property name cannot be null"

        where:
            testSchema              |   functionName                |   testArgumentType
            null                    |   "return_current_tenant"     |   null
            "public"                |   "return_current_tenant"     |   null
            "non_public_schema"     |   "return_current_tenant"     |   null
            null                    |   "return_current_tenant"     |   "text"
            "public"                |   "return_current_tenant"     |   "text"
            "non_public_schema"     |   "return_current_tenant"     |   "text"
            null                    |   "get_current_tenant"        |   "VARCHAR(128)"
            "public"                |   "get_current_tenant"        |   "VARCHAR(32)"
            "non_public_schema"     |   "get_current_tenant"        |   "text"
    }

    @Unroll
    def "should throw exception of type 'IllegalArgumentException' when tenant id property name is blank ('#testCurrentTenantIdProperty'), even if the rest of parameters are correct, function name #functionName, schema #testSchema, return type #testArgumentType"()
    {
        when:
            tested.produce(new SetCurrentTenantIdFunctionProducerParameters(functionName, testCurrentTenantIdProperty, testSchema, testArgumentType))

        then:
            def ex = thrown(IllegalArgumentException.class)

        and: "exception should have correct message"
            ex.message == "Tenant id property name cannot be blank"

        where:
            testSchema              |   functionName                |   testArgumentType    | testCurrentTenantIdProperty
            null                    |   "return_current_tenant"     |   null                | ""
            "public"                |   "return_current_tenant"     |   null                | "     "
            "non_public_schema"     |   "return_current_tenant"     |   null                | " "
            null                    |   "return_current_tenant"     |   "text"              | "         "
            "public"                |   "return_current_tenant"     |   "text"              | " "
            "non_public_schema"     |   "return_current_tenant"     |   "text"              | "             "
            null                    |   "get_current_tenant"        |   "VARCHAR(128)"      | " "
            "public"                |   "get_current_tenant"        |   "VARCHAR(32)"       | ""
            "non_public_schema"     |   "get_current_tenant"        |   "text"              | "          "
    }

    @Unroll
    def "should throw exception of type 'IllegalArgumentException' when the argument type is blank ('#testArgumentType'), even if the rest of parameters are correct, function name #functionName, tenant id property name #testCurrentTenantIdProperty, schema name #testSchema"()
    {
        when:
            tested.produce(new SetCurrentTenantIdFunctionProducerParameters(functionName, testCurrentTenantIdProperty, testSchema, testArgumentType))

        then:
            def ex = thrown(IllegalArgumentException.class)

        and: "exception should have correct message"
            ex.message == "Argument type cannot be blank"

        where:
            testCurrentTenantIdProperty             |   functionName                |   testSchema          | testArgumentType
            VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   "return_current_tenant"     |   null                | ""
            "pos.tenant"                            |   "return_current_tenant"     |   null                | "     "
            "t.id"                                  |   "return_current_tenant"     |   null                | " "
            VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   "return_current_tenant"     |   "public"            | "         "
            "pos.tenant"                            |   "return_current_tenant"     |   "public"            | " "
            "t.id"                                  |   "return_current_tenant"     |   "public"            | "             "
            VALID_CURRENT_TENANT_ID_PROPERTY_NAME   |   "get_current_tenant"        |   "non_public_schema" | " "
            "pos.tenant"                            |   "get_current_tenant"        |   "non_public_schema" | ""
            "t.id"                                  |   "get_current_tenant"        |   "non_public_schema" | "          "
    }

    @Override
    protected returnTestedObject() {
        tested
    }

    @Override
    protected returnCorrectParametersSpyObject() {
        Spy(SetCurrentTenantIdFunctionProducerParameters, constructorArgs: ["set_current_tenant", VALID_CURRENT_TENANT_ID_PROPERTY_NAME , "public", "VARCHAR(32)"])
    }
}
