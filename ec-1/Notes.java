// VUnetID: ahmedn1
// Email: nafis.ahmed@vanderbilt.edu
// Class: EECE 4371 - Vanderbilt University
// Assignment: Extra Credit - 1
// Honor statement: I have not given or received unauthorized aid on this assignment.
// Date: 09/02/2020
// Description: This program implements a basic notes manager.

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Notes {
    public static void main(String[] args) {
        System.out.println("\nWELCOME TO YOUR NOTES MANAGER!");

        Scanner scnr = new Scanner(System.in);
        // map initialization - HashMap used as order of notes is not an issue
        Map<String, String> noteMap = new HashMap<String, String>();
        char option = ' ';

        do {
            displayMenu();
            option = getInput(scnr);
            performMenuChoice(option, scnr, noteMap);
        } while (option != 'Q');
    }

    // displays Main Menu
    public static void displayMenu() {
        System.out.println("\nMAIN MENU:\n" +
                "Enter 'A' to Add a Note\n" +
                "Enter 'S' to Search for a Previous Note\n" +
                "Enter 'Q' to Quit Program\n");
    }

    // gets user input and validates it
    public static char getInput(Scanner scnr) {
        // prompt for user input
        System.out.println("Choose an option from Main Menu:");
        char input = scnr.nextLine().toUpperCase().charAt(0);

        // input data validation
        while (input != 'A' && input != 'S' && input != 'Q') {
            System.out.println("Invalid input. Please choose an option from Main Menu:");
            input = scnr.nextLine().toUpperCase().charAt(0);
        }

        return input;
    }

    // calls the proper function needed to perform the action chosen by the user
    public static void performMenuChoice(char option, Scanner scnr, Map noteMap) {
        if (option == 'A') {
            storeNote(scnr, noteMap);
        } else if (option == 'S') {
            displayOldNote(scnr, noteMap);
        }
    }

    // stores/adds a note
    public static void storeNote(Scanner scnr, Map noteMap) {
        String name = "";
        System.out.println("\nWhat would you like to name this note?");
        name = scnr.nextLine().trim();

        String note = "";
        System.out.println("Type in your note:");
        note = scnr.nextLine().trim();

        noteMap.put(name, note);
        System.out.println("Your note has been added!");
    }

    // retrieves/displays a previously stored note
    public static void displayOldNote(Scanner scnr, Map noteMap) {
        String oldNote = "";
        System.out.println("\nWhat's the name of the note you're looking for?");
        oldNote = scnr.nextLine().trim();

        if (noteMap.get(oldNote) != null) {
            System.out.println("Your note has been found! Here it is:\n" + noteMap.get(oldNote));
        } else {
            System.out.println("Note not found. Please make sure you enter the name with correct " +
            "cases and indentation.");
        }
    }
}
