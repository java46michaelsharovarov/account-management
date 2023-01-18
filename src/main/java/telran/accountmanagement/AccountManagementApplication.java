package telran.accountmanagement;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AccountManagementApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ac = SpringApplication.run(AccountManagementApplication.class, args);
		try (Scanner scanner = new Scanner(System.in)) {
			while(true) {
				System.out.println("to stop the server, enter 'exit'");
				String input = scanner.nextLine();
				if(input.equalsIgnoreCase("exit")) {
					break;
				}
			}
		}
		ac.close();
	}

}
