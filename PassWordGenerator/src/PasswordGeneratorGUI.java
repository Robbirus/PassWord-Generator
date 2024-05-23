import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JScrollPane;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Render the GUI Components (Frontend)
// This class will inherit from the JFrame class
public class PasswordGeneratorGUI extends JFrame{

    private PasswordGenerator passwordGenerator;

    public PasswordGeneratorGUI(){

        // Render frame and add a title
        super("Password Generator");

        // Set the size of the GUI
        setSize(540, 570);

        // Prevent GUI from being able to resized
        setResizable(false);

        // We will set the layout to be null to have control over the position and size of our components in our app
        setLayout(null);

        // Terminate the program when the GUI is closed (ends the process)
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Center the GUI to the screen
        setLocationRelativeTo(null);

        // Init password generator
        passwordGenerator = new PasswordGenerator();

        // Render GUI components
        addGuiComponents();

    }

    public void addGuiComponents(){
        // Create Title text
        JLabel titleLabel = new JLabel("Password Generator");

        // Increase the font size and make it bold
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 32));

        // Center the text to the screen
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Set x, y coordinates and width/height values
        titleLabel.setBounds(0, 10, 540, 39);

        // Add to GUI
        add(titleLabel);

        // Create result text area
        JTextArea passwordOutput = new JTextArea();

        // Prevent editing the text area
        passwordOutput.setEditable(false);
        passwordOutput.setFont(new Font("Dialog", Font.BOLD, 32));

        // Add scroll-ability in case output becomes too big
        JScrollPane passwordOutputPane = new JScrollPane(passwordOutput);
        passwordOutputPane.setBounds(25, 97, 479, 70);

        // Create a black border around the text area
        passwordOutputPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(passwordOutputPane);

        // Create password length label
        JLabel passwordLengthLabel = new JLabel("Password length: ");
        passwordLengthLabel.setFont(new Font("Dialog", Font.PLAIN, 32));
        passwordLengthLabel.setBounds(25, 215, 272, 39);
        add(passwordLengthLabel);

        // Create password length input
        JTextArea passwordLengthInputArea = new JTextArea();
        passwordLengthInputArea.setFont(new Font("Dialog", Font.PLAIN, 32));
        passwordLengthInputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordLengthInputArea.setBounds(310, 215, 192, 39);
        add(passwordLengthInputArea);

        // Create toggle buttons
        // Uppercase letter toggle
        JToggleButton uppercaseToggle = new JToggleButton("Uppercase");
        uppercaseToggle.setFont(new Font("Dialog", Font.PLAIN, 26));
        uppercaseToggle.setBounds(25, 302, 225, 56);
        add(uppercaseToggle);

        // Lowercase letter toggle
        JToggleButton lowercaseToggle = new JToggleButton("Lowercase");
        lowercaseToggle.setFont(new Font("Dialog", Font.PLAIN, 26));
        lowercaseToggle.setBounds(282, 302, 225, 56);
        add(lowercaseToggle);

        // numbers toggle
        JToggleButton numbersToggle = new JToggleButton("Numbers");
        numbersToggle.setFont(new Font("Dialog", Font.PLAIN, 26));
        numbersToggle.setBounds(25, 373, 225, 56);
        add(numbersToggle);


        // Symbols toggle
        JToggleButton symbolsToggle = new JToggleButton("Symbols");
        symbolsToggle.setFont(new Font("Dialog", Font.PLAIN, 26));
        symbolsToggle.setBounds(282, 373, 225, 56);
        add(symbolsToggle);

        // Create a generate button
        JButton generateButton = new JButton("Generate");
        generateButton.setFont(new Font("Dialog", Font.PLAIN, 32));
        generateButton.setBounds(155, 477, 222, 41);
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // validation: generate a password only when length > 0 and one of the toggled buttons is pressed
                if(passwordLengthInputArea.getText().length() <= 0){
                    return;
                }

                boolean anyToggleSelected = lowercaseToggle.isSelected() ||
                        uppercaseToggle.isSelected() ||
                        numbersToggle.isSelected() ||
                        symbolsToggle.isSelected();

                // Generate password
                int passwordLength = Integer.parseInt(passwordLengthInputArea.getText());
                if(anyToggleSelected){
                    String generatedPassword = passwordGenerator.generatePassword(passwordLength,
                            uppercaseToggle.isSelected(),
                            lowercaseToggle.isSelected(),
                            numbersToggle.isSelected(),
                            symbolsToggle.isSelected());

                    // Display password back to the user
                    passwordOutput.setText(generatedPassword);
                }
            }
        });
        add(generateButton);

    }
}
