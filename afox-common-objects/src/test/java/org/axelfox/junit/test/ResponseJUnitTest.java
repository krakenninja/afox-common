package org.axelfox.junit.test;

import org.axelfox.common.object.response.DefaultResponse;
import org.axelfox.common.object.response.DefaultResponseCode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class ResponseJUnitTest
{
    public ResponseJUnitTest() {}
    
    @BeforeClass
    public static void setUpClass() {}
    
    @AfterClass
    public static void tearDownClass() {}
    
    @Before
    public void setUp() {}
    
    @After
    public void tearDown() {}

    @Ignore
    @Test
    public void testResponse()
    {
        try
        {
            DefaultResponse response = new DefaultResponse()
                .code(DefaultResponseCode.OK_SUCCESS)
                .object(Integer.valueOf(4718));
            ;
            
            System.out.println("INFO: "+response.objectAs(int.class));
            System.out.println("INFO: "+response.objectAs(Integer.class));
            System.out.println("INFO: "+response);
        }
        catch(Throwable t)
        {
            t.printStackTrace(System.err);
            fail("ERROR: "+t.getMessage());
        }
    }
}
