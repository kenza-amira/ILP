package my.project.aqmaps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteToFile {
	/**
	 * This method allows us to write a String into a document.
	 * 
	 * @param filename   This parameter is the name we wish to give the file
	 * @param jsonString This parameter is the String we wish to write into the file
	 * @return The method returns a File
	 * @throws IOException. This exception is thrown if there's is a failure while
	 *         writing the file
	 */
	public File createFile(String filename, String jsonString) throws IOException {
		var output = new File(filename);

		// If the file already exists in the directory, the method will return "File
		// already exists."
		// Otherwise, it will create the file and print "File is created!"
		if (output.createNewFile()) {
			System.out.println("File is created!");
		} else {
			System.out.println("File already exists.");
		}

		FileWriter outputWriter = new FileWriter(output);
		outputWriter.write(jsonString);
		outputWriter.close();

		return output;
	}

}
