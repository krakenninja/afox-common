package org.axelfox.common.object.inf;

/**
 * Response code.
 * @author christopher
 * @param <RC>                                  The {@link ResponseCode} type 
 *                                              subclass.
 * @param <RCT>                                 The {@link ResponseCodeThreat} 
 *                                              type.
 */
public interface ResponseCode<RC extends ResponseCode<RC, RCT>,
                              RCT extends ResponseCodeThreat<RCT>>
       extends ResponseCodeThreat<RC>
{
    /**
     * Response code "friendly" string representation.
     * @return                                  Response code "friendly" string 
     *                                          representation.
     */
    @Override
    public String name();
    
    /**
     * Response code as numeric representation.
     * @return                                  Response code numeric representation.
     */
    @Override
    public long code();
    
    /**
     * Response code threat level. Consider this as a grouping level or 
     * categorization of the {@link ResponseCode} object, useful to determine 
     * whether this can be categorized as {@code OK}, {@code WARNING}, 
     * {@code ERROR} etc.
     * @return                                  Response code threat.
     */
    public RCT threat();
}
