open module com.musekeys {
        requires javafx.controls;
        requires javafx.fxml;
        requires java.desktop;
        requires java.prefs;

        //opens com.musekeys.midiplayer to javafx.fxml;
        //opens com.musekeys.control to javafx.fxml;
        exports com.musekeys.midiplayer;

}
