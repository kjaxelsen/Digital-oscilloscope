/**
 * Created by Kevin on 06/04/16.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * REads from a csv file into 2 columns
 * used for filter and reading in sample data
 */
public class SampleDataReader {
    public static ArrayList<ArrayList<Double>> parseSampleFile(String path) throws FileNotFoundException {
        ArrayList<ArrayList<Double>> samples = new ArrayList<>();
        File file = new File(path);
        Scanner input = new Scanner(file);
        String line;

        while (input.hasNextLine()) {
            line = input.nextLine();
            ArrayList<Double> sample = new ArrayList<>();
            if(line.contains(",")) {
                //parsedouble handles sci notation
                sample.add(0, Double.parseDouble(line.split(",")[1].trim()));
                sample.add(0, Double.parseDouble(line.split(",")[0].trim()));
            } else {
                sample.add(Double.parseDouble(line.trim()));
            }
            samples.add(sample);
        }
        return samples;
    }
}