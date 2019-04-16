package org.axelfox.common.util.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.axelfox.common.util.TraceId;
import org.axelfox.common.util.reader.inf.ContentReader;

public class CopyrightReaderContentHeader
       implements ContentReader
{
    private static final long serialVersionUID = -7844102076026772403L;
    private static final Logger log = Logger.getLogger(CopyrightReaderContentHeader.class.getName());
    private static final Pattern headerPattern = Pattern.compile("^(@@((CWT)\\|(.*))@@)", Pattern.CASE_INSENSITIVE);
    private static final String newline = System.getProperty("line.separator");
    private static final String mimePlainTextType = "text/plain";
    private static final int headerMatcherExpectedGroupCount = 4;
    private static final int headerMatcherCWTStartEndGroupNo = 1;
    private static final int headerMatcherCWTMarkGroupNo = 3;
    private static final int headerMatcherCWTFileExtsGroupNo = 4;
    
    private final TraceId traceIdRef;
    private final File contentHeaderFile;
    private final Map<String, String> staticTags;
    
    public CopyrightReaderContentHeader(final TraceId traceIdRef, 
                                        final File contentHeaderFile)
    {
        this(traceIdRef, contentHeaderFile, null);
    }
    
    public CopyrightReaderContentHeader(final TraceId traceIdRef, 
                                        final File contentHeaderFile,
                                        final Map<String, String> staticTags)
    {
        this.traceIdRef = traceIdRef!=null?new TraceId(traceIdRef):new TraceId();
        this.contentHeaderFile = contentHeaderFile;
        this.staticTags = staticTags;
    }

    public File getContentHeaderFile() { return contentHeaderFile; }
    
    @Override
    public String getHeader()
    {
        final TraceId traceId = new TraceId(traceIdRef);
        log.info("# "+traceId+" # getHeader() : enters");
        String header = null;
        InputStream contentHeaderStream = null;
        try
        {
            final Path path = getContentHeaderFile().toPath();
            log.info("# "+traceId+" # getHeader() : Got content header file as ["+
            path.getClass().getName()+"] object type");
            
            // get the mimetype
            final String mimeType = getMimeType();
            if(mimeType==null||!mimeType.regionMatches(true, 0, mimePlainTextType, 0, mimePlainTextType.length()))
                log.warning("# "+traceId+" # getHeader() : File ["+
                getContentHeaderFile().getPath()+"] mimetype \""+mimeType+
                "\" is not expected; expecting \""+mimePlainTextType+"\" mimetype; unable to extract header");
            else
            {
                log.info("# "+traceId+" # getHeader() : File ["+
                getContentHeaderFile().getPath()+"] mimetype \""+mimeType+
                "\" is accepted. Proceeding to extract header");
                
                // open the file stream
                contentHeaderStream = Files.newInputStream(path, StandardOpenOption.READ);
                InputStreamReader isr = null;
                BufferedReader lineReader = null;
                try
                {
                    isr = new InputStreamReader(contentHeaderStream, Charset.forName("UTF-8"));
                    lineReader = new BufferedReader(isr);
                    
                    // interested in the first line only; expecting format "@@CWT|JAVA,CPP,C@@"
                    final String headerLine = lineReader.readLine(); 
                    log.info("# "+traceId+" # getHeader() : Got first header line for file ["+
                    getContentHeaderFile().getPath()+"] --- \n\t"+headerLine);
                    
                    // get the header pattern lookup
                    final Matcher headerPatternMatcher = headerPattern.matcher(headerLine);
                    if(!headerPatternMatcher.find())
                        log.warning("# "+traceId+" # getHeader() : First header line for file ["+
                        getContentHeaderFile().getPath()+"] did not match to pattern ["+
                        headerPattern.pattern()+"] --- \n\t"+headerLine);
                    else
                    {
                        final int headerPatternMatcherGroupCount = headerPatternMatcher.groupCount();
                        log.info("# "+traceId+" # getHeader() : Got first header line pattern matcher group count ["+
                        headerPatternMatcherGroupCount+"]");

                        if(headerPatternMatcherGroupCount!=headerMatcherExpectedGroupCount)
                            log.warning("# "+traceId+" # getHeader() : Unexpected header line pattern matcher group count ["+
                            headerPatternMatcherGroupCount+"]; expecting ["+
                            headerMatcherExpectedGroupCount+"] group count\n\twhere group ["+
                            headerMatcherCWTMarkGroupNo+"] => contain the CWT header\n\tgroup ["+
                            headerMatcherCWTFileExtsGroupNo+"] => binded file extension(s)");
                        else
                        {
                            header = headerPatternMatcher.group(headerMatcherCWTStartEndGroupNo);
                            final StringBuilder dbgHeaderPatternMatcher = new StringBuilder();
                            for(int i=1;i<=headerPatternMatcherGroupCount;i++)
                            {
                                dbgHeaderPatternMatcher.append("\n\tGroup ");
                                dbgHeaderPatternMatcher.append(i);
                                dbgHeaderPatternMatcher.append(": ");
                                dbgHeaderPatternMatcher.append(headerPatternMatcher.group(i));
                            }
                            log.info("# "+traceId+" # getHeader() : Got header ["+
                            header+"] (at group 1); group fragments --- "+
                            dbgHeaderPatternMatcher);
                        }
                    }
                }
                finally
                {
                    try { if(lineReader!=null) lineReader.close(); } catch(Throwable t) {}
                    try { if(isr!=null) isr.close(); } catch(Throwable t) {}
                }
            }
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "# "+traceId+" # getHeader() : Failed to extract header from file ["+
            (getContentHeaderFile()!=null?getContentHeaderFile().getPath():null)+
            "]; exception encountered --- "+t.getMessage(), t);
        }
        finally
        {
            if(contentHeaderStream!=null)
                try { contentHeaderStream.close(); } catch(Throwable t) {}
            log.info("# "+traceId+" # getHeader() : Returning extracted header file --- "+
            header);
            log.info("# "+traceId+" # getHeader() : exits");
        }
        return header;
    }

    @Override
    public String getMimeType()
    {
        final TraceId traceId = new TraceId(traceIdRef);
        log.info("# "+traceId+" # getMimeType() : enters");
        String mimeType = null;
        try
        {
            // convert the file to path
            final Path path = getContentHeaderFile().toPath();
            log.info("# "+traceId+" # getMimeType() : Got content header file as ["+
            path.getClass().getName()+"] object type");
            
            // get the mimetype; using Java 7 NIO
            mimeType = Files.probeContentType(path);
            if(mimeType==null) // attempt using URLConnection
            {
                log.info("# "+traceId+" # getMimeType() : Get content header ["+
                getContentHeaderFile().getPath()+"] file mime type via Java 7 NIO returned null; attempting to lookup using URL connection");
                
                // get the mimetype; using URL connection
                final URI uri = getContentHeaderFile().toURI();
                final URLConnection connection;
                try
                {
                    connection = uri.toURL().openConnection();
                    connection.setConnectTimeout(2000); // maximum wait 2 seconds for local file system!!
                    connection.setReadTimeout(2000); // maximum wait 2 seconds for local file system!!
                    mimeType = connection.getContentType();
                    log.info("# "+traceId+" # getMimeType() : Got content header ["+
                    getContentHeaderFile().getPath()+"] file mime type via URL connection returned ["+
                    mimeType+"]");
                }
                catch(Throwable t)
                {
                    log.log(Level.WARNING, "# "+traceId+" # getMimeType() : Unable to obtain content header file ["+
                    getContentHeaderFile().getPath()+"] mime type using URL connection", t);
                    
                    // get the mimetype; using content stream
                    final FileInputStream fis = new FileInputStream(getContentHeaderFile());
                    try
                    {
                        mimeType = URLConnection.guessContentTypeFromStream(fis);
                        log.info("# "+traceId+" # getMimeType() : Got content header ["+
                        getContentHeaderFile().getPath()+"] file mime type via file input stream returned ["+
                        mimeType+"]");
                    }
                    catch(Throwable t1)
                    {
                        log.log(Level.WARNING, "# "+traceId+" # getMimeType() : Unable to obtain content header file ["+
                        getContentHeaderFile().getPath()+"] mime type using file input stream", t1);
                    }
                    finally
                    {
                        try { fis.close(); } catch(Throwable t1) {}
                    }
                }
            }
            if(mimeType==null||!mimeType.regionMatches(true, 0, mimePlainTextType, 0, mimePlainTextType.length()))
                log.warning("# "+traceId+" # getMimeType() : File ["+
                getContentHeaderFile().getPath()+"] mimetype \""+mimeType+
                "\" is not expected; expecting \""+mimePlainTextType+"\" mimetype");
            else
            {
                log.info("# "+traceId+" # getMimeType() : File ["+
                getContentHeaderFile().getPath()+"] mimetype \""+mimeType+
                "\" is expected");
            }
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "# "+traceId+" # getMimeType() : Failed to get mime type for content header file ["+
            (getContentHeaderFile()!=null?getContentHeaderFile().getPath():null)+"]", t);
        }
        finally
        {
            log.info("# "+traceId+" # getMimeType() : Returning mime type ["+mimeType+"]");
            log.info("# "+traceId+" # getMimeType() : exits");
        }
        return mimeType;
    }

    @Override
    public StringBuilder getContent()
    {
        final TraceId traceId = new TraceId(traceIdRef);
        log.info("# "+traceId+" # getContent() : enters");
        StringBuilder copyrightHeaderContent = new StringBuilder();
        try
        {
            final BufferedReader bufferedReader = new BufferedReader(new FileReader(getContentHeaderFile()));
            try
            {
                String copyrightHeaderContentLine;
                while((copyrightHeaderContentLine=bufferedReader.readLine())!=null)
                {
                    // replace the static tags with the static replacement values
                    if(staticTags!=null&&!staticTags.isEmpty())
                    {
                        for(Map.Entry<String, String> supportedStaticTagEntry:staticTags.entrySet())
                        {
                            final String tagLookup = supportedStaticTagEntry.getKey();
                            final String tagReplacement = supportedStaticTagEntry.getValue();
                            copyrightHeaderContentLine = copyrightHeaderContentLine.replaceAll(Pattern.quote(tagLookup), tagReplacement);
                        }
                    }
                    // all done, add it to the header content
                    copyrightHeaderContent.append(copyrightHeaderContentLine).append(newline);
                }
                
                // find the CWT header and remove it
                final String _copyrightHeaderContent = removeCWTHeader(traceId, copyrightHeaderContent.toString());
                copyrightHeaderContent.delete(0, copyrightHeaderContent.length());
                copyrightHeaderContent.append(_copyrightHeaderContent);
                log.info("# "+traceId+" # getContent() : Got copyright header content --- \n\t"+
                copyrightHeaderContent);
            }
            finally
            {
                try { bufferedReader.close(); } catch(Throwable t) {}
            }
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "# "+traceId+" # getContent() : Failed to get copyright header content for content header file ["+
            (getContentHeaderFile()!=null?getContentHeaderFile().getPath():null)+
            "] --- "+t.getMessage(), t);
        }
        finally
        {
            log.info("# "+traceId+" # getContent() : Returning copyright header content --- \n\t"+
            copyrightHeaderContent);
            log.info("# "+traceId+" # getContent() : exits");
        }
        return copyrightHeaderContent;
    }
    
    public Set<String> getBindedFileExtensions()
    {
        final TraceId traceId = new TraceId(traceIdRef);
        log.info("# "+traceId+" # getBindedFileExtensions() : enters");
        final Set<String> bindedFileExtensions = new LinkedHashSet<>();
        InputStream contentHeaderStream = null;
        try
        {
            // extract out the header
            final String header = getHeader();
            if(header==null)
            {
                log.warning("# "+traceId+" # getBindedFileExtensions() : Unable to get header for file ["+
                getContentHeaderFile().getPath()+"], returned null; this file may not contain the expected header line that matches the pattern --- \n\t"+
                headerPattern.pattern());
            }
            else
            {
                log.info("# "+traceId+" # getBindedFileExtensions() : Got header for file ["+
                getContentHeaderFile().getPath()+"] --- \n\t"+header);

                // get the header pattern lookup
                final Matcher headerPatternMatcher = headerPattern.matcher(header);
                if(!headerPatternMatcher.find())
                    log.warning("# "+traceId+" # getBindedFileExtensions() : First header line for file ["+
                    getContentHeaderFile().getPath()+"] did not match to pattern ["+
                    headerPattern.pattern()+"] --- \n\t"+header);
                else
                {
                    final int headerPatternMatcherGroupCount = headerPatternMatcher.groupCount();
                    log.info("# "+traceId+" # getBindedFileExtensions() : Got first header line pattern matcher group count ["+
                    headerPatternMatcherGroupCount+"]");

                    if(headerPatternMatcherGroupCount!=headerMatcherExpectedGroupCount)
                        log.info("# "+traceId+" # getBindedFileExtensions() : Unexpected header line pattern matcher group count ["+
                        headerPatternMatcherGroupCount+"]; expecting ["+
                        headerMatcherExpectedGroupCount+"] group count\n\twhere group ["+
                        headerMatcherCWTMarkGroupNo+"] => contain the CWT header\n\tgroup ["+
                        headerMatcherCWTFileExtsGroupNo+"] => binded file extension(s)");
                    else
                    {
                        final String cwtHeader = headerPatternMatcher.group(headerMatcherCWTMarkGroupNo);
                        final String cwtBindedExtensions = headerPatternMatcher.group(headerMatcherCWTFileExtsGroupNo);
                        log.info("# "+traceId+" # getBindedFileExtensions() : Got parsed first header line --- \n\tCWT Header: "+
                        cwtHeader+"\n\tBinded File Extension(s): "+cwtBindedExtensions);

                        if(!cwtHeader.trim().equalsIgnoreCase("CWT"))
                            throw new UnsupportedOperationException("Unknown/unsupported/unexpected CWT header ["+
                            cwtHeader+"]; expecting [CWT]");
                        final String[] _cwtBindedExtensions = cwtBindedExtensions.split(",");
                        for(String _cwtBindedExtension:_cwtBindedExtensions)
                            bindedFileExtensions.add(_cwtBindedExtension.trim().toLowerCase());
                        log.info("# "+traceId+" # getBindedFileExtensions() : File ["+
                        getContentHeaderFile().getPath()+"] content header file is for file extension(s): \n\t"+
                        Arrays.toString(bindedFileExtensions.toArray(new String[bindedFileExtensions.size()])));
                    }
                }
            }
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "# "+traceId+" # getBindedFileExtensions() : Failed to get binded file extensions from file ["+
            (contentHeaderFile!=null?contentHeaderFile.getPath():null)+
            "]; exception encountered --- "+t.getMessage(), t);
        }
        finally
        {
            if(contentHeaderStream!=null)
                try { contentHeaderStream.close(); } catch(Throwable t) {}
            final StringBuilder dbgBindedFileExtensions = new StringBuilder();
            for(String bindedFileExtension:bindedFileExtensions)
            {
                dbgBindedFileExtensions.append("\n\t");
                dbgBindedFileExtensions.append(bindedFileExtension);
            }
            log.info("# "+traceId+" # getBindedFileExtensions() : Returning binded file extensions --- "+
            dbgBindedFileExtensions);
            log.info("# "+traceId+" # getBindedFileExtensions() : exits");
        }
        return bindedFileExtensions;
    }
    
    public static String removeCWTHeader(final TraceId traceIdRef,
                                         final String headerLine)
    {
        final TraceId traceId = new TraceId(traceIdRef);
        log.info("# "+traceId+" # removeCWTHeader() : enters");
        String filteredContent = headerLine;
        try
        {
            log.info("# "+traceId+" # removeCWTHeader() : Got parameter [headerLine] value ["+headerLine+"]");
            
            final Matcher headerPatternMatcher = headerPattern.matcher(headerLine);
            if(!headerPatternMatcher.find())
            {
                log.warning("# "+traceId+" # removeCWTHeader() : Header line input does not match expected pattern ["+
                headerPattern.pattern()+"] --- \n"+headerLine);
            }
            else
            {
                final int headerPatternMatcherGroupCount = headerPatternMatcher.groupCount();
                log.info("# "+traceId+" # removeCWTHeader() : Got first header line pattern matcher group count ["+
                headerPatternMatcherGroupCount+"]");

                if(headerPatternMatcherGroupCount!=headerMatcherExpectedGroupCount)
                    log.info("# "+traceId+" # removeCWTHeader() : Unexpected header line pattern matcher group count ["+
                    headerPatternMatcherGroupCount+"]; expecting ["+
                    headerMatcherExpectedGroupCount+"] group count\n\twhere group ["+
                    headerMatcherCWTMarkGroupNo+"] => contain the CWT header\n\tgroup ["+
                    headerMatcherCWTFileExtsGroupNo+"] => binded file extension(s)");
                else
                {
                    filteredContent = headerLine.substring(headerPatternMatcher.end()).trim();
                    log.info("# "+traceId+" # removeCWTHeader() : Got filtered header content --- \n\tBEFORE:\n\t-------\n"+
                    headerLine+"\n\tAFTER:\n\t------\n"+filteredContent);
                }
            }
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "# "+traceId+" # removeCWTHeader() : Failed to remove CWT header from header line --- \n\t"+
            headerLine, t);
        }
        finally
        {
            log.info("# "+traceId+" # removeCWTHeader() : Returning filtered header content --- \n\t"+
            filteredContent);
            log.info("# "+traceId+" # removeCWTHeader() : exits");
        }
        return filteredContent;
    }
}
