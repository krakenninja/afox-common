package org.axelfox.common.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public final class TraceId
       implements Serializable
{
    private static final long serialVersionUID = -4316750556415102144L;
    
    private final String traceId;
    private final Date start;
    
    private volatile Date end;
    
    public static String generateId() { return Integer.toHexString(UUID.randomUUID().toString().hashCode())+Long.toHexString(System.nanoTime()); }
    
    public TraceId(final Object... traceId)
    {
        if(traceId==null||traceId.length==0)
            this.traceId = TraceId.generateId();
        else
        {
            String id = null;
            for(Object _traceId:traceId)
            {
                if(_traceId instanceof CharSequence)
                    id = ((CharSequence)_traceId).toString();
                else if(_traceId instanceof TraceId)
                    id = ((TraceId)_traceId).getId();
                if(id!=null&&!id.trim().equals(""))
                    break;
            }
            if(id==null||id.trim().equals(""))
                id = TraceId.generateId();
            this.traceId = id;
        }
        this.start = Calendar.getInstance().getTime();
    }

    public String getId() { return traceId; }
    
    public Date getStart() { return start; }
    
    public Date getEnd() { return end; }
    
    public synchronized long getElapsedMillis()
    {
        if(end==null)
            markEnd();
        return markEnd().getEnd().getTime()-getStart().getTime();
    }
    
    public synchronized TraceId markEnd() { this.end = Calendar.getInstance().getTime(); return this; }

    @Override
    public String toString()
    {
        synchronized(this)
        {
            markEnd();
            return getId();
        }
    }
}
