package org.axelfox.common.object;

import java.util.Iterator;
import java.util.LinkedHashSet;
import org.axelfox.common.object.exception.ParameterExistException;

public class ParameterSpec
       extends LinkedHashSet<Parameter>
       implements org.axelfox.common.object.inf.ParameterSpec<ParameterSpec, Parameter>
{
    private static final long serialVersionUID = -1120819747109480354L;
    
    private boolean silenceException;

    public boolean isSilenceException() { return silenceException; }
    public ParameterSpec setSilenceException(final boolean silenceException) { this.silenceException = silenceException; return this; }
    
    @Override
    public int indexOf(final Object object)
    {
        int i = -1;
        for(Iterator<Parameter> iter=super.iterator();iter.hasNext();)
        {
            i++;
            final Parameter p = iter.next();
            if(object instanceof Number) // get by index
            {
                if(i==((Number)object).intValue())
                    return i;
            }
            if(p.equals(object)) // get by parameter name
                return i;
        }
        return -1;
    }
    
    @Override
    public Parameter get(final Object object)
    {
        int i = -1;
        for(Iterator<Parameter> iter=super.iterator();iter.hasNext();)
        {
            i++;
            final Parameter p = iter.next();
            if(object instanceof Number) // get by index
            {
                if(i==((Number)object).intValue())
                    return p;
            }
            if(p.equals(object)) // get by parameter name
                return p;
        }
        return null;
    }
    
    @Override
    public boolean contains(final Object object)
    {
        int i = -1;
        for(Iterator<Parameter> iter=super.iterator();iter.hasNext();)
        {
            i++;
            final Parameter p = iter.next();
            if(object instanceof Number) // get by index
            {
                if(i==((Number)object).intValue())
                    return true;
            }
            if(p.equals(object)) // get by parameter name
                return true;
        }
        return false;
    }

    @Override
    public boolean remove(final Object object)
    {
        if(object==null)
            return false;
        Parameter p = get(object);
        if(p==null) // not found, assumed removed
            return true;
        return super.remove(p);
    }

    @Override
    public boolean add(final String name,
                       final Class type)
           throws ParameterExistException
    {
        return add(name, type, null);
    }
    
    @Override
    public boolean add(final String name,
                       final Class type,
                       final Object value)
           throws ParameterExistException
    {
        final Parameter parameter = new Parameter(name, type);
        if(contains(parameter))
        {
            if(!isSilenceException())
                throw new ParameterExistException(parameter);
            return false;
        }
        if(!super.add(parameter.value(value)))
        {
            if(!isSilenceException())
                throw new ParameterExistException(parameter);
            return false;
        }
        return true;
    }

    @Override
    public boolean set(final String name, 
                       final Class type)
    {
        return set(name, type, null);
    }

    @Override
    public boolean set(final String name, 
                       final Class type, 
                       final Object value)
    {
        final Parameter parameter = new Parameter(name, type);
        if(contains(parameter))
        {
            if(!remove(parameter)) // can't remove
                return false;
        }
        return super.add(parameter.value(value));
    }
    
    protected <T> T getter(final String argname,
                           final Class<T> argtype)
    {
        final Parameter parameter = get(argname);
        if(parameter!=null&&!argtype.isAssignableFrom(parameter.type()))
            throw new ClassCastException("Unexpected parameter \""+parameter.type().getName()+
            "\" type; expecting to return as \""+argtype.getName()+"\" type");
        if(parameter==null||parameter.value()==null)
        {
            if(argtype.isPrimitive())
            {
                // http://www.c4learn.com/java/java-default-values/
                if(byte.class.equals(argtype))
                    return (T)((Object)0);
                if(short.class.equals(argtype))
                    return (T)((Object)0);
                if(int.class.equals(argtype))
                    return (T)((Object)0);
                if(long.class.equals(argtype))
                    return (T)((Object)0l);
                if(float.class.equals(argtype))
                    return (T)((Object)0.0f);
                if(double.class.equals(argtype))
                    return (T)((Object)0.00d);
                if(char.class.equals(argtype))
                    return (T)((Object)'\u0000');
                if(boolean.class.equals(argtype))
                    return (T)((Object)false);
            }
            return (T)null;
        }
        if(argtype.isPrimitive())
            return (T)((Object)parameter.value());
        return argtype.cast(parameter.value());
    }
    
    protected ParameterSpec setter(final String argname,
                                   final Class argtype,
                                   final Object argvalue)
    {
        set(argname, argtype, argvalue);
        return this;
    }
}
