package fr.vuzi.file;

import java.util.Arrays;

public class FileMetadata {

    /**
     * Name of the file
     */
    public String name;

    /**
     * Path of the file
     */
    public String path;

    /**
     * Size, in byte, of the file
     */
    public long size;

    /**
     * List of chunks
     */
    public FileChunk[] chunks;

    /**
     * sha1
     */
    public String sha1;

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileMetadata that = (FileMetadata) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return path != null ? path.equals(that.path) : that.path == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", chunks=" + Arrays.toString(chunks) +
                ", sha1='" + sha1 + '\'' +
                '}';
    }
}
