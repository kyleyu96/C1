import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommentsCounter {

    // input: the programming language used in the file
    // output: the character sequence marking the start of a single line comment
    public static String commentDelim(String lang) {
        switch (lang) {
            case "py":
            case "py3":
                return "#";
            default:
                return "//";
        }
    }

    // input: the programming language used in the file
    // output: delimiters for block comment
    public static String[] blockDelim(String lang) {
        switch (lang) {
            case "c":
            case "cp":
            case "cpp":
            case "java":
            case "js":
                return new String[]{"/*", "*", "*/"};
            default:
                return null;
        }
    }

    // if line contains an inline comment, returns its starting index
    // else returns -1
    public static int inlineComment(String line, String delim) {
        int singleQuotation = 0;
        int doubleQuotation = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '\'') singleQuotation++;
            else if (line.charAt(i) == '"') doubleQuotation++;
            else if (singleQuotation % 2 == 0 && doubleQuotation % 2 == 0 && line.substring(i).startsWith(delim)) {
                return i;
            }
        }
        return -1;
    }

    // method called when the programming language used does not have different delimiters for
    // single line comment and block comment (ex: python)
    public static void analyzeSingleDelimVariant(List<String> lines, String lang) {
        int commentLines = 0;
        int singleLineComments = 0;
        int blockCommentLines = 0;
        int blockComments = 0;
        int todos = 0;

        String delim = commentDelim(lang);

        int i = 0;
        String line;
        int curBlockLen = 0;
        while (i < lines.size()) {
            line = lines.get(i);
            if (line.startsWith(delim)) {
                commentLines++;
                curBlockLen++;
                if (line.contains("TODO")) todos++;
            }
            else {
                if (curBlockLen > 0) {
                    if (curBlockLen == 1) {
                        singleLineComments++;
                    }
                    else {
                        blockComments++;
                        blockCommentLines += curBlockLen;
                    }
                    curBlockLen = 0;
                }

                int inline = inlineComment(line, delim);
                if (inline > -1) {
                    commentLines++;
                    singleLineComments++;
                    line = line.substring(inline);
                    if (line.contains("TODO")) todos++;
                }
            }
            i++;
        }
        if (curBlockLen > 0) {
            if (curBlockLen == 1) {
                singleLineComments++;
            }
            else {
                blockComments++;
                blockCommentLines += curBlockLen;
            }
        }

        System.out.println("Total # of lines: " + lines.size());
        System.out.println("Total # of comment lines: " + commentLines);
        System.out.println("Total # of single line comments: " + singleLineComments);
        System.out.println("Total # of comment lines within block comments: " + blockCommentLines);
        System.out.println("Total # of block line comments: " + blockComments);
        System.out.println("Total # of TODO’s: " + todos);
    }

    // method called when the programming language used has different delimiters for
    // single line comment and block comment (ex: java, c++)
    public static void analyze(List<String> lines, String lang) {
        int commentLines = 0;
        int singleLineComments = 0;
        int blockCommentLines = 0;
        int blockComments = 0;
        int todos = 0;

        String[] blockCommentDelim = blockDelim(lang);
        String blockCommentStart = blockCommentDelim[0];
        String blockCommentEnd = blockCommentDelim[2];
        String singleLineCommentDelim = commentDelim(lang);

        int i = 0;
        String line;
        boolean inBlock = false;
        while (i < lines.size()) {
            line = lines.get(i);
            if (inBlock) {
                if (line.startsWith(blockCommentDelim[1])) {
                    commentLines++;
                    blockCommentLines++;
                    if (line.contains("TODO")) todos++;
                    i++;
                    continue;
                }
                else {
                    inBlock = false;
                }
            }
            if (line.startsWith(singleLineCommentDelim) || inlineComment(line, singleLineCommentDelim) > -1) {
                commentLines++;
                singleLineComments++;
                if (!line.startsWith(singleLineCommentDelim)) {
                    line = line.substring(inlineComment(line, singleLineCommentDelim));
                }
                if (line.contains("TODO")) todos++;
            }
            else if (line.startsWith(blockCommentStart) || inlineComment(line, blockCommentStart) > -1) {
                blockComments++;
                commentLines++;
                blockCommentLines++;

                if (!line.startsWith(blockCommentStart)) {
                    line = line.substring(inlineComment(line, blockCommentStart));
                }
                if (line.contains("TODO")) todos++;

                if (!line.endsWith(blockCommentEnd)) {
                    inBlock = true;
                }
            }
            i++;
        }

        System.out.println("Total # of lines: " + lines.size());
        System.out.println("Total # of comment lines: " + commentLines);
        System.out.println("Total # of single line comments: " + singleLineComments);
        System.out.println("Total # of comment lines within block comments: " + blockCommentLines);
        System.out.println("Total # of block line comments: " + blockComments);
        System.out.println("Total # of TODO’s: " + todos);
    }

    public static void main(String[] args) {
        // get the file path from the user
        File inFile = null;
        if (args.length > 0) {
            inFile = new File(args[0]);
        }
        else {
            System.out.println("Enter the name of the file");
            Scanner scan = new Scanner(System.in);
            String s = scan.next();
            inFile = new File(s.trim());
            scan.close();
        }

        // verify that the filename is valid
        String fname = inFile.getName();
        if (fname.startsWith(".") || fname.lastIndexOf('.') == -1) {
            System.out.println("Invalid file");
        }

        // the file extension indicates the programming language used
        String extension = fname.substring(fname.lastIndexOf('.') + 1);

        try (BufferedReader br = new BufferedReader(new FileReader(inFile))) {
            // read the file line by line
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
            br.close();

            // method used depends on whether the programming language used has different delimiters for
            // single line comment and block comment
            if (blockDelim(extension) == null) {
                analyzeSingleDelimVariant(lines, extension);
            } else {
                analyze(lines, extension);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        catch (IOException e) {
            System.out.println("Error");
        }
    }
}
