package com.github.starnowski.posmulten.postgresql.core.common.function;

public interface IFunctionArgument {
    //( [ [ argmode ] [ argname ] argtype [ { DEFAULT | = } default_expr ] [, ...] ] )

    String getType();
}