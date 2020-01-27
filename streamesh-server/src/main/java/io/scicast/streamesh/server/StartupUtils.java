package io.scicast.streamesh.server;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class StartupUtils {
    public static final String STREAMESH_SERVER_ADDRESS_PROPERTY = "streamesh.server.address";

    static String selectAddress() throws IOException {

        System.out.println("Please specify the address that Docker containers will use to contact this server.");
        System.out.printf("(You can skip this dialog by providing the option -D%s=<ip-address>)\n", STREAMESH_SERVER_ADDRESS_PROPERTY);
        System.out.println("Available options:\n");

        List<String> options = new ArrayList<String>();

        AtomicInteger i = new AtomicInteger(1);
        Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                .forEach(ni -> {
                    Collections.list(ni.getInetAddresses()).stream()
                            .forEach(address -> {
                                System.out.println(i + ".");
                                System.out.println("Interface: " + ni.getDisplayName());
                                System.out.println("Network address: " + address.getHostAddress());
                                System.out.println();
                                i.getAndIncrement();
                                options.add(address.getHostAddress());
                            });
                });
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return selectValidInput(br, options);
    }

    static String selectValidInput(BufferedReader br, List<String> options) throws IOException {
        String result = null;
        while (result == null) {
            System.out.printf("Please select your option [1-%d] or type a different ip: ", options.size());
            String option = br.readLine();
            try {
                int index = Integer.parseInt(option);
                if (index >= 1 && index <= options.size()) {
                    result = options.get(index - 1);
                }
            } catch (NumberFormatException nfe) {
                InetAddressValidator validator = InetAddressValidator.getInstance();
                if (validator.isValid(option)) {
                    String confirmation = null;
                    while (confirmation == null) {
                        System.out.printf("Are you sure that you want to use this ip %s [y/n]? ", option);
                        confirmation = br.readLine();
                        if (confirmation.equalsIgnoreCase("y")) {
                            result = option;
                        } else if (!confirmation.equalsIgnoreCase("n")) {
                            confirmation = null;
                        }
                    }
                }
            }
        }

        System.out.println("\nSelected option: " + result + "\n");
        return result;
    }
}