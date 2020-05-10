package com.github.starnowski.posmulten.postgresql.core.rls.function;

import com.github.starnowski.posmulten.postgresql.core.common.function.AbstractFunctionFactory;
import com.github.starnowski.posmulten.postgresql.core.common.function.DefaultFunctionDefinition;
import com.github.starnowski.posmulten.postgresql.core.common.function.IFunctionArgument;
import com.github.starnowski.posmulten.postgresql.core.common.function.IFunctionDefinition;

import java.util.List;

import static com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentBuilder.forType;
import static java.util.Collections.singletonList;

/**
 * The component produces a statement that creates a function that returns boolean which determines if passed tenant id belongs to the current tenant.
 * For more details about function creation please check postgres documentation
 * @see <a href="https://www.postgresql.org/docs/9.6/sql-createfunction.html">Postgres, create function</a>
 *
 */
public class EqualsCurrentTenantIdentifierFunctionProducer extends AbstractFunctionFactory<IEqualsCurrentTenantIdentifierFunctionProducerParameters, DefaultFunctionDefinition> {

    public static final String DEFAULT_ARGUMENT_TYPE = "VARCHAR(255)";

    @Override
    protected DefaultFunctionDefinition returnFunctionDefinition(IEqualsCurrentTenantIdentifierFunctionProducerParameters parameters, IFunctionDefinition functionDefinition) {
        return new DefaultFunctionDefinition(functionDefinition);
    }

    @Override
    protected List<IFunctionArgument> prepareFunctionArguments(IEqualsCurrentTenantIdentifierFunctionProducerParameters parameters) {
        return singletonList(forType(parameters.getArgumentType() == null ? DEFAULT_ARGUMENT_TYPE : parameters.getArgumentType()));
    }

    @Override
    protected String produceStatement(IEqualsCurrentTenantIdentifierFunctionProducerParameters parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR REPLACE FUNCTION ");
        if (parameters.getSchema() != null)
        {
            sb.append(parameters.getSchema());
            sb.append(".");
        }
        sb.append(parameters.getFunctionName());
        sb.append("(");
        if (parameters.getArgumentType() == null)
        {
            sb.append(DEFAULT_ARGUMENT_TYPE);
        }
        else
        {
            sb.append(parameters.getArgumentType());
        }
        sb.append(")");
        sb.append(" RETURNS BOOLEAN");
        sb.append(" as $$");
        sb.append("\n");
        sb.append("SELECT $1 = ");
        sb.append(parameters.getCurrentTenantIdFunctionInvocationFactory().returnGetCurrentTenantIdFunctionInvocation());
        sb.append("\n");
        sb.append("$$ LANGUAGE sql");
        sb.append("\n");
        sb.append("STABLE PARALLEL SAFE");
        sb.append(";");
        return sb.toString();
    }

    @Override
    protected void validate(IEqualsCurrentTenantIdentifierFunctionProducerParameters parameters) {
        super.validate(parameters);
        if (parameters.getCurrentTenantIdFunctionInvocationFactory() == null)
        {
            throw new IllegalArgumentException("Parameter of type IGetCurrentTenantIdFunctionInvocationFactory cannot be null");
        }
        if (parameters.getArgumentType() != null && parameters.getArgumentType().trim().isEmpty())
        {
            throw new IllegalArgumentException("Argument type cannot be blank");
        }
    }
}