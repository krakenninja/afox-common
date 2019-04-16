package org.axelfox.common.object.exception;

import org.axelfox.common.object.inf.Parameter;

public class ParameterExistException
       extends IllegalArgumentException
{
    private static final long serialVersionUID = -4839291248354291819L;
    
    private final Parameter parameter;

    public ParameterExistException(final String parameterName)
    {
        this(parameterName, "Parameter \""+parameterName+"\" already exists");
    }
    public ParameterExistException(final Parameter parameter)
    {
        this(parameter, "Parameter \""+(parameter!=null?parameter.name():null)+"\" already exists");
    }

    public ParameterExistException(final String parameterName, 
                                   final String message)
    {
        this(parameterName, message, null);
    }
    public ParameterExistException(final Parameter parameter,
                                   final String message)
    {
        this(parameter, message, null);
    }
    
    public ParameterExistException(final String parameterName, 
                                   final Throwable cause)
    {
        this(parameterName, "Parameter \""+parameterName+"\" already exists", cause);
    }
    public ParameterExistException(final Parameter parameter, 
                                   final Throwable cause)
    {
        this(parameter, "Parameter \""+(parameter!=null?parameter.name():null)+"\" already exists", cause);
    }

    public ParameterExistException(final String parameterName, 
                                   final String message,
                                   final Throwable cause)
    {
        this(new org.axelfox.common.object.Parameter(parameterName), message, cause);
    }
    public ParameterExistException(final Parameter parameter, 
                                   final String message,
                                   final Throwable cause)
    {
        super(message, cause);
        this.parameter = parameter;
    }

    public Parameter getParameter() { return parameter; }
}
