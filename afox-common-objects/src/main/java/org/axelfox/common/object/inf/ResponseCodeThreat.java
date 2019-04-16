package org.axelfox.common.object.inf;

import java.io.Serializable;

/**
 * Response code threat.
 * @param <RCT>                                 The {@link ResponseCodeThreat} 
 *                                              type subclass.
 */
public interface ResponseCodeThreat<RCT extends ResponseCodeThreat<RCT>>
       extends Serializable,
               Comparable<RCT>
{
    /**
     * Response code threat "friendly" string representation.
     * @return                                  Response code threat "friendly" 
     *                                          string representation.
     */
    public String name();
    
    /**
     * Response code threat as numeric representation.
     * @return                                  Response code threat numeric 
     *                                          representation.
     */
    public long code();
}
