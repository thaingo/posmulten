package com.github.starnowski.posmulten.postgresql.core.rls.function;

import com.github.starnowski.posmulten.postgresql.core.common.function.IFunctionArgument;
import com.github.starnowski.posmulten.postgresql.core.common.function.IFunctionFactoryParameters;
import javafx.util.Pair;

import java.util.List;

import static com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentBuilder.forType;

public interface AbstractIsRecordBelongsToCurrentTenantProducerParameters extends IFunctionFactoryParameters {

    List<Pair<String, IFunctionArgument>> getKeyColumnsPairsList();

    String getTenantColumn();

    String getRecordTableName();

    String getRecordSchemaName();

    IGetCurrentTenantIdFunctionInvocationFactory getIGetCurrentTenantIdFunctionInvocationFactory();

    static Pair<String, IFunctionArgument> pairOfColumnWithType(String column, String type)
    {
        return new Pair<>(column, forType(type));
    }
}