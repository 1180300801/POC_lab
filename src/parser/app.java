package parser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class app {
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextField textField1;
    private JButton Button_chose;
    private JButton Button_run;
    private JPanel MyParser;

    public app() {
        Button_chose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //初始化文件选择框
                JFileChooser fDialog = new JFileChooser();
                //设置文件选择框的标题
                fDialog.setDialogTitle("请选择测试文件");
                fDialog.setCurrentDirectory(new File("src//test"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "TXT & C Code", "txt", "c");
                fDialog.setFileFilter(filter);
                //弹出选择框
                int returnVal = fDialog.showOpenDialog(null);
                // 如果是选择了文件
                if(JFileChooser.APPROVE_OPTION == returnVal){
                    //打印出文件的路径，你可以修改位把路径值写到 textField 中
                    textField1.setText(fDialog.getSelectedFile().getPath());
                }
            }
        });
        Button_run.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    parser p = new parser(textField1.getText());
                    p.solution();
                    p.output();
                    textArea1.setText(p.getOut().toString());
                    textArea2.setText(p.getErrors().toString());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("app");
        frame.setContentPane(new app().MyParser);
        frame.setLocation(300,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
