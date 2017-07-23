package suggestiongenerator.services;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.time.ParseLocalDate;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import suggestiongenerator.entities.Assignment;
import suggestiongenerator.entities.Person;
import suggestiongenerator.repository.AssignmentRepository;
import suggestiongenerator.repository.PersonRepository;
import util.Assignment_t;
import util.Section;

@Service
public class ImportService {
	
	@Autowired
	PersonRepository personRepository;
	
	private CellProcessor[] getAssignmentCsvProcessors() {
    	
    	final CellProcessor[] processors = new CellProcessor[] {
    		// Assignment Date
			new ParseLocalDate(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
			
			// Assignment Type
			new ParseInt(), 
			
			// Assignee
			new NotNull(),  
			
			
			// Householder
			new Optional(), 
			
			// Lesson
			new Optional(new ParseInt()),
			
			// Classroom
			new ParseInt()
    	};
    	
    	return processors;
    }
    
    public List<Assignment> readAssignmentsWithCsvListReader(String csvFileName) throws Exception {
    	
    	List<Assignment> assignments = new LinkedList<>();
    	ICsvMapReader mapReader = null;
    	
    	try {
    		mapReader = new CsvMapReader(
				new InputStreamReader(new FileInputStream(csvFileName), Charset.forName("UTF-8")),
				CsvPreference.STANDARD_PREFERENCE);
    		
    		final String[] header = mapReader.getHeader(true);
    		final CellProcessor[] processors = getAssignmentCsvProcessors();
    		
    		Map<String, Object> assignment;
    		while ((assignment = mapReader.read(header, processors)) != null) {
    			
    			Assignment assignmentToPersist = new Assignment();
    			

    			Person assignee = personRepository.findByFullName((String)assignment.get("Assignee"));
    			Person householder = personRepository.findByFullName((String)assignment.get("Householder"));

    			// TODO: what to do when the person is not found?
    			if (assignee == null) {
    				
    				System.out.println("Unable to find "+assignment.get("Assignee"));
    				System.out.println("ignoring person for now but should be asking user for clarification");
    				continue;
    			}
    			
    			assignmentToPersist.setWeek((LocalDate)assignment.get("Date"));
    			assignmentToPersist.setAssignee(assignee);
    			assignmentToPersist.setHouseholder(householder);
    			assignmentToPersist.setAssignmentType(Assignment_t.get((int)assignment.get("Type")));
    			if (assignment.get("Lesson") != null) {
    				assignmentToPersist.setLesson((int)assignment.get("Lesson"));
    			}
    			assignmentToPersist.setClassroom(Section.get((int)assignment.get("Classroom")));
    			
    			assignments.add(assignmentToPersist);
    		}
    		
    	}
    	finally {
    		if (mapReader != null) {
    			mapReader.close();
    		}
    	}
    	
    	return assignments;
    }
}
