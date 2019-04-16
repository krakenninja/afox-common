package org.axelfox.common.object.inf;

import java.io.Serializable;

/**
 * Parameter.
 * @author christopher
 * @param <T>                                   The {@link Parameter} type 
 *                                              subclass.
 * @param <V>                                   Object value type.
 */
public interface Parameter<T extends Parameter<T, V>,
                           V extends Object>
       extends Serializable,
               Comparable<T>
{
    /**
     * Name of the parameter.
     * @return                                  Name of the parameter.
     */
    public String name();
    
    /**
     * Data object value type of the parameter.
     * @return                                  Data object value type of the 
     *                                          parameter.
     */
    public Class<V> type();
    
    /**
     * Get object value of the parameter.
     * @return                                  Returns the {@code V} object 
     *                                          value type.
     */
    public V value();
    
    /**
     * Set object value for the parameter.
     * @param v                                 Object value {@code V} type.
     * @return                                  Chaining reference of {@code T}.
     */
    public T value(final V v);
    
    /**
     * Determines if this parameter name is ignoring case. This will affect the 
     * {@link Parameter} object comparison.
     * @return 
     */
    public boolean isIgnoreCase();
}
