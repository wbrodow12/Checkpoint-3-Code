import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class BTreeMain {

    // TODO: change when implementing on Linux CSL Machines
    public static final String pathName = "input.txt";

    public static void main(String[] args) {

        /** Read the input file -- input.txt */
        Scanner scan = null;
        try {
            scan = new Scanner(new File(pathName));
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Exiting.");
            return;
        }

        /** Read the minimum degree of B+Tree first */

        int degree = scan.nextInt();

        BTree bTree = new BTree(degree);

        /** Reading the database student.csv into B+Tree Node*/
        List<Student> studentsDB = getStudents();

        for (Student s : studentsDB) {
            bTree.insert(s);
        }

        /** Start reading the operations now from input file*/
        try {
            while (scan.hasNextLine()) {
                // why do we need a second scanner?
                Scanner s2 = new Scanner(scan.nextLine());

                while (s2.hasNext()) {

                    String operation = s2.next();

                    switch (operation) {
                        case "insert": {

                            long studentId = Long.parseLong(s2.next());
                            String studentName = s2.next() + " " + s2.next();
                            String major = s2.next();
                            String level = s2.next();
                            int age = Integer.parseInt(s2.next());
                            /** DONE: Write a logic to generate recordID if it is not provided
                             *        If it is provided, use the provided value
                            */
                            String recordIDstring = "";
                            if (s2.hasNext()) {
                                recordIDstring = s2.next();
                            }
                            long recordID;
                            if(recordIDstring.isEmpty()){
                                recordID = generateRandomID();
                            }
                            else{
                                recordID = Long.parseLong(recordIDstring);
                            }
                            Student s = new Student(studentId, age, studentName, major, level, recordID);
                            bTree.insert(s);

                            break;
                        }
                        case "delete": {
                            long studentId = Long.parseLong(s2.next());
                            boolean result = bTree.delete(studentId);
                            if (result)
                                System.out.println("Student deleted successfully.");
                            else
                                System.out.println("Student deletion failed.");
                            break;
                        }
                        case "search": {
                            long studentId = Long.parseLong(s2.next());
                            long recordID = bTree.search(studentId);
                            if (recordID != -1)
                                System.out.println("Student exists in the database at " + recordID);
                            else
                                System.out.println("Student does not exist.");
                            break;
                        }
                        case "print": {
                            List<Long> listOfRecordID = new ArrayList<>();
                            listOfRecordID = bTree.print();
                            System.out.println("List of recordIDs in B+Tree " + listOfRecordID.toString());
                            break;
                        }
                        default:
                            System.out.println("Wrong Operation");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Student> getStudents() {

        /** DONE:
         * Extract the students information from "Student.csv"
         * return the list<Student>
         */
        List<Student> studentList = new ArrayList<>();
        
        try(Scanner fileScanner = new Scanner(new File("Student.csv"))){
            while(fileScanner.hasNextLine()){

                String line = fileScanner.nextLine();
                String[] tokens = line.split(",");

                if(tokens.length == 6){

                    long studentID = Long.parseLong(tokens[0]);
                    String studentName = tokens[1];
                    String major = tokens[2];
                    String level = tokens[3];
                    int age = Integer.parseInt(tokens[4]);
                    long recordID;

                    //is this ever needed?
                    if(tokens[5].isEmpty()){
                        recordID = generateRandomID();
                    } else {
                        recordID = Long.parseLong(tokens[5]);
                        }

                    Student student = new Student(studentID, age, studentName, major, level, recordID);
                    studentList.add(student);
                }
            }
        }catch (Exception e) {
            System.out.println("Error reading Student.csv: " + e.getMessage());
            return studentList; // Return empty list if there's an error
        }
        
        try(FileWriter fw = new FileWriter("Student.csv",false)){
            fw.write("");
        }catch(Exception e){
            e.printStackTrace();
        }

        return studentList;
    }

    private static Long generateRandomID(){
        return new Random().nextLong(Integer.MAX_VALUE);
    }
}
