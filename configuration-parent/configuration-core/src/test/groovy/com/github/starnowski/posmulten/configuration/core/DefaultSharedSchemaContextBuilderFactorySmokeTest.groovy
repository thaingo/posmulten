package com.github.starnowski.posmulten.configuration.core

import com.github.starnowski.posmulten.configuration.core.model.*
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.starnowski.posmulten.postgresql.test.utils.MapBuilder.mapBuilder
import static java.util.Arrays.asList

class DefaultSharedSchemaContextBuilderFactorySmokeTest extends Specification {

    def tested = new DefaultSharedSchemaContextBuilderFactory()

    @Unroll
    def "should able to create builder components based on correct configuration object (#configuration)"() {
        when:
            def builder = tested.build(configuration)

        then:
            builder

        and: "builder should create correct context with ddl statements"
            builder.build().getSqlDefinitions().size() > 0

        where:
            configuration << [
                    new SharedSchemaContextConfiguration().setDefaultSchema("public")
                            .setGrantee("db-user")
                            .setValidTenantValueConstraint(new ValidTenantValueConstraintConfiguration()
                            .setTenantIdentifiersBlacklist(asList("xxxx")))
                            .setTables(asList(new TableEntry().setName("users")
                            .setRlsPolicy(new RLSPolicy()
                                .setName("rls_pol")
                                .setPrimaryKeyDefinition(new PrimaryKeyDefinition()
                                        .setNameForFunctionThatChecksIfRecordExistsInTable("does_users_record_exists")
                                        .setPrimaryKeyColumnsNameToTypeMap(mapBuilder().put("id", "biging").build())
                                ))
                            )),
                    new SharedSchemaContextConfiguration().setDefaultSchema("public")
                            .setGrantee("postgres-user")
                            .setForceRowLevelSecurityForTableOwner(true)
                            .setDefaultTenantIdColumn("tenant")
                            .setCurrentTenantIdentifierAsDefaultValueForTenantColumnInAllTables(true)
                            .setValidTenantValueConstraint(new ValidTenantValueConstraintConfiguration()
                            .setTenantIdentifiersBlacklist(asList("INVALID_Ten", "John_1234_Doe")))
                            .setTables(asList(new TableEntry().setName("users_info")
                            .setRlsPolicy(new RLSPolicy()
                            .setName("users_info_rls")
                                    .setPrimaryKeyDefinition(new PrimaryKeyDefinition()
                                        .setNameForFunctionThatChecksIfRecordExistsInTable("does_users_record_exists")
                                        .setPrimaryKeyColumnsNameToTypeMap(mapBuilder().put("id", "biging").build()))
                            ),
                            new TableEntry().setName("tweet")
                                    .setRlsPolicy(new RLSPolicy()
                                    .setName("tweet_rls_policy")
                                    .setPrimaryKeyDefinition(new PrimaryKeyDefinition()
                                    .setNameForFunctionThatChecksIfRecordExistsInTable("does_tweet_record_exists")
                                    .setPrimaryKeyColumnsNameToTypeMap(mapBuilder().put("uuid", "UUID").build()))
                                    ))
                    ),
                    new SharedSchemaContextConfiguration().setDefaultSchema("public")
                            .setGrantee("postgres-user")
                            .setForceRowLevelSecurityForTableOwner(true)
                            .setDefaultTenantIdColumn("tenant")
                            .setValidTenantValueConstraint(new ValidTenantValueConstraintConfiguration()
                            .setTenantIdentifiersBlacklist(asList("INVALID_Ten", "John_1234_Doe")))
                            .setTables(asList(new TableEntry().setName("users_info")
                            .setRlsPolicy(new RLSPolicy()
                            .setName("users_info_rls")
                            .setPrimaryKeyDefinition(new PrimaryKeyDefinition()
                                    .setNameForFunctionThatChecksIfRecordExistsInTable("does_users_record_exists")
                                    .setPrimaryKeyColumnsNameToTypeMap(mapBuilder().put("id", "biging").build())
                            )),
                            new TableEntry().setName("tweet")
                                    .setRlsPolicy(new RLSPolicy()
                                    .setName("tweet_rls_policy")
                                    .setPrimaryKeyDefinition(new PrimaryKeyDefinition()
                                            .setNameForFunctionThatChecksIfRecordExistsInTable("does_tweet_record_exists")
                                            .setPrimaryKeyColumnsNameToTypeMap(mapBuilder().put("uuid", "UUID").build())
                                    ))
                                    .setForeignKeys(asList(new ForeignKeyConfiguration()
                                    .setTableName("users_info")
                                    .setConstraintName("tweet_users_info_fk")
                                    .setForeignKeyPrimaryKeyColumnsMappings(mapBuilder().put("user_info_id", "id").build())
                            ))
                    )
                    )
            ]
    }
}
