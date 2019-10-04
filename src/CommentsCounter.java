import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommentsCounter {

    public static String commentDelim(String lang) {
        switch (lang) {
            case "java":
                return "//";
            case "py":
                return "#";
            default:
                return "//";
        }
    }

    public static String[] blockDelim(String lang) {
        switch (lang) {
            case "java":
                return new String[]{"/*", "*", "*/"};
            default:
                return null;
        }
    }

    // detect if a line of code has inline comment
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

        String fname = inFile.getName();
        System.out.println(fname);
        if (fname.startsWith(".") || fname.lastIndexOf('.') == -1) {
            System.out.println("Invalid file");
        }

        String extension = fname.substring(fname.lastIndexOf('.') + 1);

        try (BufferedReader br = new BufferedReader(new FileReader(inFile))) {
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                lines.add(line.trim());
            }
            br.close();

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
