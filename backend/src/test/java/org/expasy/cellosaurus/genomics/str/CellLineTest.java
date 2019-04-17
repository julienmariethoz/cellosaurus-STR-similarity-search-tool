package org.expasy.cellosaurus.genomics.str;

import org.expasy.cellosaurus.formats.xml.Parser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CellLineTest {

    @Test
    public void resolveTest1() throws IOException {
        Parser parser = new Parser(getClass().getClassLoader().getResource("CVCL/CVCL_0238.xml").getFile());
        List<Profile> profiles = parser.getCellLines().get(0).getProfiles();
        assertEquals(16, profiles.size());
    }

    @Test
    public void resolveTest2() throws IOException {
        Parser parser = new Parser(getClass().getClassLoader().getResource("CVCL/CVCL_0624.xml").getFile());
        List<Profile> profiles = parser.getCellLines().get(0).getProfiles();
        assertEquals(150, profiles.size());
    }

    @Test
    public void resolveTest3() throws IOException {
        Parser parser = new Parser(getClass().getClassLoader().getResource("CVCL/CVCL_0633.xml").getFile());
        List<Profile> profiles = parser.getCellLines().get(0).getProfiles();
        assertEquals(16, profiles.size());
    }

    @Test
    public void resolveTest4() throws IOException {
        Parser parser = new Parser(getClass().getClassLoader().getResource("CVCL/CVCL_1793.xml").getFile());
        List<Profile> profiles = parser.getCellLines().get(0).getProfiles();
        assertEquals(3, profiles.size());
    }

    @Test
    public void resolveTest5() throws IOException {
        Parser parser = new Parser(getClass().getClassLoader().getResource("CVCL/CVCL_1893.xml").getFile());
        List<Profile> profiles = parser.getCellLines().get(0).getProfiles();
        assertEquals(4, profiles.size());
    }

    @Test
    public void resolveTest6() throws IOException {
        Parser parser = new Parser(getClass().getClassLoader().getResource("CVCL/CVCL_D605.xml").getFile());
        List<Profile> profiles = parser.getCellLines().get(0).getProfiles();
        assertEquals(1, profiles.size());
    }

    @Test
    public void resolveTest7() throws IOException {
        Parser parser = new Parser(getClass().getClassLoader().getResource("CVCL/CVCL_0013.xml").getFile());
        List<Profile> profiles = parser.getCellLines().get(0).getProfiles();
        assertEquals(150, profiles.size());
    }
}