package com.github.starnowski.posmulten.configuration.core.model

import com.github.starnowski.posmulten.configuration.core.DefaultSharedSchemaContextBuilderConfigurationEnricher
import com.github.starnowski.posmulten.postgresql.core.context.DefaultSharedSchemaContextBuilder
import spock.lang.Specification
import spock.lang.Unroll

class DefaultSharedSchemaContextBuilderConfigurationEnricherTest extends Specification {

    def tested = new DefaultSharedSchemaContextBuilderConfigurationEnricher()

    @Unroll
    def "should set builder component with specific properties currentTenantIdPropertyType (#currentTenantIdPropertyType), currentTenantIdProperty (#currentTenantIdProperty), getCurrentTenantIdFunctionName (#getCurrentTenantIdFunctionName), setCurrentTenantIdFunctionName (#setCurrentTenantIdFunctionName), equalsCurrentTenantIdentifierFunctionName (#equalsCurrentTenantIdentifierFunctionName)"()
    {
        given:
            def builder = Mock(DefaultSharedSchemaContextBuilder)
            def configuration = new SharedSchemaContextConfiguration()
            .setCurrentTenantIdPropertyType(currentTenantIdPropertyType)
            .setCurrentTenantIdProperty(currentTenantIdProperty)
            .setGetCurrentTenantIdFunctionName(getCurrentTenantIdFunctionName)
            .setSetCurrentTenantIdFunctionName(setCurrentTenantIdFunctionName)
            .setEqualsCurrentTenantIdentifierFunctionName(equalsCurrentTenantIdentifierFunctionName)

        when:
            def result = tested.enrich(builder, configuration)

        then:
            result == builder
            1 * builder.setCurrentTenantIdPropertyType(currentTenantIdPropertyType)
            1 * builder.setCurrentTenantIdProperty(currentTenantIdPropertyType)
            1 * builder.setGetCurrentTenantIdFunctionName(getCurrentTenantIdFunctionName)
            1 * builder.setSetCurrentTenantIdFunctionName(setCurrentTenantIdFunctionName)
            1 * builder.setSetCurrentTenantIdFunctionName(equalsCurrentTenantIdentifierFunctionName)


        where:
            currentTenantIdPropertyType |   currentTenantIdProperty |   getCurrentTenantIdFunctionName  |   setCurrentTenantIdFunctionName  |   equalsCurrentTenantIdentifierFunctionName
            "VARCHAR(37)"               |   "customer_d"            |   "what_is_tenant_id"             |   "tenant_is_now"                 |   "is_it_this_tenant_id"
            "UUID"                      |   "tenantId"              |   "get_current_t"                 |   "set_t"                         |   "equals_tenant"
    }
}
