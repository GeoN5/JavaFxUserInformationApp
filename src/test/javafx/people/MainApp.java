package test.javafx.people;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import test.javafx.people.model.Person;
import test.javafx.people.model.PersonListWrapper;
import test.javafx.people.view.PersonEditDialogController;
import test.javafx.people.view.PersonNewDialogController;
import test.javafx.people.view.PersonOverviewController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import test.javafx.people.view.RootLayoutController;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    //연락처에 대한 observable 리스트
    private ObservableList<Person> personData = FXCollections.observableArrayList();

    public MainApp(){ }

    //연락처에 대한 observable 리스트 반환한다.
    public ObservableList<Person> getPersonData(){
        return  personData;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Java 수행평가(사용자 개인정보 관리)");
        this.primaryStage.getIcons().add(new Image("file:src/images/main.png"));

        initRootLayout();
        showPersonOverview();
    }

    //상위 레이아웃을 초기화한다.
    private void initRootLayout() {
        try {
            // fxml 파일에서 상위 레이아웃을 가져온다.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            // 상위 레이아웃을 포함하는 scene을 보여준다.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            //컨트롤러한테 MainApp 접근 권한을 준다.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //마지막으로 열었던 연락처 파일을 가져온다.
        File file = getPersonFilePath();
        if(file != null){
            loadPersonDataFromFile(file);
        }
    }

    //상위 레이아웃 안에 세부 정보를 보여준다.
    private void showPersonOverview() {
        try {
            // 세부 정보를 가져온다.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PersonOverview.fxml"));
            AnchorPane personOverview = loader.load();

            // rootLayout안에 setting
            rootLayout.setCenter(personOverview);

            //메인 에플리케이션이 컨트롤러를 이용할 수 있게 한다.
            PersonOverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //메인 스테이지를 반환한다.
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    //세부 정보를 변경하기 위해 다이얼로그를 연다.
    //만일 사용자가 OK를 클릭하면 주어진 person 객체에 내용을 저장한 후 true를 반환한다.
    public boolean showPersonEditDialog(Person person){
        try{
            //fxml 파일을 로드하고 나서 새로운 스테이지를 만든다.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PersonEditDialog.fxml"));
            AnchorPane page = loader.load();

            //다이얼로그 스테이지를 만든다.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("정보 변경");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            //person을 컨트롤러에 설정한다.
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            //다이얼로그를 보여주고 사용자가 닫을 때까지 기다린다.
            dialogStage.showAndWait();

            return controller.isOkClicked();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    //사람 추가를 위해 다이얼로그를 연다.
    //만일 사용자가 OK를 클릭하면 주어진 person 객체에 내용을 저장한 후 true를 반환한다.
    public boolean showPersonNewDialog(Person person){
        try{
            //fxml 파일을 로드하고 나서 새로운 스테이지를 만든다.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/PersonNewDialog.fxml"));
            AnchorPane page = loader.load();

            //다이얼로그 스테이지를 만든다.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("사용자 추가");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            //person을 컨트롤러에 설정한다.
            PersonNewDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            //다이얼로그를 보여주고 사용자가 닫을 때까지 기다린다.
            dialogStage.showAndWait();

            return controller.isOkClicked();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    // 이 경로는 OS 특정 레지스트리에 저장된다.
    public void setPersonFilePath(File file){
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        if(file!=null){
            prefs.put("filePath",file.getPath());
            primaryStage.setTitle("사용자 개인정보 관리 - "+file.getName());
        }else{
            prefs.remove("filePath");
            primaryStage.setTitle("사용자 개인정보 관리");
        }
    }

    //OS 특정 레지스트리로부터 읽는다. 만일 preference를 찾지 못하면 null을 반환한다.
    public File getPersonFilePath(){
        Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
        String filePath = prefs.get("filePath",null);
        if(filePath!=null){
            return new File(filePath);
        }else{
            return null;
        }
    }

    //현재 연락처 데이터를 지정한 파일에 저장한다.
    public void savePersonDataToFile(File file){
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(PersonListWrapper.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
            //연락처 데이터를 감싼다.
            PersonListWrapper wrapper = new PersonListWrapper();
            wrapper.setPersons(personData);
            //마샬링 후 xml을 파일에 저장한다.
            marshaller.marshal(wrapper,file);
            //파일 경로를 레지스트리에 저장한다.
            setPersonFilePath(file);
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());
            alert.showAndWait();
        }
    }

    //지정한 파일로부터 연락처 데이터를 가져온다. 현재 연락처 데이터로 대체된다.
    public void loadPersonDataFromFile(File file){
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(PersonListWrapper.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            //파일로부터 xml을 읽은 다음 역마샬링한다.
            PersonListWrapper wrapper = (PersonListWrapper)unmarshaller.unmarshal(file);
            personData.clear();
            personData.addAll(wrapper.getPersons());
            //파일 경로를 레지스트리에 저장한다.
            setPersonFilePath(file);
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save data");
            alert.setContentText("Could not save data to file:\n" + file.getPath());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
