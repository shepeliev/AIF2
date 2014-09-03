package com.aif.language.token;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class TokenSplitterTest {

    //This two String are pathes for gethering statistic
    private final String settingsPath = "/unitTestData/settings.xml";
    private final String rootResourcesPath = "/../../src/main/resources/";
    private Document doc;


    private final String pathToStatisticSet = "/Users/admin/Documents/programming/projects/AIF2/testData";
    private final String pathToStatisticResult = "/Users/admin/Documents/programming/projects/AIF2/AIF2/src/main/resources/testData/RU/Results.csv";

    @Before
    public void testResoursesEnitialize() {
        try {

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = (Document) factory.newDocumentBuilder().parse(new File(getClass().getResource(settingsPath).toURI()));

        } catch (ParserConfigurationException | IOException | URISyntaxException | SAXException e) {
            e.printStackTrace();
        }
    }




    @Test
    public void should_get_tokens_from_text_file_with_space_in_the_begining_using_PREDEFINED_separator() {

        try {

            final Path PathToFile = Paths.get(getClass().getResource("/unitTestData/TestData/RU/RU_text_with_space_begining.txt").toURI());

            //Expected results:
            final String lastToken = "токенов.";
            final String firstToken = "-";
            final long numberOfTokens = 8;

            final TokenSplitter tokenSplitter = new TokenSplitter(ITokenSeparatorExtractor.Type.PREDEFINED.getInstance());

            final List<String> output = tokenSplitter.split(textFromFileToString(PathToFile));

            assertNotNull(output);
            assertEquals(numberOfTokens, output.size());
            assertEquals(firstToken, output.get(0));
            assertEquals(lastToken, output.get(output.size()-1));


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void should_split_new_line_words() {

        try {

            final Path PathToFile = Paths.get(getClass().getResource("/unitTestData/TestData/RU/RU_text_with_new_lines.txt").toURI());

            final String lastToken = "токенов";
            final String firstToken = "В";
            final long numberOfTokens = 9;

            final TokenSplitter tokenSplitter = new TokenSplitter(ITokenSeparatorExtractor.Type.PREDEFINED.getInstance());

            final List<String> output = tokenSplitter.split(textFromFileToString(PathToFile));

            assertNotNull(output);
            assertEquals(firstToken, output.get(0));
            assertEquals(lastToken, output.get(output.size()-1));
            assertEquals(numberOfTokens, output.size());


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void should_get_tokens_from_file_using_probability_splitter() {

        final Path PathToFile = Paths.get("/Users/admin/Documents/programming/projects/AIF2/src/main/resources/unitTestData/testData/RU/RU_10000_СеврюгаГрач.txt");

        final TokenSplitter splitter = new TokenSplitter(ITokenSeparatorExtractor.Type.PROBABILITY.getInstance());

        final List<String> tokens = splitter.split(textFromFileToString(PathToFile));

        assertNotNull(tokens);
        assertEquals(10000, tokens.size());

    }

    @Test
    @Ignore
    //TODO this is not a real test, code below used for generating statistic data for probability splitter
    public void generate_statistic_for_probability_splitter() {

        final List<Path> files = getAllFilesInFolder(Paths.get(pathToStatisticSet));
        final TokenSplitter splitter = new TokenSplitter(ITokenSeparatorExtractor.Type.PROBABILITY.getInstance());

        List<String> splittedText;

        for(Path path: files) {

            splittedText = splitter.split(textFromFileToString(path));

            final String fileInnerName = path.toFile().getName();

            final int correctElems = getTokensNumFromFileName(fileInnerName);

            saveStatisticToFile(fileInnerName, correctElems, splittedText.size(), "RU");

        }
    }

    @Test
    @Ignore
    public void create_file_set_for_statistic_for_PROBABILITY_based_splitting() {

        final TextGenerator tg = new TextGenerator();

        tg.setFilesCount(10)
                .setLenguage("RU")
                .setStep(10000)
                .setBaseToken("RuTestFile")
                .setInitialTokensInFile(10000)
                .setLocation(Paths.get(pathToStatisticSet))
                .generate();
    }
    private List<Path> getAllFilesInFolder(Path parentPath) {

        List<Path> out = new ArrayList<>();

        String[] files = parentPath.toFile().list();

        for(String fileName: files)
            out.add(Paths.get(parentPath.toString()+"/"+fileName));


        //.DS_Store is Mac_OS hidden file, as I don't know how to ignore it I will remove it from list
        //But we need //TODO some thing with .DS_Store system file
        if(out.get(0).toFile().getName().equals(".DS_Store"))
            out.remove(0);

        return out;
    }
    private static String textFromFileToString(final Path pathToFile) {

        try(final BufferedReader reader = Files.newBufferedReader(pathToFile)) {

            final StringBuffer buff = new StringBuffer();

            reader.lines().forEach(x -> buff.append(x));

            return buff.toString();

        }catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
    private void saveStatisticToFile(String fileName, int correctTokensNum, int foundTokensNum, String LNG) {

        try( final BufferedWriter bw = new BufferedWriter(new FileWriter(getPathToResourse(LNG).toFile(), true))) {

            bw.write(LocalDateTime.now().toString() + "," + fileName + "," + correctTokensNum + "," + foundTokensNum);
            bw.newLine();

        }catch (IOException e) {
            e.printStackTrace();
        }



    }
    private int getTokensNumFromFileName(String name) {
        String[] elems = name.split("_");
        return Integer.parseInt(elems[1]);
    }
    private Path getPathToResourse(String LNG) {

        Path output = null;

        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xpath = xPathFactory.newXPath();
        try {
            final XPathExpression expression = xpath.compile("/Parameters/Paths/"+LNG+"/Path[@name='PathToStatisticResultFile']/text()");
            final String path = rootResourcesPath+expression.evaluate(doc);
            output = Paths.get(getClass().getResource("/").toString());
            output = output.resolve(path).toAbsolutePath();

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return output;
    }

}
