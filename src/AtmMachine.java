import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.yaml.snakeyaml.Yaml;

public class AtmMachine {


//    private static final java.nio.file.Files Files = ;

    private static Scanner in = new Scanner(System.in);


//    private static int balance = 0;
//    private int anotherTransaction;

    private static final String fileName = "src\\config\\config.yml";
    private static File file = new File(fileName);
    private static Yaml yaml = new Yaml();
    private static InputStream ios;
//    FileOutputStream fos = new FileOutputStream(fileName);
    static {
        try {
            ios = new FileInputStream(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static Map<String,Object> result = (Map<String,Object>)yaml.load(ios);
    private static int all_cost  = 0;
    private static int newMoneyValue = 0;
    private static int testMoneyValue = 0;
    private static int account_number = 0;
//    private  FileWriter filewriter = new FileWriter(fileName);

    public AtmMachine() throws IOException {
    }

    public static void main(String[] args) throws IOException {
            // authorized

            authProccess();

        }


        private static void authProccess(){




            try {


                // Parse the YAML file and return the output as a series of Maps and Lists


                Map accounts = (Map) result.get("accounts");
                System.out.print("Please Enter Your Account Number: ");
                account_number = in.nextInt();

                Map acc_number =  (Map) accounts.get(account_number);
                if(acc_number != null){

                    System.out.print("Enter Your Password: ");
                    String password = in.next();
//                    System.out.println((String) acc_number.get("password") +" == "+ password);
//                    System.out.println(password == (String) acc_number.get("password"));
                    if(password.equals((String) acc_number.get("password"))){
                        System.out.println("...");
                        System.out.println("Authorization was successful.");
                        Object accname =  acc_number.get("name");
                        System.out.println("Hello , "+accname+"!");
                        account(acc_number);
                    }else{
                        System.out.println("Ошибка! Не правильный пароль.");
                        authProccess();
                    }


                }else{
                    System.out.println("Возможно вы ошиблись! Попробуйте еще раз.");
                    authProccess();
                }
//                Map banknotes = (Map) result.get("banknotes");

//                System.out.println(accounts);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    private static void account(Map acc_id) throws IOException {


        System.out.println("Please Choose From the Following Options:");

        System.out.println(" 1. Display Balance");
        System.out.println(" 2. Withdraw");
        System.out.println(" 3. Log Out");
        int command = in.nextInt();
        switch(command) {
            case 1:
                balance(acc_id);
                break;
            case 2:
                withdrow(acc_id);
                break;
            case 3:
                System.out.println(acc_id.get("name") + ", Thank You For Using Our ATM. Good-Bye!");
                authProccess();

                break;

        }
    }

    private static void balance(Map acc_id) throws IOException {

        System.out.println("-----------------------------------------");
        System.out.println("Your Current Balance is ₴"+acc_id.get("balance"));
        System.out.println("-----------------------------------------");
        account(acc_id);

    }

    private static void withdrow(Map acc_id) throws IOException {

        System.out.println("Enter Amount You Wish to Withdraw: ");
        int ammount = in.nextInt();
        if(ammount > 10000){
            System.out.println("ERROR: INSUFFICIENT FUNDS!! PLEASE ENTER A DIFFERENT AMOUNT");
        } else {
            if (ammount > (int) acc_id.get("balance")) {
                System.out.println("ERROR: There is not enough money on the account.");

            } else if (ammount <= (int) acc_id.get("balance") && ammount > banknote()) {
                System.out.println("ERROR: THE MAXIMUM AMOUNT AVAILABLE IN THIS ATM IS ₴"+ banknote() +". PLEASE ENTER A DIFFERENT AMOUNT");
            } else if (ammount <= (int) acc_id.get("balance") && ammount <= banknote()) {
                WithdrowViaBanknotes(ammount , acc_id ,  account_number);
            }
        }
        account(acc_id);

    }

    private static int banknote(){
//         Map<String,Object> result = (Map<String,Object>)yaml.load(ios);
         Map banknotes = (Map) result.get("banknotes");

        banknotes.forEach((key, value)->{
                int banknote_nominale = (int) key;
                int banknote_value = (int) value;
                all_cost += (banknote_nominale * banknote_value);


         });
        return all_cost;
    }

    private static void  WithdrowViaBanknotes(int moneyValue , Map acc_id , int acc_number ) throws IOException {

        Map banknotes = (Map) result.get("banknotes");
        testMoneyValue = moneyValue;
        newMoneyValue = moneyValue;

        banknotes.forEach((key, value)->{



            if((int) value > 0) {
                if ((int) key <= moneyValue) {
                    for (int i = 0; i < (int) value; i++) {
                        if ((int) key <= testMoneyValue) {
                            testMoneyValue -= (int) key;

                        }

                    }

                }
            }

        });






        if(testMoneyValue == 0){

            List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));

            banknotes.forEach((key, value)->{
                int valueInteger = (int) value;
                if((int) value > 0) {
                    if ((int) key <= moneyValue) {

                        for (int i = 0; i < (int) value; i++) {

                            if ((int) key <= newMoneyValue) {
                                newMoneyValue -= (int) key;
                                valueInteger--;


                            }else{

                                for (int a = 0; a < fileContent.size(); a++) {
                                    if (fileContent.get(a).equals("  " + key + ": " + value)) {
                                        fileContent.set(a, "  " + key + ": " + valueInteger);

                                        break;
                                    }

                                }


                            }
                        }






                    }
                }

    });
            int newBalance = 0;
            for (int n = 0; n < fileContent.size(); n++) {
                if (fileContent.get(n).equals("  " + acc_number+":")) {
                     newBalance = (int) acc_id.get("balance") - moneyValue;
                    fileContent.set(n + 3, "    balance: "+ newBalance);
                    break;
                }

            }

            Files.write(Paths.get(fileName), fileContent, StandardCharsets.UTF_8);
            System.out.println("Your Current Balance is ₴"+newBalance);
        }else{
            System.out.println("ERROR: THE AMOUNT YOU REQUESTED CANNOT BE COMPOSED FROM BILLS AVAILABLE IN THIS ATM. PLEASE ENTER A DIFFERENT AMOUN");

        }



    }

    private static String readUsingScanner(String fileName) throws IOException {
        Scanner scanner = new Scanner(Paths.get(fileName), StandardCharsets.UTF_8.name());
        //здесь мы можем использовать разделитель, например: "\\A", "\\Z" или "\\z"
        String data = scanner.useDelimiter("\\A").next();
        scanner.close();
        return data;
    }

}



