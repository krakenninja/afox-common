package org.axelfox.common.object.response;

import org.axelfox.common.object.response.abs.AbstractResponse;

public class DefaultResponse<R extends DefaultResponse<R, O>, 
                             O extends Object>
       extends AbstractResponse<R, DefaultResponseCode, DefaultResponseCodeThreat, O>
{
    private static final long serialVersionUID = -7999995928117388017L;

    @Override
    public R object(final O object) { return super.object(object); }

    @Override
    public R message(final String message) { return super.message(message); }

    @Override
    public R code(final DefaultResponseCode code) { return super.code(code); }
}
