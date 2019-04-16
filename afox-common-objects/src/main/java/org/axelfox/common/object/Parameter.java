package org.axelfox.common.object;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Generic parameter type.
 * @author christopher
 * @param <V>                                   Object value type.
 */
public class Parameter<V extends Object>
       implements org.axelfox.common.object.inf.Parameter<Parameter<V>, V>
{
    private static final long serialVersionUID = 7057625229971669608L;
    
    private final String name;
    private final boolean ignoreCase;
    private final Class<V> type;
    private V value;
    
    public Parameter(final String name) { this(name, false); }
    
    public Parameter(final String name,
                     final boolean ignoreCase)
    {
        this(name, null, ignoreCase);
    }
    
    public Parameter(final String name,
                     final Class<V> type)
    {
        this(name, type, false);
    }
    
    public Parameter(final String name,
                     final Class<V> type,
                     final boolean ignoreCase)
    {
        if(name==null||name.trim().equals(""))
            throw new IllegalArgumentException("Bad parameter [name] is null or empty string ["+
            name+"]");
        if(!name.matches("(?i)[a-z][a-z0-9_]*"))
            throw new IllegalArgumentException("Not a valid parameter [name] value ["+
            name+"]");
        Type[] typeArg;
        if(type==null)
            typeArg = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments();
        else
            typeArg = new Type[]{type};
        if(typeArg==null||typeArg.length==0)
            throw new IllegalStateException("Unable to detect parameter value type; missing generic declaration or type arg");
        this.name = name;
        this.type = (Class<V>)typeArg[0];
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String name() { return name; }
    
    @Override
    public Class<V> type() { return type; }
    
    @Override
    public V value()
    {
        if(value!=null&&type().isPrimitive())
            return (V)((Object)value);
        return type().cast(value);
    }

    @Override
    public Parameter<V> value(final V value) { this.value = value; return this; }
    
    @Override
    public boolean isIgnoreCase() { return ignoreCase; }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(name());
        return hash;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if(this==obj) return true;
        if(obj==null) return false;
        if(!getClass().equals(obj.getClass()))
        {
            if(obj instanceof String)
            {
                final String other = (String)obj;
                if(isIgnoreCase())
                    return this.name().equalsIgnoreCase(other);
                return this.name().equals(other);
            }
            return false;
        }
        final Parameter other = (Parameter)obj;
        if(isIgnoreCase())
            return this.name().equalsIgnoreCase(other.name());
        return this.name().equals(other.name());
    }

    @Override
    public int compareTo(final Parameter otherT)
    {
        if(!getClass().equals(otherT.getClass()))
            throw new ClassCastException("Unexpected comparing class \""+
            otherT.getClass().getName()+"\"; expecting \""+getClass().getName()+
            "\"");
        if(isIgnoreCase())
            return name().compareToIgnoreCase(otherT.name());
        return name().compareTo(otherT.name());
    }
}
