package com.atm.main;

import com.atm.core.ATM;

import java.util.Scanner;

/** Console entry point for SecureBank India. */
public class ATMApplication {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            ATM atm = new ATM(scanner);
            atm.start();
        } finally {
            scanner.close();
        }
    }
}
