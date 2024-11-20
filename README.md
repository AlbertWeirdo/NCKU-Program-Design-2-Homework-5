Source: [PD2 Homework 5 on Notion](https://chuangkt.notion.site/PD2-Homework-5-3073f9725782416bafac5a96298e9680)


# PD2 Homework 5

Generated by DALL.E

🔥 **Announcement:**  
- **Deadline:** 2024/6/5 23:59pm  
📢 **Updates:**  
- **5/21 Update:** 計算 IDF 時，若分母為 0（包含詞彙 query_word 的文檔數為 0），請將 IDF 定義為 0 作計算。
- **5/28 Update:** 修正描述為「同時編譯 Indexer.java 、 BuildIndex.java 以及 TFIDFSearch.java」。
- **5/29 Update:** 補充 corpus 檔案規範、程式碼規範及測試流程。

---

# Search with TF-IDF

## Introduction

This assignment builds on the TF-IDF calculation learned in Homework 4. Now, we will apply it to create a simple search engine. This task is divided into two parts:  
1. **Serialization of Search Index**: Read a text corpus and construct a search index, saving it to disk.  
2. **Search Engine**: Process user queries and return the most relevant results based on the TF-IDF calculation.

---

## Serialization of Search Index

### Homework Requirement

Given a text corpus (e.g., `/home/share/hw5/corpus0.txt`), create a search index and save it to disk.

### Parsing Rules

To process text, follow these steps:
1. Replace all non-alphabetical characters with spaces.
2. Convert all uppercase letters to lowercase.
3. Segment the text into words based on spaces.

Example:

```java
String input = "Local MSMEs have leveraged on their technical and scientific";
input = input.replaceAll("[^a-zA-Z]+", " "); // Step 1
input = input.toLowerCase(); // Step 2
// Step 3 ....
```

### Serialization Example

Use Java's `java.io.Serializable` interface for serialization. Example code:

```java
public class Indexer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private transient int counter;
    // ....
}

// Serialization Example
Indexer idx = new Indexer();
try {
    FileOutputStream fos = new FileOutputStream("example.ser");
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(idx);
    oos.close();
    fos.close();
} catch (IOException e) {
    e.printStackTrace();
}
```

### Command Line Argument for BuildIndex

```bash
$ java BuildIndex corpusfile
```

- **corpusfile**: Absolute path to the text corpus file.

---

## Search Engine

### Homework Requirement

Given a query string \( q \), return the top \( n \) document IDs that best match the query.

### User Query Syntax

A query string \( q \) can be structured as:
- **AND** queries: `word1 AND word2`
- **OR** queries: `word1 OR word2`
- Single words: `word1`

#### Query Rules:
- `AND` represents an intersection of documents containing all terms.
- `OR` represents a union of documents containing any term.
- Queries are case-sensitive (`AND/OR` must be uppercase).

#### Example Queries:

- `the AND quick AND brown`
- `jumps OR over OR the`
- `hello`

### TF-IDF Calculation

The TF-IDF for a query \( q \) in a document \( d \) is calculated as the sum of TF-IDF scores for each query word \( t_i \) in \( d \).  
Formula:  
\[\sum_{i} 	ext{TF-IDF}(t_i, d, D)\]

---

## Input and Output

### Input
1. First line specifies \( n \): the number of results to return for each query.
2. Subsequent lines contain queries.

Example Input:
```
5
the AND quick AND brown
jumps OR over OR the
hello
```

### Output
For each query, output \( n \) document IDs. Save the results to `output.txt`.

Example Output:
```
4351 14402 11863 7061 19832
16720 12094 1259 5042 18216
10936 12299 846 8552 10713
```

---

## Challenge Points

Achieve an average runtime of less than 30 seconds for test cases \( 0 \sim 4 \) to earn the challenge point.

---

## Homework Validation

- Ensure `BuildIndex.java`, `TFIDFSearch.java`, and `Indexer.java` are in the directory `~/hw5`.  
- Validate your submission using the provided `validate_hw5.py`. Example:
  ```bash
  $ python3 validate_hw5.py --build_index
  ```

---

## References

- [TF-IDF wiki](https://zh.wikipedia.org/zh-tw/Tf-idf)

## Homework Validation Process

1. **Copy Files**: Copy `BuildIndex.java`, `Indexer.java`, and `TFIDFSearch.java` into an empty directory.
2. **Compile**: Compile all three Java files simultaneously.
3. **Build Index**: For each corpus file, execute:
   ```bash
   $ java BuildIndex corpusFile
   ```
4. **Modify Permissions**: Change the permissions of the corpus file to ensure it cannot be read by `TFIDFSearch`.
5. **Run Tests**: Test all 10 test cases using:
   ```bash
   $ java TFIDFSearch corpusName testcase
   ```

### Example Workflow

1. Compile:
   ```bash
   $ javac Indexer.java BuildIndex.java TFIDFSearch.java
   ```

2. Process Text Corpus (e.g., `example.txt`):
   ```bash
   $ java BuildIndex /home/share/hw5/example.txt
   ```

3. Query with `TFIDFSearch` (e.g., `exampleTC.txt`):
   ```bash
   $ java TFIDFSearch example exampleTC.txt
   ```

### Example Input
```
3
the AND quick
brown OR fox
hello
```

### Example Output
```
0 2 -1
0 2 1
10936 12299 846
```

The output should be saved in `output.txt`.

---

**Ensure that your program meets all input/output and file handling requirements. Validate your implementation thoroughly to avoid errors during testing.**
