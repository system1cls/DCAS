package org.server;

public class Parser {
    public int getCntThreads(String []args) {
        if (args.length < 1) {
            throw new RuntimeException("Zero args");
        }

        int res;
        try {
            res = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        return res;
    }

    public String getIssuer(String []args) {
        if (args.length < 2) {
            throw new RuntimeException("No issuer");
        }

        return args[1];
    }

}
