package org.axelfox.common.util.writer;

import org.axelfox.common.util.reader.CopyrightReaderContentFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.axelfox.common.object.ParameterSpec;
import org.axelfox.common.util.TraceId;
import org.axelfox.common.util.reader.CopyrightReaderContentHeader;

public final class CopyrightWriter
{
    private static final Logger log = Logger.getLogger(CopyrightWriter.class.getName());
    private static final Map<String, String> SUPPORTED_STATIC_TAGS = new ConcurrentHashMap<>();
    
    static
    {
        SUPPORTED_STATIC_TAGS.put("{current.year}", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        // https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
        final String[] sysprops = {
            "file.separator",
            "java.class.path",
            "java.home",
            "java.vendor",
            "java.vendor.url",
            "java.version",
            "line.separator",
            "os.arch",
            "os.name",
            "os.version",
            "path.separator",
            "user.dir",
            "user.home",
            "user.name",
        };
        for(String sysprop:sysprops)
            SUPPORTED_STATIC_TAGS.put("{"+sysprop+"}", System.getProperty(sysprop));
    }
    
    private CopyrightWriter() {}
    
    public static final class CopyrighterWriterParameterSpec
           extends ParameterSpec
    {
        private static final long serialVersionUID = -8024414072198488653L;
        
        private static final String ARGNAME_TRACEIDREF = "traceIdRef";
        private static final String ARGNAME_COPYRIGHTCONTENTFILE = "copyrightContentFile";
        private static final String ARGNAME_COPYRIGHTCONTENTFILEFILTER = "copyrightContentFileFilter";
        private static final String ARGNAME_APPENDATLINENO = "appendAtLineNo";
        private static final String ARGNAME_APPENDTOTARGETFILE = "appendToTargetFile";
        private static final String ARGNAME_APPENDTOTARGETFILEFILTER = "appendToTargetFileFilter";
        
        public TraceId getTraceIdRef() { return getter(ARGNAME_TRACEIDREF, TraceId.class); }
        public CopyrighterWriterParameterSpec setTraceIdRef(final TraceId traceIdRef) { return (CopyrighterWriterParameterSpec)setter(ARGNAME_TRACEIDREF, TraceId.class, traceIdRef); }

        public File getCopyrightContentFile() { return getter(ARGNAME_COPYRIGHTCONTENTFILE, File.class); }
        public CopyrighterWriterParameterSpec setCopyrightContentFile(final File copyrightContentFile) { return (CopyrighterWriterParameterSpec)setter(ARGNAME_COPYRIGHTCONTENTFILE, File.class, copyrightContentFile); }
        public CopyrighterWriterParameterSpec setCopyrightContentFile(final String copyrightContentFilepath)
        {
            if(copyrightContentFilepath!=null)
                return setCopyrightContentFile(new File(copyrightContentFilepath.trim().replaceAll("\\\\", "/")));
            return setCopyrightContentFile((File)null);
        }
        
        public FileFilter getCopyrightContentFileFilter() { return getter(ARGNAME_COPYRIGHTCONTENTFILEFILTER, FileFilter.class); }
        public CopyrighterWriterParameterSpec setCopyrightContentFileFilter(final FileFilter copyrightContentFileFilter) { return (CopyrighterWriterParameterSpec)setter(ARGNAME_COPYRIGHTCONTENTFILEFILTER, FileFilter.class, copyrightContentFileFilter); }

        public int getAppendAtLineNo() { return getter(ARGNAME_APPENDATLINENO, int.class); }
        public CopyrighterWriterParameterSpec setAppendAtLineNo(final int appendAtLineNo) { return (CopyrighterWriterParameterSpec)setter(ARGNAME_APPENDATLINENO, int.class, appendAtLineNo); }

        public File getAppendToTargetFile() { return getter(ARGNAME_APPENDTOTARGETFILE, File.class); }
        public CopyrighterWriterParameterSpec setAppendToTargetFile(final File appendToTargetFile) { return (CopyrighterWriterParameterSpec)setter(ARGNAME_APPENDTOTARGETFILE, File.class, appendToTargetFile); }
        public CopyrighterWriterParameterSpec setAppendToTargetFile(final String appendToTargetFilepath)
        {
            if(appendToTargetFilepath!=null)
                return setAppendToTargetFile(new File(appendToTargetFilepath.trim().replaceAll("\\\\", "/")));
            return setAppendToTargetFile((File)null);
        }
        
        public FileFilter getAppendToTargetFileFilter() { return getter(ARGNAME_APPENDTOTARGETFILEFILTER, FileFilter.class); }
        public CopyrighterWriterParameterSpec setAppendToTargetFileFilter(final FileFilter appendToTargetFileFilter) { return (CopyrighterWriterParameterSpec)setter(ARGNAME_APPENDTOTARGETFILEFILTER, FileFilter.class, appendToTargetFileFilter); }
    }
    
    public static boolean appendCopyrightText(final CopyrighterWriterParameterSpec parameterSpec)
           throws IOException
    {
        final TraceId traceId = parameterSpec!=null&&parameterSpec.getTraceIdRef()!=null?
        new TraceId(parameterSpec.getTraceIdRef()):new TraceId();
        
        final Set<File> appendSourceFiles = new LinkedHashSet<>(); // all source files to process
        final Set<File> appendSourceSuccessFiles = new LinkedHashSet<>(); // success copyright appended ones
        final Map<File, String> appendSourceIgnoreFiles = new LinkedHashMap<>(); // key=the file, value=reason why ignored
        final Map<File, String> appendSourceFailureFiles = new LinkedHashMap<>(); // key=the file, value=reason why failed
        
        log.info("# "+traceId+" # appendCopyrightText() : enters");
        try
        {
            if(parameterSpec==null)
                throw new IllegalArgumentException("Bad parameter [parameterSpec] is null; expecting ["+
                CopyrighterWriterParameterSpec.class.getName()+"] object type");
            if(parameterSpec.getCopyrightContentFile()==null)
                throw new IllegalArgumentException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_COPYRIGHTCONTENTFILE+
                "] is null; expecting ["+File.class.getName()+"] object type containing the copyright content template to use to append to the target file");
            if(!parameterSpec.getCopyrightContentFile().exists())
                throw new FileNotFoundException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_COPYRIGHTCONTENTFILE+
                "] is not found at path ["+parameterSpec.getCopyrightContentFile().getPath()+"]");
            if(!parameterSpec.getCopyrightContentFile().canRead())
                throw new SecurityException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_COPYRIGHTCONTENTFILE+
                "] has no READ access at path ["+parameterSpec.getCopyrightContentFile().getPath()+"]");
            if(parameterSpec.getAppendToTargetFile()==null)
                throw new IllegalArgumentException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_APPENDTOTARGETFILE+
                "] is null; expecting ["+File.class.getName()+"] object type target file to append copyright content");
            if(!parameterSpec.getAppendToTargetFile().exists())
                throw new IllegalArgumentException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_APPENDTOTARGETFILE+
                "] is not found at path ["+parameterSpec.getAppendToTargetFile().getPath()+"]");
            if(!parameterSpec.getAppendToTargetFile().canRead())
                throw new SecurityException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_APPENDTOTARGETFILE+
                "] has no READ access at path ["+parameterSpec.getAppendToTargetFile().getPath()+"]");
            if(!parameterSpec.getAppendToTargetFile().canWrite())
                throw new SecurityException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_APPENDTOTARGETFILE+
                "] has no WRITE access at path ["+parameterSpec.getAppendToTargetFile().getPath()+"]");
            if(parameterSpec.getAppendAtLineNo()==0)
                throw new IllegalArgumentException("Bad parameter ["+
                CopyrighterWriterParameterSpec.ARGNAME_APPENDATLINENO+"] is not a valid line no. ["+
                parameterSpec.getAppendAtLineNo()+"]; line no. should start at [1]");
            
            // let's get all the header files that we can use
            final Set<File> copyrightHeaderFiles = new LinkedHashSet<>();
            listFiles(parameterSpec.getCopyrightContentFile(), 
                copyrightHeaderFiles,
                (parameterSpec.getCopyrightContentFileFilter()!=null?
                    parameterSpec.getCopyrightContentFileFilter():
                    new CopyrightReaderContentFilter(traceId) // default this // default this
                )
            );
            // no header files to use
            if(copyrightHeaderFiles.isEmpty())
                log.warning("# "+traceId+" # appendCopyrightText() : No copyright header files found, require at least one (1) copyright header file to use as a reference");
            else
            {
                log.info("# "+traceId+" # appendCopyrightText() : Found ["+
                copyrightHeaderFiles.size()+"] copyright header files");

                // let's get all the source files that we need to append the copyright headers
                listFiles(
                    parameterSpec.getAppendToTargetFile(), 
                    appendSourceFiles,
                    parameterSpec.getAppendToTargetFileFilter()
                );
                
                // no source files to use
                if(appendSourceFiles.isEmpty())
                    log.warning("# "+traceId+" # appendCopyrightText() : No source files found, require at least one (1) source file");
                else
                {
                    log.info("# "+traceId+" # appendCopyrightText() : Found ["+
                    appendSourceFiles.size()+"] source files");

                    // let's get the newline to use, go by system specific...so if somehow this was run on windows...it'll not be the same as *nix
                    final String newline = System.getProperty("line.separator");

                    // let's get the copyright string template to append
                    final Map<String, String> copyrightHeaderContents = new LinkedHashMap<>(); // key=file extension, value=copyright content
                    for(File copyrightHeaderFile:copyrightHeaderFiles)
                    {
                        if(!copyrightHeaderFile.canRead())
                        {
                            log.warning("# "+traceId+" # appendCopyrightText() : Unable to gain READ access for copyright header file ["+
                            copyrightHeaderFile.getPath()+"]; READ permission denied");
                            continue;
                        }

                        // create the copyright content header
                        final CopyrightReaderContentHeader copyrightWriterContentHeader = 
                        new CopyrightReaderContentHeader(
                            traceId, 
                            copyrightHeaderFile, 
                            SUPPORTED_STATIC_TAGS
                        );
                        final Set<String> copyrightWriterContentHeaderExtensions = 
                        copyrightWriterContentHeader.getBindedFileExtensions();
                        if(copyrightWriterContentHeaderExtensions.isEmpty())
                        {
                            log.warning("# "+traceId+" # appendCopyrightText() : Unable to determine file extension for copyright header file ["+
                            copyrightHeaderFile.getPath()+"]; can't extract extension");
                            continue;
                        }
                        log.info("# "+traceId+" # appendCopyrightText() : Found copyright header file ["+
                        copyrightHeaderFile.getPath()+"] extensions size ["+
                        copyrightWriterContentHeaderExtensions.size()+"] --- \n\t"+
                        Arrays.toString(copyrightWriterContentHeaderExtensions.toArray(
                        new String[copyrightWriterContentHeaderExtensions.size()])));

                        // get the copyright header content; and replace it with the static contents that matches the tags
                        final StringBuilder copyrightHeaderContent = copyrightWriterContentHeader.getContent();
                        if(copyrightHeaderContent==null)
                        {
                            log.warning("# "+traceId+" # appendCopyrightText() : Unable to get contenet for copyright header file ["+
                            copyrightHeaderFile.getPath()+"] returned null; expecting ["+
                            StringBuilder.class.getName()+"] object type. Check previous log for details");
                            continue;
                        }
                        log.info("# "+traceId+" # appendCopyrightText() : Copyright header file ["+
                        copyrightHeaderFile.getPath()+"] content --- \n\t"+copyrightHeaderContent);

                        // add to the header file extension map
                        for(String copyrightWriterContentHeaderExtension:copyrightWriterContentHeaderExtensions)
                        {
                            if(copyrightHeaderContents.containsKey(copyrightWriterContentHeaderExtension.trim().toLowerCase()))
                            {
                                log.warning("# "+traceId+" # appendCopyrightText() : Copyright header for file extension ["+
                                copyrightWriterContentHeaderExtension+"] already available; skipping to add extension binding content");
                                continue;
                            }
                            copyrightHeaderContents.put(
                                copyrightWriterContentHeaderExtension, 
                                copyrightHeaderContent.toString()
                            );
                            log.info("# "+traceId+" # appendCopyrightText() : Header content added for --- \n\tFile Extension: "+
                            copyrightWriterContentHeaderExtension+"\n\tHeader Content:\n"+
                            copyrightHeaderContent+"\n\n");
                        }
                    }
                    final StringBuilder dbgCopyrightHeaderExtensions = new StringBuilder();
                    for(String copyrightHeaderExtension:copyrightHeaderContents.keySet())
                    {
                        dbgCopyrightHeaderExtensions.append("\n\t");
                        dbgCopyrightHeaderExtensions.append(copyrightHeaderExtension);
                    }
                    log.info("# "+traceId+" # appendCopyrightText() : Copyright headers size ["+
                    copyrightHeaderContents.size()+"]; supported extension(s) --- "+
                    dbgCopyrightHeaderExtensions);

                    // ok, let's loop all the source files and find the matching header 
                    // file extension that MUST MATCH the source extension
                    for(File appendSourceFile:appendSourceFiles)
                    {
                        if(!appendSourceFile.canRead())
                        {
                            log.warning("# "+traceId+" # appendCopyrightText() : Unable to gain READ access for target source file ["+
                            appendSourceFile.getPath()+"]; READ permission denied");
                            appendSourceFailureFiles.put(appendSourceFile, "READ PERMISSION DENIED");
                            continue;
                        }
                        if(!appendSourceFile.canWrite())
                        {
                            log.warning("# "+traceId+" # appendCopyrightText() : Unable to gain WRITE access for target source file ["+
                            appendSourceFile.getPath()+"]; WRITE permission denied");
                            appendSourceFailureFiles.put(appendSourceFile, "WRITE PERMISSION DENIED");
                            continue;
                        }

                        // determine the file extension
                        final int sourceFileExtIdx = appendSourceFile.getName().lastIndexOf('.');
                        if(sourceFileExtIdx<0)
                        {
                            log.warning("# "+traceId+" # appendCopyrightText() : Unable to determine file extension type for target source file ["+
                            appendSourceFile.getPath()+"]; index of '.' returned invalid index ["+
                            sourceFileExtIdx+"], expecting greater or equals to [0]");
                            appendSourceFailureFiles.put(appendSourceFile, "UNKNOWN FILE EXTENSION");
                            continue;
                        }
                        final String sourceFileExt = appendSourceFile.getName().substring((sourceFileExtIdx+1)).toLowerCase();
                        if(!copyrightHeaderContents.containsKey(sourceFileExt))
                        {
                            log.warning("# "+traceId+" # appendCopyrightText() : Target source file ["+
                            appendSourceFile.getPath()+"] contains no copyright header content to append for file extension ["+
                            sourceFileExt+"]; available copyright header content extensions are --- "+
                            dbgCopyrightHeaderExtensions);
                            appendSourceIgnoreFiles.put(appendSourceFile, "NO MATCHING COPYRIGHT HEADER CONTENT");
                            continue;
                        }
                        final String copyrightHeaderContent = copyrightHeaderContents.get(sourceFileExt);
                        log.info("# "+traceId+" # appendCopyrightText() : Got target source file ["+
                        appendSourceFile.getPath()+"] extension ["+sourceFileExt+"], copyright header content to append --- \n"+
                        copyrightHeaderContent+"\n");

                        // https://stackoverflow.com/questions/16665124/java-how-to-append-text-to-top-of-file-txt
                        final File tempSourceFile = File.createTempFile(appendSourceFile.getName(), ".bak");
                        final FileOutputStream fos = new FileOutputStream(tempSourceFile);
                        RandomAccessFile appendToTargetRndAcsFile = null;
                        boolean sourceFileBackedup = false;
                        try
                        {
                            // backup the original source before appending the copyright information
                            appendToTargetRndAcsFile = new RandomAccessFile(appendSourceFile, "rw");
                            int data = appendToTargetRndAcsFile.read();
                            while(data!=-1)
                            {
                                fos.write(data);
                                data = appendToTargetRndAcsFile.read();
                            }
                            try { fos.flush(); } catch(Throwable t) {}
                            try { fos.close(); } catch(Throwable t) {}
                            sourceFileBackedup = true;
                            
                            // write the copyright content first
                            appendToTargetRndAcsFile.seek(0); // to the beginning
                            appendToTargetRndAcsFile.write(copyrightHeaderContent.getBytes());
                            appendToTargetRndAcsFile.write(newline.getBytes());
                            
                            // append back the original data
                            final FileInputStream fis = new FileInputStream(tempSourceFile);
                            try
                            {
                                data = fis.read();
                                while(data!=-1)
                                {
                                    appendToTargetRndAcsFile.write(data);
                                    data = fis.read();
                                }
                            }
                            finally
                            {
                                try { fis.close(); } catch(Throwable t) {}
                            }
                            
                            // mark success
                            appendSourceSuccessFiles.add(appendSourceFile);
                        }
                        catch(Throwable t)
                        {
                            log.log(Level.SEVERE, "# "+traceId+" # appendCopyrightText() : Unable to WRITE copyright header to target source file ["+
                            appendSourceFile.getPath()+"]; exception occurred --- "+t.getMessage(), t);
                            appendSourceFailureFiles.put(appendSourceFile, "WRITE FAILURE COPYRIGHT HEADER CONTENT");
                            
                            // revert back the backup; overwrite the file
                            if(sourceFileBackedup)
                            {
                                final FileInputStream fisOri = new FileInputStream(tempSourceFile);
                                final FileOutputStream fosOri = new FileOutputStream(appendSourceFile);
                                try
                                {
                                    int data = fisOri.read();
                                    while(data!=-1)
                                    {
                                        fosOri.write(data);
                                        data = fisOri.read();
                                    }
                                }
                                finally
                                {
                                    try { fosOri.flush(); } catch(Throwable t1) {}
                                    try { fosOri.close(); } catch(Throwable t1) {}
                                    try { fisOri.close(); } catch(Throwable t1) {}
                                }
                            }
                        }
                        finally
                        {
                            try { fos.flush(); } catch(Throwable t) {}
                            try { fos.close(); } catch(Throwable t) {}
                            try
                            {
                                if(tempSourceFile.exists())
                                {
                                    if(tempSourceFile.delete())
                                        log.info("# "+traceId+" # appendCopyrightText() : Deleted temporary source file ["+
                                        tempSourceFile.getPath()+"]");
                                    else
                                        log.warning("# "+traceId+" # appendCopyrightText() : Unable to delete temporary source file ["+
                                        tempSourceFile.getPath()+"]");
                                }
                            }
                            catch(Throwable t) {}
                            if(appendToTargetRndAcsFile!=null)
                                try { appendToTargetRndAcsFile.close(); } catch(Throwable t) {}
                        }
                    }
                }
            }
        }
        catch(Throwable t)
        {
            log.log(Level.SEVERE, "# "+traceId+" # appendCopyrightText() : Exception occurred", t);
            if(t instanceof RuntimeException)
                throw (RuntimeException)t;
            if(t instanceof IOException)
                throw (IOException)t;
            final IOException e = new IOException("Unable to append copyright text to target file ["+
            (parameterSpec!=null&&parameterSpec.getAppendToTargetFile()!=null?parameterSpec.getAppendToTargetFile().getPath():null)+
            "] at line no. ["+(parameterSpec!=null?parameterSpec.getAppendAtLineNo():null)+"]", t);
            throw e;
        }
        finally
        {
            log.info("# "+traceId+" # appendCopyrightText() : Returning appended result ["+
            (appendSourceFiles.size()==(appendSourceSuccessFiles.size()+appendSourceIgnoreFiles.size()))+
            "] --- \n\tTotal Source File(s): "+appendSourceFiles.size()+
            "\n\t[SUCCESS] Total Appended Source File(s): "+appendSourceSuccessFiles.size()+
            "\n\t[IGNORED] Total Ignored Source File(s): "+appendSourceIgnoreFiles.size()+
            "\n\t[FAILURE] Total Failed Source File(s): "+appendSourceFailureFiles.size());
            log.info("# "+traceId+" # appendCopyrightText() : exits");
        }
        return (appendSourceFiles.size()==(appendSourceSuccessFiles.size()+appendSourceIgnoreFiles.size()));
    }
    
    private static void listFiles(final File file,
                                  final Collection<File> flattenFileStructure,
                                  final FileFilter fileFilter)
    {
        if(file==null)
            return;
        if(file.isFile())
        {
            boolean accept = true;
            if(fileFilter!=null)
                accept = fileFilter.accept(file);
            if(accept)
                flattenFileStructure.add(file);
        }
        else
        {
            final File[] _dirFiles = fileFilter!=null?
            file.listFiles(fileFilter):file.listFiles();
            for(File _dirFile:_dirFiles)
                listFiles(_dirFile, flattenFileStructure, fileFilter);
        }
    }
}
