package org.axelfox.common.object.response;

import org.axelfox.common.object.inf.ResponseCode;

public enum DefaultResponseCode
       implements ResponseCode<DefaultResponseCode, DefaultResponseCodeThreat>
{
    ////////////////////////////////////////////////////////////////////////////
    // SUCCESS [RESERVE 10000-19999]
    ////////////////////////////////////////////////////////////////////////////
    OK_SUCCESS(10000l, DefaultResponseCodeThreat.NONE),
    OK_PENDING(10001l, DefaultResponseCodeThreat.NONE),
    
    ////////////////////////////////////////////////////////////////////////////
    // ERROR [RESERVE 30000-69999]
    ////////////////////////////////////////////////////////////////////////////
    ERR_SYSTEM(30000l, DefaultResponseCodeThreat.ERROR),
    ERR_ILLEGAL_ARGUMENT(30001l, DefaultResponseCodeThreat.ERROR),
    ERR_ILLEGAL_STATE(30002l, DefaultResponseCodeThreat.ERROR),
    
    ////////////////////////////////////////////////////////////////////////////
    // WARNING [RESERVE 80000-99999]
    ////////////////////////////////////////////////////////////////////////////
    UNDEFINED(80000l, DefaultResponseCodeThreat.WARNING),
    WARN_NOT_FOUND(80001l, DefaultResponseCodeThreat.WARNING),
    
    ;
    
    private final long code;
    private final DefaultResponseCodeThreat threat;
    private DefaultResponseCode(final long code, final DefaultResponseCodeThreat threat)
    {
        this.code = code;
        this.threat = threat;
    }

    @Override
    public long code() { return code; }

    @Override
    public DefaultResponseCodeThreat threat() { return threat; }
    
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder()
            .append("\n\t[CODE]")
            .append("\n\tImplementation Class: ")
            .append(getClass().getName())
            .append("\n\t\t>>> Name: ")
            .append(name())
            .append("\n\t\t>>> Code: ")
            .append(code())
            .append('\n')
            .append(threat())
        ;
        return sb.toString();
    }
}
