import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildIndex {
    public static void main(String[] args) {

        long s = System.currentTimeMillis();

        // read how many lines are in the doc
        int lineCount = 0;
        int rootNumber = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            while (br.readLine() != null) {
                lineCount++;
            }
            rootNumber = lineCount / 5;
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // refine the doc
        EditDoc ed = new EditDoc(lineCount, args[0]);
        ed.removeNonEngAlphabet();
        String[] line = ed.clone();

        // build up the Trie and Hashmap
        HashMap<Integer, HashMap<String, Integer>> docs = new HashMap<Integer, HashMap<String, Integer>>();
        HashMap<String, HashSet<Integer>> occurFiles = new HashMap<String, HashSet<Integer>>();

        for (int i = 0; i < rootNumber; ++i) {
            HashMap<String, Integer> doc = new HashMap<String, Integer>();
            for (int j = 0; j < 5; j++) {
                Scanner sc = new Scanner(line[i * 5 + j]);
                sc.useDelimiter(" ");
                while (sc.hasNext()) {
                    String vocab = sc.next();
                    if (doc.containsKey(vocab)) {
                        int repeat = doc.get(vocab) + 1;
                        doc.remove(vocab);
                        doc.put(vocab, repeat);
                    } else {
                        doc.put(vocab, 1);
                        // count how many docs contain the specific words
                        // add 1 only if it is the first its appearence in the doc
                        if (occurFiles.containsKey(vocab)) {
                            if (occurFiles.get(vocab).contains(i)) {
                                continue;
                            } else {
                                occurFiles.get(vocab).add(i);
                            }
                        } else {
                            HashSet<Integer> list = new HashSet<>();
                            list.add(i);
                            occurFiles.put(vocab, list);
                        }
                    }
                }
            }
            docs.put(i, doc);
        }

        // serialize
        long serailS = System.currentTimeMillis();
        System.out.println("Build maps: " + (serailS - s) / 1000);
        Indexer idx = new Indexer(docs, occurFiles);
        Pattern p = Pattern.compile("corpus(\\d+)\\.txt");
        Matcher m = p.matcher(args[0]);
        String outputFile = null;
        if (m.find()) {
            outputFile = "corpus" + m.group(1) + ".ser";
            System.out.println(outputFile);
        }

        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(idx);
            oos.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        long e = System.currentTimeMillis();
        System.out.println("Serailization: " + (e - serailS) / 1000);
        System.out.println("Duration: " + (e - s) / 1000);

    }
}

class EditDoc implements Cloneable {
    private String[] line;

    public EditDoc(int lineCount, String sourceFile) {
        line = new String[lineCount];
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            int i = 0;
            String currenrLine;
            while ((currenrLine = br.readLine()) != null) {
                line[i] = currenrLine;
                ++i;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeNonEngAlphabet() {
        for (int i = 0; i < line.length; ++i) {
            line[i] = line[i].replaceAll("[^a-zA-Z]", " ").toLowerCase().trim().replaceAll("\\s+", " ");
        }
    }

    public String[] clone() {
        String[] line = this.line.clone();
        return line;
    }
}

class TrieNode implements Serializable {
    private static final long serialVersionUID = 1L;
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
}

class Trie implements Serializable {
    private static final long serialVersionUID = 1L;
    TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        node.isEndOfWord = true;
    }
}