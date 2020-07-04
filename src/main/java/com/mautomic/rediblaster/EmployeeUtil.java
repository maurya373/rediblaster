package com.mautomic.rediblaster;

import java.util.Random;

/**
 * Utility class to help generate a random {@link Employee}
 */
public class EmployeeUtil {

    private static Random randomGenerator = new Random();
    private static char[] letters = new char[26];
    private static String[] occupations = {"Full Stack Developer", "Frontend Developer", "Backend Developer", "Manager"};

    static {
        char letter = 'a';
        for (int i=0; i<letters.length; i++) {
            letters[i] = letter++;
        }
    }

    /**
     * Create a new {@link Employee} with randomly generated properties provided an id. For the sake
     * of simplicity, first name is always 5 characters, last name is always 8 characters. Email
     * is always firstName.lastName@email.com, occupation is one of {Full Stack Developer, Frontend Developer,
     * Backend Developer, Manager}, and salary is between 50,000 and 250,000.
     *
     * @param id of this particular Employee
     * @return an Employee
     */
    public static Employee generateEmployee(int id) {
        String firstName = generateName(5);
        String lastName = generateName(8);
        String email = generateEmail(firstName, lastName);
        String occupation = generateOccupation();
        int salary = generateSalary();
        return new Employee(id, firstName, lastName, email, occupation, salary);
    }

    private static String generateName(int length) {
        StringBuilder name = new StringBuilder();
        for (int i=0; i<length; i++) {
            int randomIndex = randomGenerator.nextInt(26);
            name.append(letters[randomIndex]);
        }
        return name.toString();
    }

    private static String generateEmail(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder(24);
        builder.append(firstName);
        builder.append(".");
        builder.append(lastName);
        builder.append("@email.com");
        return builder.toString();
    }

    private static String generateOccupation() {
        return occupations[randomGenerator.nextInt(4)];
    }

    private static int generateSalary() {
        return randomGenerator.nextInt(200_000) + 50_000;
    }
}
