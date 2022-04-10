package com.cat.fsai;

import com.alibaba.fastjson.JSONObject;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;


@SpringBootApplication
@Slf4j
public class FsaiApplication extends Application{

	private static String[] savedArgs;
	private ConfigurableApplicationContext context;

	@Override
	public void init() throws Exception {
		this.context=SpringApplication.run(FsaiApplication.class, savedArgs);
	}

	@Override
	public void stop() throws Exception {
		context.close();
		log.info("shut down finish");
		//System.gc();
		//System.runFinalization();
	}
	private Object createControllerForType(Class<?> type) {
		return this.context.getBean(type);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/hello-view.fxml"));
			loader.setControllerFactory(this::createControllerForType);
			Parent root = loader.load();
			Scene scene = new Scene(root,900,500);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Fsai爆赚");
			primaryStage.show();
		} catch(IOException e) {
			throw new IllegalStateException("Unable to load view:", e);
		}
	}

	public static void main(String[] args) {
		savedArgs=args;
		launch(args);
	}
}
	