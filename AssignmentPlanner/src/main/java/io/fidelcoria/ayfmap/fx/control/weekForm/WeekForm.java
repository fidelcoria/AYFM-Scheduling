package io.fidelcoria.ayfmap.fx.control.weekForm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Predicate;

import io.fidelcoria.ayfmap.domain.Person;
import io.fidelcoria.ayfmap.util.Assignment_t;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Custom JavaFX control for input of a schedule week
 * @author FidelCoria
 *
 */
public class WeekForm extends VBox {
	
	@FXML
	Label weekDate;
	
	@FXML
	ChoiceBox<Assignment_t> assgnType;
	@FXML
	ChoiceBox<Integer> lesson;
	@FXML
	ComboBox<Person> pubMain; // pub is for publisher
	@FXML
	ComboBox<Person> hholdMain;
	@FXML
	ComboBox<Person> pubAux;
	@FXML
	ComboBox<Person> hholdAux;
	
	private ObservableList<Person> ps;
	
	public WeekForm(ObservableList<Person> ps) {
		
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WeekForm.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this); // needed for injection
		
		this.ps = ps;
		
		try {
			// injection of the annotated class members is done by load
			fxmlLoader.load();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void initialize() {
				
		// There are 20 lessons in the "Apply yourself to reading and teaching" manual
		for (int i = 1; i <= 20; i++) {
			lesson.getItems().add(i);
		}
		lesson.getSelectionModel().selectFirst();
		
		// initialize ComboBox values
		for (Assignment_t t : Assignment_t.values()) {
			if (t != Assignment_t.UNKNOWN) {
				assgnType.getItems().add(t);
			}
		}
		
		// add ComboBoxes to ArrayList so that they can be streamed
		ArrayList<ComboBox<Person>> cs = new ArrayList<ComboBox<Person>>();
		cs.add(pubMain);
		cs.add(hholdMain);
		cs.add(pubAux);
		cs.add(hholdAux);
	
		// ps added here (and not constructor) because the comboBoxes 
		// are guaranteed to be wired before initialize() is called
		cs.stream().forEach(c -> {
			// wrap ObservableList in FilteredList so drop down
			// can be filtered by selected assignment type
			c.setItems(new FilteredList<>(ps));
		});

		// event handler must be registered before doing the selection
		// future assignmentRow control
		assgnType.setOnAction(e -> {
			
			// hide householder ComboBox when it is not applicable
			if (assgnType.getValue() == Assignment_t.READING
					|| assgnType.getValue() == Assignment_t.TALK) {
				hholdMain.setVisible(false);
				hholdAux .setVisible(false);
			} else {
				hholdMain.setVisible(true);
				hholdAux .setVisible(true);
			}
			
			// clear selection when type changes
			// in case someone not eligible for the new type was selected before
			cs.forEach(c -> { c.setValue(null); });
			
			Predicate<Person> f = (p) -> {
				return p.getEligibility().contains(assgnType.getValue());
			};
			
			// update the filter predicate when type changes
			cs.forEach(c -> {
				((FilteredList<Person>) c.getItems())
					.setPredicate(f);
			});
		});
	
		assgnType.getSelectionModel().selectFirst();
	}
	
	public void setWeekDate(String date) {
		weekDate.setText(date);
	}
	
	public String getWeekDate() {
		return weekDate.getText();
	}
	
	public String getPubMain() {
		return pubMain.getValue().toString();
	}
	
	public String getHholdMain() {
		return hholdMain.getValue().toString();
	}
	
	public String getPubAux() {
		return pubAux.getValue().toString();
	}
	
	public String getHholdAux() {
		return hholdAux.getValue().toString();
	}
	
}
