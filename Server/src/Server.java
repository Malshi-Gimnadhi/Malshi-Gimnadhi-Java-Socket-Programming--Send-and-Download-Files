import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    static ArrayList<MyFile> myFiles = new ArrayList<>();
    static JPanel jPanel;

    public static void main(String[] args) throws IOException {
        JFrame jFrame = new JFrame("The Server");
        jFrame.setSize(450, 600);
        jFrame.setLayout(new BoxLayout(jFrame.getContentPane(), BoxLayout.Y_AXIS));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Color lightPurple = new Color(220, 198, 255); 
        jFrame.getContentPane().setBackground(lightPurple);

        jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel jlTitle = new JLabel("The File Receiver");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        jFrame.add(jlTitle);
        jFrame.add(jScrollPane);
        jFrame.setVisible(true);

        ServerSocket serverSocket = new ServerSocket(1234);

        while (true) {
            try {
                Socket socket = serverSocket.accept();

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                int fileNameLength = dataInputStream.readInt();

                if (fileNameLength > 0) {
                    byte[] fileNameBytes = new byte[fileNameLength];
                    dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                    String fileName = new String(fileNameBytes);

                    int fileContentLength = dataInputStream.readInt();

                    if (fileContentLength > 0) {
                        byte[] fileContentBytes = new byte[fileContentLength];
                        dataInputStream.readFully(fileContentBytes, 0, fileContentLength);

                        addFileToPanel(myFiles.size(), fileName, fileContentBytes);
                    }
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }
    }

    public static void addFileToPanel(int fileId, String fileName, byte[] fileContentBytes) {
        JPanel jpFileRow = new JPanel();
        jpFileRow.setLayout(new BoxLayout(jpFileRow, BoxLayout.Y_AXIS));

        JLabel jlFileName = new JLabel(fileName);
        jlFileName.setFont(new Font("Arial", Font.BOLD, 20));
        jlFileName.setBorder(new EmptyBorder(10, 0, 10, 0));

        jpFileRow.add(jlFileName);
        jpFileRow.setName(String.valueOf(fileId));
        jpFileRow.addMouseListener(getMyMouseListener());

        jPanel.add(jpFileRow);

        // Refresh the scroll pane
        jPanel.revalidate();
        jPanel.repaint();

        myFiles.add(new MyFile(fileId, fileName, fileContentBytes, getFileExtension(fileName)));
    }

    public static MouseListener getMyMouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel jPanel = (JPanel) e.getSource();

                int fileId = Integer.parseInt(jPanel.getName());

                for (MyFile myFile : myFiles) {
                    if (myFile.getId() == fileId) {
                        int option = JOptionPane.showConfirmDialog(null, "Do you want to download this file?",
                                "Confirmation", JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.YES_OPTION) {
                            // Open file chooser dialog
                            JFileChooser fileChooser = new JFileChooser();
                            int returnValue = fileChooser.showSaveDialog(null);

                            if (returnValue == JFileChooser.APPROVE_OPTION) {
                                File selectedFile = fileChooser.getSelectedFile();
                                saveFile(selectedFile, myFile.getData());
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        };
    }

    public static void saveFile(File file, byte[] data) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(data);
            JOptionPane.showMessageDialog(null, "File downloaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "No extension found.";
        }
    }
}

class MyFile {
    private final int id;
    private final String name;
    private final byte[] data;
    private final String fileExtension;

    public MyFile(int id, String name, byte[] data, String fileExtension) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.fileExtension = fileExtension;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
