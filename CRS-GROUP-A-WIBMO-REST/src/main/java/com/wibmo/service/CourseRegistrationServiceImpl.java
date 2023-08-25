package com.wibmo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wibmo.exception.CourseNotExistsInCatalogException;
import com.wibmo.exception.StudentAlreadyRegisteredForAllAlternativeCoursesException;
import com.wibmo.exception.StudentAlreadyRegisteredForAllPrimaryCoursesException;
import com.wibmo.exception.StudentAlreadyRegisteredForCourseInSemesterException;
import com.wibmo.exception.StudentAlreadyRegisteredForSemesterException;
import com.wibmo.exception.StudentNotRegisteredForCourseInSemesterException;
import com.wibmo.exception.StudentNotRegisteredForSemesterException;
import com.wibmo.exception.UserNotFoundException;
import com.wibmo.exception.ProfessorNotAssignedForCourseException;
import com.wibmo.dao.CourseRegistrationDAOImpl;
import com.wibmo.dto.RegisteredCourse;
import com.wibmo.entity.Course;
import com.wibmo.entity.CourseRegistration;
import com.wibmo.entity.Professor;
import com.wibmo.entity.Student;
import com.wibmo.enums.CourseType;
import com.wibmo.enums.RegistrationStatus;

@Service
public class CourseRegistrationServiceImpl implements CourseRegistrationService {
	
	@Autowired
	private UserServiceImpl userOperation;
	
	@Autowired
	private StudentServiceImpl studentOperation;
	
	@Autowired
	private ProfessorServiceImpl professorOperation;
	
	@Autowired
	private CourseServiceImpl courseOperation;
	
	@Autowired
	private CourseRegistrationDAOImpl courseRegistrationDAO;
	
	@Override
	public void register(List<Integer> primaryCourseIds, List<Integer> alternativeCourseIds, Student student)
			throws 
				StudentAlreadyRegisteredForSemesterException, 
				CourseNotExistsInCatalogException {
		
		// TODO: Check if Registration is Enabled by Admin

		if(isStudentRegistered(student)) {
			throw new StudentAlreadyRegisteredForSemesterException(student);
		}
		
		for(Integer courseId : primaryCourseIds) {
			if(!courseOperation.isCourseExistsInCatalog(courseId)) {
				throw new CourseNotExistsInCatalogException(courseId);
			}
		}
		for(Integer courseId : alternativeCourseIds) {
			if(!courseOperation.isCourseExistsInCatalog(courseId)) {
				throw new CourseNotExistsInCatalogException(courseId);
			}
		}
		
		CourseRegistration courseRegistration = new CourseRegistration();
		courseRegistration.setStudentId(student.getStudentId());
		courseRegistration.setSemester(student.getCurrentSemester());
		
		// TODO:
		courseRegistration.setYear(2021);
		
		courseRegistration.setPrimaryCourse1Id(primaryCourseIds.get(0));
		courseRegistration.setPrimaryCourse2Id(primaryCourseIds.get(1));
		courseRegistration.setPrimaryCourse3Id(primaryCourseIds.get(2));
		courseRegistration.setPrimaryCourse4Id(primaryCourseIds.get(3));
		courseRegistration.setAlternativeCourse1Id(alternativeCourseIds.get(0));
		courseRegistration.setAlternativeCourse2Id(alternativeCourseIds.get(1));
		courseRegistration.setRegistrationStatus(RegistrationStatus.PENDING);
		
		courseRegistrationDAO.save(courseRegistration);
		
		System.out.println("Course Registration Request sent to Admin for Approval.");
		
	}

	@Override
	public List<RegisteredCourse> viewRegisteredCoursesByStudent(Student student) 
			throws StudentNotRegisteredForSemesterException {
		
		if(!isStudentRegistered(student)) {
			throw new StudentNotRegisteredForSemesterException(student);
		}
		List<RegisteredCourse> registeredCourses = new ArrayList<RegisteredCourse>();
		CourseRegistration courseRegistration = courseRegistrationDAO.findByStudent(student);
		Set<Integer> courseIds = new HashSet<>();
		Integer courseId;
		// TODO: Move to Join Query to avoid redundant code
		if(null != (courseId = courseRegistration.getPrimaryCourse1Id())) {
			courseIds.add(courseId);
		}
		if(null != (courseId = courseRegistration.getPrimaryCourse2Id())) {
			courseIds.add(courseId);
		}
		if(null != (courseId = courseRegistration.getPrimaryCourse3Id())) {
			courseIds.add(courseId);
		}
		if(null != (courseId = courseRegistration.getPrimaryCourse4Id())) {
			courseIds.add(courseId);
		}
		if(null != (courseId = courseRegistration.getAlternativeCourse1Id())) {
			courseIds.add(courseId);
		}
		if(null != (courseId = courseRegistration.getAlternativeCourse2Id())) {
			courseIds.add(courseId);
		}
		Map<Integer, Course> courseIdToCourseMap = courseOperation
				.getCourseIdToCourseMap(courseIds);
		Map<Integer, Professor> professorIdToProfessorMap = professorOperation.getProfessorIdToProfessorMap(
				courseIdToCourseMap
					.entrySet()
					.stream()
					.map(entry -> entry.getValue().getProfessorId())
					.collect(Collectors.toSet()));
		
		courseIdToCourseMap
			.entrySet()
			.stream()
			.map(entry -> entry.getValue())
			.forEach(course -> {
				registeredCourses.add(new RegisteredCourse(course.getCourseId(),course.getCourseTitle(),course.getDepartment(),professorIdToProfessorMap.get(course.getProfessorId()).getProfessorName()));

			});
		return registeredCourses;
	}
	
	@Override
	public RegistrationStatus getRegistrationStatusByStudent(Student student) 
			throws StudentNotRegisteredForSemesterException {
		
		if(!isStudentRegistered(student)) {
			throw new StudentNotRegisteredForSemesterException(student);
		}
		
		return courseRegistrationDAO.findRegistrationStatusByStudent(student);
	}
	
	@Override
	public void addCourse(Integer courseId, Student student) 
			throws 
				StudentNotRegisteredForSemesterException, 
				StudentAlreadyRegisteredForCourseInSemesterException, 
				StudentAlreadyRegisteredForAllAlternativeCoursesException, 
				StudentAlreadyRegisteredForAllPrimaryCoursesException, 
				CourseNotExistsInCatalogException {

		if(!isStudentRegistered(student)) {
			throw new StudentNotRegisteredForSemesterException(student);
		}
		
		if(!courseOperation.isCourseExistsInCatalog(courseId)) {
			throw new CourseNotExistsInCatalogException(courseId);
		}
		
		// TODO: Improve dual DB call for same thing. Handle with index. 
		if(isStudentRegisteredForCourse(student, courseId)) {
			throw new StudentAlreadyRegisteredForCourseInSemesterException(student, courseId);
		}
		
		// TODO: CourseLite object can help avoid redundant DB access
		CourseType courseType = courseOperation.getCourseTypeByCourseId(courseId);
				
		switch(courseType) {
		
		case ALTERNATIVE:
			if(isStudentRegisteredForAllAlternativeCourses(student)) {
				throw new StudentAlreadyRegisteredForAllAlternativeCoursesException(student);
			}
			break;
		case PRIMARY:
			if(isStudentRegisteredForAllPrimaryCourses(student)) {
				throw new StudentAlreadyRegisteredForAllPrimaryCoursesException(student);
			}
		}
		
		Integer courseRegistrationId = courseRegistrationDAO
				.findCourseRegistrationIdByStudent(student);
		
		
		switch(courseType) {
			
		case ALTERNATIVE:
			courseRegistrationDAO
				.setAlternativeCourseIdAsAtIndexByCourseRegistrationId(
					courseId,
					courseRegistrationDAO
						.findFirstVacantAlternativeCourseIdIndexByCourseRegistrationId(
							courseRegistrationId),
					courseRegistrationId);
			break;
			
		case PRIMARY:
			courseRegistrationDAO
				.setPrimaryCourseIdAsAtIndexByCourseRegistrationId(
					courseId,
					courseRegistrationDAO
						.findFirstVacantPrimaryCourseIdIndexByCourseRegistrationId(
							courseRegistrationId),
					courseRegistrationId);
		}
		
		System.out.println("Course Enrollment Success!");
	}

	@Override
	public void dropCourse(Integer courseId, Student student) 
			throws 
				CourseNotExistsInCatalogException,
				StudentNotRegisteredForSemesterException, 
				StudentNotRegisteredForCourseInSemesterException {
		
		if(!isStudentRegistered(student)) {
			throw new StudentNotRegisteredForSemesterException(student);
		}
		
		if(!isStudentRegisteredForCourse(student, courseId)) {
			throw new StudentNotRegisteredForCourseInSemesterException(student, courseId);
		}
		
		Integer courseRegistrationId = courseRegistrationDAO.findCourseRegistrationIdByStudent(student);
		CourseType courseType = courseOperation.getCourseTypeByCourseId(courseId);
		
		switch(courseType) {
		
		case ALTERNATIVE:
			courseRegistrationDAO
				.setAlternativeCourseIdAsNullAtIndexByCourseRegistrationId(
					courseRegistrationDAO
						.findAlternativeCourseIdIndexByCourseRegistrationIdForCourse(
							courseRegistrationId, courseId), 
					courseRegistrationId);
			break;
		
		case PRIMARY:
			courseRegistrationDAO
				.setPrimaryCourseIdAsNullAtIndexByCourseRegistrationId(
					courseRegistrationDAO
						.findPrimaryCourseIdIndexByCourseRegistrationIdForCourse(
							courseRegistrationId, courseId),
					courseRegistrationId);
		}
		
		System.out.println("Course Drop Success!");
	}

	@Override
	public List<Student> getRegisteredStudentsByCourseId(Integer courseId) throws CourseNotExistsInCatalogException {
		
		if(!courseOperation.isCourseExistsInCatalog(courseId)) {
			throw new CourseNotExistsInCatalogException(courseId);
		}
		
		return courseRegistrationDAO
					.findAllStudentIdsByCourseId(courseId)
					.stream()
					.map(studentId -> studentOperation.getStudentById(studentId))
					.collect(Collectors.toList());
	}

	@Override
	public Map<Integer, List<Student>> getCourseIdToRegisteredStudentsMappingByProfessorId(Integer professorId)
			throws UserNotFoundException {
		List<Course> courses = courseOperation.getCoursesAssignedToProfessor(professorId);
		Map<Integer, List<Student>> courseIdToRegisteredStudentsMap = new HashMap<>();
		courses
			.forEach(course -> {
				CourseRegistration courseRegistration = courseRegistrationDAO
						.findByCourseIdAndSemesterAndYear(
								course.getCourseId(), 
								course.getSemester(),
								course.getYear());
				Student student = studentOperation.getStudentById(courseRegistration.getStudentId());
				if(!courseIdToRegisteredStudentsMap.containsKey(course.getCourseId())) {
					courseIdToRegisteredStudentsMap.put(course.getCourseId(), new ArrayList<>());
				}
				courseIdToRegisteredStudentsMap.get(course.getCourseId()).add(student);
			});
		return courseIdToRegisteredStudentsMap;
	}
	
	@Override
	public List<CourseRegistration> getCourseRegistrationsByRegistrationStatus(
			RegistrationStatus registrationStatus){
		return courseRegistrationDAO
			.findAllByRegistrationStatus(registrationStatus);
	}
	
	@Override
	public Boolean updateCourseRegistrationStatusToByRegistrationIds(
			RegistrationStatus registrationStatus,
			Set<Integer> courseRegistrationIds){
		
		// TODO: Should add this check
//		Optional<Integer> invalidCourseRegistrationId = getFirstInvalidCourseRegistrationId(courseRegistrationIds);
//		if(invalidCourseRegistrationId.isPresent()) {
//			throw new InvalidCourseRegistrationIdArgumentException(invalidCourseRegistrationId.get());
//		}
		
		return courseRegistrationDAO
					.updateRegistrationStatusAsByIdIn(
						registrationStatus,
						courseRegistrationIds);
	}
	
	@Override
	public Boolean updateAllPendingCourseRegistrationsTo(
			RegistrationStatus registrationStatus) {
		return courseRegistrationDAO
					.updateRegistrationStatusAsByIdIn(
							registrationStatus, 
							courseRegistrationDAO
								.findAllByRegistrationStatus(RegistrationStatus.PENDING)
								.stream()
								.map(courseRegistration -> courseRegistration.getCourseRegId())
								.collect(Collectors.toSet()));
	}

	@Override
	public void viewRegisteredStudentsByProfessorIdAndCourseId(
		Integer professorId, Integer courseId)
			throws 
				CourseNotExistsInCatalogException,
				UserNotFoundException, 
				ProfessorNotAssignedForCourseException {
		
		if(null == professorId || null == courseId) {
			return;
		}
		
		if(!userOperation.isUserExistsById(professorId)) {
			throw new UserNotFoundException(professorId);
		}
		
		if(!courseOperation.isCourseExistsInCatalog(courseId)) {
			throw new CourseNotExistsInCatalogException(courseId);
		}
		
		if(!courseOperation.isProfessorAssignedForCourse(professorId, courseId)) {
			throw new ProfessorNotAssignedForCourseException(professorId, courseId);
		}
		
		System.out.println("*** List of Registered Students for Course Id: " + courseId + " :- ***\n");
		System.out.print(
				  "+--------------------------------------------------------+\n"
				+ " StudentId  |  StudentName\t| StudentEmail \n"
				+ "+--------------------------------------------------------+\n");
		
		getRegisteredStudentsByCourseId(courseId)
			.forEach(student -> System.out.format(
					"    %d    | %s\t\t| %s\n", 
						student.getStudentId(), 
						student.getStudentName(),
						student.getStudentEmail()));
	}


	/*************************** Utility Methods ********************************/
	
	private Boolean isStudentRegistered(Student student) {
		return courseRegistrationDAO.existsByStudent(student);
	}
	
	private Boolean isStudentRegisteredForCourse(Student student, Integer courseId) {
		return courseRegistrationDAO
				.existsByStudentAndCourseId(student, courseId);
	}
	
	private Boolean isStudentRegisteredForAllAlternativeCourses(Student student) {
		return courseRegistrationDAO
				.findFirstVacantAlternativeCourseIdIndexByCourseRegistrationId(
					courseRegistrationDAO.findCourseRegistrationIdByStudent(student)) == -1;
	}
	
	private Boolean isStudentRegisteredForAllPrimaryCourses(Student student) {
		return courseRegistrationDAO
				.findFirstVacantPrimaryCourseIdIndexByCourseRegistrationId(
					courseRegistrationDAO.findCourseRegistrationIdByStudent(student)) == -1;
	}	
}
