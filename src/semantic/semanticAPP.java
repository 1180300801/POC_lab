package semantic;

import parser.InterCode;
import parser.parser;
import symbols.SymbolTable;
import symbols.CommonSymbol;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class semanticAPP {
    private JTextArea textArea1;
    private JTextArea textArea2;
    private JTextField textField1;
    private JButton Button_chose;
    private JButton Button_run;
    private JPanel MySemanticAnalyzer;
    private JTextArea textArea3;

    public semanticAPP() {
        Button_chose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //初始化文件选择框
                JFileChooser fDialog = new JFileChooser();
                //设置文件选择框的标题
                fDialog.setDialogTitle("请选择测试文件");
                fDialog.setCurrentDirectory(new File("src//test1"));
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
                    List<InterCode> interCodeList = p.getInterCodeList();
                    textArea1.setText("");
                    for (int i = 0; i < interCodeList.size(); i++) {
                        textArea1.append(i + " : " + interCodeList.get(i).anotherToString() + "\t\t" + interCodeList.get(i)+"\n");
                    }
                    textArea2.setText(p.getErrors().toString());
                    List<SymbolTable> symbolBoards = p.getSymbol_tables();
                    textArea3.setText("");
                    for(int i = 0;i<symbolBoards.size();i++){
                        for(CommonSymbol symbolItem:symbolBoards.get(i).getSymbolTable().values()){
                            textArea3.append(i+"\t"+symbolItem.getIdentifier()+"\t"+symbolItem.getType()+"\t"+symbolItem.getOffset()+"\n");
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("semanticAPP");
        frame.setContentPane(new semanticAPP().MySemanticAnalyzer);
        frame.setLocation(400,150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
