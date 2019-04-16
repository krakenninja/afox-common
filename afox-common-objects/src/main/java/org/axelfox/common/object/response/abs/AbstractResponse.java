package org.axelfox.common.object.response.abs;

import java.lang.reflect.Method;
import org.axelfox.common.object.inf.Response;
import org.axelfox.common.object.inf.ResponseCode;
import org.axelfox.common.object.inf.ResponseCodeThreat;

public abstract class AbstractResponse<R extends AbstractResponse<R, RC, RCT, O>,
                                       RC extends ResponseCode<RC, RCT>,
                                       RCT extends ResponseCodeThreat<RCT>,
                                       O extends Object>
       implements Response<R, RC, RCT, O>
{
    private static final long serialVersionUID = -6072988355838803184L;
    
    private RC code;
    private String message;
    private O object;
    
    @Override
    public RC code() { return code; }
    @Override
    public R code(final RC code) { this.code = code; return (R)this; }

    @Override
    public String message() { return message;}
    @Override
    public R message(final String message) { this.message = message; return (R)this; }

    @Override
    public O object() { return object; }
    @Override
    public R object(final O object) { this.object = object; return (R)this; }

    @Override
    public <ROA extends O> ROA objectAs(final Class<ROA> type)
           throws ClassCastException
    {
        if(type==null)
            throw new IllegalArgumentException("Bad parameter [type] is null; expecting ["+
            Class.class.getName()+"<ROA>] object type");
        final int defaultInt = 0;
        final long defaultLong = 0l;
        final float defaultFloat = 0.0f;
        final double defaultDouble = 0.00d;
        final boolean defaultBoolean = false;
        final char defaultChar = '\u0000';
        final short defaultShort = 0;
        final byte defaultByte = 0;
        final O o = object();
        if(o==null)
        {
            if(type.isPrimitive()) // want it back as primitive
            {
                if(int.class.equals(type))
                    return (ROA)Integer.valueOf(defaultInt);
                else if(long.class.equals(type))
                    return (ROA)Long.valueOf(defaultLong);
                else if(float.class.equals(type))
                    return (ROA)Float.valueOf(defaultFloat);
                else if(double.class.equals(type))
                    return (ROA)Double.valueOf(defaultDouble);
                else if(boolean.class.equals(type))
                    return (ROA)Boolean.valueOf(defaultBoolean);
                else if(char.class.equals(type))
                    return (ROA)Character.valueOf(defaultChar);
                else if(short.class.equals(type))
                    return (ROA)Short.valueOf(defaultShort);
                else if(byte.class.equals(type))
                    return (ROA)Byte.valueOf(defaultByte);
                else
                    throw new IllegalArgumentException("Bad parameter [type] is an unsupported/unhandled primitive ["+
                    type.getName()+"] type");
            }
            return (ROA)o;
        }
        if(type.isPrimitive()) // want it back as primitive
            return (ROA)o;
        return type.cast(o);
    }

    @Override
    public String toString()
    {
        String objectToString = null;
        if(object()!=null)
        {
            try
            {
                final Method objectToStringMethod = lookupToStringMethod(object()); // don't bother if null, silenced exception
                final Object objectToStringReturn = objectToStringMethod.invoke(object()); // toString() always public, so don't bother to do any check
                
                if(objectToStringReturn instanceof String)
                {
                    if(object().getClass().isPrimitive()||object().getClass().getPackage().equals(Package.getPackage("java.lang")))
                        objectToString = (String)objectToStringReturn;
                    else
                    {
                        objectToString = (String)objectToStringReturn;
                        if(!objectToString.startsWith("\n"))
                            objectToString = "\n"+objectToString;
                    }
                }
            }
            catch(Throwable t) {}
        }
        
        final StringBuilder sb = new StringBuilder()
            .append("\n\t[RESPONSE]")
            .append("\n\tImplementation Class: ")
            .append(getClass().getName())
            .append("\n\t\t>>> Object Type: ")
            .append((object()!=null?object().getClass().getName():null))
        ;
        if(objectToString!=null)
        {
            sb.append("\n\t\t>>> Object Value: ")
                .append(objectToString)
            ;
        }
        sb.append('\n')
            .append(code())
            .append("\n\n")
        ;
        return sb.toString();
    }
    
    private Method lookupToStringMethod(final Object object)
    {
        if(object==null)
            return null;
        if((object instanceof Class)&&((Class)object).equals(Object.class))
            return null;
        else if(object.getClass().equals(Object.class))
            return null;
        try
        {
            Method toStringMethod = (object instanceof Class)?
            ((Class)object).getDeclaredMethod("toString"):
            object.getClass().getDeclaredMethod("toString");
            return toStringMethod;
        }
        catch(NoSuchMethodException t)
        {
            return lookupToStringMethod(object.getClass().getSuperclass());
        }
        catch(Throwable t)
        {
            return null;
        }
    }
}
