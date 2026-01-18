package com.cgvsu.objreader;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

class ObjReaderTest {

    // ========== ТЕСТЫ ДЛЯ parseVertex ==========

    @Test
    public void testParseVertex01() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.03f);
        Assertions.assertTrue(result.equals(expectedResult));
    }

    @Test
    public void testParseVertex02() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.01", "1.02", "1.03"));
        Vector3f result = ObjReader.parseVertex(wordsInLineWithoutToken, 5);
        Vector3f expectedResult = new Vector3f(1.01f, 1.02f, 1.10f);
        Assertions.assertFalse(result.equals(expectedResult));
    }

    @Test
    public void testParseVertex03() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("ab", "o", "ba"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Failed to parse float value.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex04() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Wrong number of vertex.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    @Test
    public void testParseVertex05() {
        ArrayList<String> wordsInLineWithoutToken = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0"));
        try {
            ObjReader.parseVertex(wordsInLineWithoutToken, 10);
        } catch (ObjReaderException exception) {
            String expectedError = "Error parsing OBJ file on line: 10. Wrong number of vertex.";
            Assertions.assertEquals(expectedError, exception.getMessage());
        }
    }

    // ========== ТЕСТЫ ДЛЯ parseNormal ==========
    @Test
    public void testParseNormal01() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("0.0", "1.0", "0.0"));
        Vector3f result = ObjReader.parseNormal(words, 5);
        Vector3f expected = new Vector3f(0.0f, 1.0f, 0.0f);
        Assertions.assertTrue(result.equals(expected));
    }

    @Test
    public void testParseNormal02() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("-1.0", "0.5", "2.5"));
        Vector3f result = ObjReader.parseNormal(words, 5);
        Vector3f expected = new Vector3f(-1.0f, 0.5f, 2.5f);
        Assertions.assertTrue(result.equals(expected));
    }

    @Test
    public void testParseNormal03() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("x", "y", "z"));
        try {
            ObjReader.parseNormal(words, 10);
            Assertions.fail("Должно быть исключение");
        } catch (ObjReaderException e) {
            Assertions.assertEquals("Error parsing OBJ file on line: 10. Failed to parse float value.", e.getMessage());
        }
    }

    @Test
    public void testParseNormal04() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1.0", "2.0"));
        try {
            ObjReader.parseNormal(words, 10);
            Assertions.fail("Должно быть исключение");
        } catch (ObjReaderException e) {
            Assertions.assertEquals("Error parsing OBJ file on line: 10. Wrong number of normal.", e.getMessage());
        }
    }

    @Test
    public void testParseNormal05() {
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1.0", "2.0", "3.0", "4.0"));
        try {
            ObjReader.parseNormal(words, 10);
            Assertions.fail("Должно быть исключение");
        } catch (ObjReaderException e) {
            Assertions.assertEquals("Error parsing OBJ file on line: 10. Wrong number of normal.", e.getMessage());
        }
    }
    //================= some Face tests =================


    @Test
    public void testParseFace_TriangleBasic() {
        // f 1 2 3
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1", "2", "3"));
        Polygon polygon = ObjReader.parseFace(words, 5);

        Assertions.assertEquals(Arrays.asList(0, 1, 2), polygon.getVertexIndices());
        Assertions.assertNull(polygon.getTextureVertexIndices());
        Assertions.assertNull(polygon.getNormalIndices()); //должны быть null
    }

    @Test
    public void testParseFace_QuadWithTextures() {
        // f 1/1 2/2 3/3 4/4
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1/1", "2/2", "3/3", "4/4"));
        Polygon polygon = ObjReader.parseFace(words, 6);

        Assertions.assertEquals(Arrays.asList(0, 1, 2, 3), polygon.getVertexIndices());
        Assertions.assertEquals(Arrays.asList(0, 1, 2, 3), polygon.getTextureVertexIndices());
        Assertions.assertNull(polygon.getNormalIndices());
    }

    @Test
    public void testParseFace_TooFewVertices() {
        // f 1 2  ← ошибка!
        ArrayList<String> words = new ArrayList<>(Arrays.asList("1", "2"));

        try {
            ObjReader.parseFace(words, 10);
            Assertions.fail("Должно быть исключение");
        } catch (ObjReaderException e) {
            Assertions.assertTrue(e.getMessage().contains("Polygon must have at least 3 vertices"));
        }
    }
}