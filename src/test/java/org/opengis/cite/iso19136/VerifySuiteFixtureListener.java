package org.opengis.cite.iso19136;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

public class VerifySuiteFixtureListener {

    private XmlSuite xmlSuite;
    private ISuite suite;

    public VerifySuiteFixtureListener() {
    }

    @Before
    public void setUpClass() {
        xmlSuite = mock(XmlSuite.class);
        suite = mock(ISuite.class);
        when(suite.getXmlSuite()).thenReturn(xmlSuite);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noSuiteParameters() {
        Map<String, String> params = new HashMap<String, String>();
        when(xmlSuite.getParameters()).thenReturn(params);
        SuiteFixtureListener iut = new SuiteFixtureListener();
        iut.onStart(suite);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void processIUTParameter() throws URISyntaxException {
        URL url = this.getClass().getResource("/xsd/gamma.xsd");
        Map<String, String> params = new HashMap<String, String>();
        params.put(TestRunArg.IUT.toString(), url.toURI().toString());
        when(xmlSuite.getParameters()).thenReturn(params);
        ArgumentCaptor<Set> uriSet = ArgumentCaptor.forClass(Set.class);
        SuiteFixtureListener iut = new SuiteFixtureListener();
        iut.onStart(suite);
        verify(suite).setAttribute(
                ArgumentMatchers.eq(SuiteAttribute.SCHEMA_LOC_SET.getName()),
                uriSet.capture());
        assertFalse("Expected non-empty set of URIs.", uriSet.getValue()
                .isEmpty());
    }

}
