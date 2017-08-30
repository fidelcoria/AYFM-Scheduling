package suggestiongenerator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import suggestiongenerator.entities.Person;
import util.Participation;

/**
 * AUTO IMPLEMENTED by Spring into a Bean called personRepository
 * when PersonRepository '@Autowired'
 * 
 * @author FidelCoria
 *
 */
@Repository("personRepository")
public interface PersonRepository extends JpaRepository<Person, Integer> {
	
	/**
	 * Find person by full name.
	 * @param fullName string to match
	 * @return Person with matching name
	 */
	@Query("FROM Person p WHERE CONCAT(p.firstName, ' ', p.lastName) = :fullname")
	public Person findByFullName(@Param("fullname") String fullName);
	
	/**
	 * Find all active Persons
	 * @return Persons wrapped in a List of Participations
	 * with the Assignment set to null. 
	 */
	@Query("SELECT new util.Participation(p) FROM Person p WHERE p.isActive = true ")
	public List<Participation> findAllActiveStudents();
}