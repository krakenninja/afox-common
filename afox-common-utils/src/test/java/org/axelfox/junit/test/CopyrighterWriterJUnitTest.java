package org.axelfox.junit.test;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import org.axelfox.common.util.writer.CopyrightWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class CopyrighterWriterJUnitTest
{
    public CopyrighterWriterJUnitTest() {}
    
    // http://tutorials.jenkov.com/java-logging/configuration.html
    @BeforeClass
    public static void setUpClass()
    {
        final URL loggingPropsURL = Thread.currentThread().getContextClassLoader().getResource("logging.properties");
        assertNotNull("Unable to get resource \"logging.properties\" returned null", loggingPropsURL);
        try
        {
            final String loggingPropsFilePath = new File(loggingPropsURL.toURI()).getPath();
            System.setProperty("java.util.logging.config.file", loggingPropsFilePath);
        }
        catch(Throwable t)
        {
            t.printStackTrace(System.err);
            fail("ERROR: Failed to setup JUnit Test class --- "+t.getMessage());
        }
    }
    
    @AfterClass
    public static void tearDownClass() {}
    
    @Before
    public void setUp() {}
    
    @After
    public void tearDown() {}

//    @Ignore
    @Test
    public void appendCopyrightText()
    {
        final String sources = "sources";
        final String headers = "headers";
        try
        {
            final Enumeration<URL> resourceSourceTargets = Thread.currentThread().getContextClassLoader().getResources(sources);
            if(resourceSourceTargets==null||!resourceSourceTargets.hasMoreElements())
                throw new Exception("No source targets found at \""+sources+"\" returned null or empty ["+
                (resourceSourceTargets!=null?0:null)+"]");
            final Set<File> sourceTargetFiles = new LinkedHashSet<>();
            while(resourceSourceTargets.hasMoreElements())
            {
                final URL sourceTarget = resourceSourceTargets.nextElement();
                final File sourceTargetFile = new File(sourceTarget.toURI());
                if(!sourceTargetFile.exists())
                    continue;
                sourceTargetFiles.add(sourceTargetFile);
            }
            if(sourceTargetFiles.isEmpty())
                throw new Exception("No resources found at \""+sources+"\" returned empty");
            
            final Enumeration<URL> resourceHeaders = Thread.currentThread().getContextClassLoader().getResources(headers);
            if(resourceHeaders==null||!resourceHeaders.hasMoreElements())
                throw new Exception("No resources found at \""+headers+"\" returned null or empty ["+
                (resourceHeaders!=null?0:null)+"]");
            final Set<File> copyrightHeaderFiles = new LinkedHashSet<>();
            while(resourceHeaders.hasMoreElements())
            {
                final URL resourceHeader = resourceHeaders.nextElement();
                final File resourceHeaderFile = new File(resourceHeader.toURI());
                if(!resourceHeaderFile.exists())
                    continue;
                copyrightHeaderFiles.add(resourceHeaderFile);
            }
            if(copyrightHeaderFiles.isEmpty())
                throw new Exception("No resources found at \""+headers+"\" returned empty");
            
            for(File sourceTargetFile:sourceTargetFiles)
            {
                for(File copyrightHeaderFile:copyrightHeaderFiles)
                {
                    final CopyrightWriter.CopyrighterWriterParameterSpec parameterSpec = 
                    new CopyrightWriter.CopyrighterWriterParameterSpec()
                        .setAppendAtLineNo(1)
                        .setAppendToTargetFile(sourceTargetFile)
                        .setCopyrightContentFile(copyrightHeaderFile)
                    ;
                    CopyrightWriter.appendCopyrightText(parameterSpec);
                }
            }
        }
        catch(Throwable t)
        {
            t.printStackTrace(System.err);
            fail("ERROR: Failed to append copyright text - "+t.getMessage());
        }
    }
}
