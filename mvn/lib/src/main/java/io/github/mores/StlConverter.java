package io.github.mores;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class StlConverter {

    private DataInputStream br;
    private int converted = 0;
    private ArrayList<String> vertices = new ArrayList<>();
    private ArrayList<String> triangles = new ArrayList<>();
    private int vertexIndex = 0;

    public StlConverter(InputStream stl) {
        br = new DataInputStream(stl);
    }

    public ArrayList<String> getTriangles() {
        return triangles;
    }

    public ArrayList<String> getVertices() {
        return vertices;
    }

    public String getScad() {
        String points = "points=[";
        points = points + String.join(", ", getVertices());

        points = points + "],";

        String faces = "faces=[";
        faces = faces + String.join(", ", getTriangles());
        faces = faces + "]";

        String data = points + faces;
        return data;
    }

    public static int readUnsignedInt(DataInputStream dis) throws IOException {
        int ch1 = dis.read();
        int ch2 = dis.read();
        int ch3 = dis.read();
        int ch4 = dis.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new IOException("EOF reached while reading unsigned int");
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
    }

    public static float readUnsignedFloat(DataInputStream dis) throws IOException {

        byte[] bytes = new byte[4];
        dis.readFully(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        return buffer.getFloat();
    }

    public void convert() throws IOException {
        br.skipBytes(80); // Skip header
        int totalTriangles = readUnsignedInt(br); // Read # triangles

        float minx = Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float minz = Float.MAX_VALUE;
        float maxx = Float.MIN_VALUE;
        float maxy = Float.MIN_VALUE;
        float maxz = Float.MIN_VALUE;

        for (int tr = 0; tr < totalTriangles; tr++) {
            try {

                // Skip Normal Vector
                br.readFloat();
                br.readFloat();
                br.readFloat(); // SKIP NORMAL

                // Parse every 3 subsequent floats as a vertex
                float x1 = readUnsignedFloat(br);
                float y1 = readUnsignedFloat(br);
                float z1 = readUnsignedFloat(br);

                float x2 = readUnsignedFloat(br);
                float y2 = readUnsignedFloat(br);
                float z2 = readUnsignedFloat(br);

                float x3 = readUnsignedFloat(br);
                float y3 = readUnsignedFloat(br);
                float z3 = readUnsignedFloat(br);

                minx = Math.min(minx, Math.min(x1, Math.min(x2, x3)));
                maxx = Math.max(maxx, Math.max(x1, Math.max(x2, x3)));
                miny = Math.min(miny, Math.min(y1, Math.min(y2, y3)));
                maxy = Math.max(maxy, Math.max(y1, Math.max(y2, y3)));
                minz = Math.min(minz, Math.min(z1, Math.min(z2, z3)));
                maxz = Math.max(maxz, Math.max(z1, Math.max(z2, z3)));

                String v1 = "[" + x1 + "," + y1 + "," + z1 + "]";
                String v2 = "[" + x2 + "," + y2 + "," + z2 + "]";
                String v3 = "[" + x3 + "," + y3 + "," + z3 + "]";

                // Every 3 vertices create a triangle.
                String triangle = "[" + (vertexIndex++) + "," + (vertexIndex++) + "," + (vertexIndex++) + "]";

                br.readUnsignedShort(); // Read attribute byte count

                // Add 3 vertices for every triangle
                vertices.add(v1);
                vertices.add(v2);
                vertices.add(v3);
                triangles.add(triangle);
            } catch (Exception err) {
                err.printStackTrace();
                return;
            }
        }
    }
}
