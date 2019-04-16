package org.axelfox.common.util.reader;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.axelfox.common.util.TraceId;

/**
 * A default {@link CopyrightWriter} content file filter to use to lookup for 
 * file content filters.
 * @author christopher
 */
public class CopyrightReaderContentFilter
       implements FileFilter
{
    private static final Logger log = Logger.getLogger(CopyrightReaderContentFilter.class.getName());
    
    private final TraceId traceIdRef;
    
    public CopyrightReaderContentFilter(final TraceId traceIdRef) { this.traceIdRef = traceIdRef; }
    
    @Override
    public boolean accept(final File file)
    {
        final TraceId traceId = new TraceId(traceIdRef);
        log.info("# "+traceId+" # accept() : enters");
        boolean accept = false;
        try
        {
            if(file!=null)
            {
                // let's accept files that matches our template descriptive file 
                // header information; i.e. in this case the file MUST BE:
                // 1)   A plain text file.
                // 2)   Its first line header to contain the following format: 
                //         @@CWT|<FOR_SOURCE_EXTENSION(S)>@@
                //      for example:
                //         @@CWT|JAVA@@
                //         @@CWT|JAVA,CPP,C@@
                if(!file.isDirectory()) 
                {
                    if(!file.canRead())
                        log.warning("# "+traceId+" # accept() : Unable to gain READ access to file ["+
                        file.getPath()+"]; can't read file header info");
                    else
                    {
                        log.info("# "+traceId+" # accept() : Preparing to read file ["+
                        file.getPath()+"] header info");
                        
                        final CopyrightReaderContentHeader copyrightWriterContentHeader = 
                        new CopyrightReaderContentHeader(traceIdRef, file);
                        final Set<String> bindedFileExtensions = copyrightWriterContentHeader.getBindedFileExtensions();
                        if(bindedFileExtensions.isEmpty())
                        {
                            accept = false;
                            log.info("# "+traceId+" # accept() : Not accepting file ["+
                            file.getPath()+"], no binded file extensions found");
                        }
                        else
                        {
                            accept = true;
                            final StringBuilder dbgBindedFileExtensions = new StringBuilder();
                            for(String bindedFileExtension:bindedFileExtensions)
                            {
                                dbgBindedFileExtensions.append("\n\t");
                                dbgBindedFileExtensions.append(bindedFileExtension);
                            }
                            log.info("# "+traceId+" # accept() : Accepting file ["+
                            file.getPath()+"], found ["+bindedFileExtensions.size()+
                            "] binded file extensions --- "+dbgBindedFileExtensions);
                        }
                    }
                }
                // directories will get accepted, so that it'll continue to process later on
                else
                {
                    accept = true;
                    log.info("# "+traceId+" # accept() : Explicitly accepting directory type ["+
                    file.getPath()+"]");
                }
            }
            else
                log.warning("# "+traceId+" # accept() : Parameter [file] is null; expecting ["+
                File.class.getName()+"] object type");
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "# "+traceId+" # accept() : Failed to process file ["+
            (file!=null?file.getPath():null)+"] to determine if it is accepted", t);
        }
        finally
        {
            log.info("# "+traceId+" # accept() : Returning is accepted ["+accept+"]");
            log.info("# "+traceId+" # accept() : exits");
        }
        return accept;
    }
}
