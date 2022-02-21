package com.cat.fsai;

import com.cat.fsai.fx.HelloworldView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javafx.application.Application;

import java.io.IOException;


@SpringBootApplication
public class FsaiApplication extends AbstractJavaFxApplicationSupport {

//	@Override
//	public void start(Stage stage) throws IOException {
//		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/hello-view.fxml"));
//		Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//		stage.setTitle("Hello!");
//		stage.setScene(scene);
//		stage.show();
//	}

	public static void main(String[] args) {
		launch(FsaiApplication.class, HelloworldView.class, args);
		//SpringApplication.run(FsaiApplication.class, args);
	}
}
	