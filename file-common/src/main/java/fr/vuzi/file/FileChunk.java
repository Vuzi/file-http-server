package fr.vuzi.file;

import java.util.ArrayList;
import java.util.List;

public class FileChunk {

    public String id;
    public long size;
    public String sha1;
    public List<String> storageNodes = new ArrayList<>();

}
