package org.axelfox.common.object.inf;

import java.io.Serializable;
import java.util.Set;
import org.axelfox.common.object.exception.ParameterExistException;

/**
 * Parameter specification.
 * @author christopher
 * @param <T>                                   The {@link ParameterSpec} type 
 *                                              subclass.
 * @param <P>                                   {@link Parameter} type.
 */
public interface ParameterSpec<T extends ParameterSpec<T, P>,
                               P extends Parameter>
       extends Set<P>, 
               Serializable
{
    /**
     * Index of the object.
     * @param object                            Object to lookup its index.
     * @return                                  The index of the first occurrence 
     *                                          of the specified element in this 
     *                                          collection, or {@code -1} if this 
     *                                          collection does not contain the 
     *                                          element.
     * @see java.util.List#indexOf(java.lang.Object) 
     */
    public int indexOf(final Object object);
    
    /**
     * Get the object.
     * @param object                            Object to lookup.
     * @return                                  The element {@link P} that matches 
     *                                          the specified {@code object} in 
     *                                          this collection.
     */
    public P get(final Object object);
    
    /**
     * Add a new {@link P} into the collection. No value will be available for 
     * this parameter.
     * @param name                              Parameter name.
     * @param type                              Parameter value type.
     * @return                                  {@code true} if added, {@code false} 
     *                                          otherwise.
     * @throws ParameterExistException          If the parameter already exists.
     */
    public boolean add(final String name,
                       final Class type)
           throws ParameterExistException;
    
    /**
     * Add a new {@link P} into the collection.
     * @param name                              Parameter name.
     * @param type                              Parameter value type.
     * @param value                             Parameter value.
     * @return                                  {@code true} if added, {@code false} 
     *                                          otherwise.
     * @throws ParameterExistException          If the parameter already exists.
     */
    public boolean add(final String name,
                       final Class type,
                       final Object value)
           throws ParameterExistException;
    
    /**
     * Set * override the {@link P} into the collection. No value will be 
     * available for this parameter.
     * @param name                              Parameter name.
     * @param type                              Parameter value type.
     * @return                                  {@code true} if added, {@code false} 
     *                                          otherwise.
     */
    public boolean set(final String name,
                       final Class type);
    
    
    /**
     * Set & override the {@link P} into the collection.
     * @param name                              Parameter name.
     * @param type                              Parameter value type.
     * @param value                             Parameter value.
     * @return                                  {@code true} if added, {@code false} 
     *                                          otherwise.
     * @throws ParameterExistException          If the parameter already exists.
     */
    public boolean set(final String name,
                       final Class type,
                       final Object value);
}
