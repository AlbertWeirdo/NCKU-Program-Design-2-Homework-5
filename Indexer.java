import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Indexer implements Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap<Integer, HashMap<String, Integer>> docs = new HashMap<Integer, HashMap<String, Integer>>();
    private HashMap<String, HashSet<Integer>> occurFiles = new HashMap<String, HashSet<Integer>>();

    public Indexer(HashMap<Integer, HashMap<String, Integer>> docs,
            HashMap<String, HashSet<Integer>> occurFiles) {
        this.docs = docs;
        this.occurFiles = occurFiles;
    }

    public HashMap<Integer, HashMap<String, Integer>> getDocs() {
        return docs;
    }

    public HashMap<String, HashSet<Integer>> getOccurFiles() {
        return occurFiles;
    }

}
