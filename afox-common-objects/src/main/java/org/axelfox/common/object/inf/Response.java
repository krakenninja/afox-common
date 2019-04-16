package org.axelfox.common.object.inf;

import java.io.Serializable;

/**
 * Response.
 * @author christopher
 * @param <R>                                   The {@link Response} type subclass.
 * @param <RC>                                  The {@link ResponseCode} code.
 * @param <RCT>                                 The {@link ResponseCodeThreat} 
 *                                              threat.
 * @param <RO>                                  The response object.
 */
public interface Response<R extends Response<R, RC, RCT, RO>,
                          RC extends ResponseCode<RC, RCT>,
                          RCT extends ResponseCodeThreat<RCT>,
                          RO extends Object>
       extends Serializable
{
    /**
     * Get response code.
     * @return                                  Response code.
     */
    public RC code();
    
    /**
     * Set response code.
     * @param code                              Response code.
     * @return                                  Chain reference of {@link R} 
     *                                          object type.
     */
    public R code(final RC code);
    
    /**
     * Get response message, i.e. if any exception occurs, this can be used for 
     * friendly messaging logging.
     * @return                                  Response message.
     */
    public String message();
    
    /**
     * Set response message, i.e. if any exception occurs, this can be used for 
     * friendly messaging logging.
     * @param message                           Response message.
     * @return                                  Chain reference of {@link R} 
     *                                          object type.
     */
    public R message(final String message);
    
    /**
     * Get response object.
     * @return                                  Response object.
     */
    public RO object();
    
    /**
     * Get response object as.
     * @param <ROA>                             Response object as.
     * @param type                              Response object as type.
     * @return                                  Response object as.
     * @throws ClassCastException               If unable to get {@link #object()} 
     *                                          as {@code type}.
     */
    public <ROA extends RO> ROA objectAs(final Class<ROA> type)
           throws ClassCastException;
    
    /**
     * Set response object.
     * @param object                            Response object.
     * @return                                  Chain reference of {@link R} 
     *                                          object type.
     */
    public R object(final RO object);
}
