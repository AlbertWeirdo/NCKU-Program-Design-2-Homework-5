import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Iterator;
import java.util.Map;

public class TFIDFSearch {
    public static void main(String[] args) {
        long s = System.currentTimeMillis();

        // deserializes
        String deserializedFile = args[0] + ".ser";
        Indexer idx;
        HashMap<Integer, HashMap<String, Integer>> docs = new HashMap<Integer, HashMap<String, Integer>>();
        HashMap<String, HashSet<Integer>> occurFiles = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream(deserializedFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            idx = (Indexer) ois.readObject();
            docs = idx.getDocs();
            occurFiles = idx.getOccurFiles();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cfe) {
            cfe.printStackTrace();
        }

        // read lines
        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            while (br.readLine() != null) {
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // store content
        String[][] lines = new String[lineCount - 1][];
        int tfidfSize = 0;
        // output array
        int[][] output = new int[lineCount - 1][tfidfSize];

        try (BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            tfidfSize = Integer.parseInt(br.readLine());
            String currentLine;
            int i = 0;
            TFIDFCalculator tfc = new TFIDFCalculator(docs, occurFiles, tfidfSize);
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.contains("AND")) {
                    lines[i] = currentLine.replaceAll(" ", "").split("AND");
                    int[] outputInt = tfc.andQuery(lines[i]);
                    output[i] = Arrays.copyOf(outputInt, tfidfSize);
                    if (outputInt.length < tfidfSize) {
                        for (int k = outputInt.length; k < tfidfSize; k++) {
                            output[i][k] = -1;
                        }
                    }

                } else if (currentLine.contains("OR")) {
                    lines[i] = currentLine.replaceAll(" ", "").split("OR");
                    int[] outputInt = tfc.orQuery(lines[i]);
                    output[i] = Arrays.copyOf(outputInt, tfidfSize);
                    if (outputInt.length < tfidfSize) {
                        for (int k = outputInt.length; k < tfidfSize; k++) {
                            output[i][k] = -1;
                        }
                    }

                } else {
                    lines[i] = currentLine.split(" ");
                    int[] outputInt = tfc.singleQuery(lines[i]);
                    output[i] = Arrays.copyOf(outputInt, tfidfSize);
                    if (outputInt.length < tfidfSize) {
                        for (int k = outputInt.length; k < tfidfSize; k++) {
                            output[i][k] = -1;
                        }
                    }

                }
                ++i;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // output
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < output.length; i++) {
                for (int j = 0; j < tfidfSize; j++) {
                    sb.append(output[i][j]);
                    sb.append(" ");
                }
                sb.append(System.lineSeparator());
            }
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long e = System.currentTimeMillis();
        System.out.println("Duration: " + (e - s) / 1000);

    }
}

class TFIDFCalculator {
    private HashMap<Integer, HashMap<String, Integer>> docs;
    private HashMap<String, HashSet<Integer>> occurFiles;
    private int docsNum = 0;
    private int tfidfSize = 0;

    public TFIDFCalculator(HashMap<Integer, HashMap<String, Integer>> docs,
            HashMap<String, HashSet<Integer>> occurFiles, int tfidfSize) {
        this.docs = docs;
        this.occurFiles = occurFiles;
        this.docsNum = docs.keySet().size();
        this.tfidfSize = tfidfSize;
    }

    public int[] andQuery(String[] andLine) {
        // count how many times the file occur in the Hashsets
        HashMap<Integer, Integer> occurTimes = new HashMap<>();
        // the files' intersection
        HashSet<Integer> intersectionFile = new HashSet<>();

        for (String s : andLine) {
            if (occurFiles.containsKey(s)) {
                HashSet<Integer> set = occurFiles.get(s);
                Iterator<Integer> it = set.iterator();
                while (it.hasNext()) {
                    Integer i = it.next();
                    if (!occurTimes.containsKey(i)) {
                        occurTimes.put(i, 1);
                    } else {
                        Integer temp = occurTimes.get(i);
                        temp += 1;
                        occurTimes.put(i, temp);
                    }
                }
            } else {
                int[] temp = new int[tfidfSize];
                for (int i = 0; i < tfidfSize; i++) {
                    temp[i] = -1;
                }
                return temp;
            }
        }
        for (Integer key : occurTimes.keySet()) {
            if (occurTimes.get(key).equals(andLine.length)) {
                intersectionFile.add(key);
            }
        }

        // Iterator<Integer> i = intersectionFile.iterator();
        // try (BufferedWriter bw = new BufferedWriter(new
        // FileWriter("intersection.txt"), 1)) {
        // bw.write("start" + '\n');
        // while (i.hasNext()) {
        // Integer k = i.next();
        // bw.write(k + " ");
        // }
        // bw.newLine();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        return tfidf(intersectionFile, andLine);
    }

    public int[] orQuery(String[] orLine) {
        HashMap<Integer, Integer> occurTimes = new HashMap<>();
        HashSet<Integer> unionFile = new HashSet<>();

        for (String s : orLine) {
            if (occurFiles.containsKey(s)) {
                Iterator<Integer> it = occurFiles.get(s).iterator();
                while (it.hasNext()) {
                    Integer i = it.next();
                    // elements in the hashset can't duplicate
                    if (unionFile.add(i)) {
                        unionFile.add(i);
                    }
                }
            } else {
                continue;
            }

        }
        return tfidf(unionFile, orLine);
    }

    public int[] singleQuery(String[] singleWord) {
        if (occurFiles.containsKey(singleWord[0])) {
            HashSet<Integer> single = occurFiles.get(singleWord[0]);
            return tfidf(single, singleWord);
        } else {
            int[] temp = new int[tfidfSize];
            for (int i = 0; i < tfidfSize; i++) {
                temp[i] = -1;
            }
            return temp;
        }

    }

    public double tf(HashMap<String, Integer> doc, String term) {
        int ftd = doc.getOrDefault(term, 0);
        if (ftd == 0) {
            return 0.0;
        } else {
            int wordCount = 0;
            for (Integer t : doc.values()) {
                wordCount += t;
            }
            return ((double) ftd / (double) wordCount);
        }
    }

    public double idf(String term) {
        if (occurFiles.containsKey(term)) {
            int number_doc_contain_term = occurFiles.get(term).size();
            return Math.log((double) docsNum / (double) number_doc_contain_term);
        } else {
            return 0.0;
        }

    }

    public int[] tfidf(HashSet<Integer> files, String[] wordList) {
        double[] output = new double[files.size()];
        int[] outputInt = new int[files.size()];
        HashMap<Double, HashSet<Integer>> tfidfs = new HashMap<>();

        int j = 0;
        for (int i : files) {
            HashMap<String, Integer> doc = this.docs.get(i);
            for (String s : wordList) {
                output[j] += tf(doc, s) * idf(s);
            }
            // store the results wrt the targetDoc
            if (!tfidfs.containsKey(output[j])) {
                HashSet<Integer> temp = new HashSet<>();
                temp.add(i);
                tfidfs.put(output[j], temp);
            } else {
                HashSet<Integer> temp = tfidfs.get(output[j]);
                temp.add(i);
                tfidfs.put(output[j], temp);
            }
            ++j;
        }

        Arrays.sort(output);
        int k = 0;
        for (int i = 0; i < output.length; i++) {
            ArrayList<Integer> al = new ArrayList<>(tfidfs.get(output[output.length - i - 1]));
            Collections.sort(al);
            int alSize = al.size();
            for (Integer sameValuesFiles : al) {
                outputInt[k] = sameValuesFiles;
                k++;
            }
            i += (alSize - 1);
        }
        return outputInt;
    }
}