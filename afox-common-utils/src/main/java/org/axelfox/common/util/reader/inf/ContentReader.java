package org.axelfox.common.util.reader.inf;

import java.io.Serializable;

/**
 * Content reader.
 * @author christopher
 */
public interface ContentReader
       extends Serializable
{
    /**
     * Get the text content header. Usually text files have no content headers; 
     * unlike certain binary files.
     * @return                                  Text content header.
     */
    public String getHeader();
    
    /**
     * Get the mime type of this content.
     * @return                                  Content mime type.
     */
    public String getMimeType();
    
    /**
     * Get the content.
     * @param <T>                               Content type to be returned.
     * @return                                  Content {@link T} type.
     */
    public <T extends Serializable> T getContent();
}
