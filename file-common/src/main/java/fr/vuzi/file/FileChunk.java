package fr.vuzi.file;

import java.util.ArrayList;
import java.util.List;

public class FileChunk {

    public String id;
    public long size;
    public String sha1;
    public List<String> storageNodes = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileChunk fileChunk = (FileChunk) o;

        if (size != fileChunk.size) return false;
        if (id != null ? !id.equals(fileChunk.id) : fileChunk.id != null) return false;
        return sha1 != null ? sha1.equals(fileChunk.sha1) : fileChunk.sha1 == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + (sha1 != null ? sha1.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FileChunk{" +
                "id='" + id + '\'' +
                ", size=" + size +
                ", sha1='" + sha1 + '\'' +
                ", storageNodes=" + storageNodes +
                '}';
    }
}
