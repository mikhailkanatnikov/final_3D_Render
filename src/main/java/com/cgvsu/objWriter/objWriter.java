package com.cgvsu.objWriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class objWriter {

    // Основной метод
    public void write(Model model, String path) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("Модель не может быть null");
        }
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Путь не может быть null");
        }

        try (PrintWriter writer = new PrintWriter(path)) {
            writeModel(model, writer);
        }
    }

    // Запись в строку для тестов
    public String writeToString(Model model) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("Модель не может быть null");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos)) {
            writeModel(model, writer);
        }
        return baos.toString();
    }

    // ОБЩАЯ логика записи
    private void writeModel(Model model, PrintWriter writer) {
        //вершины v
        for (Vector3f v : model.vertices) {
            writer.println("v " + v.getX() + " " + v.getY() + " " + v.getZ());
        }

        //текстуры vt
        if (model.textureVertices != null) {
            for (Vector2f vt : model.textureVertices) {
                writer.println("vt " + vt.getX() + " " + vt.getY());
            }
        }

        //нормали vn
        if (model.normals != null) {
            for (Vector3f vn : model.normals) {
                writer.println("vn " + vn.getX() + " " + vn.getY() + " " + vn.getZ());
            }
        }

        //ПОЛИГОНЫ
        for (Polygon pol : model.polygons) {

            //состоит только из вершин
            if (pol.getTextureVertexIndices() == null && pol.getNormalIndices() == null) {
                writer.print("f ");
                for (int v : pol.getVertexIndices()) {
                    writer.print((v + 1) + " ");
                }
                writer.println();

            }

            //вершины + текстуры
            if (pol.getTextureVertexIndices() != null && pol.getNormalIndices() == null) {
                writer.print("f ");
                for (int i = 0; i < pol.getVertexIndices().size(); i++) {
                    int verIndex = pol.getVertexIndices().get(i) + 1;
                    int textIndex = pol.getTextureVertexIndices().get(i) + 1;
                    writer.print(verIndex + "/" + textIndex + " ");
                }
                writer.println();
            }

            //вершины + нормали
            if (pol.getTextureVertexIndices() == null && pol.getNormalIndices() != null) {
                writer.print("f ");
                for (int i = 0; i < pol.getVertexIndices().size(); i++) {
                    int verInd = pol.getVertexIndices().get(i) + 1;
                    int normInd = pol.getNormalIndices().get(i) + 1;
                    writer.print(verInd + "//" + normInd + " ");
                }
                writer.println();
            }

            //вершины + текстуры + нормали
            if (pol.getTextureVertexIndices() != null && pol.getNormalIndices() != null) {
                writer.print("f ");
                for (int i = 0; i < pol.getVertexIndices().size(); i++) {
                    int ver = pol.getVertexIndices().get(i) + 1;
                    int verTex = pol.getTextureVertexIndices().get(i) + 1;
                    int normInd = pol.getNormalIndices().get(i) + 1;
                    writer.print(ver + "/" + verTex + "/" + normInd + " ");
                }
                writer.println();
            }
        }
    }
}