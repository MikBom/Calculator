package application;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.util.*;

// simppeli laskin.
//Luodaan pohja laskimelle
public class Main extends Application {
    private static final String[][] pohja = {
            {"7", "8", "9", "/"},
            {"4", "5", "6", "*"},
            {"1", "2", "3", "-"},
            {"0", "c", "=", "+"}
    };

    private final Map<String, Button> muuttujat = new HashMap<>();

    private final DoubleProperty tallennetutArvot = new SimpleDoubleProperty();
    private final DoubleProperty arvo = new SimpleDoubleProperty();

    private enum lasku {NOOP, LISAA, VAHENNA, KERRO, JAA}

    private lasku nykyAr = lasku.NOOP;
    private lasku tallennettuAr = lasku.NOOP;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        final TextField naytto = luoNaytto();
        final TilePane napit = luoNapit();

        stage.setTitle("Laskin");
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.setScene(new Scene(luoNaytto(naytto, napit)));
        stage.show();
    }

    private VBox luoNaytto(TextField naytto, TilePane napit) {
        final VBox ulkoasu = new VBox(40);
        ulkoasu.setAlignment(Pos.CENTER);
        ulkoasu.setStyle("-fx-background-color: green; -fx-padding: 40; -fx-font-size: 30;");
        ulkoasu.getChildren().setAll(naytto, napit);
        kaytaMuuttuja(ulkoasu);
        naytto.prefWidthProperty().bind(napit.widthProperty());
        return ulkoasu;
    }

    private void kaytaMuuttuja(VBox ulkoasu) {
        ulkoasu.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            Button activated = muuttujat.get(keyEvent.getText());
            if (activated != null) {
                activated.fire();
            }
        });
    }

    private TextField luoNaytto() {
        final TextField naytto = new TextField();
        naytto.setStyle("-fx-background-color: white;");
        naytto.setAlignment(Pos.CENTER_RIGHT);
        naytto.setEditable(false);
        naytto.textProperty().bind(Bindings.format("%.0f", arvo));
        return naytto;
    }

    private TilePane luoNapit() {
        TilePane napit = new TilePane();
        napit.setVgap(7);
        napit.setHgap(7);
        napit.setPrefColumns(pohja[0].length);
        for (String[] r : pohja) {
            for (String s : r) {
                napit.getChildren().add(luoNappi(s));
            }
        }
        return napit;
    }

    private Button luoNappi(final String s) {
        Button nappi = teeStandardiNappi(s);

        if (s.matches("[0-9]")) {
            teeNumeroNappi(s, nappi);
        } else {
            final ObjectProperty<lasku> mitaNappiTekee = determineOperand(s);
            if (mitaNappiTekee.get() != lasku.NOOP) {
                teeOperoivaNappi(nappi, mitaNappiTekee);
            } else if ("c".equals(s)) {
                teeTyhjennaNappi(nappi);
            } else if ("=".equals(s)) {
                teeYhtaKuinNappi(nappi);
            }
        }

        return nappi;
    }

    private ObjectProperty<lasku> determineOperand(String s) {
        final ObjectProperty<lasku> mitaNappiTekee = new SimpleObjectProperty<>(lasku.NOOP);
        switch (s) {
            case "+" -> mitaNappiTekee.set(lasku.LISAA);
            case "-" -> mitaNappiTekee.set(lasku.VAHENNA);
            case "*" -> mitaNappiTekee.set(lasku.KERRO);
            case "/" -> mitaNappiTekee.set(lasku.JAA);
        }
        return mitaNappiTekee;
    }
    //Näppäimet + - / *
    private void teeOperoivaNappi(Button nappi, final ObjectProperty<lasku> mitaNappiTekee) {
        nappi.setStyle("-fx-base: black;");
        nappi.setOnAction(actionEvent -> nykyAr = mitaNappiTekee.get());
    }
    //Numeronäppäimet
    private Button teeStandardiNappi(String s) {
        Button nappi = new Button(s);
        nappi.setStyle("-fx-base: beige;");
        muuttujat.put(s, nappi);
        nappi.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return nappi;
    }
    
    //metodi numeronäppäimelle
    private void teeNumeroNappi(final String s, Button nappi) {
        nappi.setOnAction(actionEvent -> {
            if (nykyAr == lasku.NOOP) {
                arvo.set(arvo.get() * 10 + Integer.parseInt(s)); //tällä lisätään seuraava numero edellisen perään
            } else {
                tallennetutArvot.set(arvo.get());
                arvo.set(Integer.parseInt(s));
                tallennettuAr = nykyAr;
                nykyAr = lasku.NOOP;
            }
        });
    }
    
    //metodi näytön tyhjentämiseksi
    private void teeTyhjennaNappi(Button nappi) {
        nappi.setStyle("-fx-base: mistyrose;");
        nappi.setOnAction(actionEvent -> arvo.set(0));
    }
    
    //metodi kun halutaan laskea tulos
    private void teeYhtaKuinNappi(Button nappi) {
        nappi.setStyle("-fx-base: red;");
        nappi.setOnAction(actionEvent -> {
            switch (tallennettuAr) {
                case LISAA -> arvo.set(tallennetutArvot.get() + arvo.get());
                case VAHENNA -> arvo.set(tallennetutArvot.get() - arvo.get());
                case KERRO -> arvo.set(tallennetutArvot.get() * arvo.get());
                case JAA -> arvo.set(tallennetutArvot.get() / arvo.get());
            }
        });
    }
}