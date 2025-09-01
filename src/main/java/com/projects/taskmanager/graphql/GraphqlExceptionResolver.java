package com.projects.taskmanager.graphql;

import java.time.format.DateTimeParseException;

import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

@Component
public class GraphqlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof IllegalArgumentException || ex instanceof DateTimeParseException) {
            String message = ex instanceof DateTimeParseException
                    ? "Invalid dueDate format, expected YYYY-MM-DD"
                    : ex.getMessage();
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(message)
                    .build();
        }
        return null;
    }
}


