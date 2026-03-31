import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ArgentApp extends Application {

    static Map<String, User> users = new HashMap<>();
    static User currentUser;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private static final String DATA_FILE = "users.dat";

    public static void main(String[] args) {
             launch(args);
    }

    @Override
    public void start(Stage stage) {
        loadUsers();  

        try {
            Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found");
        }
        showWelcome(stage);
    }

    // WELCOME
    private void showWelcome(Stage stage) {
        Label logo = logoLabel(stage);
        Label title = title("Welcome");

        Button signIn = new Button("Sign In");
        Button signUp = new Button("Sign Up");

        signIn.setOnAction(e -> showLogin(stage));
        signUp.setOnAction(e -> showRegister(stage));

        VBox root = new VBox(15, logo, title, signIn, signUp);
        root.setAlignment(Pos.CENTER);
        root.setStyle(bg());

        stage.setScene(new Scene(root, 350, 300));
        stage.setTitle("Argent");
        stage.show();
    }

    // LOGIN
    private void showLogin(Stage stage) {
        Label logo = logoLabel(stage);
        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Label msg = new Label();

        Button login = new Button("Login");
        Button back = new Button("Back");

        login.setOnAction(e -> {
            User u = users.get(username.getText().trim());
            if (u != null && u.password.equals(password.getText().trim())) {
                currentUser = u;
                showDashboard(stage);
            } else msg.setText("Invalid credentials");
        });

        back.setOnAction(e -> showWelcome(stage));

        VBox root = new VBox(12,
                logo,
                title("Login"),
                username, password,
                login, back, msg
        );
        root.setAlignment(Pos.CENTER);
        root.setStyle(bg());

        stage.setScene(new Scene(root, 350, 320));
    }

    // REGISTER
    private void showRegister(Stage stage) {
        Label logo = logoLabel(stage);

        TextField firstName = new TextField();
        firstName.setPromptText("First Name (required)");

        TextField lastName = new TextField();
        lastName.setPromptText("Last Name (required)");

        TextField username = new TextField();
        username.setPromptText("Username (required)");

        PasswordField password = new PasswordField();
        password.setPromptText("Password (required)");

        TextField email = new TextField();
        email.setPromptText("Email (optional)");

        TextField phone = new TextField();
        phone.setPromptText("Phone Number (optional)");

        TextField address = new TextField();
        address.setPromptText("Address (optional)");

        Label msg = new Label();

        Button create = new Button("Create");
        Button back = new Button("Back");

        create.setOnAction(e -> {
            String fName = firstName.getText().trim();
            String lName = lastName.getText().trim();
            String uName = username.getText().trim();
            String pass = password.getText().trim();

            if (fName.isEmpty() || lName.isEmpty() || uName.isEmpty() || pass.isEmpty()) {
                msg.setText("Required fields missing");
                return;
            }

            if (users.containsKey(uName)) {
                msg.setText("Username exists");
                return;
            }

            User newUser = new User(
                    fName, lName, uName, pass,
                    email.getText().trim(),
                    phone.getText().trim(),
                    address.getText().trim()
            );

            users.put(uName, newUser);
            saveUsers(); 
            msg.setText("Account created. Account #: " + newUser.accountNumber);
        });

        back.setOnAction(e -> showWelcome(stage));

        VBox root = new VBox(12,
                logo,
                title("Register"),
                firstName, lastName, username, password,
                email, phone, address,
                create, back, msg
        );
        root.setAlignment(Pos.CENTER);
        root.setStyle(bg());

        stage.setScene(new Scene(root, 350, 450));
    }

    // DASHBOARD
    private void showDashboard(Stage stage) {

        Label logo = logoLabel(stage);
        Label balanceLabel = new Label();
        updateBalance(balanceLabel);

        TextField amount = new TextField();
        amount.setPromptText("Amount");

        Label msg = new Label();

        Button deposit = new Button("Deposit");
        Button withdraw = new Button("Withdraw");

        deposit.setOnAction(e -> handleTransaction(amount, balanceLabel, msg, "DEPOSIT"));
        withdraw.setOnAction(e -> handleTransaction(amount, balanceLabel, msg, "WITHDRAW"));

        ListView<String> history = new ListView<>();
        refreshHistory(history);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> refreshHistory(history));

        TextField recipient = new TextField();
        recipient.setPromptText("Recipient Account #");
        TextField sendAmount = new TextField();
        sendAmount.setPromptText("Amount");
        Label sendMsg = new Label();
        Button send = new Button("Send");
        send.setOnAction(e -> handleTransfer(recipient, sendAmount, balanceLabel, sendMsg, history));

        VBox dashboard = new VBox(10,
                logo,
                title("Welcome " + currentUser.fullName() + " | Acc#: " + currentUser.accountNumber),
                card("Balance", balanceLabel),
                amount, deposit, withdraw, msg
        );
        dashboard.setPadding(new Insets(10));

        VBox transact = new VBox(10,
                logo,
                title("Transactions"),
                history, refresh
        );
        transact.setPadding(new Insets(10));

        VBox sendBox = new VBox(10,
                logo,
                title("Send Money"),
                recipient, sendAmount, send, sendMsg
        );
        sendBox.setPadding(new Insets(10));

        VBox nav = new VBox(10);
        Button dashBtn = new Button("Dashboard");
        Button transBtn = new Button("Transactions");
        Button sendBtn = new Button("Send");
        Button logout = new Button("Logout");

        nav.getChildren().addAll(dashBtn, transBtn, sendBtn, logout);
        nav.setStyle("-fx-background-color:#1e1e1e; -fx-padding:15;");
        nav.setPrefWidth(120);

        StackPane mainContent = new StackPane(dashboard, transact, sendBox);
        transact.setVisible(false);
        sendBox.setVisible(false);

        dashBtn.setOnAction(e -> {
            dashboard.setVisible(true);
            transact.setVisible(false);
            sendBox.setVisible(false);
        });
        transBtn.setOnAction(e -> {
            dashboard.setVisible(false);
            transact.setVisible(true);
            sendBox.setVisible(false);
        });
        sendBtn.setOnAction(e -> {
            dashboard.setVisible(false);
            transact.setVisible(false);
            sendBox.setVisible(true);
        });
        logout.setOnAction(e -> showWelcome(stage));

        HBox root = new HBox(nav, mainContent);
        root.setStyle(bg());

        stage.setScene(new Scene(root, 700, 500));
    }

    private void handleTransaction(TextField field, Label balance, Label msg, String type) {
        try {
            double amt = Double.parseDouble(field.getText().trim());
            if (amt <= 0) throw new Exception();

            if (type.equals("WITHDRAW") && amt > currentUser.balance) {
                msg.setText("Insufficient funds");
                return;
            }

            if (type.equals("DEPOSIT")) currentUser.balance += amt;
            else currentUser.balance -= amt;

            currentUser.transactions.add(new Transaction(type, amt));
            updateBalance(balance);
            msg.setText(type + " successful");
            saveUsers(); // persist after transaction

        } catch (Exception e) {
            msg.setText("Invalid amount");
        }
    }

    private void handleTransfer(TextField rec, TextField amtField,
                                Label balance, Label msg, ListView<String> history) {
        try {
            String accNum = rec.getText().trim();
            double amt = Double.parseDouble(amtField.getText().trim());

            if (amt <= 0) throw new Exception();
            if (accNum.equals(currentUser.accountNumber)) {
                msg.setText("Cannot send to self");
                return;
            }

            User recipientUser = null;
            for (User u : users.values()) {
                if (u.accountNumber.equals(accNum)) {
                    recipientUser = u;
                    break;
                }
            }
            if (recipientUser == null) {
                msg.setText("User not found");
                return;
            }

            if (amt > currentUser.balance) {
                msg.setText("Insufficient funds");
                return;
            }

            currentUser.balance -= amt;
            recipientUser.balance += amt;

            currentUser.transactions.add(new Transaction("SENT", amt, recipientUser.accountNumber));
            recipientUser.transactions.add(new Transaction("RECEIVED", amt, currentUser.accountNumber));
            saveUsers();

            updateBalance(balance);
            msg.setText("Transfer successful");
            refreshHistory(history);

        } catch (Exception e) {
            msg.setText("Invalid input");
        }
    }

    private void updateBalance(Label label) {
        label.setText("$" + df.format(currentUser.balance));
        label.setStyle("-fx-text-fill:white; -fx-font-size:22px;");
    }

    private void refreshHistory(ListView<String> history) {
        history.getItems().clear();
        currentUser.transactions.forEach(t -> history.getItems().add(t.toString()));
    }

    private Label title(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:white; -fx-font-size:20px;");
        return l;
    }

    private Label logoLabel(Stage stage) {
        Label l = new Label("Argent – Your Financial Compass");
        l.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        l.setAlignment(Pos.CENTER);

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            double fontSize = newVal.doubleValue() / 20;
            l.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size:" + fontSize + "px;");
        });

        return l;
    }

    private VBox card(String title, Label content) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill:#ccc;");
        VBox box = new VBox(5, t, content);
        box.setStyle("-fx-background-color:#4facfe; -fx-padding:15; -fx-background-radius:10;");
        return box;
    }

    private String bg() {
        return "-fx-background-image: url('/icon2.png');" +
                "-fx-background-size: 100% 100%;" +
                "-fx-background-position: center;" +
                "-fx-background-repeat: no-repeat;" +
                "-fx-padding: 15;";
    }

    //SERIALIZATION 
    private static void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(users);
        } catch (Exception e) {
            System.out.println("Failed to save users: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadUsers() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            users = (Map<String, User>) in.readObject();
        } catch (Exception e) {
            System.out.println("Failed to load users: " + e.getMessage());
        }
    }

    //USER & TRANSACTION
    static class User implements Serializable {
        private static final long serialVersionUID = 1L;

        String firstName, lastName;
        String username, password;
        String email, phone, address;
        double balance = 0;
        String accountNumber;
        List<Transaction> transactions = new ArrayList<>();

        User(String first, String last, String u, String p,
             String email, String phone, String address) {
            firstName = first;
            lastName = last;
            username = u;
            password = p;
            this.email = email;
            this.phone = phone;
            this.address = address;
            accountNumber = generateAccountNumber();
        }

        private String generateAccountNumber() {
            Random r = new Random();
            return String.format("%010d", Math.abs(r.nextLong()) % 1_000_000_0000L);
        }

        String fullName() {
            return firstName + " " + lastName;
        }
    }

    static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;

        String type;
        double amount;
        String target;
        String time;

        Transaction(String type, double amt) {
            this(type, amt, "");
        }

        Transaction(String type, double amt, String target) {
            this.type = type;
            this.amount = amt;
            this.target = target;
            this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        }

        @Override
        public String toString() {
            return time + " | " + type + " $" + String.format("%.2f", amount) +
                    (target.isEmpty() ? "" : " -> Account: " + target);
        }
    }
}
