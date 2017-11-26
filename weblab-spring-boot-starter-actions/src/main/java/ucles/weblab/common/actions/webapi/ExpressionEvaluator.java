package ucles.weblab.common.actions.webapi;

import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ucles.weblab.common.webapi.ActionParameter;
import ucles.weblab.common.webapi.resource.ActionableResourceSupport;

public class ExpressionEvaluator extends ApplicationObjectSupport {
    Object evaluateParameter(ActionParameter parameter, ActionableResourceSupport resource) {
        final String value = parameter.value();
        return evaluateExpression(resource, value);
    }

    Object evaluateExpression(ActionableResourceSupport resource, String expr) {
        final Expression expression = new SpelExpressionParser().parseExpression(expr, new TemplateParserContext());

        StandardEvaluationContext evalContext = new StandardEvaluationContext(resource);
        evalContext.setBeanResolver(new BeanFactoryResolver(getApplicationContext()));

        return expression.getValue(evalContext);
    }

    <T> T evaluateExpression(ActionableResourceSupport resource, String expr, Class<T> returnType) {
        final Expression expression = new SpelExpressionParser().parseExpression(expr, new TemplateParserContext());

        StandardEvaluationContext evalContext = new StandardEvaluationContext(resource);
        evalContext.setBeanResolver(new BeanFactoryResolver(getApplicationContext()));

        return expression.getValue(evalContext, returnType);
    }
}