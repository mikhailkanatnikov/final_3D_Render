package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

    private static final String OBJ_VERTEX_TOKEN = "v";
    private static final String OBJ_TEXTURE_TOKEN = "vt";
    private static final String OBJ_NORMAL_TOKEN = "vn";
    private static final String OBJ_FACE_TOKEN = "f";

    public static Model read(String fileContent) {
        Model result = new Model();

        int lineInd = 0;
        Scanner scanner = new Scanner(fileContent);
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            ArrayList<String> wordsInLine = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
            if (wordsInLine.isEmpty()) {
                continue;
            }

            final String token = wordsInLine.getFirst();
            wordsInLine.removeFirst();

            ++lineInd;
            switch (token) {

                case OBJ_VERTEX_TOKEN -> result.getVertices().add(parseVertex(wordsInLine, lineInd));
                case OBJ_TEXTURE_TOKEN -> result.getTextureVertices().add(parseTextureVertex(wordsInLine, lineInd));
                case OBJ_NORMAL_TOKEN -> result.getNormals().add(parseNormal(wordsInLine, lineInd));
                case OBJ_FACE_TOKEN -> result.getPolygons().add(parseFace(wordsInLine, lineInd));
                default -> {
                }
            }
        }

        checkModel(result);
        return result;
    }

    // Всем методам кроме основного я поставил модификатор доступа protected, чтобы обращаться к ним в тестах
    protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        try {
            if (wordsInLineWithoutToken.size() != 3) {
                throw new ObjReaderException("Wrong number of vertex.", lineInd);
            }
            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few vertex arguments.", lineInd);
        }
    }

    protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        try {
            if (wordsInLineWithoutToken.size() != 2) {
                throw new ObjReaderException("Wrong number of texture vertex.", lineInd);
            }
            return new Vector2f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
        }
    }

    protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        try {
            if (wordsInLineWithoutToken.size() != 3) {
                throw new ObjReaderException("Wrong number of normal.", lineInd);
            }
            return new Vector3f(
                    Float.parseFloat(wordsInLineWithoutToken.get(0)),
                    Float.parseFloat(wordsInLineWithoutToken.get(1)),
                    Float.parseFloat(wordsInLineWithoutToken.get(2)));

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse float value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few normal arguments.", lineInd);
        }
    }

    protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
        ArrayList<Integer> onePolygonVertexIndices = new ArrayList<Integer>();
        ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<Integer>();
        ArrayList<Integer> onePolygonNormalIndices = new ArrayList<Integer>();

        for (String s : wordsInLineWithoutToken) {
            parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
        }

        if (onePolygonVertexIndices.size() < 3) {
            throw new ObjReaderException("Polygon must have at least 3 vertices.", lineInd);
        }

        Polygon result = new Polygon();

        result.setLineNumber(lineInd);
        result.setVertexIndices(onePolygonVertexIndices);


        result.setTextureVertexIndices(onePolygonTextureVertexIndices.isEmpty() ? null : onePolygonTextureVertexIndices);

        result.setNormalIndices(onePolygonNormalIndices.isEmpty() ? null : onePolygonNormalIndices);

        return result;
    }

    // Обратите внимание, что для чтения полигонов я выделил еще один вспомогательный метод.
    // Это бывает очень полезно и с точки зрения структурирования алгоритма в голове, и с точки зрения тестирования.
    // В радикальных случаях не бойтесь выносить в отдельные методы и тестировать код из одной-двух строчек.
    protected static void parseFaceWord(
            String wordInLine,
            ArrayList<Integer> onePolygonVertexIndices,
            ArrayList<Integer> onePolygonTextureVertexIndices,
            ArrayList<Integer> onePolygonNormalIndices,
            int lineInd) {
        try {
            String[] wordIndices = wordInLine.split("/");
            switch (wordIndices.length) {
                // f v1 v2 v3
                case 1 -> {
                    int vertex = Integer.parseInt(wordIndices[0]);
                    if (vertex <= 0) {
                        throw new ObjReaderException("Vertex should be >= 1", lineInd);
                    }
                    onePolygonVertexIndices.add(vertex - 1);
                }
                case 2 -> {
                    // f v1/vt1 v2/vt2 v3/vt3
                    int vertex = Integer.parseInt(wordIndices[0]);
                    if (vertex <= 0) {
                        throw new ObjReaderException("Vertex should be >= 1", lineInd);
                    }
                    onePolygonVertexIndices.add(vertex - 1);

                    int texture = Integer.parseInt(wordIndices[1]);
                    if (texture <= 0) {
                        throw new ObjReaderException("Texture should be >= 1", lineInd);
                    }
                    onePolygonTextureVertexIndices.add(texture - 1);
                }
                case 3 -> {
                    // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
                    //  f v1//vn1 v2//vn2 v3//vn3
                    int vertex = Integer.parseInt(wordIndices[0]);
                    if (vertex <= 0) {
                        throw new ObjReaderException("Vertex should be >= 1", lineInd);
                    }
                    onePolygonVertexIndices.add(vertex - 1);

                    if (!wordIndices[1].isEmpty()) {
                        int texture = Integer.parseInt(wordIndices[1]);
                        if (texture <= 0) {
                            throw new ObjReaderException("Texture should be >= 1", lineInd);
                        }
                        onePolygonTextureVertexIndices.add(texture - 1);
                    }

                    if (!wordIndices[2].isEmpty()) {
                        int normal = Integer.parseInt(wordIndices[2]);
                        if (normal <= 0) {
                            throw new ObjReaderException("Normal should be >= 1", lineInd);
                        }
                        onePolygonNormalIndices.add(normal - 1);

                    }
                }
                default -> throw new ObjReaderException("Invalid element size.", lineInd);
            }

        } catch (NumberFormatException e) {
            throw new ObjReaderException("Failed to parse int value.", lineInd);

        } catch (IndexOutOfBoundsException e) {
            throw new ObjReaderException("Too few arguments.", lineInd);
        }
    }

    //обработка ошибок при чтении
    protected static void checkModel(Model result) {
        if (result.getVertices().isEmpty()) {
            throw new ObjReaderException("Файл не содержит вершин.", 0);
        }

        if (result.getPolygons().isEmpty()) {
            throw new ObjReaderException("Файл не содержит полигонов.", 0);
        }
        //проверка чтобы индексы в полигонах совпадали с количеством вершин/текстур/нормалей в файле
        int amountVertices = result.getVertices().size();
        int amountTextures = result.getTextureVertices().size();
        int amountNormals = result.getNormals().size();

        for (Polygon pol : result.getPolygons()) {
            for (int vertexIndex : pol.getVertexIndices()) {
                if (vertexIndex >= amountVertices) {
                    throw new ObjReaderException("Индекс вершины " + (vertexIndex + 1) + " превышает количество вершин в файле.", pol.getLineNumber());
                }
            }

            if (pol.getTextureVertexIndices() != null) {
                for (int textureIndex : pol.getTextureVertexIndices()) {
                    if (textureIndex >= amountTextures) {
                        throw new ObjReaderException("Индекс текстуры " + (textureIndex + 1) + " превышает количество текстур в файле.", pol.getLineNumber());
                    }
                }
            }

            if (pol.getNormalIndices() != null) {
                for (int normalIndex : pol.getNormalIndices()) {
                    if (normalIndex >= amountNormals) {
                        throw new ObjReaderException("Индекс нормали " + (normalIndex + 1) + " превышает количество нормалей в файле.", pol.getLineNumber());
                    }
                }
            }
        }


    }
}