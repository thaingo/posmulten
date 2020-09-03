/**
 *     Posmulten library is an open-source project for the generation
 *     of SQL DDL statements that make it easy for implementation of
 *     Shared Schema Multi-tenancy strategy via the Row Security
 *     Policies in the Postgres database.
 *
 *     Copyright (C) 2020  Szymon Tarnowski
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */
package com.github.starnowski.posmulten.postgresql.core.context.enrichers;

import com.github.starnowski.posmulten.postgresql.core.context.ISharedSchemaContext;
import com.github.starnowski.posmulten.postgresql.core.context.ISharedSchemaContextEnricher;
import com.github.starnowski.posmulten.postgresql.core.context.SharedSchemaContextRequest;
import com.github.starnowski.posmulten.postgresql.core.rls.function.*;

public class SetCurrentTenantIdFunctionDefinitionEnricher implements ISharedSchemaContextEnricher {

    private SetCurrentTenantIdFunctionProducer setCurrentTenantIdFunctionProducer = new SetCurrentTenantIdFunctionProducer();

    @Override
    public ISharedSchemaContext enrich(ISharedSchemaContext context, SharedSchemaContextRequest request) {
        String functionName = request.getSetCurrentTenantIdFunctionName() == null ? "set_current_tenant_id" : request.getSetCurrentTenantIdFunctionName();
        SetCurrentTenantIdFunctionDefinition sqlDefinition = setCurrentTenantIdFunctionProducer.produce(new SetCurrentTenantIdFunctionProducerParameters(functionName, request.getCurrentTenantIdProperty(), request.getDefaultSchema(), request.getCurrentTenantIdPropertyType()));
        context.addSQLDefinition(sqlDefinition);
        context.setISetCurrentTenantIdFunctionInvocationFactory(sqlDefinition);
        context.setISetCurrentTenantIdFunctionPreparedStatementInvocationFactory(sqlDefinition);
        return context;
    }

    void setSetCurrentTenantIdFunctionProducer(SetCurrentTenantIdFunctionProducer setCurrentTenantIdFunctionProducer) {
        this.setCurrentTenantIdFunctionProducer = setCurrentTenantIdFunctionProducer;
    }
}
