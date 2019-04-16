package org.axelfox.common.object.response;

import org.axelfox.common.object.inf.ResponseCodeThreat;

public enum DefaultResponseCodeThreat
       implements ResponseCodeThreat<DefaultResponseCodeThreat>
{
    /**
     * No threat.
     */
    NONE(0),
    
    /**
     * Warning threat. Depending on the application, this threat level indicates 
     * ignorable but worth to look at as it may break certain logic in future.
     */
    WARNING(1),
    
    /**
     * Error threat. This is considered critical and will cause issues/failure.
     */
    ERROR(2),
    
    ;
    
    private final long code;
    private DefaultResponseCodeThreat(final long code) { this.code = code; }

    @Override
    public long code() { return code; }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder()
            .append("\n\t[THREAT]")
            .append("\n\tImplementation Class: ")
            .append(getClass().getName())
            .append("\n\t\t>>> Name: ")
            .append(name())
            .append("\n\t\t>>> Code: ")
            .append(code())
        ;
        return sb.toString();
    }
}
